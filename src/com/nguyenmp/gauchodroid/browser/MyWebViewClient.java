package com.nguyenmp.gauchodroid.browser;

import java.util.List;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.login.LoginManager;

public class MyWebViewClient extends WebViewClient {
	private Context mContext;
	
	public MyWebViewClient(Context context) {
		mContext = context;
	}
	
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//    	System.out.println(url);
    	if (url.startsWith("https://gauchospace.ucsb.edu/courses/file.php/")) {
			List<String> pathSegments = Uri.parse(url).getPathSegments();
			String filename = pathSegments.get(pathSegments.size() - 1);

    		int notifID = new Random().nextInt();
    		Notification notif = new Notification(R.drawable.ic_launcher, "Downloading... " + filename, System.currentTimeMillis());
    		notif.flags |= Notification.FLAG_ONGOING_EVENT;
			notif.flags &= ~Notification.FLAG_AUTO_CANCEL;
			notif.flags |= Notification.FLAG_NO_CLEAR;
			notif.flags |= Notification.FLAG_SHOW_LIGHTS;
			notif.defaults &= ~Notification.DEFAULT_ALL;
    		notif.setLatestEventInfo(mContext, "Downloading... ", filename, PendingIntent.getActivity(mContext, 0, new Intent(), 0));
    		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    		nm.notify(notifID, notif);
    		
    		Thread downloader = new GauchoDownloader(url, filename, LoginManager.getCookies(mContext), new DownloadHandler(notif, notifID, mContext));
    		downloader.start();
    		return true;
    	} else if (url.startsWith("https://gauchospace.ucsb.edu/courses/mod/resource/view.php")) {
    		url = url.concat("&inpopup=false");
    		view.loadUrl(url);
    		return true;
    	}
    	return super.shouldOverrideUrlLoading(view, url);
    }
}
