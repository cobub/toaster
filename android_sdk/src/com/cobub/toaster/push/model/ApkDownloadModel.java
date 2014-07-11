package com.cobub.toaster.push.model;

import java.io.Serializable;
import android.support.v4.app.NotificationCompat.Builder;

public class ApkDownloadModel implements Serializable{
	private String mAppName;
	private String mDlUrl;
	private int mViewId;
	private String mAppId;
	private Builder mBuilder;
	private String mApkPath;
	private long mTotalSize;
	private long mCurrentSize;
	private long mProgressSize;


	public ApkDownloadModel(String appName, String dlUrl, int vid, String appId) {
		// TODO Auto-generated constructor stub
		mAppName = appName;
		mDlUrl = dlUrl;
		mViewId = vid;
		mAppId = appId;
	}

	public String getAppName() {
		return mAppName;
	}

	public void setAppName(String mAppName) {
		this.mAppName = mAppName;
	}

	public String getDlUrl() {
		return mDlUrl;
	}

	public void setDlUrl(String mDlUrl) {
		this.mDlUrl = mDlUrl;
	}

	public int getViewId() {
		return mViewId;
	}

	public void setViewId(int mViewId) {
		this.mViewId = mViewId;
	}

	public String getAppId() {
		return mAppId;
	}

	public void setAppId(String mAppId) {
		this.mAppId = mAppId;
	}

	public String getApkPath() {
		return mApkPath;
	}

	public void setApkPath(String mApkPath) {
		this.mApkPath = mApkPath;
	}

	public Builder getBuilder() {
		return mBuilder;
	}

	public void setBuilder(Builder mBuilder) {
		this.mBuilder = mBuilder;
	}

	public long getTotalSize() {
		return mTotalSize;
	}

	public void setTotalSize(long mTotalSize) {
		this.mTotalSize = mTotalSize;
	}

	public long getCurrentSize() {
		return mCurrentSize;
	}

	public void setCurrentSize(long mCurrentSize) {
		this.mCurrentSize = mCurrentSize;
	}
	public long getmProgressSize() {
		return mProgressSize;
	}
	
	public void setmProgressSize(long mProgressSize) {
		this.mProgressSize = mProgressSize;
	}
}
