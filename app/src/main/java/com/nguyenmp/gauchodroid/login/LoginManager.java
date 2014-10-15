package com.nguyenmp.gauchodroid.login;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.loopj.android.http.PersistentCookieStore;

public class LoginManager {
	
	public static void setCookies(Context context, CookieStore cookies) {
		PersistentCookieStore store = new PersistentCookieStore(context);
		store.clear();
		
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		cookieManager.setAcceptCookie(true);
		
		if (cookies != null) {
			for (Cookie cookie : cookies.getCookies()) {
				store.addCookie(cookie);
				cookieManager.setCookie(cookie.getDomain(), cookie.getName() + "=" + cookie.getValue());
			}
		}
		
		CookieSyncManager.getInstance().sync();
	}
	
	public static CookieStore getCookies(Context context) {
		PersistentCookieStore store = new PersistentCookieStore(context);
		return store;
	}
}
