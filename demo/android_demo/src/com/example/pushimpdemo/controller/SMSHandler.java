package com.example.pushimpdemo.controller;

import com.example.pushimpdemo.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class SMSHandler {
	public static void handle(Context context, Intent intent) {
		/*
		 * need permission:<uses-permission
		 * android:name="android.permission.WRITE_SMS" /> <uses-permission
		 * android:name="android.permission.READ_SMS" />
		 */
		String msg = intent.getStringExtra("data");
		String expired = intent.getStringExtra("expired");
		String mid = intent.getStringExtra("mid");
		ContentValues values = new ContentValues();
		values.put("date", System.currentTimeMillis());
		values.put("read", 0);
		values.put("type", 1);
		values.put("address", "PushDemo");
		values.put("body", msg);
		context.getContentResolver().insert(Uri.parse("content://sms"), values);
		// notify
		NotificationManager mgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notify = new Notification(R.drawable.ic_launcher,
				"new message", System.currentTimeMillis());
		notify.flags = Notification.FLAG_AUTO_CANCEL;
		Uri uri = Uri.parse("smsto:" + "PushDemo");
		Intent intent1 = new Intent(Intent.ACTION_SENDTO, uri);
		PendingIntent pendInt = PendingIntent.getActivity(context, 0, intent1,
				android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		CharSequence title = "PushDemo";
		CharSequence t = msg;
		notify.setLatestEventInfo(context, title, t, pendInt);
		int randNotId = (int) (Math.random() * (0x7fffff));
		mgr.notify(randNotId, notify);
	}
}
