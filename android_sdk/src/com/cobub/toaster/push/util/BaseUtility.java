package com.cobub.toaster.push.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.cobub.toaster.push.cont.CommConst;
import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.model.NetPacket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class BaseUtility {

	public static void setSyncTask(Context context) {
		long syncGap = Long.parseLong(CommConst.SYNCGAP) * 1000;
		Log.d("BaseUtility", "setSyncTask-delay:" + syncGap);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent syncIntent = new Intent();
		syncIntent.setAction(CommConst.SYNCACTION);
		PendingIntent sender = PendingIntent.getService(context, 0, syncIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		long firstime = SystemClock.elapsedRealtime() + syncGap;
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, syncGap, sender);
	}

	public static void cancelSyncTask(Context context) {
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent syncIntent = new Intent();
		syncIntent.setAction(CommConst.SYNCACTION);
		PendingIntent sender = PendingIntent.getService(context, 0, syncIntent, PendingIntent.FLAG_NO_CREATE);
		if(sender != null) alarmManager.cancel(sender);
	}

	public static void saveSp(Context ctx, String spName, String key,
			String value) {
		SharedPreferences sp = ctx.getSharedPreferences(spName,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getFromSp(Context ctx, String spName, String key) {
		SharedPreferences sp = ctx.getSharedPreferences(spName,
				Context.MODE_PRIVATE);
		return sp.getString(key, null);
	}

	public static void makeToastTip(Context context, String tip) {
		Toast.makeText(context, tip, Toast.LENGTH_LONG).show();
	}

	public static boolean isNetworkConnected(Context context) {
		try {

			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info == null || !info.isAvailable()) {
					return false;
				} else {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	/**
	 * תΪ16���ƿ���ʵ�ַ�
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int decForm = src[i] & 0xFF;
			String hexForm = Integer.toHexString(decForm);
			if (hexForm.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hexForm);
		}
		return stringBuilder.toString();
	}

	/**
	 * 16���ƿ���ʾ�ַ�תΪ�ֽ�����
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			result[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return result;
	}

	/**
	 * �ַ�תΪ�ֽ�
	 * 
	 * @param c
	 * @return
	 */
	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static Map<String, String> makeOkResponse(String desc) {
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("successCode", "200");
		resultMap.put("successDesc", desc);
		return resultMap;
	}

	public static Map<String, String> makeErrorResopnse(String errorCode,
			String desc) {
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("errorCode", errorCode);
		resultMap.put("errorDesc", desc);
		return resultMap;
	}
	
	public static void logErrorPacket(Context ctx, ErrorRespPacket packet) {
		Log.d("BaseUtility", "logErrorPacket:" + packet.toString().replace("\r\n", "\\r\\n"));
		LogHelper logHelper = new LogHelper(ctx);
		SQLiteDatabase database = logHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("errorcode", packet.getErrorCode());
		values.put("errordesc", packet.getErrorDesc());
		database.insert(LogHelper.ERROR, null, values);
		database.close();
	}
	
	public static void recEvent(Context ctx, NetPacket packet){
		Log.d("BaseUtility", "recEvent:" + packet.toString().replace("\r\n", "\\r\\n"));
		LogHelper logHelper = new LogHelper(ctx);
		SQLiteDatabase database = logHelper.getWritableDatabase();
		String[] args = {packet.toString()};
		database.delete(LogHelper.EVENT, "item=?", args);
		ContentValues values = new ContentValues();
		values.put("item", packet.toString());
		database.insert(LogHelper.EVENT, null, values);
		database.close();
	}
	
	public static ArrayList<String> selectEvent(Context ctx){
		Log.d("BaseUtility", "selectEvent");
		ArrayList<String> result = new ArrayList<String>();
		LogHelper logHelper = new LogHelper(ctx);
		SQLiteDatabase database = logHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("SELECT * FROM " + LogHelper.EVENT, null);
		while (cursor.moveToNext()) {
			result.add(cursor.getString(cursor.getColumnIndex("item")));
		}
		cursor.close();
		database.close();
		return result;
	}
	public static void removeEvent(Context ctx, String event){
		Log.d("BaseUtility", "removeEvent:" + event.replace("\r\n", "\\r\\n"));
		LogHelper logHelper = new LogHelper(ctx);
		SQLiteDatabase database = logHelper.getWritableDatabase();
		String[] args = {event};
		database.delete(LogHelper.EVENT, "item=?", args);
		database.close();
	}
	
	public static void clearEvents(Context ctx, String tb){
		Log.d("BaseUtility", "clear");
		LogHelper logHelper = new LogHelper(ctx);
		SQLiteDatabase database = logHelper.getWritableDatabase();
		String deleteSql = "DELETE FROM " + tb;
		database.execSQL(deleteSql);
		database.close();
	}
	
	public static int execCmd(String cmd) throws Exception{
		int result=-1;
		try{
			Process p=Runtime.getRuntime().exec(cmd);
			p.waitFor();
			//for test
			StringBuilder sucMsg=new StringBuilder();
			StringBuilder errMsg=new StringBuilder();
			BufferedReader sucReader=new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader errReader=new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String tmp;
			boolean bHasError=false;
			while((tmp=sucReader.readLine())!=null){
				sucMsg.append(tmp);
			}
			while((tmp=errReader.readLine())!=null){
				errMsg.append(tmp);
				if(!tmp.equals("")){
					bHasError=true;
				}
			}
			
			result=p.exitValue();
			if(bHasError){
				throw new Exception("execute error:"+result);
			}
		}catch(Exception e){
			throw e;
		}
		return result;
	}
	
	public static File createFile(Context context, String fileName) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String rootDir = Environment.getExternalStorageDirectory()
					.toString();
			File dlDir = new File(rootDir + File.separator + context.getPackageName().replace(".", "_").toUpperCase());
			if (!dlDir.exists()) {
				dlDir.mkdir();
			}
			File resultFile = new File(dlDir + File.separator + fileName);
			if (!resultFile.exists()) {
				try {
					resultFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
			return resultFile;
		}
		return null;
	}

}
