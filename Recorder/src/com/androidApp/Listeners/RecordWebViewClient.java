package com.androidApp.Listeners;

import java.net.URLDecoder;

import com.androidApp.EventRecorder.EventRecorder;
import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.Constants;
import com.androidApp.Utility.StringUtils;

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

	public RecordWebViewClient(String activityName, EventRecorder eventRecorder, WebView webView) {
		mEventRecorder = eventRecorder;
		mActivityName = activityName;
		try {
			mOriginalWebViewClient = ListenerIntercept.getWebViewClient(webView);
			webView.setWebViewClient(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, activityName, webView, "create on pageStarted listener");
		}		
	}
	
	
	public RecordWebViewClient(EventRecorder eventRecorder, WebViewClient originalWebViewClient) {
		mEventRecorder = eventRecorder;
		mOriginalWebViewClient = originalWebViewClient;
	}

	
	public Object getOriginalListener() {
		return mOriginalWebViewClient;
	}
	
	public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.doUpdateVisitedHistory(view, url, isReload);
		}
	}
	public void onFormResubmission(WebView view, Message dontResend, Message resend) {
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onFormResubmission(view, dontResend, resend);
		}
		
	}
	public void onLoadResource(WebView view, String url){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onLoadResource(view, url);
		}
		
	}
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
			} catch (Exception ex) {
				mEventRecorder.writeException(ex, mActivityName, view, " on pageStarted");
			}	
		}
		
	}
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
	
	public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
		}	
	}
	
	/* TODO: version problem?
	public void onReceivedLoginRequest(WebView view, String realm, String account, String args){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedLoginRequest(view, realm, account, args);
		}
		
	}
	*/
	
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedSslError(view, handler, error);
		}
		
	}
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
	
	public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
		}
		
	}
	
	public void onUnhandledKeyEvent(WebView view, KeyEvent event){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onUnhandledKeyEvent(view, event);
		}
	}
	
	/* TODO: version?
	public WebResourceResponse shouldInterceptRequest(WebView view, String url){
		if (mOriginalWebViewClient != null) {
			return mOriginalWebViewClient.shouldInterceptRequest(view, url);
		} else {
			return null;
		}
	}
	*/
	public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event){
		if (mOriginalWebViewClient != null) {
			return mOriginalWebViewClient.shouldOverrideKeyEvent(view, event);
		}
		return false;
	}
	
	public boolean shouldOverrideUrlLoading(WebView view, String url){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.shouldOverrideUrlLoading(view, url);
		}
		return false;	
	}
}
