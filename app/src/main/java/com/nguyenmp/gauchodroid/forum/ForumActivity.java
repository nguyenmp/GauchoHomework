package com.nguyenmp.gauchodroid.forum;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.TabsAdapter;

public class ForumActivity extends ActionBarActivity {
	public static final String EXTRA_FORUM_ID = "extra+forum_id";
	
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	
	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		super.setContentView(R.layout.view_pager);
		
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		final ActionBar actionBar = super.getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		Bundle args = new Bundle();
		args.putInt(ForumFragment.ARGUMENT_KEY_FORUM_ID, getIntent().getIntExtra(EXTRA_FORUM_ID, 1));
		mTabsAdapter.addTab(actionBar.newTab().setText("Forum"), ForumFragment.class, args);
	}
}
