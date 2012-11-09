package com.nguyenmp.gauchodroid;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nguyenmp.gauchodroid.common.MenuUtils;
import com.nguyenmp.gauchodroid.upload.UploadActivity;

public class EventFragment extends SherlockFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		//Must call to have our options menu placed 
		//in the action bar's options menu
		super.setHasOptionsMenu(true);
		
		//Return the generic inflated layout for our temp fragment
		return inflater.inflate(R.layout.fragment_not_ready, container, false);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		//Try to add refresh and upload to the options menu
		MenuUtils.addMenuItem(menu, "Refresh");
		MenuUtils.addMenuItem(menu, "Upload Event");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Refresh")) {
			//TODO: Implement refreshing in the event fragment
			return true;
		} else if (item.getTitle().equals("Upload Event")) {
			//if hte user chose to upload an event 
			//open the Upload activity and select the event tab
			Intent intent = new Intent(getActivity(), UploadActivity.class);
			intent.putExtra(UploadActivity.EXTRA_SELECTED_TAB, UploadActivity.SELECTED_TAB_EVENT);
			startActivity(intent);
		}
		
		return super.onOptionsItemSelected(item);
	}
}
