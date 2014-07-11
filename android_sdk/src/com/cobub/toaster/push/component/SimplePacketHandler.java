package com.cobub.toaster.push.component;

public class SimplePacketHandler extends PacketHandler {
	public SimplePacketHandler() {
		// TODO Auto-generated constructor stub
		super();
		//add packet handler here
		mHandlers.put("hbReply", new SyncHandler());
		mHandlers.put("pubmsg", new PushMsgHandler());
		mHandlers.put("subReply", new SubCmdHandler());
		mHandlers.put("unsubReply", new UnsubCmdHandler());
	}
}
