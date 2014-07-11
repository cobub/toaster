package com.cobub.toaster.push.component;

import java.io.IOException;

import com.cobub.toaster.push.component.UConnection;

public abstract class BaseIOAdapter {
	public UConnection mConnection;
	public BaseIOAdapter(UConnection connection) {
		// TODO Auto-generated constructor stub
		mConnection = connection;
	}

	public abstract Object recPack() throws IOException;
	
	public abstract void sendPack(Object data) throws IOException;
	
	public UConnection getConnection() {
		return mConnection;
	}
}
