package com.nguyenmp.gauchodroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.login.LoginManager;
import com.nguyenmp.gauchospace.GauchoSpaceClient;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;

import java.io.IOException;

public class SuperGauchoActivity extends ActionBarActivity {
	
	private boolean mInProgress = false;
	
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
//		super.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.getSupportActionBar().setHomeButtonEnabled(true);
		super.getSupportActionBar().setDisplayShowHomeEnabled(true);
		super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public void onRestoreInstanceState(Bundle inState) {
		super.onRestoreInstanceState(inState);
		
		mInProgress = inState.getBoolean("in_progress_key");
		
		super.setSupportProgressBarIndeterminateVisibility(mInProgress);
	}
	
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("in_progress_key", mInProgress);
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add("Settings");
		menu.add("Log Out");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Log Out")) {
			logout();
			return true;
		}
		
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	
	public void logout() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle("Logging out");
		dialog.setMessage("Please wait...");
		
		LogoutThread thread = new LogoutThread(this);
		thread.setHandler(new LogoutHandler(this, dialog));
		thread.start();
		
		dialog.show();
	}
	
	protected void setInProgress(boolean inProgress) {
		mInProgress = inProgress;
		
		super.setSupportProgressBarIndeterminateVisibility(mInProgress);
	}
	
	private static class LogoutHandler extends Handler {
		private final ProgressDialog mDialog;
		private final Context mContext;
		
		LogoutHandler(Context context, ProgressDialog dialog) {
			mContext = context;
			mDialog = dialog;
		}
		
		public void handleMessage(Message msg) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mDialog.getContext());
			if (msg.obj instanceof Boolean && ((Boolean) msg.obj) == Boolean.TRUE) {
				builder.setTitle("Logout Successful!");
				builder.setMessage("You are now logged out.");
			} else if (msg.obj instanceof Exception) {
				builder.setTitle("Logout Error");
				builder.setMessage(((Exception) msg.obj).toString());
			} else {
				builder.setTitle("Logout Error");
				builder.setMessage("Unknown cause.");
			}
			mDialog.dismiss();
			builder.show();
		}
	}
	
	private static class LogoutThread extends HandledThread {
		private final Context mContext;
		
		private LogoutThread(Context context) {
			mContext = context;
		}
		
		public void run() {
			Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_BACKGROUND);
			CookieStore cookies = LoginManager.getCookies(mContext);
			
			try {
				Boolean logoutSuccess = false;
				
				if (cookies != null) logoutSuccess = GauchoSpaceClient.logout(cookies);
				dispatchMessage(logoutSuccess);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (IOException e) {
				e.printStackTrace();
				dispatchMessage(e);
			}
		}
	}
}
