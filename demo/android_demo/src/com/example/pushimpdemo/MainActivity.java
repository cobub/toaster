package com.example.pushimpdemo;


import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cobub.toaster.push.api.CPushInterface;

public class MainActivity extends Activity {
	private Button subButton;
	private Button unsubButton;
	private EditText channelInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		componentInit();
		CPushInterface.initPushService(getApplicationContext(), "appid",
				"userid", "192.168.1.2", 8001);
		Log.e("UUID", Build.MODEL);
		CPushInterface.subChannel(MainActivity.this, "channel1");
	}

	private void componentInit() {
		subButton = (Button) findViewById(R.id.channel_submit);
		unsubButton = (Button) findViewById(R.id.channel_unsubmit);
		channelInput = (EditText) findViewById(R.id.channel_input);
		subButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String input = channelInput.getText().toString();
				if (!TextUtils.isEmpty(input)) {
					CPushInterface.subChannel(MainActivity.this, input);
					Toast.makeText(MainActivity.this, "set channel:" + input,
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(MainActivity.this, "sss",
							Toast.LENGTH_LONG).show();
				}
			}
		});  
		unsubButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String input = channelInput.getText().toString();
				if (!TextUtils.isEmpty(input)) {
					CPushInterface.unsubChannel(MainActivity.this, input);
					Toast.makeText(MainActivity.this, "set channel:" + input,
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(MainActivity.this, "ss",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
