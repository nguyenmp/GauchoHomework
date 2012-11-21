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
import android.os.Vibrator;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.browser.BrowserActivity;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.course.CourseActivity;
import com.nguyenmp.gauchodroid.login.LoginActivity;
import com.nguyenmp.gauchodroid.login.LoginManager;
import com.nguyenmp.gauchospace.GauchoSpaceClient;
import com.nguyenmp.gauchospace.thing.Course;
import com.nguyenmp.gauchospace.thing.Instructor;

public class MyCoursesFragment extends SherlockListFragment {
	private BaseAdapter mListAdapter;
	private List<Course> mCourseList;
	private boolean mLoaded = false;
	private static final String COURSE_LIST_KEY = "KOURSE LCISKD KEY";
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mLoaded) outState.putSerializable(COURSE_LIST_KEY, (Serializable) mCourseList);
	}
	
	@Override
	public void onActivityCreated(Bundle inState) {
		super.onActivityCreated(inState);
		super.registerForContextMenu(getListView());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		if (inState != null && inState.containsKey(COURSE_LIST_KEY)) {
			mCourseList = (List<Course>) inState.getSerializable(COURSE_LIST_KEY);
			mListAdapter = new CourseListAdapter(mCourseList, getActivity());
			
			setListAdapter(mListAdapter);
			mLoaded = true;
		} else {
			mCourseList = new ArrayList<Course>();
			mListAdapter = new CourseListAdapter(mCourseList, getActivity());
			
			setListAdapter(mListAdapter);
			CookieStore cookies = LoginManager.getCookies(getSherlockActivity());
	
			Handler coursesDownloadHandler = new CoursesDownloadHandler(this, mCourseList, mListAdapter);
			Thread coursesDownloadThread = new CoursesDownloadThread(coursesDownloadHandler, cookies);
			coursesDownloadThread.start();
		}
		
		return super.onCreateView(inflater, container, inState);
	}
	
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		Course course = mCourseList.get(position);
		Uri uri = Uri.parse(course.getUrl());
		Intent intent = new Intent(getActivity(), CourseActivity.class);
		intent.putExtra(CourseActivity.EXTRA_COURSE_ID, Integer.parseInt(uri.getQueryParameter("id")));
		intent.putExtra(CourseActivity.EXTRA_TITLE, course.getName() + ": " + course.getTitle());
		getActivity().startActivity(intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
		super.onCreateContextMenu(menu, view, info);
		
		final int position = ((AdapterContextMenuInfo) info).position;
		
		final Course course = mCourseList.get(position);
		final String name = course.getName();
		final String title = course.getTitle();
		final String summary = course.getSummary();
		final Instructor[] instructors = course.getInstructors().toArray(new Instructor[] {});
		
		Dialog dialog = new Dialog(getActivity());
		dialog.setTitle(name + ": " + title);
		dialog.setContentView(R.layout.dialog_course_description);
		
		TextView summaryView = (TextView) dialog.findViewById(R.id.dialog_course_description_summary);
		summaryView.setText(summary);
		
		LinearLayout instructorGroup = (LinearLayout) dialog.findViewById(R.id.dialog_course_description_instructors);
		for (final Instructor instructor : instructors) {
			final Button button = new Button(getActivity());
			button.setText(instructor.getName());
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), BrowserActivity.class);
					intent.setData(Uri.parse(instructor.getUrl()));
					startActivity(intent);
				}
			});
			instructorGroup.addView(button);
		}
		
		dialog.show();
		
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(50);
	}

	private static class CoursesDownloadThread extends HandledThread {
		private CookieStore mCookies;
		
		CoursesDownloadThread(Handler handler, CookieStore cookies) {
			setHandler(handler);
			mCookies = cookies;
		}
		
		public void run() {
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
		private final Context mContext;
		private final List<Course> mCourseList;
		private final BaseAdapter mListAdapter;
		private final MyCoursesFragment mFragment;
		
		CoursesDownloadHandler(MyCoursesFragment fragment, List<Course> courseList, BaseAdapter listAdapter) {
			mContext = fragment.getActivity();
			mCourseList = courseList;
			mListAdapter = listAdapter;
			mFragment = fragment;
		}
		
		@Override
		public void handleMessage(Message message) {
			mFragment.mLoaded = true;
			
			if (message.obj instanceof CoursesPayload) {
				List<Course> courses = ((CoursesPayload) message.obj).getPayload();
				mCourseList.addAll(courses);
				mListAdapter.notifyDataSetChanged();
			} else if (message.obj instanceof NullPointerException) {
				LoginManager.setCookies(mContext, null);
				Intent intent = new Intent(mContext, LoginActivity.class);
				mContext.startActivity(intent);
				//TODO:  Handle null pointers
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