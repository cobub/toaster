package com.cobub.toaster.push.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.cobub.toaster.push.component.UConnection;
import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;

public class RedisIOAdapter extends BaseIOAdapter {
	private Object rLock = new Object();
	private Object wLock = new Object();
	private OutputStream mOs;
	private BufferedReader mBr;

	public RedisIOAdapter(UConnection connection) {
		super(connection);
		// TODO Auto-generated constructor stub
	}

	public void reset() throws IOException {
		InputStream is = mConnection.getSocInputStream();
		if (is == null)
			throw new IOException("no valid channel to read");
		mBr = new BufferedReader(new InputStreamReader(is));
		mOs = mConnection.getSocOutputStream();
	}

	@Override
	public NetPacket recPack() throws IOException {
		// TODO Auto-generated method stub
		synchronized (rLock) {
			Map<String, String> packMap = new HashMap<String, String>();
			String packSizeLine = mBr.readLine();
			if (packSizeLine == null)
				throw new IOException("connection broken down");
			// check is an error response
			if (packSizeLine.matches("^(-)[0-9]*\\s.*")) {
				// is an error response
				int splPos = packSizeLine.indexOf(" ");
				// String[] parts = packSizeLine.split("\\s");
				String errorCode = packSizeLine.substring(1, splPos);
				String errorDesc = packSizeLine.substring(splPos + 1);
				Map<String, String> errorResp = BaseUtility.makeErrorResopnse(
						errorCode, errorDesc);
				return new ErrorRespPacket(errorResp, true);
			}
			if (!packSizeLine.matches("\\*[0-9]+"))
				throw new PacketParseErrorException("size fomat not match");
			int size = Integer.parseInt(packSizeLine.substring(1));
			String argSize = null;
			String keyLine = null;
			String valueLine = null;
			for (int i = 0; i < size / 2; i++) {
				// get key
				argSize = mBr.readLine();
				if (argSize == null)
					throw new IOException("connection broken down");
				if (argSize.matches("\\$[0-9]+")) {
					argSize = argSize.substring(1);
				} else {
					throw new PacketParseErrorException(
							"arg length in error format");
				}
				keyLine = mBr.readLine();
				if (keyLine == null)
					throw new IOException("not a complete packet");
				if (keyLine.getBytes("UTF8").length != Integer
						.parseInt(argSize))
					throw new PacketParseErrorException(
							"key length not match:argSize("
									+ Integer.parseInt(argSize)
									+ "), value bytes("
									+ keyLine.getBytes("UTF8").length + ")");
				// get value
				argSize = mBr.readLine();
				if (argSize == null)
					throw new IOException("connection broken down");
				if (argSize.matches("\\$[0-9]+")) {
					argSize = argSize.substring(1);
				} else {
					throw new PacketParseErrorException(
							"arg length in error format");
				}
				valueLine = mBr.readLine();
				if (valueLine == null)
					throw new IOException("not a complete packet");
				if (valueLine.getBytes("UTF8").length != Integer
						.parseInt(argSize))
					throw new PacketParseErrorException(
							"value length not match:argSize("
									+ Integer.parseInt(argSize)
									+ "), value bytes("
									+ valueLine.getBytes("UTF8").length + ")");

				packMap.put(keyLine, valueLine);
			}
			NetPacket result = new NetPacket(packMap, false);
			Log.d(this.getClass().getSimpleName(),
					"rec packet:" + result.getType());
			return new NetPacket(packMap, false);
		}
	}

	@Override
	public void sendPack(Object data) throws IOException {
		// TODO Auto-generated method stub
		NetPacket packet = (NetPacket) data;
		Log.d(this.getClass().getSimpleName(),
				"send packet:" + packet.getType());
		synchronized (wLock) {
			if (mOs == null)
				throw new IOException("no valid channel to write");
			mOs.write(packet.toString().getBytes("UTF8"));
			mOs.flush();
		}
	}

	public void sendRawPack(String data) throws IOException {
		Log.d(this.getClass().getSimpleName(),
				"send packet:" + data.replace("\r\n", "\\r\\n"));
		synchronized (wLock) {
			if (mOs == null)
				throw new IOException("no valid channel to write");
			mOs.write(data.toString().getBytes("UTF8"));
			mOs.flush();
		}
	}

}
