package com.cobub.toaster.push.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import android.R.raw;
import android.content.Context;

import com.cobub.toaster.push.api.CPushInterface;
import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;

public abstract class PushDataHandler {
	protected Map<String, PushDataHandlerItem> mHandlers = new HashMap<String, PushDataHandlerItem>();
	
	public NetPacket handle(Context context, JSONObject json, NetPacket rawPacket){
		try {
			Iterator<String> iterator = mHandlers.keySet().iterator();
			String type = json.getString("type");
			String tmpKey = null;
			while(iterator.hasNext()){
				tmpKey = iterator.next();
				if (type.equals(tmpKey)) {
					return mHandlers.get(tmpKey).handle(context, json, rawPacket);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Map<String, String> excpResp = BaseUtility.makeErrorResopnse("409", "push data handle error:" + e.getMessage());
			return new ErrorRespPacket(excpResp, true);
		}
		Map<String, String> excpResp = BaseUtility.makeErrorResopnse("408", "push type not found");
		return new ErrorRespPacket(excpResp, true);
	}
}
