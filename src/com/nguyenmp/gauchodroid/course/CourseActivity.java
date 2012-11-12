package com.nguyenmp.gauchodroid.course;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.SuperGauchoActivity;
import com.nguyenmp.gauchodroid.common.TabsAdapter;
import com.nguyenmp.gauchodroid.forum.ForumsFragment;

public class CourseActivity extends SuperGauchoActivity {
	public static final String EXTRA_COURSE_ID = "course_idslkj";
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
		
		//Add weekly outline to the list
		Bundle weeklyOutlineArgs = new Bundle();
		weeklyOutlineArgs.putInt(WeeklyOutlineFragment.ARGUMENT_COURSE_ID, getIntent().getIntExtra(EXTRA_COURSE_ID, 1));
		mTabsAdapter.addTab(actionBar.newTab().setText("Weekly Outline"), WeeklyOutlineFragment.class, weeklyOutlineArgs);
		
		//Add grades to the tabs
		Bundle gradesArgs = new Bundle();
		gradesArgs.putInt(GradesFragment.ARGUMENT_COURSE_ID, getIntent().getIntExtra(EXTRA_COURSE_ID, 0));
		mTabsAdapter.addTab(actionBar.newTab().setText("Grades"), GradesFragment.class, gradesArgs);
		
		Bundle forumsArguments = new Bundle();
		forumsArguments.putInt(ForumsFragment.ARGUMENT_FORUM_ID, getIntent().getIntExtra(EXTRA_COURSE_ID, 1));
		mTabsAdapter.addTab(actionBar.newTab().setText("Forum"), ForumsFragment.class, forumsArguments);
		
		
		mTabsAdapter.addTab(actionBar.newTab().setText("Resources"), ResourcesFragment.class, null);
	}
}