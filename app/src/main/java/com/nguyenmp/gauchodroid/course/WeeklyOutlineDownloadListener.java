package com.nguyenmp.gauchodroid.course;

import java.util.List;

import com.nguyenmp.gauchospace.thing.Week;

public interface WeeklyOutlineDownloadListener {
	public void onDownloaded(List<Week> weeklyOutline, String message);
}
