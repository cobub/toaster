package com.cobub.toaster.push.component;

public class PushDataHandlerFactory {
	public static PushDataHandler getPushTypeHandler(){
		return new CertainPushTypeHandler();
	}
}
