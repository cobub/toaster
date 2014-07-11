package com.cobub.toaster.push.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LogHelper extends SQLiteOpenHelper {
	public static final String EVENT = "event";
	public static final String ERROR = "error";
	private static final String DB_NAME = "record.db"; 
	private static final int version = 1; 

	public LogHelper(Context context) {
		super(context, DB_NAME, null, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql4createErrTb = "create table IF NOT EXISTS "
				+ ERROR
				+ "(id integer primary key autoincrement, errorcode varchar(20), errordesc varchar(45))";
		String sql4createEveTb = "create table IF NOT EXISTS "
				+ EVENT
				+ "(id integer primary key autoincrement, item varchar(45))";
		db.execSQL(sql4createEveTb);
		db.execSQL(sql4createErrTb);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE if exists person");  
        onCreate(db);  
	}

}
