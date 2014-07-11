package com.cobub.toaster.push.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;

public class PushMsgHandler extends PackHandlerItem {
	private String cmd_type = "pubmsg";

	@Override
	public boolean check(String type) {
		// TODO Auto-generated method stub
		return cmd_type.equals(type);
	}

	@Override
	public NetPacket handle(Context context, NetPacket packet) {
		// TODO Auto-generated method stub
		try {
			String mid = packet.getValueByKey("mid", "");
			String msgContent = packet.getValueByKey("data", "");
			String expired = packet.getValueByKey("expired", "");
			long expiredTime = Long.parseLong(expired);
			long currentTT = System.currentTimeMillis() / 1000;
			if (expiredTime != 0 && expiredTime < currentTT) {
				Map<String, String> errorInfo = BaseUtility.makeErrorResopnse(
						"405", "msg over expired");
				return new ErrorRespPacket(errorInfo, true);
			}
			//handle push data
			JSONObject dataJson = new JSONObject(msgContent);
			PushDataHandler handler = PushDataHandlerFactory.getPushTypeHandler();
			NetPacket pushHandleResult = handler.handle(context, dataJson, packet);
			// if is an error packet then handle it
			if (pushHandleResult.isErrorPacket()) {
				Log.e("PushMsgHandler", "ERRINFO:" + pushHandleResult);
				BaseUtility.logErrorPacket(context, (ErrorRespPacket)pushHandleResult);
			} 
			NetPacket netPacket = makePushResp(mid, expired);
			// feedback for push
			PushService.getIoAdapter().sendPack(netPacket);
			// return raw packet
			return packet;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			PushService.getIoAdapter().getConnection().close();
			Map<String, String> errorInfo = BaseUtility.makeErrorResopnse("407",
					"push feedback failed");
			return new ErrorRespPacket(errorInfo, true);
		} catch (Exception e){
			e.printStackTrace();
			Map<String, String> errorInfo = BaseUtility.makeErrorResopnse("400",
					"packet parse error:" + e.getMessage());
			return new ErrorRespPacket(errorInfo, true);
		}
	}

	private NetPacket makePushResp(String mid, String expired) {
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("cmd", "recvdFeedback");
		resultMap.put("mid", mid);
		resultMap.put("expired", expired);
		return new NetPacket(resultMap, false);
	}

}
