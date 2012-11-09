package com.nguyenmp.gauchodroid.course;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.SuperGauchoActivity;
import com.nguyenmp.gauchodroid.common.TabsAdapter;
import com.nguyenmp.gauchodroid.forum.ForumFragment;

public class CourseActivity extends SuperGauchoActivity {
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	
	
	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		super.setContentView(R.layout.view_pager);
		
		mViewPager = (ViewPager) super.findViewById(R.id.view_pager);
		
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mTabsAdapter = new TabsAdapter(this, mViewPager);

		mTabsAdapter.addTab(actionBar.newTab().setText("Weekly Outline"), WeeklyOutlineFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("Grades"), GradesFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("Forum"), ForumFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("Resources"), ResourcesFragment.class, inState);
	}
}