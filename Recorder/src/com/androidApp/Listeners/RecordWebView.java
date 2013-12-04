package com.androidApp.Listeners;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.androidApp.EventRecorder.ListenerIntercept;
import com.androidApp.Utility.FileUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

// class to record a web view.  It uses the javascript library traverse.js, which adds listeners using the HTML DOM
// TODO: add a timer to replace the listeners when the HTML changes. It would be nice to get a DOM event, which is
// specified in HTML5, and while that is available in chrome, it isn't available in the webview (sad face)
public class RecordWebView extends WebViewClient {
	public String 			TAG = "RecordWebView";
	public String 			TRAVERSE_JS = "traverse.js";
	protected Context 		mContext;
	protected WebViewClient	mOriginalWebViewClient;
	
	public static boolean InterceptWebView(WebView wv) throws NoSuchFieldException, SecurityException, IllegalAccessException, ClassNotFoundException {
		WebViewClient originalWebViewClient = ListenerIntercept.getWebViewClient(wv);
		RecordWebView recordWebView = new RecordWebView(wv.getContext(), originalWebViewClient);
		wv.setWebViewClient(recordWebView);
		return originalWebViewClient != null;
	}
	
	
	public RecordWebView(Context context, WebViewClient originalWebViewClient) {
		mContext = context;
		mOriginalWebViewClient = originalWebViewClient;
	}
	
	public void onPageFinished(WebView wv, String url) {
		try {
			/* for debugging only
			Thread thread = new Thread(new GetHtmlRunnable(url));
			thread.start();
			*/
			RecordWebView.this.traverse(mContext, wv);
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}
	
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		Log.i(TAG, "code = " + errorCode + "error = " + description);
	}
	
	public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.doUpdateVisitedHistory(view, url, isReload);
		}
	}
	public void  onFormResubmission(WebView view, Message dontResend, Message resend){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onFormResubmission(view, dontResend, resend);
		}
	}
	public void  onLoadResource(WebView view, String url){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onLoadResource(view, url);
		}
	}
	public void  onPageStarted(WebView view, String url, Bitmap favicon){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onPageStarted(view, url, favicon);
		}
	}
	public void  onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
		}
	}

	public void  onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onReceivedSslError(view, handler, error);
		}
	}
	
	public void  onScaleChanged(WebView view, float oldScale, float newScale){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onScaleChanged(view, oldScale, newScale);
		}
	}
	public void  onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
		}
	}
	public void  onUnhandledKeyEvent(WebView view, KeyEvent event){
		if (mOriginalWebViewClient != null) {
			mOriginalWebViewClient.onUnhandledKeyEvent(view, event);
		}
	}
	
	public boolean  shouldOverrideKeyEvent(WebView view, KeyEvent event){
		if (mOriginalWebViewClient != null) {
			return mOriginalWebViewClient.shouldOverrideKeyEvent(view, event);
		}
		return false;
	}
	public boolean  shouldOverrideUrlLoading(WebView view, String url){
		if (mOriginalWebViewClient != null) {
			return mOriginalWebViewClient.shouldOverrideUrlLoading(view, url);
		}
		return false;
	}
	
	public class WebViewTouchListener implements View.OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			WebView webView = (WebView) v;
			WebView.HitTestResult hitTestResult = webView.getHitTestResult();
			Log.i(TAG, Integer.toString(hitTestResult.getType()) + " : " + hitTestResult.getExtra());
			return true;
		}	
	}

	// given a webview, inject the traverse.js code which intercepts the click listeners to show the 
    // fully qualified paths of the clicked HTML elements.
	
	public void traverse(Context context, WebView wv) throws IOException {
		String javascriptCode = FileUtils.readJarResourceString(RecordWebView.class, TRAVERSE_JS);
		String inject = "javascript:var script = document.createElement(\"script\"); script.innerHTML = \"" + javascriptCode + "\";document.head.appendChild(script);overrideclicks();";
		wv.loadUrl(inject);
	}

	/**
         * javascript bridge object which actually records events.  The element definition is 	
 	 */
	public class JsObject {
		protected String mElementDefinition;
		protected String mEventName;
		
		public JsObject() {
			mElementDefinition = null;
			mEventName = null;
		}
		
		public String getElementDefinition() {
			return mElementDefinition;
		}
		
		public String getEventName() {
			return mEventName;
		}
		
		public void recordEvent(String elementDefinition, String event) {
			mElementDefinition = elementDefinition;
			mEventName = event;
			Log.i(TAG, "definition = " + elementDefinition + " event = " + event);
		}
	}
	
	public String getHtml(String url) {
	    HttpClient vClient = new DefaultHttpClient();
	    HttpGet vGet = new HttpGet(url);
	    String response = "";    

	    try {
	        ResponseHandler<String> vHandler = new BasicResponseHandler();
	        response = vClient.execute(vGet, vHandler);
	        Log.i(TAG, "response = " + response);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return response;
	}
	
	public class GetHtmlRunnable implements Runnable {
		public String mUrl;
		
		public GetHtmlRunnable(String url) {
			mUrl = url;
		}
		
		public void run() {
			String html = getHtml(mUrl);
			Log.i(TAG, "html = " + html);
		}
	}

}
