package com.example.pushimpdemo;

import com.example.pushimpdemo.controller.NotificationHandler;
import com.example.pushimpdemo.controller.SMSHandler;
import com.example.pushimpdemo.controller.ToastHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DemoReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent != null){
			//弹出通知栏
			NotificationHandler.handle(context, intent);
			//插入短信
			SMSHandler.handle(context, intent);
			//Toast弹窗
			ToastHandler.handle(context, intent);
		}
	}

}
