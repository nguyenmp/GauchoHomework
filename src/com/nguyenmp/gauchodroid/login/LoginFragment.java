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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.loopj.android.http.PersistentCookieStore;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.common.ObscuredSharedPreferences;
import com.nguyenmp.gauchospace.GauchoSpaceClient;

/**
 * The activity to handle logging into GauchoSpace.
 * @author Mark Nguyen
 */
public class LoginFragment extends SherlockFragment implements OnLoginListener {
//	private Context mContext;
	private static LoginTask mLoginTask = null;
	/**
	 * @deprecated Clear text storage is no longer supported for security purposes
	 */
	private static final String KEY_USERNAME = "key_username", KEY_PASSWORD = "key_password";
	private static final String KEY_USERNAME_BASE64 = "key_username_base64", KEY_PASSWORD_BASE64 = "key_password_base64";
	
	
	private EditText mUsernameEditText = null, mPasswordEditText = null;
	private Button mLoginButton = null;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		//Inflate the login fragment view
		View view = inflater.inflate(R.layout.fragment_login, container, false);
		
		Context context = getActivity();
		
		//Fetch the username and password field
		mUsernameEditText = (EditText) view.findViewById(R.id.fragment_login_username);
		mPasswordEditText = (EditText) view.findViewById(R.id.fragment_login_password);
		
		//Initialize from preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		//Remove previous unencrypted credential stores
		//This was before the initial release.
		prefs.edit().remove(KEY_USERNAME).remove(KEY_PASSWORD).commit();
		
		//Use the encrypted credential store instead
		ObscuredSharedPreferences obscuredPrefs = new ObscuredSharedPreferences(context, prefs);
		mUsernameEditText.setText(obscuredPrefs.getString(KEY_USERNAME_BASE64, null));
		mPasswordEditText.setText(obscuredPrefs.getString(KEY_PASSWORD_BASE64, null));
		
		//Overwrite from instate if applicable
		if (inState != null) {
			if (inState.containsKey(KEY_USERNAME_BASE64)) mUsernameEditText.setText(inState.getString(KEY_USERNAME_BASE64));
			if (inState.containsKey(KEY_PASSWORD_BASE64)) mPasswordEditText.setText(inState.getString(KEY_PASSWORD_BASE64));
		}
		
		//Find the login button and bind our click listener to it
		mLoginButton = (Button) view.findViewById(R.id.fragment_login_login_button);
		mLoginButton.setOnClickListener(new LoginButtonClickListener());
		
		//If login is already in progress, set it's handler
		if (mLoginTask != null) {
//			setInProgress(true);
			mLoginTask.setHandler(new MyLoginHandler(context, this));
		} else {
//			setInProgress(false);
		}
		
		//Return our inflated view
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		//Store username and password into saved state
		outState.putString(KEY_USERNAME_BASE64, mUsernameEditText.getText().toString());
		outState.putString(KEY_PASSWORD_BASE64, mPasswordEditText.getText().toString());
	}
	
	/**
	 * Sets the editibility of the forms as well as the indeterminant progress bar
	 * @param inProgress true to disable fields, buttons, and show progress bar.  false for opposite.
	 */
	public void setInProgress(boolean inProgress) {
		//Toggle fields editabilty
		mUsernameEditText.setEnabled(!inProgress);
		mPasswordEditText.setEnabled(!inProgress);
		
		//Toggle button clickbility
		mLoginButton.setEnabled(!inProgress);
	}
	
	public static void setCookies(Context context, CookieStore cookies) {
		PersistentCookieStore store = new PersistentCookieStore(context);
		store.clear();
		
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		cookieManager.setAcceptCookie(true);
		
		if (cookies != null) {
			for (Cookie cookie : cookies.getCookies()) {
				store.addCookie(cookie);
				cookieManager.setCookie(cookie.getDomain(), cookie.getName() + "=" + cookie.getValue());
			}
		}
		
		CookieSyncManager.getInstance().sync();
	}
	
	public static CookieStore getCookies(Context context) {
		PersistentCookieStore store = new PersistentCookieStore(context);
		return store;
	}
	
	private class LoginButtonClickListener implements View.OnClickListener {
		public void onClick(View v) {
			if (mLoginTask == null) {
				String username = mUsernameEditText.getText().toString();
				String password = mPasswordEditText.getText().toString();
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
				ObscuredSharedPreferences obscuredPrefs = new ObscuredSharedPreferences(v.getContext(), prefs);
				obscuredPrefs.edit().putString(KEY_USERNAME_BASE64, username)
							.putString(KEY_PASSWORD_BASE64, password)
							.commit();
				
				MyLoginHandler handler = new MyLoginHandler(v.getContext(), LoginFragment.this);
				mLoginTask = new LoginTask(username, password, handler);
				mLoginTask.start();
				setInProgress(true);
			} else {
				Toast.makeText(v.getContext(), "Login already in progress.", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private static class MyLoginHandler extends Handler {
		private final Context mContext;
		private final OnLoginListener mLoginListener;
		
		private MyLoginHandler(Context context, OnLoginListener listener) {
			mContext = context;
			mLoginListener = listener;
		}
		
		@Override
		public void handleMessage(Message message) {
//			mLoginActivity.setInProgress(false);
			Object payload = message.obj;
			if (payload == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Login Failed");
				builder.setMessage("Invalid Username or Password, most likely");
				builder.show();
				
				if (mLoginListener != null) mLoginListener.onLogin(false);
			} else if (payload instanceof CookieStore) {
				setCookies(mContext, ((CookieStore) payload));

				if (mLoginListener != null) mLoginListener.onLogin(true);
				if (mContext instanceof OnLoginListener) {
					((OnLoginListener) mContext).onLogin(true);
				}
			} else {
				String errorMessage = "Unexpected error";
				if (payload instanceof Exception) 
					errorMessage = ((Exception) payload).toString();

				if (mLoginListener != null) mLoginListener.onLogin(false);
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Error!");
				builder.setMessage(errorMessage);
				builder.show();
			}
		}
	}
	
	private static class LoginTask extends HandledThread {
		private final String mUsername;
		private final String mPassword;
		
		LoginTask(String username, String password, Handler handler) {
			mUsername = username;
			mPassword = password;
			setHandler(handler);
		}
		
		@Override
		public void run() {
			try {
				CookieStore cookie = GauchoSpaceClient.login(mUsername, mPassword);
				dispatchMessage(cookie);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (IOException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} finally {
				mLoginTask = null;
			}
			
		}
	}

	public void onLogin(boolean success) {
		setInProgress(false);
	}
}