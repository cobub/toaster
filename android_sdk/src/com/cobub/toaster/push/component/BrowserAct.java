package com.cobub.toaster.push.component;

import java.util.Map;

import com.cobub.toaster.push.model.ErrorRespPacket;
import com.cobub.toaster.push.util.BaseUtility;

import android.net.http.SslError;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class BrowserAct extends Activity {
	final Activity activity = this;

	// private WebView m_webview;
	@SuppressLint({ "SetJavaScriptEnabled", "ResourceAsColor" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		RelativeLayout layout = new RelativeLayout(this);
		LayoutParams params;
		params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		layout.setLayoutParams(params);
		WebView webview = new WebView(this);
		layout.addView(webview);
		setContentView(layout);
		Intent intent = this.getIntent();
		if (intent == null) {
			finish();
		}
		webview.getSettings().setJavaScriptEnabled(true);
		// webview.getSettings().setBlockNetworkImage(false);
		// webview.getSettings().setBlockNetworkLoads(false);
		webview.getSettings().setDomStorageEnabled(true);
		webview.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				super.onProgressChanged(view, newProgress);
				activity.setTitle("Loading...");
				activity.setProgress(newProgress * 100);
				if (newProgress == 100)
					activity.setTitle("");
			}
		});
		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.d("shouldOverrideUrlLoading", "true");
				view.loadUrl(url);
				return true;
			}

			public void onPageFinished(WebView view, String url) {

			}
		});
		String url = intent.getStringExtra("url");
		String page = intent.getStringExtra("page");

		if (url != null) {
			webview.loadUrl(url);
			return;
		}

		if (page != null) {
			webview.getSettings().setDefaultTextEncodingName("UTF8");
			webview.loadData(page, "text/html", "UTF8");
			return;
		}

		Map<String, String> errorMap = BaseUtility.makeErrorResopnse("414",
				"webview load error:no url or page found");
		BaseUtility.logErrorPacket(this, new ErrorRespPacket(errorMap, true));
	}

}
