package com.nguyenmp.gauchodroid.user;

import com.nguyenmp.gauchospace.thing.User;

public interface ProfileDownloadListener {
	public void onDownloaded(User user, String message);
}
