package com.nguyenmp.gauchodroid.browser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.DownloadListener;
import android.widget.Toast;


public class MyDownloadListener implements DownloadListener {
	
	private Context mContext;
	
	public MyDownloadListener(Context context) {
		mContext = context;
	}

	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		Toast.makeText(mContext, "Downloading: " + url, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		intent.setType(mimetype);
		mContext.startActivity(intent);
	}
}