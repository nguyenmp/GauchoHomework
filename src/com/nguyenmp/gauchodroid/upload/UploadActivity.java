package com.nguyenmp.gauchodroid.upload;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.SuperGauchoActivity;
import com.nguyenmp.gauchodroid.common.TabsAdapter;

public class UploadActivity extends SuperGauchoActivity {
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		
		setContentView(R.layout.view_pager);
		
		mViewPager = (ViewPager) super.findViewById(R.id.view_pager);
		
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mTabsAdapter = new TabsAdapter(this, mViewPager);

		mTabsAdapter.addTab(actionBar.newTab().setText("Upload Photo"), UploadPhotoFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("Upload Event"), UploadEventFragment.class, inState);
	}
}