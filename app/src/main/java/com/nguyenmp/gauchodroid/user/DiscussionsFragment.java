package com.nguyenmp.gauchodroid.user;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nguyenmp.gauchodroid.R;

public class DiscussionsFragment extends Fragment {
	public static final String ARGUMENT_USER_ID = "argument_user_id";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		return inflater.inflate(R.layout.fragment_not_ready, container, false);
	}
}
