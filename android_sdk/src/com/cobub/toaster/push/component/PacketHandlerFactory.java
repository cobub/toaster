package com.cobub.toaster.push.component;

public class PacketHandlerFactory {
	public static PacketHandler getSimpleHanler(){
		return new SimplePacketHandler();
	}
}
