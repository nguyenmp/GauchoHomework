package com.nguyenmp.gauchodroid.forum;

import java.util.List;

import com.nguyenmp.gauchospace.thing.Discussion;

public interface ForumDownloadListener {
	public void onDownloaded(List<Discussion> discussions, String error);
}
