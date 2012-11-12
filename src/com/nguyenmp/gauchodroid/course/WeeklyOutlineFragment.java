package com.nguyenmp.gauchodroid.course;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.client.CookieStore;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.browser.MyWebViewClient;
import com.nguyenmp.gauchodroid.common.AlertDialogFactory;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.login.LoginManager;
import com.nguyenmp.gauchospace.GauchoSpaceClient;
import com.nguyenmp.gauchospace.parser.WeeklyOutlineParser.UnparsableHtmlException;
import com.nguyenmp.gauchospace.thing.Week;

public class WeeklyOutlineFragment extends SherlockListFragment implements WeeklyOutlineDownloadListener {
	private List<Week> mCalendar;
	private BaseAdapter mListAdapter;
	private boolean mLoaded = false;
	public static final String ARGUMENT_COURSE_ID = "course_id";
	private static final String KEY_CALENDAR = "lwkjefiuo32u490-2934b q08u4";
	
	@Override
	public void onActivityCreated(Bundle inState) {
		super.onActivityCreated(inState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		View view = super.onCreateView(inflater, container, inState);
		
		mCalendar = new ArrayList<Week>();
		mListAdapter = new CourseWeekAdapter(mCalendar);
		setListAdapter(mListAdapter);
		
		if (inState != null && inState.containsKey(KEY_CALENDAR)) {
			onDownloaded((List<Week>) inState.getSerializable(KEY_CALENDAR));
		} else {
			onDownloaded(null);
			CookieStore cookies = LoginManager.getCookies(getActivity());
			
			CalendarHandler handler = new CalendarHandler(this, inflater.getContext());
			System.out.println(getArguments().getInt(ARGUMENT_COURSE_ID));
			CalendarDownloader downloader = new CalendarDownloader(getArguments().getInt(ARGUMENT_COURSE_ID), cookies);
			downloader.setHandler(handler);
			downloader.start();
		}
		
		return view;
	}
	
	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		Week week = (Week) listView.getItemAtPosition(position);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(week.getTitle());
		
		final WebView webView = new WebView(getActivity());
		
		RelativeLayout header = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_web_view_header, null);
		
		builder.setCustomTitle(header);
		builder.setView(webView);
		
//		webView.loadData("<body bgcolor=\"#B0B0B0\">" + week.getHtml() + "</body>", "text/html; charset=UTF-8", null);
		webView.loadData(week.getHtml(), "text/html; charset=UTF-8", null);
		webView.setWebViewClient(new MyWebViewClient(getActivity()));
		webView.setBackgroundColor(Color.parseColor("#C0C0C0"));
		
		
		
		final Dialog dialog = builder.show();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				webView.destroy();
			}
		});
		
		Button closeButton = (Button) header.findViewById(R.id.dialog_web_view_header_close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		Button backButton = (Button) header.findViewById(R.id.dialog_web_view_header_left);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (webView.canGoBack())
					webView.goBack();
				else
					Toast.makeText(webView.getContext(), "Cannot go back!", Toast.LENGTH_SHORT).show();
			}
		});
		
		Button forwardButton = (Button) header.findViewById(R.id.dialog_web_view_header_right);
		forwardButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (webView.canGoForward())
					webView.goForward();
				else
					Toast.makeText(webView.getContext(), "Cannot go forward!", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mLoaded) {
			outState.putSerializable(KEY_CALENDAR, (Serializable) mCalendar);
		}
	}
	
	@Override
	public void onDownloaded(List<Week> weeklyOutline) {
		if (weeklyOutline != null) {
			mLoaded = true;
			mCalendar.clear();
			mCalendar.addAll(weeklyOutline);
		} else {
			mLoaded = false;
			mCalendar.clear();
		}
		
		mListAdapter.notifyDataSetChanged();
	}
	
	private static class CourseWeekAdapter extends BaseAdapter {
		private final List<Week> mWeeklyOutline;
		
		CourseWeekAdapter(List<Week> weeklyOutline) {
			mWeeklyOutline = weeklyOutline;
		}

		public int getCount() {
			return mWeeklyOutline.size();
		}

		public Week getItem(int position) {
			return mWeeklyOutline.get(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Week week = getItem(position);
			
			if (convertView == null) {
				//If view isn't initialized, reinitialize
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_weekly_outline, parent, false);
			}
			
			TextView weekTitle = (TextView) convertView.findViewById(R.id.list_item_weekly_outline_week);
			weekTitle.setText(week.getTitle());
			
			TextView weekSummary = (TextView) convertView.findViewById(R.id.list_item_weekly_outline_summary);
			weekSummary.setText(week.getSummary());
			
			if (week.getCurrent()) {
				convertView.setBackgroundColor(0xcc00ffff);
			} else {
				convertView.setBackgroundColor(Color.TRANSPARENT);
			}
			
			return convertView;
		}
		
	}
	
	private static class CalendarDownloader extends HandledThread {
		private final int mCourseID;
		private final CookieStore mCookies;
		
		private CalendarDownloader(int courseId, CookieStore cookies) {
			mCookies = cookies;
			mCourseID = courseId;
		}
		
		public void run() {
			try {
				System.out.println(mCourseID);
				dispatchMessage(GauchoSpaceClient.getWeeklyOutlineFromCourse(mCourseID, mCookies));
			} catch (SAXNotRecognizedException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (SAXNotSupportedException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (IOException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (TransformerException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (UnparsableHtmlException e) {
				dispatchMessage(e);
				e.printStackTrace();
			}
		}
	}
	
	private static class CalendarHandler extends Handler {
		private final WeeklyOutlineDownloadListener mListener;
		private final Context mContext;
		
		CalendarHandler(WeeklyOutlineDownloadListener listener, Context context) {
			mListener = listener;
			mContext = context;
		}
		
		public void handleMessage(Message message) {
			if (message.obj instanceof List<?>) {
				mListener.onDownloaded((List<Week>) message.obj);
			} else if (message.obj instanceof Exception) {
				Exception e = (Exception) message.obj;
				AlertDialogFactory.createAlert(e.getClass().getName(), e.toString(), mContext).show();
			}
		}
	}
}