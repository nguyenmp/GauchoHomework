package com.nguyenmp.gauchodroid.course;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.login.LoginManager;
import com.nguyenmp.gauchodroid.user.UserActivity;
import com.nguyenmp.gauchospace.GauchoSpaceClient;
import com.nguyenmp.gauchospace.parser.WeeklyOutlineParser.UnparsableHtmlException;
import com.nguyenmp.gauchospace.thing.User;

public class ParticipantsFragment extends SherlockFragment implements ParticipantsDownloadListener, AvatarDownloadListener, OnItemClickListener {
	public static final String ARGUMENT_COURSE_ID = "course_id";
	private static final String SAVED_STATE_KEY_PARTICIPANTS = "lwkjefiuo32u490-2934b q08u4";
	
	private ListView mListView;
	private ProgressBar mProgressBar;
	private TextView mTextView;
	
	private List<User> mParticipants;
	private BaseAdapter mListAdapter;
	
	private boolean mLoaded;
	
	private Context mContext;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		View inflatedView = inflater.inflate(R.layout.fragment_list, container, false);
		
		mContext = getActivity();

		mListView = (ListView) inflatedView.findViewById(R.id.fragment_list_list_view);
		mProgressBar = (ProgressBar) inflatedView.findViewById(R.id.fragment_list_progress_bar);
		mTextView = (TextView) inflatedView.findViewById(R.id.fragment_list_text_view);
		
		mParticipants = new ArrayList<User>();
		mListAdapter = new ParticipantsListAdapter(mParticipants, mContext, this);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
		
		if (inState != null && inState.containsKey(SAVED_STATE_KEY_PARTICIPANTS)) {
			onDownloaded((List<User>) inState.getSerializable(SAVED_STATE_KEY_PARTICIPANTS), "Unknown error");
		} else {
			onDownloaded(null, null);
			
			int courseID = getArguments().getInt(ARGUMENT_COURSE_ID);
			CookieStore cookies = LoginManager.getCookies(mContext);
			Handler handler = new ParticipantsDownloadHandler(this);
			Thread thread = new ParticipantsDownloadThread(courseID, cookies, handler);
			thread.start();
		}
		
		return inflatedView;
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		User participant = mParticipants.get(position);
		
		Intent intent = new Intent(mContext, UserActivity.class);
		intent.setData(Uri.parse(participant.getUrl()));
		startActivity(intent);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mLoaded) outState.putSerializable(SAVED_STATE_KEY_PARTICIPANTS, (Serializable) mParticipants);
	}
	
	@Override
	public void onDownloaded(List<User> participants, String message) {
		mLoaded = (participants != null);
		
		if (participants == null) {
			if (message == null) {
				mProgressBar.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
				mTextView.setVisibility(View.GONE);
			} else {
				mTextView.setText(message);
				mTextView.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
			}
		} else {
			if (participants.isEmpty()) {
				mTextView.setText("No Participants Could Be Found");
				mTextView.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
			} else {
				Collections.sort(participants);
				
				mParticipants.clear();
				mParticipants.addAll(participants);
				mListAdapter.notifyDataSetChanged();
				
				mListView.setVisibility(View.VISIBLE);
				mTextView.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onDownloaded(Bitmap bitmap) {
		mListAdapter.notifyDataSetChanged();
	}

	private static class ParticipantsDownloadThread extends HandledThread {
		private final int mCourseID;
		private final CookieStore mCookies;
		
		ParticipantsDownloadThread(int courseID, CookieStore cookies, Handler handler) {
			mCourseID = courseID;
			mCookies = cookies;
			setHandler(handler);
		}
		
		@Override
		public void run() {
			Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_BACKGROUND);
			try {
				List<User> participants = GauchoSpaceClient.getParticipantsFromCourse(mCourseID, mCookies);
				dispatchMessage(participants);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (SAXNotRecognizedException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (SAXNotSupportedException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (IOException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (TransformerFactoryConfigurationError e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (TransformerException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (UnparsableHtmlException e) {
				e.printStackTrace();
				dispatchMessage(e);
			}
		}
	}
	
	private static class ParticipantsDownloadHandler extends Handler {
		private final ParticipantsDownloadListener mListener;
		
		ParticipantsDownloadHandler(ParticipantsDownloadListener listener) {
			mListener = listener;
		}
		
		@Override
		public void handleMessage(Message message) {
			if (message.obj instanceof List<?>) {
				List<User> participants = (List<User>) message.obj;
				mListener.onDownloaded(participants, "Unknown error");
			} else if (message.obj instanceof Exception) {
				mListener.onDownloaded(null, ((Exception) message.obj).toString());
			} else {
				mListener.onDownloaded(null, "Unknown error");
			}
		}
	}
}
