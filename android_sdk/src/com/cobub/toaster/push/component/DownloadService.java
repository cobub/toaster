package com.cobub.toaster.push.component;

import java.io.File;
import java.util.Map;

import com.cobub.toaster.push.model.ApkDownloadModel;
import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.util.AsynFileDownloader;
import com.cobub.toaster.push.util.BaseUtility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;

/*
 * need permission <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 */
public class DownloadService extends Service {
	private NotificationManager notificationManager;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		String appName = intent.getStringExtra("appname");
		String dlUrl = intent.getStringExtra("dlurl");
		int viewId = intent.getIntExtra("v_id", 0);
		if(appName == null || dlUrl == null ){
			Map<String, String> dlerrorMap = BaseUtility.makeErrorResopnse("413", "download error:appname or dlurl is null");
			BaseUtility.logErrorPacket(this, new ErrorRespPacket(dlerrorMap, true));
			return super.onStartCommand(intent, flags, startId);
		}
		byte[] byteValues = appName.getBytes();
		int appIdValue = 0;
		for(byte tmpByte:byteValues){
			appIdValue += tmpByte;
		}
		String appId = appIdValue + "";
		
		ApkDownloadModel apkModel = new ApkDownloadModel(appName, dlUrl, viewId, appId);
		File apkFile = BaseUtility.createFile(DownloadService.this, appName + ".apk");
		if(apkFile != null){
			apkModel.setApkPath(apkFile.getAbsolutePath());
			Builder builder4apk = new Builder(this);
			apkModel.setBuilder(builder4apk);
		}else{
			Map<String, String> dlerrorMap = BaseUtility.makeErrorResopnse("413", "download error:cannot create new file");
			BaseUtility.logErrorPacket(this, new ErrorRespPacket(dlerrorMap, true));
			return super.onStartCommand(intent, flags, startId);
		}
		new AsynFileDownloader(this, apkModel).execute();
		return super.onStartCommand(intent, flags, startId);
	}
	
	public final void postBeginDLNotificaton(ApkDownloadModel model){
		Builder builder = model.getBuilder();
		builder.setContentIntent(null).setContentText("��ʼ����...").setContentTitle(model.getAppName()).setSmallIcon(android.R.drawable.stat_sys_download);
		Notification notification = builder.build();
		notification.flags |= 0x20;
		int notId = Integer.parseInt(model.getAppId());
		notification.icon = android.R.drawable.stat_sys_download;
		notificationManager.notify(notId, notification);
	}
	
	public final void postRefreshDLNotification(ApkDownloadModel model){
		float total = model.getTotalSize();
		float current = model.getCurrentSize();
		int rate = (int) (100 * (current / total));
		Builder builder = model.getBuilder();
		builder.setProgress(100, rate, false);
		builder.setContentIntent(null).setContentText(rate + "%").setContentTitle(model.getAppName()).setSmallIcon(android.R.drawable.stat_sys_download);
		Notification notification = builder.build();
		notification.flags |= 0x20;
		notification.icon = android.R.drawable.stat_sys_download;
		int notId = Integer.parseInt(model.getAppId());
		notificationManager.notify(notId, notification);
	}
	
	public final void postFinishDLNotification(ApkDownloadModel model){
		Uri fileUri = Uri.fromFile(new File(model.getApkPath()));
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(fileUri,
				"application/vnd.android.package-archive");
		this.startActivity(intent);
//		PendingIntent pendingIntent = PendingIntent.getActivity(
//				this, 0, intent, 0);
//		Builder builder = model.getBuilder();
//		builder.setAutoCancel(false).setContentIntent(pendingIntent).setContentText("�������").setContentTitle(model.getAppName()).setProgress(100, 100, false).setSmallIcon(android.R.drawable.stat_sys_download);
//		Notification notification  = builder.build();
//		notificationManager.notify(Integer.parseInt(model.getAppId()), notification);
		notificationManager.cancel(Integer.parseInt(model.getAppId()));
	}
	
	public final void postErrorDLNotification(ApkDownloadModel model){
		Builder builder = model.getBuilder();
		Intent intent = new Intent();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		builder.setContentIntent(pendingIntent).setContentText("����ʧ��").setContentTitle(model.getAppName()).setSmallIcon(android.R.drawable.stat_notify_error);
		Notification notification = builder.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.icon = android.R.drawable.stat_notify_error;
		int notId = Integer.parseInt(model.getAppId());
		notificationManager.notify(notId, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
