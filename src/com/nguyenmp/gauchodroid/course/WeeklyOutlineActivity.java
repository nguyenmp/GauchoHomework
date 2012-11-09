//package com.nguyenmp.gauchodroid.course;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactoryConfigurationError;
//
//import org.apache.http.client.CookieStore;
//import org.xml.sax.SAXNotRecognizedException;
//import org.xml.sax.SAXNotSupportedException;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.webkit.WebView;
//import android.widget.BaseAdapter;
//import android.widget.Button;
//import android.widget.ListView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.actionbarsherlock.app.ActionBar;
//import com.nguyenmp.gauchodroid.LoginActivity;
//import com.nguyenmp.gauchodroid.R;
//import com.nguyenmp.gauchodroid.browser.MyWebViewClient;
//import com.nguyenmp.gauchodroid.common.AlertDialogFactory;
//import com.nguyenmp.gauchodroid.common.HandledThread;
//import com.nguyenmp.gauchospace.GauchoSpaceClient;
//import com.nguyenmp.gauchospace.parser.WeeklyOutlineParser.UnparsableHtmlException;
//import com.nguyenmp.gauchospace.thing.Week;
//
//public class WeeklyOutlineActivity extends CourseActivity {
//	private List<Week> mCalendar = null;
//	
//	@Override
//	public void onCreate(Bundle inState) {
//		super.onCreate(inState);
//		
//		super.setContentView(R.layout.course_weekly_outline);
//		
//		mCalendar = new ArrayList<Week>();
//		BaseAdapter adapter = new CourseWeekAdapter(mCalendar);
//		setListAdapter(adapter);
//		
//		int courseID = Integer.valueOf(getIntent().getData().getQueryParameter("id"));
//		CookieStore cookies = LoginActivity.getCookies(this);
//		
//		CalendarHandler handler = new CalendarHandler(mCalendar, adapter, this);
//		CalendarDownloader downloader = new CalendarDownloader(courseID, cookies);
//		downloader.setHandler(handler);
//		downloader.start();
//	}
//	
//	@Override
//	public void onListItemClick(ListView listView, View view, int position, long id) {
//		Week week = (Week) listView.getItemAtPosition(position);
//		
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(week.getTitle());
//		
//		final WebView webView = new WebView(this);
//		
//		RelativeLayout header = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.dialog_web_view_header, null);
//		
//		builder.setCustomTitle(header);
//		builder.setView(webView);
//		
////		webView.loadData("<body bgcolor=\"#B0B0B0\">" + week.getHtml() + "</body>", "text/html; charset=UTF-8", null);
//		webView.loadData(week.getHtml(), "text/html; charset=UTF-8", null);
//		webView.setWebViewClient(new MyWebViewClient(this));
//		webView.setBackgroundColor(Color.parseColor("#C0C0C0"));
//		
//		
//		
//		final Dialog dialog = builder.show();
//		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//			public void onDismiss(DialogInterface dialog) {
//				webView.destroy();
//			}
//		});
//		
//		Button closeButton = (Button) header.findViewById(R.id.dialog_web_view_header_close);
//		closeButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				dialog.dismiss();
//			}
//		});
//		
//		Button backButton = (Button) header.findViewById(R.id.dialog_web_view_header_left);
//		backButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				if (webView.canGoBack())
//					webView.goBack();
//				else
//					Toast.makeText(webView.getContext(), "Cannot go back!", Toast.LENGTH_SHORT).show();
//			}
//		});
//		
//		Button forwardButton = (Button) header.findViewById(R.id.dialog_web_view_header_right);
//		forwardButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				if (webView.canGoForward())
//					webView.goForward();
//				else
//					Toast.makeText(webView.getContext(), "Cannot go forward!", Toast.LENGTH_SHORT).show();
//			}
//		});
//	}
//	
//	
//	
//	private static class CourseWeekAdapter extends BaseAdapter {
//		private final List<Week> mWeeklyOutline;
//		
//		CourseWeekAdapter(List<Week> weeklyOutline) {
//			mWeeklyOutline = weeklyOutline;
//		}
//
//		public int getCount() {
//			return mWeeklyOutline.size();
//		}
//
//		public Week getItem(int position) {
//			return mWeeklyOutline.get(position);
//		}
//
//		public long getItemId(int position) {
//			return 0;
//		}
//
//		public View getView(int position, View convertView, ViewGroup parent) {
//			Week week = getItem(position);
//			
//			if (convertView == null) {
//				//If view isn't initialized, reinitialize
//				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_weekly_outline, parent, false);
//			}
//			
//			TextView weekTitle = (TextView) convertView.findViewById(R.id.list_item_weekly_outline_week);
//			weekTitle.setText(week.getTitle());
//			
//			TextView weekSummary = (TextView) convertView.findViewById(R.id.list_item_weekly_outline_summary);
//			weekSummary.setText(week.getSummary());
//			
//			return convertView;
//		}
//		
//	}
//	
//	private class CalendarDownloader extends HandledThread {
//		private final int mCourseID;
//		private final CookieStore mCookies;
//		
//		private CalendarDownloader(int courseId, CookieStore cookies) {
//			mCookies = cookies;
//			mCourseID = courseId;
//		}
//		
//		public void run() {
//			try {
//				dispatchMessage(GauchoSpaceClient.getWeeklyOutlineFromCourse(mCourseID, mCookies));
//			} catch (SAXNotRecognizedException e) {
//				dispatchMessage(e);
//				e.printStackTrace();
//			} catch (SAXNotSupportedException e) {
//				dispatchMessage(e);
//				e.printStackTrace();
//			} catch (TransformerFactoryConfigurationError e) {
//				dispatchMessage(e);
//				e.printStackTrace();
//			} catch (IOException e) {
//				dispatchMessage(e);
//				e.printStackTrace();
//			} catch (TransformerException e) {
//				dispatchMessage(e);
//				e.printStackTrace();
//			} catch (UnparsableHtmlException e) {
//				dispatchMessage(e);
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	private static class CalendarHandler extends Handler {
//		private final List<Week> mCalendar;
//		private final BaseAdapter mAdapter;
//		private final Context mContext;
//		
//		CalendarHandler(List<Week> calendar, BaseAdapter adapter, Context context) {
//			mCalendar = calendar;
//			mContext = context;
//			mAdapter = adapter;
//		}
//		
//		public void handleMessage(Message message) {
//			if (message.obj instanceof List<?>) {
//				List<Week> calendar = (List<Week>) message.obj;
//				mCalendar.addAll(calendar);
//				mAdapter.notifyDataSetChanged();
//			} else if (message.obj instanceof Exception) {
//				Exception e = (Exception) message.obj;
//				AlertDialogFactory.createAlert(e.getClass().getName(), e.toString(), mContext).show();
//			}
//		}
//	}
//	
//	private static class CourseNavigationListener implements ActionBar.OnNavigationListener {
//		private final String[] mTitles;
//		private final Context mContext;
//		
//		CourseNavigationListener(Context context, String[] titles) {
//			mTitles = titles;
//			mContext = context;
//		}
//		
//		public boolean onNavigationItemSelected(int itemPosition, long itemId) {
//			Toast.makeText(mContext, mTitles[itemPosition], Toast.LENGTH_SHORT).show();
//			
//			return true;
//		}
//	}
//}