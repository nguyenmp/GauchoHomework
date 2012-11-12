package com.nguyenmp.gauchodroid.forum;

import java.util.List;

import com.nguyenmp.gauchospace.thing.Forum;

public interface ForumsDownloadListener {
	public void onDownloaded(List<Forum> forums);
}
