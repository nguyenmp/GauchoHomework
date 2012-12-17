/* Copyright (C) 2012 Mark
 *
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, 
 * and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.nguyenmp.gauchodroid.login;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.nguyenmp.gauchodroid.EventFragment;
import com.nguyenmp.gauchodroid.PhotoFragment;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.TabsAdapter;
import com.nguyenmp.gauchodroid.courses.MyCoursesActivity;
import com.nguyenmp.gauchodroid.forum.ForumFragment;

/**
 * The activity to handle logging into GauchoSpace.
 * @author Mark Nguyen
 */
public class LoginActivity extends Activity  
			implements OnLoginListener {
	
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		super.setContentView(R.layout.view_pager);
		
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager);
		final ActionBar actionBar = super.getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		
		Bundle siteNewsArgs = new Bundle();
		siteNewsArgs.putInt(ForumFragment.ARGUMENT_KEY_FORUM_ID, 1);
		mTabsAdapter.addTab(actionBar.newTab().setText("Site News"), ForumFragment.class, siteNewsArgs);
		mTabsAdapter.addTab(actionBar.newTab().setText("Login"), LoginFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("Photo"), PhotoFragment.class, inState);
		mTabsAdapter.addTab(actionBar.newTab().setText("Event"), EventFragment.class, inState);
		
		actionBar.setSelectedNavigationItem(1);
	}
	
	public void onLogin(boolean success) {
		Intent intent = new Intent(this, MyCoursesActivity.class);
		startActivity(intent);
		finish();
	}
}