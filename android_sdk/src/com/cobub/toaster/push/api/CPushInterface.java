package com.cobub.toaster.push.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cobub.toaster.push.component.PushService;
import com.cobub.toaster.push.component.RedisIOAdapter;
import com.cobub.toaster.push.cont.CommConst;
import com.cobub.toaster.push.model.NetPacket;
import com.cobub.toaster.push.util.BaseUtility;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CPushInterface {

	public static void initPushService(Context ctx, String appid, String uid, String host,
			int port) {
		BaseUtility.saveSp(ctx, CommConst.SPNAME, "host", host);
		BaseUtility.saveSp(ctx, CommConst.SPNAME, "uid", uid);
		BaseUtility.saveSp(ctx, CommConst.SPNAME, "appid", appid);
		BaseUtility.saveSp(ctx, CommConst.SPNAME, "port", port + "");
		Intent intent = new Intent(ctx, PushService.class);
		ctx.startService(intent);
	}

	public static void subChannel(Context ctx, String channelName) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("cmd", "sub");
		map.put("channel", channelName);
		map.put("appid", BaseUtility.getFromSp(ctx, CommConst.SPNAME, "appid"));
		BaseUtility.recEvent(ctx, new NetPacket(map, false));
		RedisIOAdapter ioAdapter = (RedisIOAdapter) PushService.getIoAdapter();
		if (ioAdapter != null) {
			try {
				ioAdapter.sendPack(new NetPacket(map, false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (ioAdapter.getConnection() != null) {
					ioAdapter.getConnection().close();
				}
			}
		}
	}

	public static void unsubChannel(Context ctx, String channelName) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("cmd", "unsub");
		map.put("channel", channelName);
		map.put("appid", BaseUtility.getFromSp(ctx, CommConst.SPNAME, "appid"));
		BaseUtility.recEvent(ctx, new NetPacket(map, false));
		RedisIOAdapter ioAdapter = (RedisIOAdapter) PushService.getIoAdapter();
		if (ioAdapter != null) {
			try {
				ioAdapter.sendPack(new NetPacket(map, false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (ioAdapter.getConnection() != null) {
					ioAdapter.getConnection().close();
				}
			}
		}
	}
	
	public static void sendReadFeedback(Context ctx, String mid,
			String expired) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("cmd", "msgReadFeedback");
		map.put("expired", expired);
		map.put("mid", mid);
		RedisIOAdapter ioAdapter = (RedisIOAdapter) PushService.getIoAdapter();
		if (ioAdapter != null) {
			try {
				ioAdapter.sendPack(new NetPacket(map, false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				BaseUtility.recEvent(ctx, new NetPacket(map, false));
				if (ioAdapter.getConnection() != null) {
					ioAdapter.getConnection().close();
				}
			}
		}else{
			BaseUtility.recEvent(ctx, new NetPacket(map, false));
		}
	}

}
