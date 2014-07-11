package com.cobub.toaster.push.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.util.Log;

public class UConnection {
	private static UConnection singleton;
	private Socket mSocket;
	private final int SOTIMEOUT = 5 * 60 * 1000 + 30000;
	private final int CONNECTTIMEOUT = 20 * 1000;
	
	private UConnection() {
		// TODO Auto-generated constructor stub
	}
	
	public void connect(String ip, int port) throws IOException{
		SocketAddress socAddress = new InetSocketAddress(ip, port);
		mSocket = new Socket();
		mSocket.setTcpNoDelay(true);
		mSocket.setKeepAlive(true);
		mSocket.setSoTimeout(SOTIMEOUT);
		mSocket.connect(socAddress, CONNECTTIMEOUT);
	}
	
	public InputStream getSocInputStream(){
		if(mSocket != null)
			try {
				return mSocket.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		return null;
	}
	
	public OutputStream getSocOutputStream(){
		if(mSocket != null){
			try {
				return mSocket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return null;
	}
	
	public void close(){
		if(mSocket != null && !mSocket.isClosed()){
			try {
				if(getSocInputStream() != null){
					getSocInputStream().close();
				}
				if(getSocInputStream() != null){
					getSocOutputStream().close();
				}
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}
	
	public static UConnection getInstance(){
		if(singleton == null){
			singleton = new UConnection();
			return singleton;
		}
		return singleton;
	}
}
