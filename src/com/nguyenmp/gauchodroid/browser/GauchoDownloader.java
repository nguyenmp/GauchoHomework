package com.nguyenmp.gauchodroid.browser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.Environment;
import android.os.Handler;

import com.nguyenmp.gauchodroid.common.HandledThread;

public class GauchoDownloader extends HandledThread {
	private final String mUrl;
	private final String mFileName;
	private final CookieStore mCookies;
	
	public GauchoDownloader(String url, String filename, CookieStore cookies, Handler handler) {
		mCookies = cookies;
		mUrl = url;
		setHandler(handler);
//			courses/file.php/3860/Lecture02.pdf
//		List<String> pathSegments = Uri.parse(url).getPathSegments();
		mFileName = filename;
	}
	
	public void run() {
		final HttpClient client = new DefaultHttpClient();
		final HttpGet get = new HttpGet(mUrl);
		final HttpContext context = getContext(mCookies);
		
		HttpResponse response = null;
		
		InputStream inStream = null;
		OutputStream outStream = null;
		
		try {
			response = client.execute(get, context);
			final HttpEntity responseEntity = response.getEntity();
			inStream = responseEntity.getContent();
			final byte[] buffer = new byte[1024];
			int bytesRead;
			
			File file = getFile(mFileName);
			file.createNewFile();
			outStream = new FileOutputStream(file);
			
			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
			
			outStream.flush();
			dispatchMessage(file.getPath());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			dispatchMessage(e);
		} catch (IOException e) {
			e.printStackTrace();
			dispatchMessage(e);
		} finally {
			System.out.println("Exiting download thread");
			if (outStream != null) try {outStream.close();} catch (IOException e) {e.printStackTrace();}
			if (inStream != null) try {inStream.close();} catch (IOException e) {e.printStackTrace();}
		}
		
	}
	
	private File getFile(String filename) throws IOException {
		System.out.println("Extern storage dir: " + Environment.getExternalStorageDirectory());
		
		File file = null;
		String rootPath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
		
		file = new File(rootPath + filename);
		if (file.exists()) {
			file = getAlternateFile(filename);
		}
		
		return file;
	}
	
	private File getAlternateFile(String filename) {
		String rootPath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
		File file = new File(rootPath + filename);
		int extension = 0;
		
		while (file.exists()) {
			StringBuffer buffer = new StringBuffer(filename);
			int index = buffer.lastIndexOf(".");
			
			if (index == -1) index = buffer.length();
			
			buffer.insert(index, "-" + extension);
			
			String alternateFileName = buffer.toString();
			
			file = new File(rootPath + alternateFileName);
			
			extension++;
		}
		
		return file;
	}

	private HttpContext getContext(CookieStore cookies) {
		HttpContext context = new BasicHttpContext();;
		
		if (cookies == null) cookies = new BasicCookieStore();
		
		context.setAttribute(ClientContext.COOKIE_STORE, cookies);
		
		return context;
	}
}