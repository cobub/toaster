package com.cobub.toaster.push.component;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;

public class TransparentMsgHandler extends PushDataHandlerItem{
	private String mType = "transparent";

	@Override
	public boolean check(String type) {
		// TODO Auto-generated method stub
		return mType.equals(type);
	}

	@Override
	public NetPacket handle(Context context, JSONObject json, NetPacket rawPacket) {
		// TODO Auto-generated method stub
		String mid = rawPacket.getValueByKey("mid", "");
		String expired = rawPacket.getValueByKey("expired", "");
		String channel = rawPacket.getValueByKey("channel", "");
		try{
			JSONObject configJs = json.getJSONObject("config");
			String msgContent = configJs.getString("content");
			
			Intent intent = new Intent();
			intent.setAction(context.getApplicationContext().getPackageName());
			intent.putExtra("mid", mid);
			intent.putExtra("data", msgContent);
			intent.putExtra("expired", expired);
			intent.putExtra("channel", channel);
			context.sendBroadcast(intent);
		}catch(JSONException e){
			e.printStackTrace();
			Map<String, String> errorMap = BaseUtility.makeErrorResopnse("410", "Json parse error:" + e.getMessage());
			return new ErrorRespPacket(errorMap, true);
		}
		return rawPacket;
	}

}
