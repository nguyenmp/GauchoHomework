package com.nguyenmp.gauchodroid.browser;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.webkit.MimeTypeMap;

public class DownloadHandler extends Handler {
	private final Notification mNotif;
	private final Context mContext;
	private final int mID;
	
	public DownloadHandler(Notification notif, int id, Context context) {
		mNotif = notif;
		mContext = context;
		mID = id;
	}
	
	@Override
	public void handleMessage(Message message) {
		mNotif.flags &= ~Notification.FLAG_ONGOING_EVENT;
		mNotif.flags &= ~Notification.FLAG_AUTO_CANCEL;
		mNotif.flags |= Notification.FLAG_SHOW_LIGHTS;
		mNotif.flags &= ~Notification.FLAG_NO_CLEAR;
		mNotif.defaults |= Notification.DEFAULT_ALL;
		
		if (message.obj instanceof String) {
			File file = new File((String) message.obj);
			
			Uri data = Uri.fromFile(file);
			
			String fileExtension = MimeTypeMap.getFileExtensionFromUrl(data.toString());
			String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
			
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(data);
			intent.setType(mimeType);
			intent.setDataAndType(data, mimeType);
			PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent , 0);
			
			mNotif.setLatestEventInfo(mContext, "Downloaded " + file.getName(), file.getPath(), contentIntent);
		} else if (message.obj instanceof Exception){
			mNotif.flags &= Notification.FLAG_AUTO_CANCEL;
			mNotif.setLatestEventInfo(mContext, message.obj.getClass().getName(), ((Exception) message.obj).toString() , null);
		} else {
			mNotif.flags &= Notification.FLAG_AUTO_CANCEL;
			mNotif.setLatestEventInfo(mContext, "Error: Failed to download", "", null);
		}
		
		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(mID, mNotif);
	}
}
