package com.nguyenmp.gauchodroid;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.common.MenuUtils;
import com.nguyenmp.gauchodroid.upload.UploadActivity;

public class PhotoFragment extends SherlockFragment
							implements PhotoDownloadListener {
	
	//This is the current photo download that we are displaying
	//We will use this to store and restore instance states
	private PhotoDownload mCurrentPhotoDownload = null;
	private ImageView mImageView = null;
	private ProgressBar mProgressBar = null;
	private TextView mHeaderView = null;
	
	private static final String KEY_PHOTO_DOWNLOAD = "klakdfoiwue09";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		//Must be called in order to show our options menu in the action bar
		super.setHasOptionsMenu(true);
		
		//Inflate the content view
		View contentView = inflater.inflate(R.layout.fragment_photo, container, false);
		
		//Get the main views from the newly inflated content view
		ProgressBar progressBar = (ProgressBar) contentView.findViewById(R.id.fragment_photo_progress_bar);
		mProgressBar = progressBar;
		
		ImageView imageView = (ImageView) contentView.findViewById(R.id.fragment_photo_image_view);
		mImageView = imageView;
		mImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
			}
		});
		
		TextView headerView = (TextView) contentView.findViewById(R.id.fragment_photo_header);
		mHeaderView = headerView;
		mHeaderView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
			}
		});
		
		//If there was a previous state and it was already initialized
		if (inState != null && inState.containsKey(KEY_PHOTO_DOWNLOAD)) {
			PhotoDownload payload = (PhotoDownload) inState.getParcelable(KEY_PHOTO_DOWNLOAD);
			setPhotoDownload(payload);
		}
		
		//Otherwise, we are generating this fragment from scratch
		if (inState == null) {
			//Start a new download task
			mCurrentPhotoDownload = null;
			refresh();
		}
		
		
		return contentView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mCurrentPhotoDownload != null) outState.putParcelable(KEY_PHOTO_DOWNLOAD, mCurrentPhotoDownload);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuUtils.addMenuItem(menu, "Refresh");
		MenuUtils.addMenuItem(menu, "Share Photo");
		MenuUtils.addMenuItem(menu, "Upload Photo");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Refresh")) {
			refresh();
			return true;
		} else if (item.getTitle().equals("Upload Photo")) {
			Intent intent = new Intent(getActivity(), UploadActivity.class);
			intent.putExtra(UploadActivity.EXTRA_SELECTED_TAB, UploadActivity.SELECTED_TAB_PHOTO);
			startActivity(intent);
		} else if (item.getTitle().equals("Share Photo")) {
			if (mCurrentPhotoDownload != null) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, mCurrentPhotoDownload.url);
				startActivity(Intent.createChooser(intent, "Send via:"));
			} else {
				Toast.makeText(getActivity(), "No photo to share.", Toast.LENGTH_LONG).show();
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void refresh() {
		mProgressBar.setVisibility(View.VISIBLE);
		mImageView.setVisibility(View.GONE);
		mHeaderView.setVisibility(View.GONE);
		
		PhotoDownloadHandler handler = new PhotoDownloadHandler(this, mProgressBar, mHeaderView);
		Thread downloadThread = new PhotoDownloadThread(handler);
		downloadThread.start();
	}
	
	private void setPhotoDownload(PhotoDownload payload) {
		//Set the current payload to the one we just got
		mCurrentPhotoDownload = payload;
		
		//Hide the progress bar
		mProgressBar.setVisibility(View.GONE);
		
		//Initialize the image view
		mImageView.setImageBitmap(mCurrentPhotoDownload.bitmap);
		mImageView.setVisibility(View.VISIBLE);
		
		//Initialize the header view
		//Make sure to display null for empty titles and display names
		//Set up the header to be "'title' by 'displayName'"
		mHeaderView.setText(Html.fromHtml(
						(mCurrentPhotoDownload.title.length() == 0 ? "<i>\"Untitled\"</i>" : "<i>\"" + mCurrentPhotoDownload.title + "\"</i>") + 
						" by " + 
						(mCurrentPhotoDownload.displayName.length() == 0 ? "<b>Anonymous</b>" : "<b>" + mCurrentPhotoDownload.displayName + "</b>")));
		mHeaderView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * A thread that asynchronously downloads an image from the 
	 * GauchoSpace front page.  Because the GauchoSpace front 
	 * page returns random images each time, predicting the 
	 * upcoming image is probably impossible.  This thread may 
	 * return repeated images or images out of order.
	 * @author Mark Nguyen
	 *
	 */
	private static class PhotoDownloadThread extends HandledThread {
		
		/**
		 * Creates a new Thread to download.  You still need to call
		 * the start() method in order to initialize the download.
		 * @param handler The PhotoDownloadHandler to notify when 
		 * the download is complete or as reached an error.
		 */
		private PhotoDownloadThread(PhotoDownloadHandler handler) {
			super.setHandler(handler);
		}
		
		@Override
		public void run() {
			try {
				//Connect to the homepage of gauchospace
				URL url = new URL("https://gauchospace.ucsb.edu/");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				
				//Read the response as a string (html document)
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder stringBuilder = new StringBuilder();
				char[] buffer = new char[1024];
				int charsRead;
				while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
					stringBuilder.append(buffer, 0, charsRead);
				}
				
				//Scrape the relative url of the photo from the html response
				int start = stringBuilder.indexOf("/photouploads/photos//");
				int end = stringBuilder.indexOf("\"", start);
				String imageURL = "https://gauchospace.ucsb.edu/" + stringBuilder.substring(start, end);
				
				//Create a url connection and pass the input stream to the 
				//bitmap factory to decode and generate a bitmap of the image
				url = new URL(imageURL);
				Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				
				//Get the display name from the html response
				start = stringBuilder.indexOf("<span style=\"text-decoration:italic\">") + "<span style=\"text-decoration:italic\">".length();
				end = stringBuilder.indexOf("</span>", start);
				String displayName = stringBuilder.substring(start, end);
				
				//Get the title of the image from the html repsonse
				start = stringBuilder.indexOf("<span style=\"text-decoration:italic\">", start) + "<span style=\"text-decoration:italic\">".length();
				end = stringBuilder.indexOf("</span>", start);
				String title = stringBuilder.substring(start, end);
				
				//Return the payload
				PhotoDownload payload = new PhotoDownload(displayName, title, bitmap, imageURL);
				dispatchMessage(payload);
			} catch (MalformedURLException e) {
				//Return the error
				e.printStackTrace();
				dispatchMessage(e);
			} catch (IOException e) {
				//Return the error
				e.printStackTrace();
				dispatchMessage(e);
			}
		}
	}
	
	/**
	 * Handles photo downloads.  Used in conjunction with
	 * PhotoDownloadThread.
	 * @author Mark
	 *
	 */
	private static class PhotoDownloadHandler extends Handler {
		private final TextView mHeaderView;
		private final PhotoDownloadListener mListener;
		private final ProgressBar mProgressBar;
		
		/**
		 * Creates a new 
		 * @param progressBar The progress bar to hide when progress is received
		 * @param imageView The image view to update and show when a bitmap was 
		 * downloaded and returned here.
		 * @param headerView The text view to display the name and title of the 
		 * piece or any error messages that may result from the PhotoDownloadThread.
		 */
		private PhotoDownloadHandler(PhotoDownloadListener listener, ProgressBar progressBar, TextView headerView) {
			mHeaderView = headerView;
			mListener = listener;
			mProgressBar = progressBar;
		}
		
		@Override
		public void handleMessage(Message message) {
			mProgressBar.setVisibility(View.GONE);
			
			//If we get a payload
			if (message.obj != null && message.obj instanceof PhotoDownload) {
				PhotoDownload photoDownload = (PhotoDownload) message.obj;
				mListener.onPhotoDownloaded(photoDownload);
			}
			
			//If the payload fails to initialize
			else if (message.obj != null && message.obj instanceof Exception) {
				//Alert user with header view
				mHeaderView.setText("Error downloading photo:\n\n" + ((Exception) message.obj + "\n\nTap to refresh").toString());
				mHeaderView.setVisibility(View.VISIBLE);
			}
			
			//If some unknown error occured and something freakish and unexpected happened
			else {
				//Alert the user with the header view
				mHeaderView.setText("Error downloading photo:\n\n" + "Unknown error \n\nTap to refresh");
				mHeaderView.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onPhotoDownloaded(PhotoDownload download) {
		setPhotoDownload(download);
	}
}
