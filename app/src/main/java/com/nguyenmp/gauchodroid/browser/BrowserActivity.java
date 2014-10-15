package com.nguyenmp.gauchodroid.browser;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Toast;

public class BrowserActivity extends ActionBarActivity {
	private WebView mWebView = null;
	/**
	 * The Key specifying the String extra for the HTML data.  The value will 
	 * be considered the html data to render in the web view if no Data uri is 
	 * present.
	 */
	public static final String EXTRA_DATA_HTML = "extra_data_html_key";
	public static final String EXTRA_BACKGROUND = "extra_background_key";
	
	
	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		this.requestWindowFeature(Window.FEATURE_PROGRESS);

		this.getSupportActionBar().setHomeButtonEnabled(true);
		this.getSupportActionBar().setDisplayShowHomeEnabled(true);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
		mWebView = new WebView(getApplicationContext());
		
//		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		setContentView(mWebView);
		
		String backgroundColor = getIntent().getStringExtra(EXTRA_BACKGROUND);
		if (backgroundColor != null)
			mWebView.setBackgroundColor(Color.parseColor(backgroundColor));
		else
			mWebView.setBackgroundColor(Color.BLACK);
		
	    mWebView.setWebViewClient(new MyWebViewClient(this));
	    mWebView.setWebChromeClient(new MyWebChromeClient(this));
	    mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.setDownloadListener(new MyDownloadListener(this));        
        mWebView.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				return false;
			}
        });
        
        if (inState == null) {
        	//If there is no saved state, initialize the webview
        	
        	if (getIntent().getData() != null)
        		//If we are supplied data via URI, then load from it
        		mWebView.loadUrl(getIntent().getDataString());
        	else {
        		//If no data entry is present, grab the data entry from the extra bundle
        		String data = getIntent().getStringExtra(EXTRA_DATA_HTML);
        		
        		//If that is not present as well, render nothing
        		if (data == null)
        			data = "";
        		
        		//Load the html page
        		mWebView.loadData(data, "text/html; charset=UTF-8", null);
        	}
        }
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (mWebView.getUrl() != null && mWebView.getUrl().startsWith("http"))
			menu.add("Open in browser");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		
		if (((String) item.getTitle()).equalsIgnoreCase("Open in browser") && mWebView.getUrl() != null && mWebView.getUrl().startsWith("http")) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(mWebView.getUrl()));
			startActivity(intent);
		}
		
		return super.onOptionsItemSelected(item);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mWebView.saveState(outState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle inState) {
		super.onRestoreInstanceState(inState);
		mWebView.restoreState(inState);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	if (mWebView.canGoBack())
	    		mWebView.goBack();
	    	else
	    		finish();
	    	
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Toast.makeText(this, "Exiting browser...", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		return super.onKeyLongPress(keyCode, event);
	}
}

