package com.nguyenmp.gauchodroid.browser;

import com.actionbarsherlock.app.SherlockActivity;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MyWebChromeClient extends WebChromeClient {
	private SherlockActivity mActivity;
	
	public MyWebChromeClient(SherlockActivity activity) {
		mActivity = activity;
	}
	
	public void onProgressChanged(WebView view, int progress) {
		mActivity.setSupportProgress(progress*100);
		
		if (progress == 100) {
			mActivity.setSupportProgressBarVisibility(false);
			mActivity.setSupportProgressBarIndeterminateVisibility(false);
		}
		else {
			mActivity.setSupportProgressBarVisibility(true);
			mActivity.setSupportProgressBarIndeterminateVisibility(true);
		}
    }
}