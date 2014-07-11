package com.cobub.toaster.push.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NetPacket extends Object {
	private Map<String, String> mPackElements;
	private boolean mErrorFlag;

	public NetPacket(Map<String, String> packElements, boolean isError) {
		if (packElements == null || packElements.size() <= 0) {
			mPackElements = new HashMap<String, String>();
			mErrorFlag = true;
			return;
		}
		mPackElements = packElements;
		mErrorFlag = isError;
	}
	
	public NetPacket(String raw, boolean isError){
		String[] elements = raw.split("\\r\\n");
		Map<String, String> map = new HashMap<String, String>();
		String tmpKey = null;
		String tmpValue = null;
		for(int i = 2; i < elements.length - 2; i+=4){
			tmpKey = elements[i];
			tmpValue = elements[i + 2];
			map.put(tmpKey, tmpValue);
		}
		mPackElements = map;
		mErrorFlag = isError;
	}

	public String getType() {
		String type = mPackElements.get("cmd");
		if (type != null)
			return type;
		return "";
	}

	public String getValueByKey(String key, String op) {
		if (mPackElements.get(key) == null)
			return op;
		return mPackElements.get(key);
	}

	public int length() {
		return mPackElements.size() * 2;
	}

	public boolean isErrorPacket() {
		return mErrorFlag;
	}

	public String toString() {
		// "*2\r\n$3\r\ncmd\r\n$7\r\nhbReply\r\n"
		StringBuffer strBuff = new StringBuffer("*");
		strBuff.append(length());
		strBuff.append("\r\n");
		String tmpKey = null;
		String tmpValue = null;
		Iterator<String> iterator = mPackElements.keySet().iterator();
		while (iterator.hasNext()) {
			tmpKey = iterator.next();
			strBuff.append("$" + tmpKey.getBytes().length + "\r\n");
			strBuff.append(tmpKey + "\r\n");
			tmpValue = mPackElements.get(tmpKey);
			strBuff.append("$" + tmpValue.getBytes().length + "\r\n");
			strBuff.append(tmpValue + "\r\n");
		}
		return strBuff.toString();
	}
}
