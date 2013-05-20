package com.androidApp.util;

import java.net.URLDecoder;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * wrapper for web view clients so we can wait on web view events.
 * @author Matthew
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class InterceptWebViewClient extends WebViewClient {
	protected WebViewClient mOriginalWebViewClient;
	protected Object mWaitObject;
	
	public InterceptWebViewClient(WebViewClient originalWebViewClient) {
		mOriginalWebViewClient = originalWebViewClient;
		mWaitObject = new Object();
	}
	
	/**
	 * wait for a page to load.
	 * @param timeoutMsec
	 * @return
	 */
	public boolean waitForPageLoad(String url, long timeoutMsec) {
		try {
			synchronized(mWaitObject) {
				mWaitObject.wait(timeoutMsec);
			}
		} catch (InterruptedException iex) {
			return false;
		}
		return true;
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
		synchronized(mWaitObject) {
			mWaitObject.notify();
		}
	}
	
	public void onPageStarted(WebView view, String url, Bitmap favicon){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onPageStarted(view, url, favicon);
		}
	}
	
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedError(view, errorCode, description, failingUrl);
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
