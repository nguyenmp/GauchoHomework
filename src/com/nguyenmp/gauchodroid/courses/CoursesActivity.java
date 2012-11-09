package com.nguyenmp.gauchodroid.courses;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.nguyenmp.gauchodroid.EventFragment;
import com.nguyenmp.gauchodroid.PhotoFragment;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.SuperGauchoActivity;
import com.nguyenmp.gauchodroid.common.TabsAdapter;
import com.nguyenmp.gauchodroid.forum.ForumFragment;

public class CoursesActivity extends SuperGauchoActivity {
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;

	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		
		setContentView(R.layout.view_pager);
		
		mViewPager = (ViewPager) super.findViewById(R.id.view_pager);
		
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mTabsAdapter = new TabsAdapter(this, mViewPager);

		mTabsAdapter.addTab(actionBar.newTab().setText("Site News"), ForumFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("My Courses"), MyCoursesFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("Photos"), PhotoFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("Events"), EventFragment.class, inState);
		
		actionBar.setSelectedNavigationItem(1);
	}
}