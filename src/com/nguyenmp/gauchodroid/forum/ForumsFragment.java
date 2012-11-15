package com.nguyenmp.gauchodroid.forum;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.nguyenmp.gauchospace.GauchoSpaceClient;
import com.nguyenmp.gauchospace.thing.Forum;

/**
 * A fragment that downloads and lists the Forums under the domain of 
 * a course's id including the site's forum (id = 1)
 * @author Mark
 *
 */
public class ForumsFragment extends SherlockFragment 
								implements ForumsDownloadListener, OnItemClickListener {
	/** the key to pass the 'id' argument to this fragment.
	 * This argument is meant ot identify which class to 
	 * get the forums for.  id = 1 seems to be site news. */
	public static final String ARGUMENT_FORUM_ID = "argument_id";
	
	private static final String KEY_FORUM_LIST = "forum listl_#{r";
	
	private List<Forum> mForumList = null;
	private boolean mLoaded = false;
	private BaseAdapter mListAdapter = null;
	private ListView mListView;
	private ProgressBar mProgressBar;
	private TextView mTextView;
	
	@Override
	public void onActivityCreated(Bundle inState) {
		super.onActivityCreated(inState);

		mForumList = new ArrayList<Forum>();
		mListAdapter = new ForumsListAdapter(getActivity(), mForumList);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
		if (inState != null && inState.containsKey(KEY_FORUM_LIST)) {
			setForums((List<Forum>) inState.getSerializable(KEY_FORUM_LIST));
			mLoaded = true;
		} else {
			//Disable loadedness
			mLoaded = false;
			
			//Execute download
			refresh();
		}
		
	}
	
	public void refresh() {
		//Reset our list and show progress bar
		setForums(null);
		
		//Get the id of the course for which to show the forums for
		int courseID = getArguments().getInt(ARGUMENT_FORUM_ID, 1);
		
		//Get the cookies
		CookieStore cookies = LoginManager.getCookies(getActivity());
		
		//Set up the handler
		Handler handler = new ForumsDownloadHandler(this, mTextView);
		
		//Download again
		Thread downloadThread = new ForumsDownloadThread(courseID, cookies, handler);
		downloadThread.start();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mLoaded) outState.putSerializable(KEY_FORUM_LIST, (Serializable) mForumList);
	}
	
	@Override
	public void onDownloaded(List<Forum> forums) {
		setForums(forums);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		View inflatedView = inflater.inflate(R.layout.fragment_list, container, false);
		
		mListView = (ListView) inflatedView.findViewById(R.id.fragment_list_list_view);
		mProgressBar = (ProgressBar) inflatedView.findViewById(R.id.fragment_list_progress_bar);
		mTextView = (TextView) inflatedView.findViewById(R.id.fragment_list_text_view);
		mTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
			}
		});
		
		return inflatedView;
	}
	
	/**
	 * Sets the content of this list fragment
	 * @param forums The list of forums to show.  An empty list will 
	 * hide the list view and progress bar but show the text view.  
	 * A null parameter will simply show the progress bar and hide 
	 * everything else.  A filled list will show the list view and 
	 * hide the progress bar and the text view
	 */
	private void setForums(List<Forum> forums) {
		if (forums != null) {
			//Reinitialize the list with the new data
			mForumList.clear();
			mForumList.addAll(forums);
			
			//Alert the adapter that the list has changed
			mListAdapter.notifyDataSetChanged();
		}
		
		//Show and hide all the views
		mListView.setVisibility(forums == null || forums.isEmpty() ? View.GONE : View.VISIBLE);
		mProgressBar.setVisibility(forums == null ? View.VISIBLE : View.GONE);
		mTextView.setVisibility(forums != null && forums.isEmpty()? View.VISIBLE : View.GONE);
		
		mLoaded = (forums != null);
	}
	
	private static class ForumsDownloadThread extends HandledThread {
		private final int mCourseID;
		private final CookieStore mCookieStore;
		
		ForumsDownloadThread(int courseID, CookieStore cookies, Handler handler) {
			setHandler(handler);
			mCookieStore = cookies;
			mCourseID = courseID;
		}
		
		@Override
		public void run() {
			List<Forum> forums;
			try {
				forums = GauchoSpaceClient.getForums(mCourseID, mCookieStore);
				dispatchMessage(forums);
			} catch (ClientProtocolException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (IOException e) {
				dispatchMessage(e);
				e.printStackTrace();
			}
		}
	}
	
	private static class ForumsDownloadHandler extends Handler {
		private final ForumsDownloadListener mListener;
		private final TextView mTextView;
		
		ForumsDownloadHandler(ForumsDownloadListener listener, TextView textView) {
			mListener = listener;
			mTextView = textView;
		}
		
		@Override
		public void handleMessage(Message message) {
			if (message.obj == null) {
				//Fake an empty list to show the empty list text
				mListener.onDownloaded(new ArrayList<Forum>());
				
				//But replace the empty list text with an error message
				mTextView.setText("Error: Data could not be processed.\n\nTap to refresh!");
			} else if (message.obj instanceof List<?>) {
				//Pass the received list to the listener
				mListener.onDownloaded((List<Forum>) message.obj);
				mTextView.setText("No Forums Found\n\nTap to refresh!");
			} else if (message.obj instanceof Exception) {
				//Fake an empty list to show the empty list text
				mListener.onDownloaded(new ArrayList<Forum>());
				
				//But replace the empty list text with an error message
				mTextView.setText("Error: " + message.obj.toString() + "\n\nTap to refresh");
			} else {
				//Fake an empty list to show the empty list text
				mListener.onDownloaded(new ArrayList<Forum>());
				
				//But replace the empty list text with an error message
				mTextView.setText("Error: Unknown cause\n\nTap to refresh");
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Forum forum = mForumList.get(position);
		
		Intent intent = new Intent(getActivity(), ForumActivity.class);
		intent.putExtra(ForumActivity.EXTRA_FORUM_ID, forum.getID());
		startActivity(intent);
	}
}
