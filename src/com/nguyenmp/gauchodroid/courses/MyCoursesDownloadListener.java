package com.nguyenmp.gauchodroid.courses;

import java.util.List;

import com.nguyenmp.gauchospace.thing.Course;

public interface MyCoursesDownloadListener {
	public void onDownloaded(List<Course> courses, String message);
}
