package com.nguyenmp.gauchodroid.user;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.course.AvatarDownloadListener;
import com.nguyenmp.gauchodroid.login.LoginManager;
import com.nguyenmp.gauchospace.GauchoSpaceClient;
import com.nguyenmp.gauchospace.thing.User;
import com.nguyenmp.gauchospace.thing.User.Attribute;

public class ProfileFragment extends SherlockFragment implements ProfileDownloadListener, AvatarDownloadListener {
	public static final String ARGUMENT_USER_ID = "argument_user_id";
	public static final String ARGUMENT_COURSE_ID = "argument_course_id";
	private static final String SAVED_STATE_KEY_USER = "saved_state_key_user";
	private ProgressBar mProgressBar;
	private View mContentView;
	private TextView mTextView;
	private Context mContext;
	private User mUser;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		View inflatedView = inflater.inflate(R.layout.fragment_user, container, false);
		
		mContext = getActivity();
		mProgressBar = (ProgressBar) inflatedView.findViewById(R.id.fragment_user_progress_bar);
		mContentView = inflatedView.findViewById(R.id.fragment_user_content);
		mTextView = (TextView) inflatedView.findViewById(R.id.fragment_user_header);
		
		if (inState != null && inState.containsKey(SAVED_STATE_KEY_USER)) {
			onDownloaded((User) inState.getSerializable(SAVED_STATE_KEY_USER), "Unknown Error");
		} else {
			refresh();
		}
		
		return inflatedView;
	}
	
	private void refresh() {
		onDownloaded(null, null);

		int userID = getArguments().getInt(ARGUMENT_USER_ID);
		int courseID = getArguments().getInt(ARGUMENT_COURSE_ID);
		CookieStore cookieStore = LoginManager.getCookies(mContext);
		Handler handler = new ProfileDownloadHandler(this);
		Thread thread = new ProfileDownloadThread(userID, courseID, cookieStore, handler);
		thread.start();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mUser != null) outState.putSerializable(SAVED_STATE_KEY_USER, mUser);
	}
	
	@Override
	public void onDownloaded(User user, String message) {
		mUser = user;
		if (user == null) {
			if (message == null) {
				mProgressBar.setVisibility(View.VISIBLE);
				mContentView.setVisibility(View.GONE);
				mTextView.setVisibility(View.GONE);
			} else {
				mProgressBar.setVisibility(View.GONE);
				mContentView.setVisibility(View.GONE);
				mTextView.setText(message);
				mTextView.setVisibility(View.VISIBLE);
			}
		} else {
			SherlockFragmentActivity activity = getSherlockActivity();
			if (activity != null) activity.getSupportActionBar().setTitle(user.getName());
			
			Handler handler = new AvatarDownloadHandler(this);
			Thread thread = new AvatarDownloadThread(user.getAvatarUrl(), handler);
			thread.start();
			
			TextView profileTextView = (TextView) mContentView.findViewById(R.id.fragment_user_profile_text);
			profileTextView.setText(Html.fromHtml(user.getSummary()));
			Linkify.addLinks(profileTextView, Linkify.ALL);
			
			LinearLayout attributes = (LinearLayout) mContentView.findViewById(R.id.fragment_user_attributes);
			for (Attribute attribute : user.getAttributes()) {
				TextView attributeView = new TextView(mContext);
				attributeView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				attributeView.setText(attribute.getKey() + ": " + attribute.getValue());
				Linkify.addLinks(attributeView, Linkify.ALL);
				attributes.addView(attributeView);
			}
			
			mProgressBar.setVisibility(View.GONE);
			mTextView.setVisibility(View.GONE);
			mContentView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDownloaded(Bitmap bitmap) {
		if (bitmap != null) {
			ImageView imageView = (ImageView) mContentView.findViewById(R.id.fragment_user_avatar);
			imageView.setImageBitmap(bitmap);
		}
	}

	private static class ProfileDownloadThread extends HandledThread {
		private final String mUrl;
		private final CookieStore mCookies;
		
		ProfileDownloadThread(int userID, int courseID, CookieStore cookies, Handler handler) {
			mUrl = "https://gauchospace.ucsb.edu/courses/user/view.php?id=" + userID + "&course=" + courseID;
			mCookies = cookies;
			setHandler(handler);
		}
		@Override
		public void run() {
			Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_BACKGROUND);
			try {
				User user = GauchoSpaceClient.getUserProfile(mUrl, mCookies);
				dispatchMessage(user);
			} catch (ClientProtocolException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (SAXNotRecognizedException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (SAXNotSupportedException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (IOException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (TransformerException e) {
				dispatchMessage(e);
				e.printStackTrace();
			}
		}
	}
	
	private static class ProfileDownloadHandler extends Handler {
		private final ProfileDownloadListener mListener;
		
		ProfileDownloadHandler(ProfileDownloadListener listener) {
			mListener = listener;
		}
		
		@Override
		public void handleMessage(Message message) {
			if (message.obj instanceof User) {
				mListener.onDownloaded(((User) message.obj), "Unknown error");
			} else if (message.obj instanceof Exception) {
				mListener.onDownloaded(null, ((Exception) message.obj).toString());
			} else {
				mListener.onDownloaded(null, "Unknown error");
			}
		}
	}
	
	private static class AvatarDownloadThread extends HandledThread {
		private final String url;
		AvatarDownloadThread(String url, Handler handler) {
			setHandler(handler);
			this.url = url;
		}
		
		@Override
		public void run() {
			Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_BACKGROUND);
			try {
				URLConnection conn = new URL(url).openConnection();
				Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
				dispatchMessage(bitmap);
			} catch (IOException e) {
				dispatchMessage(e);
				e.printStackTrace();
			}
		}
	}
	
	private static class AvatarDownloadHandler extends Handler {
		private final AvatarDownloadListener mListener;
		
		AvatarDownloadHandler(AvatarDownloadListener listener) {
			mListener = listener;
		}
		
		@Override
		public void handleMessage(Message message) {
			if (message.obj instanceof Bitmap) {
				mListener.onDownloaded((Bitmap) message.obj);
			} else if (message.obj instanceof Exception) {
				//Do nothing
			}
		}
	}
}
