package com.nguyenmp.gauchodroid.upload;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.SuperGauchoActivity;
import com.nguyenmp.gauchodroid.common.TabsAdapter;

public class UploadActivity extends SuperGauchoActivity {
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	
	public static final String EXTRA_SELECTED_TAB = "extra_selected_tab";
	public static final int SELECTED_TAB_PHOTO = 0;
	public static final int SELECTED_TAB_EVENT = 1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		
		//Initialize our content view with a 
		//view pager for swyping between fragments
		setContentView(R.layout.view_pager);
		
		//Initialize our action bar to support tabs
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		//Create our tabs/view pager adapter to support swyping between fragments
		mViewPager = (ViewPager) super.findViewById(R.id.view_pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		
		//Add the two upload fragments to our tab list and our tab adapter
		mTabsAdapter.addTab(actionBar.newTab().setText("Upload Photo"), UploadPhotoFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("Upload Event"), UploadEventFragment.class, inState);
		
		//Default to the given selected tab based on the extra given
		Bundle extras = getIntent().getExtras();
		Uri data = getIntent().getData();
		if (extras != null && extras.containsKey(EXTRA_SELECTED_TAB)) {
			actionBar.setSelectedNavigationItem(extras.getInt(EXTRA_SELECTED_TAB));
		} else if (data != null) {
			String target = data.getLastPathSegment();
			if (target != null) {
				if (target.equalsIgnoreCase("index.php")) 
					actionBar.setSelectedNavigationItem(SELECTED_TAB_PHOTO);
				else if (target.equalsIgnoreCase("index2.php"))
					actionBar.setSelectedNavigationItem(SELECTED_TAB_EVENT);
			}
		}
	}
}