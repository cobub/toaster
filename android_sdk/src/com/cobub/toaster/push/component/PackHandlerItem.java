package com.cobub.toaster.push.component;

import android.content.Context;

import com.cobub.toaster.push.model.NetPacket;

public abstract class PackHandlerItem {
	public PackHandlerItem() {
		// TODO Auto-generated constructor stub
	}
	public abstract boolean check(String type);
	public abstract NetPacket handle(Context context, NetPacket packet);
}
