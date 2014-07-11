package com.cobub.toaster.push.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;

import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;

public abstract class PacketHandler {
	protected Map<String, PackHandlerItem> mHandlers = new HashMap<String, PackHandlerItem>();
	
	public NetPacket hanlde(Context context, NetPacket packet){
		Iterator<String> iterator = mHandlers.keySet().iterator();
		String type = packet.getType();
		String tmpKey = null;
		while(iterator.hasNext()){
			tmpKey = iterator.next();
			if (type.equals(tmpKey)) {
				return mHandlers.get(tmpKey).handle(context, packet);
			}
		}
		
		if(packet.isErrorPacket()){
			return packet;
		}
		//if cmd type not found, record it
		Map<String, String> excpResp = BaseUtility.makeErrorResopnse("404", "operation not found");
		//not exactly an error, just an unknown operation, ignore it
		return new ErrorRespPacket(excpResp, true);
	}
}
