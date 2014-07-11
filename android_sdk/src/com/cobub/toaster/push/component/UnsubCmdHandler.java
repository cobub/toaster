package com.cobub.toaster.push.component;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;

import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;

public class UnsubCmdHandler extends PackHandlerItem {
	private String mType = "unsubReply";

	@Override
	public boolean check(String type) {
		// TODO Auto-generated method stub
		return mType.equals(type);
	}

	@Override
	public NetPacket handle(Context context, NetPacket packet) {
		// TODO Auto-generated method stub
		String status = packet.getValueByKey("status", "400");
		String errormsg = packet.getValueByKey("errmsg", "unknown error");
		if(status.equals("200")){
			ArrayList<String> events = BaseUtility.selectEvent(context);
			NetPacket tmpNetpacket = null;
			for(String event:events){
				tmpNetpacket = new NetPacket(event, false);
				if(tmpNetpacket.getType().equals("unsub")){
					BaseUtility.removeEvent(context, event);
				}
			}
			return packet;
		}else if(status.equals("400")){
			ArrayList<String> events = BaseUtility.selectEvent(context);
			NetPacket tmpNetpacket = null;
			for(String event:events){
				tmpNetpacket = new NetPacket(event, false);
				if(tmpNetpacket.getType().equals("unsub")){
					BaseUtility.removeEvent(context, event);
				}
			}
			//return error
			Map<String, String> errMap = BaseUtility.makeErrorResopnse("400", "invalid request:" + errormsg);
			return new ErrorRespPacket(errMap, true);
		}else if(status.equals("500")){
			//inner error, do not remove cache
			Map<String, String> errMap = BaseUtility.makeErrorResopnse("500", "srv inner error:" + errormsg);
			return new ErrorRespPacket(errMap, true);
		}
		return packet;
	}
}
