package com.example.pushimpdemo.controller;

import com.cobub.toaster.push.api.CPushInterface;
import com.example.pushimpdemo.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationHandler {
	public static void handle(Context context, Intent intent){
		String msg = intent.getStringExtra("data");
		String expired = intent.getStringExtra("expired");
		String mid = intent.getStringExtra("mid");
		NotificationManager mgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notify = new Notification(R.drawable.ic_launcher,
				"new Message", System.currentTimeMillis());
		notify.flags = Notification.FLAG_AUTO_CANCEL;

		PendingIntent pendInt = PendingIntent.getActivity(context, 0, intent,
				android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		CharSequence titleSeq = "new Message";
		CharSequence t = msg;
		notify.setLatestEventInfo(context, titleSeq, t, pendInt);
		int randNotId = (int) (Math.random() * (0x7fffff));
		mgr.notify(randNotId, notify);
		//feedback
		CPushInterface.sendReadFeedback(context, mid, expired);
	}
}
