package com.cobub.toaster.push.component;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import com.cobub.toaster.push.cont.CommConst;
import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;
import com.cobub.toaster.push.util.LogHelper;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class PushService extends Service {
	private String TAG = this.getClass().getSimpleName();
	// network sync lock
	public static Object netStateLock = new Object();
	// Thread flag
	private boolean runnableFlag = false;
	// uid
	public static String mUid;
	//
	public static String mAppId;
	// context arg
	private Context mContext;
	// io adapter
	public static BaseIOAdapter ioAdapter;
	// max try num
	private static final int MAX_TRYNUM = 10;
	//max error num
	private static final int MAX_ERRNUM = 20;

	private ScreenReceiver screenReceiver = new ScreenReceiver();
	
	private String HOST;
	private int PORT;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		try {
			initOp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// if init error, finish itself
			stopService();
			e.printStackTrace();
			return;
		}
		new Thread(new TaskRunnable(mContext)).start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent != null) {
			handleIntent(intent);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopService();
	}

	public static BaseIOAdapter getIoAdapter() {
		return ioAdapter;
	}

	public static void setIoAdapter(BaseIOAdapter ioAdapter) {
		PushService.ioAdapter = ioAdapter;
	}

	// init operation
	private void initOp() throws Exception {
		// TODO Auto-generated method stub
		mContext = PushService.this;
		regScreenListener();
		mAppId = BaseUtility.getFromSp(mContext, CommConst.SPNAME, "appid");
		if (TextUtils.isEmpty(mAppId)) {
			BaseUtility.makeToastTip(mContext, "no appid found");
			throw new Exception("no appid found");
		}
		mUid = BaseUtility.getFromSp(mContext, CommConst.SPNAME, "uid");
		if (TextUtils.isEmpty(mUid)) {
			BaseUtility.makeToastTip(mContext, "no uid found");
			throw new Exception("no uid found");
		}
		HOST = BaseUtility.getFromSp(mContext, CommConst.SPNAME, "host");
		if (TextUtils.isEmpty(HOST)) {
			BaseUtility.makeToastTip(mContext, "host not set");
			throw new Exception("no host found");
		}
		String portStr = BaseUtility.getFromSp(mContext, CommConst.SPNAME, "port");
		if (TextUtils.isEmpty(portStr)) {
			BaseUtility.makeToastTip(mContext, "port not set");
			throw new Exception("no port found");
		}
		PORT = Integer.parseInt(portStr);
	}

	// handle intent in onStartCommand
	private void handleIntent(Intent intent) {
		if (CommConst.SYNCACTION.equals(intent.getAction())) {
			RedisIOAdapter ioAdapter = (RedisIOAdapter) getIoAdapter();
			Log.d(this.getClass().getSimpleName(), "----sync----");
			try {
				if (ioAdapter != null) {
					ioAdapter.sendPack(makeSyncPack());
					NetPacket tmpPack = null;
					ArrayList<String> events = BaseUtility
							.selectEvent(mContext);
					if (events != null && events.size() > 0) {
						for (String event : events) {
							ioAdapter.sendRawPack(event);
							tmpPack = new NetPacket(event, false);
							if(tmpPack.getType().equals("msgReadFeedback")){
								BaseUtility.removeEvent(mContext, event);
							}
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (ioAdapter.getConnection() != null) {
					ioAdapter.getConnection().close();
				}
			}
		}
	}

	// finish the servie
	private void stopService() {
		runnableFlag = false;
		unregScreenListener();
		BaseUtility.cancelSyncTask(mContext);
		stopSelf();
	}

	private class TaskRunnable implements Runnable {
		private Context ctx;

		public TaskRunnable(Context context) {
			// TODO Auto-generated constructor stub
			setRunnableFlag(true);
			ctx = context;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String ip = null;
			int maxErrNum = MAX_ERRNUM;
			int tryNum = 0;
			UConnection connection = UConnection.getInstance();
			// get a simpleHandler for hanlde packet
			// to be honest, handler is relying adapter, not good
			PacketHandler packetHandler = PacketHandlerFactory
					.getSimpleHanler();
			// define an adapter for IO
			ioAdapter = new RedisIOAdapter(connection);
			while (true) {
				if (!runnableFlag)
					break;
				// if no valid network, sleep&wait for invoke
				if (!BaseUtility.isNetworkConnected(mContext)) {
					synchronized (netStateLock) {
						try {
							netStateLock.wait(CommConst.NETWAITGAP);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				try {
					InetAddress inetAddress = InetAddress.getByName(HOST);
					if(inetAddress == null)throw new Exception("null inetAddress");
					ip = inetAddress.getHostAddress();
					tryNum++;
					connection.connect(ip, PORT);
					//reset adapter, terrible code
					((RedisIOAdapter) ioAdapter).reset();
					tryNum = 0;
					//handshake
					ioAdapter.sendPack(makeHandShakePack());
					BaseUtility.setSyncTask(ctx);
					Log.d(TAG, "----Backlink handshake success----");
					//check app install report
					if(BaseUtility.getFromSp(mContext, CommConst.SPNAME, "appinsflag") == null){
						ioAdapter.sendPack(makeAppInstallPack());
						NetPacket insRespPack = (NetPacket)ioAdapter.recPack();
						if (insRespPack == null) {
							throw new IOException("connection broken down");
						}
						NetPacket insResult = new AppInsCmdHandler().handle(mContext, insRespPack);
						if(insResult.isErrorPacket()){
							Log.e(TAG, "ERRINFO:" + insRespPack);
							handleErrorPacket((ErrorRespPacket)insResult);
						}
					}
					while (true) {
						NetPacket packet = (NetPacket) ioAdapter.recPack();
						if (packet == null) {
							throw new IOException("connection broken down");
						}
						NetPacket resp = packetHandler.hanlde(ctx, packet);
						// if is an error packet then handle it
						if (resp.isErrorPacket()) {
							Log.e(TAG, "error{" + resp + "}");
							handleErrorPacket((ErrorRespPacket) resp);
						} 
						// restore
						maxErrNum = MAX_ERRNUM;
					}
				}catch (UnknownHostException e){
					//if dns fail, sleep until network change
					Log.e(TAG, e.getMessage());
					waitForNetSignal();
				}
				catch (ConnectException e) {
					connection.close();
					Log.e(TAG, e.getMessage());
					BaseUtility.cancelSyncTask(ctx);
					if (tryNum > MAX_TRYNUM) {
						Log.e("PushService", "----Backlink handshake failed----");
						waitForNetSignal();
						tryNum = 0;
					}
					sleep(2000);
				} catch (SocketTimeoutException e) {
					BaseUtility.cancelSyncTask(ctx);
					Log.e(TAG, e.getMessage());
					if (tryNum > MAX_TRYNUM) {
						Log.e("PushService", "----Backlink handshake failed----");
						waitForNetSignal();
						tryNum = 0;
					}
				} catch (SocketException e) {
					Log.e(TAG, e.getMessage());
					connection.close();
					BaseUtility.cancelSyncTask(ctx);
					if (tryNum > MAX_TRYNUM) {
						Log.e("PushService", "----Backlink handshake failed----");
						waitForNetSignal();
						tryNum = 0;
					}
				} catch (PacketParseErrorException e) {
					// TODO: handle exception
					Map<String, String> packFormatErr = BaseUtility
							.makeErrorResopnse("400", e.getMessage());
					BaseUtility.logErrorPacket(ctx, new ErrorRespPacket(
							packFormatErr, true));
					connection.close();
					BaseUtility.cancelSyncTask(ctx);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					maxErrNum--;
					connection.close();
					BaseUtility.cancelSyncTask(ctx);
					if (maxErrNum < 0) {
						Log.e("PushService", "----Communication error----");
						waitForNetSignal();
						maxErrNum = MAX_ERRNUM;
					}
				} catch (SrvBusyException e) {
					// TODO: handle exception
					connection.close();
					Log.e("PushService", "----Server Busy----");
					BaseUtility.cancelSyncTask(ctx);
					waitForNetSignal();
				} catch (Exception e) {
					maxErrNum--;
					connection.close();
					BaseUtility.cancelSyncTask(ctx);
					if (maxErrNum < 0) {
						Log.e("PushService", "----Unknown error occurs----");
						waitForNetSignal();
						maxErrNum = MAX_ERRNUM;
					}
				}
			}
		}
		
//		private NetPacket handleAppInsRespPacket(NetPacket packet) throws SrvBusyException{
//			String cmd = packet.getType();
//			String status = packet.getValueByKey("status", "200");
//			String errmsg = packet.getValueByKey("errmsg", "unknown");
//			if(!cmd.equals("appInstallReply")){
//				Map<String, String> unknownMap= BaseUtility.makeErrorResopnse("404", "operation not found:" + packet);
//				BaseUtility.logErrorPacket(mContext, new ErrorRespPacket(unknownMap, true));
//			}
//			if("500".equals(status)){
//				throw new SrvBusyException("busy in appInsResp");
//			}else if("400".equals(errmsg)){
//				BaseUtility.saveSp(mContext, CommConst.SPNAME, "appinsflag", "false");
//				Map<String, String> unknownMap= BaseUtility.makeErrorResopnse("400", "invalid request:" + errmsg);
//				BaseUtility.logErrorPacket(mContext, new ErrorRespPacket(unknownMap, true));
//			}else if("200".equals(status)){
//				BaseUtility.saveSp(mContext, CommConst.SPNAME, "appinsflag", "true");
//			}
//			return packet;
//			
//		}

		private void handleErrorPacket(ErrorRespPacket packet)
				throws SrvBusyException {
			BaseUtility.logErrorPacket(ctx, packet);
			Log.d("TaskRunnable", "error Resp:" + packet.toString());
			// if server busy error, handle it
			if (packet.getErrorCode().equals("5000")) {
				throw new SrvBusyException(packet.getErrorDesc());
			}
		}

		private void waitForNetSignal() {
			synchronized (netStateLock) {
				try {
					netStateLock.wait(CommConst.NETWAITGAP);
				} catch (InterruptedException nouse) {
				}
			}
		}

		private void sleep(long timeMill) {
			try {
				Thread.sleep(timeMill);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	//pack a appInstall packet 2 report
	private static NetPacket makeAppInstallPack(){
		Map<String, String> appInsMap = new HashMap<String, String>();
		appInsMap.put("cmd", "appInstall");
		JSONArray appidArr = new JSONArray();
		appidArr.put(mAppId);
		appInsMap.put("appid", appidArr.toString());
		return new NetPacket(appInsMap, false);
	}

	// pack a handshake packet
	private static NetPacket makeHandShakePack() {
		Map<String, String> handshakeMap = new HashMap<String, String>();
		handshakeMap.put("cmd", "handshake");
		handshakeMap.put("version", CommConst.VERSIONCODE);
		handshakeMap.put("uid", mUid);
		// if MI 2* set sync 330
		if (Build.MODEL.matches("MI\\s2.*")) {
			handshakeMap.put("hbTimeout", "330");
		}else{
			handshakeMap.put("hbTimeout", CommConst.SYNCGAP);
		}
		JSONArray appidArr = new JSONArray();
		appidArr.put(mAppId);
		handshakeMap.put("appid", appidArr.toString());
		return new NetPacket(handshakeMap, false);
	}

	// pack a sync packet
	private static NetPacket makeSyncPack() {
		Map<String, String> syncMap = new HashMap<String, String>();
		syncMap.put("cmd", "hb");
		return new NetPacket(syncMap, false);
	}

	public void setRunnableFlag(boolean runnableFlag) {
		this.runnableFlag = runnableFlag;
	}

	// receiver for refresh
	private class ScreenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d("ScreenReceiver", "screen on");
			Intent i = new Intent(context, PushService.class);
			context.startService(i);
		}
	}

	// register listener
	private void regScreenListener() {
		IntentFilter screenFilter = new IntentFilter();
		screenFilter.addAction("android.intent.action.SCREEN_ON");
		registerReceiver(screenReceiver, screenFilter);
	}

	// unregister listener
	private void unregScreenListener() {
		unregisterReceiver(screenReceiver);
	}
}
