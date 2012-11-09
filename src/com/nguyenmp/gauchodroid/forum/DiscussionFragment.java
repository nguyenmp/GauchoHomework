package com.nguyenmp.gauchodroid.forum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;
import com.nguyenmp.gauchodroid.R;

public class DiscussionFragment extends SherlockListFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		return inflater.inflate(R.layout.fragment_not_ready, container, false);
	}
}
