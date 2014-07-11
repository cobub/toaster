package com.cobub.toaster.push.model;

import java.util.Map;

public class ErrorRespPacket extends NetPacket {

	public ErrorRespPacket(Map<String, String> packElements, boolean isError) {
		super(packElements, isError);
		// TODO Auto-generated constructor stub
	}
	
	public String getErrorCode(){
		return getValueByKey("errorCode", "410");
	}
	
	public String getErrorDesc(){
		return getValueByKey("errorDesc", "no desc found by unknown error");
	}
	
	@Override
	public String toString(){
		StringBuffer strBuff = new StringBuffer("-");
		strBuff.append(getErrorCode() + " ");
		strBuff.append(getErrorDesc() + "\r\n");
		return strBuff.toString();
	}
}
