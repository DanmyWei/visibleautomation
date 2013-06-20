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
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** we can't derive from RecordListener, because we extend WebViewClient
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */

public class RecordWebViewClient extends WebViewClient implements IOriginalListener {
	protected WebViewClient 	mOriginalWebViewClient;
	protected EventRecorder 	mEventRecorder;				// handle to the recorder 

	public RecordWebViewClient(EventRecorder eventRecorder, WebView webView) {
		mEventRecorder = eventRecorder;
		try {
			mOriginalWebViewClient = ListenerIntercept.getWebViewClient(webView);
			webView.setWebViewClient(this);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, webView, "create on pageStarted listener");
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
		try {
			String happyUrl = StringUtils.escapeString(url, "\"\'", '\\');
			String message = RecordListener.getDescription(view) + ",\"" + happyUrl + "\"";
			mEventRecorder.writeRecord(Constants.EventTags.ON_PAGE_FINISHED, view, message);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, view, " on pageStarted");
		}		
		
	}
	public void onPageStarted(WebView view, String url, Bitmap favicon){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onPageStarted(view, url, favicon);
		}
		try {
			String happyUrl = StringUtils.escapeString(url, "\"\'", '\\');
			String message = RecordListener.getDescription(view) + "," + happyUrl;
			mEventRecorder.writeRecord(Constants.EventTags.ON_PAGE_STARTED, view, message);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, view, "on pageStarted");
			ex.printStackTrace();
		}		
	}
	
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedError(view, errorCode, description, failingUrl);
		}		
		try {
			mEventRecorder.writeRecord(Constants.EventTags.ON_RECEIVED_ERROR, view, RecordListener.getDescription(view));
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, view, "on onReceivedError");
			ex.printStackTrace();
		}
	}
	
	public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
		}
		
	}
	public void onReceivedLoginRequest(WebView view, String realm, String account, String args){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedLoginRequest(view, realm, account, args);
		}
		
	}
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedSslError(view, handler, error);
		}
		
	}
	public void onScaleChanged(WebView view, float oldScale, float newScale){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onScaleChanged(view, oldScale, newScale);
		}
		try {
			String scales = Float.toString(oldScale) + "," + Float.toString(newScale);
			String message = RecordListener.getDescription(view) + "," + scales;
			mEventRecorder.writeRecord(Constants.EventTags.ON_SCALE_CHANGED, view, message);
		} catch (Exception ex) {
			mEventRecorder.writeException(ex, view, "on onReceivedError");
			ex.printStackTrace();
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
	public WebResourceResponse shouldInterceptRequest(WebView view, String url){
		if (mOriginalWebViewClient != null) {
			return mOriginalWebViewClient.shouldInterceptRequest(view, url);
		} else {
			return null;
		}
		
	}
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
