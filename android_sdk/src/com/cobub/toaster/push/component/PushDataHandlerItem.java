package com.cobub.toaster.push.component;

import org.json.JSONObject;

import android.content.Context;

import com.cobub.toaster.push.model.NetPacket;

public abstract class PushDataHandlerItem {
	public PushDataHandlerItem() {
		// TODO Auto-generated constructor stub
	}
	public abstract boolean check(String type);
	public abstract NetPacket handle(Context context, JSONObject json, NetPacket rawPacket);
}
