package com.nguyenmp.gauchodroid.user;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.TabsAdapter;

public class UserActivity extends ActionBarActivity {
	public static final String EXTRA_USER_ID = "extra_user_id";
	public static final String EXTRA_COURSE_ID = "extra_course_id";
	public static final String EXTRA_TAB_SELECTED = "extra_tab_selected";
	public static final String VALUE_TAB_SELECTED_PROFILE = "value_tab_selected_profile";
	public static final String VALUE_TAB_SELECTED_POSTS = "value_tab_selected_posts";
	public static final String VALUE_TAB_SELECTED_DISCUSSIONS = "value_tab_selected_discussions";
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	
	@Override
	public void onCreate(Bundle inState) {

		super.onCreate(inState);
		super.setContentView(R.layout.view_pager);
		
		//Fetch the view pager
		mViewPager = (ViewPager) super.findViewById(R.id.view_pager);
		
		//fetch the actionbar
		final ActionBar actionBar = getSupportActionBar();
		
		//Initialize action bar and view pager for tabs
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		
		int userID = 2;
		int courseID = 1;
		if (getIntent().getData() != null) {
			System.out.println("using data");
			Uri uri = getIntent().getData();
			userID = Integer.parseInt(uri.getQueryParameter("id"));
			courseID = Integer.parseInt(uri.getQueryParameter("course"));
		} else if (getIntent().hasExtra(EXTRA_USER_ID) && getIntent().hasExtra(EXTRA_COURSE_ID)) {
			System.out.println("using extras");
			userID = getIntent().getIntExtra(EXTRA_USER_ID, userID);
			courseID = getIntent().getIntExtra(EXTRA_COURSE_ID, courseID);
		}
		
		//Add weekly outline to the list
		Bundle arguments = new Bundle();
		arguments.putInt(ProfileFragment.ARGUMENT_USER_ID, userID);
		arguments.putInt(ProfileFragment.ARGUMENT_COURSE_ID, courseID);
		mTabsAdapter.addTab(actionBar.newTab().setText("Profile"), ProfileFragment.class, arguments);
		
		arguments = new Bundle();
		
		mTabsAdapter.addTab(actionBar.newTab().setText("Posts"), PostsFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("Discussions"), DiscussionsFragment.class, null);
		
		int selectedTab = 0;
		if (getIntent().getData() != null) {
			Uri data = getIntent().getData();
			if (data.toString().contains("mod/forum/")) {
				if (data.toString().contains("mode=discussions")) {
					selectedTab = 2;
				} else {
					selectedTab = 1;
				}
			} else {
				selectedTab = 0;
			}
		}
		
		actionBar.setSelectedNavigationItem(selectedTab);
	}
}
