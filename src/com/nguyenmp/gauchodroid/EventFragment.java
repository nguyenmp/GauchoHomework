package com.nguyenmp.gauchodroid;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.common.MenuUtils;
import com.nguyenmp.gauchodroid.upload.UploadActivity;

public class EventFragment extends SherlockFragment implements EventDownloadListener {
	private EventDownload mCurrentEvent = null;
	private ProgressBar mProgressBar;
	private TextView mTitleView, mTimeView, mDescriptionView;
	private ImageView mImageView;
	private View mContentView;
	private static final String KEY_CURRENT_EVENT = "lwekjliou3409u 302";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		//Must call to have our options menu placed 
		//in the action bar's options menu
		super.setHasOptionsMenu(true);
		
		//Inflate the content view
		View inflatedView = inflater.inflate(R.layout.fragment_event, container, false);
		
		mContentView = inflatedView.findViewById(R.id.fragment_event_content);
		
		//Find the content views and it's componenets
		mTitleView = (TextView) mContentView.findViewById(R.id.fragment_event_title);
		mTimeView = (TextView) mContentView.findViewById(R.id.fragment_event_time);
		mDescriptionView = (TextView) mContentView.findViewById(R.id.fragment_event_description);
		mImageView = (ImageView) mContentView.findViewById(R.id.fragment_event_image);
		mProgressBar = (ProgressBar) mContentView.findViewById(R.id.fragment_event_progress_bar);
		
		if (inState != null && inState.containsKey(KEY_CURRENT_EVENT)) setCurrentEvent((EventDownload) inState.getParcelable(KEY_CURRENT_EVENT));
		else {
			setCurrentEvent(null);
			Thread downloadThread = new EventDownloadThread(new EventDownloadHandler(this, mProgressBar, mTitleView));
			downloadThread.start();
		}
		//Return the generic inflated layout for our temp fragment
		return inflatedView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mCurrentEvent != null) outState.putParcelable(KEY_CURRENT_EVENT, mCurrentEvent);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		//Try to add refresh and upload to the options menu
		MenuUtils.addMenuItem(menu, "Refresh");
		MenuUtils.addMenuItem(menu, "Upload Event");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Refresh")) {
			setCurrentEvent(null);
			Thread downloadThread = new EventDownloadThread(new EventDownloadHandler(this, mProgressBar, mTitleView));
			downloadThread.start();
			return true;
		} else if (item.getTitle().equals("Upload Event")) {
			//if hte user chose to upload an event 
			//open the Upload activity and select the event tab
			Intent intent = new Intent(getActivity(), UploadActivity.class);
			intent.putExtra(UploadActivity.EXTRA_SELECTED_TAB, UploadActivity.SELECTED_TAB_EVENT);
			startActivity(intent);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void setCurrentEvent(final EventDownload newEvent) {
		mCurrentEvent = newEvent;
		if (newEvent != null) {
			mProgressBar.setVisibility(View.GONE);
			
			if (newEvent.url != null && newEvent.url.length() > 0) mContentView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(newEvent.url));
					startActivity(intent);
				}
			});
			
			mTitleView.setText(newEvent.title);
			mTitleView.setVisibility(View.VISIBLE);
			mTimeView.setText(newEvent.time);
			mTimeView.setVisibility(View.VISIBLE);
			mDescriptionView.setText(newEvent.description);
			mDescriptionView.setVisibility(View.VISIBLE);
			mImageView.setImageBitmap(newEvent.image);
			mImageView.setVisibility(View.VISIBLE);
			
		} else {
			mContentView.setOnClickListener(null);
			mProgressBar.setVisibility(View.VISIBLE);
			mTitleView.setVisibility(View.GONE);
			mTimeView.setVisibility(View.GONE);
			mDescriptionView.setVisibility(View.GONE);
			mImageView.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onEventDownloaded(EventDownload download) {
		setCurrentEvent(download);
	}

	private static class EventDownloadThread extends HandledThread {
		
		private EventDownloadThread(Handler handler) {
			setHandler(handler);
		}
		
		public void run() {
			try {
				//Download from the login page which contains the events
				URL url = new URL("https://gauchospace.ucsb.edu/courses/login/");
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
				char[] buffer = new char[1024];
				int charsRead;
				StringBuilder stringBuilder = new StringBuilder();
				
				//Store response to the stringBuilder
				while((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
					stringBuilder.append(buffer, 0, charsRead);
				}
				
				//Initialize the start index to be the begining of the 
				//event's html
				int start, end;
				start = stringBuilder.indexOf("<div class=\"signuppanel\">");
				
				//Get the event url
				start = stringBuilder.indexOf("<a href=", start) + "<a href=".length();
				end = stringBuilder.indexOf("target=\"_blank\">", start);
				
				String eventURL = stringBuilder.substring(start, end).trim();
				
				//Get the event title
				start = end + "target=\"_blank\">".length();
				end = stringBuilder.indexOf("<", start);
				String eventTitle = stringBuilder.substring(start, end).trim();

				//Get the event time
				start = stringBuilder.indexOf("<h3>", start) + "<h3>".length();
				end = stringBuilder.indexOf("</h3>");
				String eventTime = stringBuilder.substring(start, end).trim();
				
				//Get the event description
				start = stringBuilder.indexOf("<p>", start) + "<p>".length();
				end = stringBuilder.indexOf("</p>", start);
				String eventDescription = stringBuilder.substring(start, end).trim();
				
				//Get the image url
				start = stringBuilder.indexOf("https://gauchospace.ucsb.edu/photouploads/events/", end);
				end = stringBuilder.indexOf("'", start);
				
				//Get the image bitmap
				Bitmap eventImage = BitmapFactory.decodeStream(new URL(stringBuilder.substring(start, end)).openConnection().getInputStream());
				
				//Return scraped event data
				EventDownload download = new EventDownload(eventTitle, eventTime, eventDescription, eventURL, eventImage);
				dispatchMessage(download);
			} catch (MalformedURLException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (IOException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				dispatchMessage(e);
				e.printStackTrace();
			}
			
			
		}
	}
	
	private static class EventDownloadHandler extends Handler {
		private final TextView mTextView;
		private final EventDownloadListener mListener;
		private final ProgressBar mProgressBar;
		
		private EventDownloadHandler(EventDownloadListener listener, ProgressBar progressBar, TextView textView) {
			mTextView = textView;
			mListener = listener;
			mProgressBar = progressBar;
		}
		
		public void handleMessage(Message message) {
			//Disable progress bar
			mProgressBar.setVisibility(View.GONE);
			
			//If we got an event download
			if (message.obj instanceof EventDownload) {
				//Broadcast it to the listener
				mListener.onEventDownloaded((EventDownload) message.obj);
			}
			
			//Otherwise, if we got an exception, display it as an error
			else if (message.obj instanceof Exception) {
				mTextView.setText("Error downloading event: " + ((Exception) message.obj).toString());
				mTextView.setVisibility(View.VISIBLE);
			}
			
			//Otherwise, display a general error
			else {
				mTextView.setTag("Error downloading event...");
				mTextView.setVisibility(View.VISIBLE);
			}
		}
	}
}
