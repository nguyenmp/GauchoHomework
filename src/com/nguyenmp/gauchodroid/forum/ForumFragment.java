package com.nguyenmp.gauchodroid.forum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.nguyenmp.gauchodroid.R;

public class ForumFragment extends SherlockFragment {
	public static final String ARGUMENT_FORUM_ID = "argument_forum id";
	
	@Override
	public void onActivityCreated(Bundle inState) {
		super.onActivityCreated(inState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		return inflater.inflate(R.layout.fragment_not_ready, container, false);
	}
}
