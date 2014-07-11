package com.cobub.toaster.push.component;

import android.content.Context;

import com.cobub.toaster.push.model.NetPacket;

public class SyncHandler extends PackHandlerItem {
	private String cmd_type = "hbReply";

	@Override
	public boolean check(String type) {
		// TODO Auto-generated method stub
		return cmd_type.equals(type);
	}

	@Override
	public NetPacket handle(Context context, NetPacket packet) {
		// TODO Auto-generated method stub
		return packet;
	}

}
