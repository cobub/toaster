package com.example.pushimpdemo.controller;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ToastHandler {
	public static void handle(Context context, Intent intent){
		String msg = intent.getStringExtra("data");
		String expired = intent.getStringExtra("expired");
		String mid = intent.getStringExtra("mid");
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
}
