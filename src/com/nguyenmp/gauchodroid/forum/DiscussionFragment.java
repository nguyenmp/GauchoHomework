package com.nguyenmp.gauchodroid.forum;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ListFragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.nguyenmp.gauchodroid.R;

public class DiscussionFragment extends ListFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		return inflater.inflate(R.layout.fragment_not_ready, container, false);
	}
}
