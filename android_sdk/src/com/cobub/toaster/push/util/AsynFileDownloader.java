package com.cobub.toaster.push.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.cobub.toaster.push.component.DownloadService;
import com.cobub.toaster.push.model.ApkDownloadModel;

import android.os.AsyncTask;

public class AsynFileDownloader extends AsyncTask<Void, Integer, Boolean> {
	private final float proUpdateGap = 0.03f;
	private DownloadService mDlService;
	private ApkDownloadModel mModel;

	public AsynFileDownloader(DownloadService downloadService,
			ApkDownloadModel model) {
		// TODO Auto-generated constructor stub
		mDlService = downloadService;
		mModel = model;
	}

	@Override
	protected Boolean doInBackground(Void[] parms) {
		// TODO Auto-generated method stub
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet req = new HttpGet(mModel.getDlUrl());
			HttpParams params = req.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
			HttpConnectionParams.setSoTimeout(params, 20 * 1000);
			req.setParams(params);
			HttpResponse resp = client.execute(req);
			if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return false;
			}
			HttpEntity httpEntity = resp.getEntity();
			mModel.setTotalSize(httpEntity.getContentLength());
			InputStream is = httpEntity.getContent();

			// save
			File apkFile = new File(mModel.getApkPath());
			FileOutputStream fos = new FileOutputStream(apkFile);
			byte buf[] = new byte[2048];
			int numReads = 0;
			while ((numReads = is.read(buf)) > 0) {
				fos.write(buf, 0, numReads);
				publishProgress(numReads);
			}
			fos.close();
			is.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (result) {
			mDlService.postFinishDLNotification(mModel);
		} else {
			mDlService.postErrorDLNotification(mModel);
		}
	}

	@Override
	protected void onProgressUpdate(Integer[] values) {
		// TODO Auto-generated method stub
//		long current = mModel.getCurrentSize();
//		current += values[0];
//		mModel.setCurrentSize(current);
//		mDlService.postRefreshDLNotification(mModel);
		long current = mModel.getCurrentSize();
		current += values[0];
		mModel.setCurrentSize(current);
		if((float)((mModel.getCurrentSize() - mModel.getmProgressSize())) / (float)mModel.getTotalSize() > proUpdateGap){
			mModel.setmProgressSize(current);
			mDlService.postRefreshDLNotification(mModel);
		}
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		mDlService.postBeginDLNotificaton(mModel);
		super.onPreExecute();
	}

}
