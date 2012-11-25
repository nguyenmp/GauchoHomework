package com.nguyenmp.gauchodroid.courses;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.common.MenuUtils;
import com.nguyenmp.gauchodroid.course.CourseActivity;
import com.nguyenmp.gauchodroid.login.LoginActivity;
import com.nguyenmp.gauchodroid.login.LoginManager;
import com.nguyenmp.gauchodroid.user.UserActivity;
import com.nguyenmp.gauchospace.GauchoSpaceClient;
import com.nguyenmp.gauchospace.thing.Course;
import com.nguyenmp.gauchospace.thing.Instructor;

public class MyCoursesFragment extends SherlockFragment implements MyCoursesDownloadListener, OnItemClickListener, OnItemLongClickListener {
	private BaseAdapter mListAdapter;
	private List<Course> mCourseList;
	private String mText;
	private boolean mLoaded = false;
	private static final String COURSE_LIST_KEY = "KOURSE LCISKD KEY";
	private Context mContext;
	private ListView mListView;
	private ProgressBar mProgressBar;
	private TextView mTextView;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mLoaded) outState.putSerializable(COURSE_LIST_KEY, (Serializable) mCourseList);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		//Inflate our custom list view layout
		View inflatedView = inflater.inflate(R.layout.fragment_list, container, false);
		
		//Must be called in order to show our options menu in the action bar
		super.setHasOptionsMenu(true);
		
		//Get member views
		mListView = (ListView) inflatedView.findViewById(R.id.fragment_list_list_view);
		mProgressBar = (ProgressBar) inflatedView.findViewById(R.id.fragment_list_progress_bar);
		mTextView = (TextView) inflatedView.findViewById(R.id.fragment_list_text_view);
		
		mContext = getActivity();
		mCourseList = new ArrayList<Course>();
		mListAdapter = new CourseListAdapter(mCourseList, mContext);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		super.registerForContextMenu(mListView);
		
		if (inState != null && inState.containsKey(COURSE_LIST_KEY)) {
			List<Course> courses = (List<Course>) inState.getSerializable(COURSE_LIST_KEY);
			onDownloaded(courses, "Error");  //If we somehow stored a null list, then show error
		} else {
			refresh();
		}
		
		return inflatedView;
	}
	
	private void refresh() {
		onDownloaded(null, null); //Show progress
		CookieStore cookies = LoginManager.getCookies(mContext);
		
		Handler coursesDownloadHandler = new CoursesDownloadHandler(this, mContext);
		Thread coursesDownloadThread = new CoursesDownloadThread(coursesDownloadHandler, cookies);
		coursesDownloadThread.start();
	}
	
	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
		Course course = mCourseList.get(position);
		Uri uri = Uri.parse(course.getUrl());
		Intent intent = new Intent(mContext, CourseActivity.class);
		intent.putExtra(CourseActivity.EXTRA_COURSE_ID, Integer.parseInt(uri.getQueryParameter("id")));
		intent.putExtra(CourseActivity.EXTRA_TITLE, course.getName() + ": " + course.getTitle());
		mContext.startActivity(intent);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final Course course = mCourseList.get(position);
		final String name = course.getName();
		final String title = course.getTitle();
		final String summary = course.getSummary();
		final Instructor[] instructors = course.getInstructors().toArray(new Instructor[] {});
		
		Dialog dialog = new Dialog(mContext);
		dialog.setTitle(name + ": " + title);
		dialog.setContentView(R.layout.dialog_course_description);
		
		TextView summaryView = (TextView) dialog.findViewById(R.id.dialog_course_description_summary);
		summaryView.setText(summary);
		
		LinearLayout instructorGroup = (LinearLayout) dialog.findViewById(R.id.dialog_course_description_instructors);
		for (final Instructor instructor : instructors) {
			final Button button = new Button(mContext);
			button.setText(instructor.getName());
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), UserActivity.class);
					intent.setData(Uri.parse(instructor.getUrl()));
					startActivity(intent);
				}
			});
			instructorGroup.addView(button);
		}
		
		dialog.show();
		
		
		
		return true;
	}

	@Override
	public void onDownloaded(List<Course> courses, String message) {
		mLoaded = (courses != null);
		mCourseList.clear();
		
		if (courses == null) {
			if (message == null) {
				mText = null;
				mProgressBar.setVisibility(View.VISIBLE);
				mTextView.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
			} else {
				mText = message;
				mTextView.setText(mText);
				mTextView.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
			}
		} else {
			if (courses.isEmpty()) {
				mText = "You Are Not Enrolled In Any Courses";
				mTextView.setText(mText);
				mTextView.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
			} else {
				mText = null;
				mTextView.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.GONE);
				mCourseList.addAll(courses);
				mListView.setVisibility(View.VISIBLE);
			}
		}
		mListAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		MenuUtils.addMenuItem(menu, "Refresh").setIcon(R.drawable.ic_menu_refresh).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Refresh")) {
			refresh();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private static class CoursesDownloadThread extends HandledThread {
		private CookieStore mCookies;
		
		CoursesDownloadThread(Handler handler, CookieStore cookies) {
			setHandler(handler);
			mCookies = cookies;
		}
		
		public void run() {
			Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_BACKGROUND);
			try {
				List<Course> courses = GauchoSpaceClient.getCourses(mCookies);
				CoursesPayload payload = new CoursesPayload(courses);
				dispatchMessage(payload);
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
			} catch (NullPointerException e) {
				e.printStackTrace();
				dispatchMessage(e);
			}
		}
	}
	
	private static class CoursesDownloadHandler extends Handler {
		private final MyCoursesDownloadListener mListener;
		private final Context mContext;
		
		CoursesDownloadHandler(MyCoursesDownloadListener listener, Context context) {
			mListener = listener;
			mContext = context;
		}
		
		@Override
		public void handleMessage(Message message) {
			if (message.obj instanceof CoursesPayload) {
				List<Course> courses = ((CoursesPayload) message.obj).getPayload();
				mListener.onDownloaded(courses, null);
			} else if (message.obj instanceof NullPointerException) {
				LoginManager.setCookies(mContext, null);
				Intent intent = new Intent(mContext, LoginActivity.class);
				mContext.startActivity(intent);
			} else if (message.obj instanceof Exception) {
				mListener.onDownloaded(null, ((Exception) message.obj).toString());
			} else {
				mListener.onDownloaded(null, "Unknown error has occured.");
			}
		}
	}
	
	private static class CoursesPayload {
		private List<Course> courses = null;
		CoursesPayload(List<Course> courses) {
			this.courses = courses;
		}
		List<Course> getPayload() {
			return courses;
		}
	}
	
	private static class CourseListAdapter extends BaseAdapter {
		private final List<Course> mCourseList;
		private final Context mContext;
		
		public CourseListAdapter(List<Course> courses, Context context) {
			if (courses == null) throw new NullPointerException("Data list cannot be null.");
			mCourseList = courses;
			mContext = context;
		}
		
		public int getCount() {
			return mCourseList.size();
		}
		
		public Course getItem(int position) {
			return mCourseList.get(position);
		}
		
		public long getItemId(int position) {
			return mCourseList.get(position).hashCode();
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			Course course = mCourseList.get(position);
			
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_course, parent, false);
			}
			
			TextView nameTextView = (TextView) convertView.findViewById(R.id.list_item_course_name);
			nameTextView.setText(course.getName());
			
			TextView titleTextView = (TextView) convertView.findViewById(R.id.list_item_course_title);
			titleTextView.setText(course.getTitle());
			
			TextView quarterTextView = (TextView) convertView.findViewById(R.id.list_item_course_quarter);
			quarterTextView.setText(course.getQuarter());
			
			return convertView;
		}
	}
}