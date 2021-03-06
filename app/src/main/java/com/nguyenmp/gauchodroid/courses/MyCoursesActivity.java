package com.nguyenmp.gauchodroid.courses;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

import com.nguyenmp.gauchodroid.EventFragment;
import com.nguyenmp.gauchodroid.PhotoFragment;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.SuperGauchoActivity;
import com.nguyenmp.gauchodroid.common.TabsAdapter;
import com.nguyenmp.gauchodroid.forum.ForumFragment;

public class MyCoursesActivity extends SuperGauchoActivity {
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;

	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		super.setContentView(R.layout.view_pager);
		
		//Initialize Action bar and view pager
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mViewPager = (ViewPager) super.findViewById(R.id.view_pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		
		//Set title
		actionBar.setTitle("My Courses");
		
		//Add the site news to the list
		Bundle siteNewsForumArgs = new Bundle();
		siteNewsForumArgs.putInt(ForumFragment.ARGUMENT_KEY_FORUM_ID, 1);
		mTabsAdapter.addTab(actionBar.newTab().setText("Site News"), ForumFragment.class, siteNewsForumArgs);
		
		mTabsAdapter.addTab(actionBar.newTab().setText("My Courses"), MyCoursesFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("Photos"), PhotoFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("Events"), EventFragment.class, null);
		
		actionBar.setSelectedNavigationItem(1);
	}
}