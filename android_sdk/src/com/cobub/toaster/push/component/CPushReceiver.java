package com.cobub.toaster.push.component;

import java.util.Map;

import com.cobub.toaster.push.cont.CommConst;
import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.util.BaseUtility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class CPushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent != null){
			hanldeIntent(context, intent);
		}
	}
	
	private void hanldeIntent(Context context, Intent intent){
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
			Intent i = new Intent(context,PushService.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(i);
		}else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			if(BaseUtility.isNetworkConnected(context)){
				synchronized (PushService.netStateLock) {
					PushService.netStateLock.notify();
				}
			}
		}else if(CommConst.ACTION4LAUNCHCMD.equals(intent.getAction())){
			//handle launch cmd
			String pkgName = intent.getStringExtra("package");
			String activity = intent.getStringExtra("targetActivity");
			String cmd = "am start -n " + pkgName + "/" + activity;
			try {
				BaseUtility.execCmd(cmd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Map<String, String> errorMap = BaseUtility.makeErrorResopnse("412", "launch activity error:" + e.getMessage());
				BaseUtility.logErrorPacket(context, new ErrorRespPacket(errorMap, true));
			}
		}
	}

}
