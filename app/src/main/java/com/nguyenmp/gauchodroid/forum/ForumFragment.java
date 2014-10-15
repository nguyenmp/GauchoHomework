package com.nguyenmp.gauchodroid.forum;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.login.LoginManager;
import com.nguyenmp.gauchospace.GauchoSpaceClient;
import com.nguyenmp.gauchospace.thing.Discussion;


public class ForumFragment extends Fragment implements ForumDownloadListener {
	//These are arguments/keys used to store into bundles
	public static final String ARGUMENT_KEY_FORUM_ID = "argument_forum id";
	private static final String SAVED_STATE_KEY_DISCUSSIONS = "in_state_discussions";
	private static final String SAVED_STATE_KEY_TEXT_VIEW_MESSAGE = "in_state_text_view_message";
	
	//These are the views for this fragment
	private ListView mListView;
	private ProgressBar mProgressBar;
	private TextView mTextView;
	
	//This is the list adapter for the list view
	private BaseAdapter mListAdapter;
	
	//This context is allocated and initialized at the
	//createView method so when this fragment detaches, 
	//we don't lose our context
	private Context mContext;
	
	//The list and the boolean is used for persisting 
	//our list's state across multiple instances
	private List<Discussion> mDiscussions = null;
	private String mText = null;
	private boolean mLoaded = false;
	
	@Override
	public void onActivityCreated(Bundle inState) {
		super.onActivityCreated(inState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		//Inflate the new view
		View inflatedView = inflater.inflate(R.layout.fragment_list, container, false);
		
		//Get our inflated subviews
		mListView = (ListView) inflatedView.findViewById(R.id.fragment_list_list_view);
		mProgressBar = (ProgressBar) inflatedView.findViewById(R.id.fragment_list_progress_bar);
		mTextView = (TextView) inflatedView.findViewById(R.id.fragment_list_text_view);
		
		//Initialize our context
		mContext = getActivity();
		
		//Initialize our data list
		mDiscussions = new ArrayList<Discussion>();
		
		mListView.setAdapter((mListAdapter = new ForumListAdapter(mDiscussions, mContext)));
		super.registerForContextMenu(mListView);
		
		if (inState != null && inState.containsKey(SAVED_STATE_KEY_DISCUSSIONS)) {
			//Load from previous state
			List<Discussion> dataList = (List<Discussion>) inState.getSerializable(SAVED_STATE_KEY_DISCUSSIONS);
			String text = inState.getString(SAVED_STATE_KEY_TEXT_VIEW_MESSAGE);
			onDownloaded(dataList, text);
			
		} else {
			
			//Show progress bar
			onDownloaded(null, null);
			
			//Start the download
			int forumID = getArguments().getInt(ARGUMENT_KEY_FORUM_ID);
			CookieStore cookies = LoginManager.getCookies(mContext);
			Thread downloadThread = new ForumDownloadThread(forumID, cookies, new ForumDownloadHandler(this));
			downloadThread.start();
		}
		
		return inflatedView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(SAVED_STATE_KEY_DISCUSSIONS, (Serializable) mDiscussions);
		outState.putString(SAVED_STATE_KEY_TEXT_VIEW_MESSAGE, mText);
	}
	
	private static class ForumDownloadThread extends HandledThread {
		private final int mForumID;
		private final CookieStore mCookies;
		
		ForumDownloadThread(int forumID, CookieStore cookies, ForumDownloadHandler handler) {
			super.setHandler(handler);
			mForumID = forumID;
			mCookies = cookies;
		}
		
		@Override
		public void run() {
			Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_BACKGROUND);
			try {
				List<Discussion> discussions = GauchoSpaceClient.getForum(mForumID, mCookies);
				dispatchMessage(discussions);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				dispatchMessage(e);
			} catch (IOException e) {
				e.printStackTrace();
				dispatchMessage(e);
			}
		}
	}
	
	private static class ForumDownloadHandler extends Handler {
		private final ForumDownloadListener mListener;
		
		ForumDownloadHandler(ForumDownloadListener listener) {
			mListener = listener;
		}
		
		public void handleMessage(Message message) {
			if (message.obj instanceof List<?>) {
				List<Discussion> discussions = (List<Discussion>) message.obj;
				mListener.onDownloaded(discussions, null);
			} else if (message.obj instanceof Exception) {
				mListener.onDownloaded(null, ((Exception) message.obj).toString());
			} else {
				mListener.onDownloaded(null, "Unknown error");
			}
		}
	}

	@Override
	public void onDownloaded(List<Discussion> discussions, String error) {
		//If we are given a list, then we have downloaded the data
		mLoaded = (discussions != null);
		
		if (discussions == null) {
			//We must use the given error message
			mText = error;
			
			if (mText == null) {
				//Show progress bar
				mProgressBar.setVisibility(View.VISIBLE);
				
				//Hide everything else
				mTextView.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
			} else {
				//Hide inapplicable views
				mProgressBar.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
				
				//Show error message
				mTextView.setText(mText);
				mTextView.setVisibility(View.VISIBLE);
			}
		} else {
			if (discussions.isEmpty()) {
				//Hide inapplicable views
				mProgressBar.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
				
				//Initialize and show the text
				mText = "No Discussions To Show";
				mTextView.setText(mText);
				mTextView.setVisibility(View.VISIBLE);
			} else {	//We have a list to show
				//Hide the text and the progress bar
				mTextView.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.GONE);
				
				//Update the data list with new content and alert the adapter
				mDiscussions.clear();
				mDiscussions.addAll(discussions);
				mListAdapter.notifyDataSetChanged();
				
				//Show the updated list
				mListView.setVisibility(View.VISIBLE);
			}
		}
	}
}