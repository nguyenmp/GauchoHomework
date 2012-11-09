package com.nguyenmp.gauchodroid;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.nguyenmp.gauchodroid.common.HandledThread;

public class PhotoFragment extends SherlockFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		View contentView = inflater.inflate(R.layout.fragment_photo, container, false);
		
		
		
		return contentView;
	}
	
	private static class PhotoDownloadThread extends HandledThread {
		
	}
}
