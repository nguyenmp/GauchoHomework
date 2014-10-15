package com.nguyenmp.gauchodroid.course;

import java.util.List;

import com.nguyenmp.gauchospace.thing.User;

public interface ParticipantsDownloadListener {
	public void onDownloaded(List<User> participants, String message);
}
