package com.cobub.toaster.push.component;

import java.util.Map;

import android.content.Context;

import com.cobub.toaster.push.cont.CommConst;
import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;

public class AppInsCmdHandler extends PackHandlerItem {
	private String mType = "appInstallReply";

	@Override
	public boolean check(String type) {
		// TODO Auto-generated method stub
		return mType.equals(type);
	}

	@Override
	public NetPacket handle(Context context, NetPacket packet) {
		// TODO Auto-generated method stub
		String status = packet.getValueByKey("status", "400");
		String errmsg = packet.getValueByKey("errmsg", "unknown error");
		if("500".equals(status)){
			Map<String, String> errMap = BaseUtility.makeErrorResopnse("500", "srv inner error:" + errmsg);
			return new ErrorRespPacket(errMap, true);
		}else if("400".equals(errmsg)){
			BaseUtility.saveSp(context, CommConst.SPNAME, "appinsflag", "false");
			Map<String, String> unknownMap= BaseUtility.makeErrorResopnse("400", "invalid request:" + errmsg);
			return new ErrorRespPacket(unknownMap, true);
		}else if("200".equals(status)){
			BaseUtility.saveSp(context, CommConst.SPNAME, "appinsflag", "true");
			return packet;
		}
		return packet;
	}

}
