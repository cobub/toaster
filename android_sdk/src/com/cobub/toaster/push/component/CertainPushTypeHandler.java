package com.cobub.toaster.push.component;

public class CertainPushTypeHandler extends PushDataHandler {
	public CertainPushTypeHandler() {
		// TODO Auto-generated constructor stub
		super();
		//add type here
		mHandlers.put("notification", new NotificationHandler());
		mHandlers.put("transparent", new TransparentMsgHandler());
	}
}
