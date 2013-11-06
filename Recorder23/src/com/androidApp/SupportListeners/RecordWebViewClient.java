package com.androidApp.SupportListeners;

import java.io.IOException;
import java.net.URLDecoder;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Listeners.IOriginalListener;
import com.androidApp.Listeners.RecordListener;
import com.androidApp.Listeners.RecordWebView;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.FileUtils;
import com.androidApp.Utility.StringUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** we can't derive from RecordListener, because we extend WebViewClient
 * we like to wait for pageloaded events, because it's polite to wait for a webview to load before interacting with it.
 * @author mattrey
 * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */

public class RecordWebViewClient extends WebViewClient implements IOriginalListener {
	protected WebViewClient 	mOriginalWebViewClient;
	protected EventRecorder 	mEventRecorder;				// handle to the recorder 
	protected String			mActivityName;				// to record events and exceptions filtered by activity
	protected Context			mContext;
	
	public RecordWebViewClient(String activityName, EventRecorder eventRecorder, WebView webView) {
		mEventRecorder = eventRecorder;
		mActivityName = activityName;
		mContext = webView.getContext();
		try {
			mOriginalWebViewClient = ListenerIntercept.getWebViewClient(webView);
			webView.setWebViewClient(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, activityName, webView, "create on pageStarted listener");
		}		
	}
	
	// given a webview, inject the traverse.js code which intercepts the click listeners to show the 
    // fully qualified paths of the clicked HTML elements.
	
	public void traverse(Context context, WebView wv) throws IOException {
		String javascriptCode = FileUtils.readJarResourceString(RecordWebView.class, Constants.Asset.TRAVERSE_JS);
		String inject = "javascript:var script = document.createElement(\"script\"); script.innerHTML = \"" + javascriptCode + "\";document.head.appendChild(script);overrideclicks();";
		wv.loadUrl(inject);
	}
	
	
	@Override
	public void onPageFinished(WebView view, String url){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onPageFinished(view, url);
		}
		// robotium indexes by shown views.
		if (view.isShown()) {
			try {
				String happyUrl = StringUtils.escapeString(url, "\"\'", '\\');
				String message = RecordListener.getDescription(view) + ",\"" + happyUrl + "\"";
				mEventRecorder.writeRecord(Constants.EventTags.ON_PAGE_FINISHED, mActivityName, view, message);
				RecordWebViewClient.this.traverse(mContext, view);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, view, " on pageStarted");
			}	
		}
		
	}
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onPageStarted(view, url, favicon);
		}
		// robotium indexes by shown views.
		if (view.isShown()) {
			try {
				String happyUrl = StringUtils.escapeString(url, "\"\'", '\\');
				String message = RecordListener.getDescription(view) + "," + happyUrl;
				mEventRecorder.writeRecord(Constants.EventTags.ON_PAGE_STARTED, mActivityName, view, message);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, view, "on pageStarted");
			}	
		}
	}
	
	public RecordWebViewClient(EventRecorder eventRecorder, WebViewClient originalWebViewClient) {
		mEventRecorder = eventRecorder;
		mOriginalWebViewClient = originalWebViewClient;
	}

	
	public Object getOriginalListener() {
		return mOriginalWebViewClient;
	}
	
	@Override
	public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.doUpdateVisitedHistory(view, url, isReload);
		}
	}
	
	@Override
	public void onFormResubmission(WebView view, Message dontResend, Message resend) {
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onFormResubmission(view, dontResend, resend);
		}
		
	}
	
	@Override
	public void onLoadResource(WebView view, String url){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onLoadResource(view, url);
		}
		
	}
	
	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedError(view, errorCode, description, failingUrl);
		}		
		// robotium indexes by shown views.
		if (view.isShown()) {
			try {
				mEventRecorder.writeRecord(Constants.EventTags.ON_RECEIVED_ERROR, mActivityName, view, RecordListener.getDescription(view));
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, view, "on onReceivedError");
			}
		}
	}
	
	@Override
	public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
		}	
	}
	
	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedSslError(view, handler, error);
		}
		
	}
	
	@Override
	public void onScaleChanged(WebView view, float oldScale, float newScale){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onScaleChanged(view, oldScale, newScale);
		}
		// robotium indexes by shown views.
		if (view.isShown()) {
	
			try {
				String scales = Float.toString(oldScale) + "," + Float.toString(newScale);
				String message = RecordListener.getDescription(view) + "," + scales;
				mEventRecorder.writeRecord(Constants.EventTags.ON_SCALE_CHANGED, mActivityName, view, message);
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, view, "on onReceivedError");
			}
		}
	}
	
	@Override
	public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
		}
		
	}
	
	@Override
	public void onUnhandledKeyEvent(WebView view, KeyEvent event){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onUnhandledKeyEvent(view, event);
		}
	}

	@Override
	public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event){
		if (mOriginalWebViewClient != null) {
			return mOriginalWebViewClient.shouldOverrideKeyEvent(view, event);
		}
		return false;
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.shouldOverrideUrlLoading(view, url);
		}
		return false;	
	}

}
