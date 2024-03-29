package org.fuhsd.hhs.music;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;

import org.fuhsd.hhs.music.R;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * This is a generic Fragment that shows the given link or url.
 * It handles CALENDAR and SOCIAL tabs for Mustang Music App.
 */
public class LinkFragment extends SherlockFragment {
    
    protected static final String LOG_MODULE = "LinkFragment";
    protected WebView webView;
    protected long lastRefreshTime = 0;
    
    /**
     * Instance of this Fragment is created by the main activity.
     */
    public static LinkFragment newInstance(String linkUrl) {
        LinkFragment frag = new LinkFragment();
        Bundle bdl = new Bundle(1);
        bdl.putString(Constants.KEY_LINK_URL, linkUrl);
        frag.setArguments(bdl);
        return frag;
    }
    
    /**
     * On creating View, it inflates the xml for this fragment.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = (View) inflater.inflate(R.layout.fragment_link, container, false);
        webView = (WebView)view.findViewById(R.id.webview);
        return view;
    }

    /**
     * On activity created, it loads the given url.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (savedInstanceState != null) {
            lastRefreshTime = savedInstanceState.getLong("lastRefreshTime");
        }
        if (lastRefreshTime == 0) {
            Log.d(LOG_MODULE, "lastRefreshTims is found 0");
            lastRefreshTime = System.currentTimeMillis();
        }
        Log.d(LOG_MODULE, "lastRefreshTime: " + lastRefreshTime);
        
        String linkUrl = getArguments().getString(Constants.KEY_LINK_URL);
        if (linkUrl != null) {
            loadUrl(linkUrl);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("lastRefreshTime", lastRefreshTime);
        super.onSaveInstanceState(outState);
    }
    
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        Log.d(LOG_MODULE, "LinkFragment Refresh request came in");
        long currTime = System.currentTimeMillis();
        if (currTime > (lastRefreshTime + Constants.LINK_EXPIRY_MILLIS)) {
            // webView.loadUrl(webView.getUrl());
            Log.i(LOG_MODULE, "LinkFragment RELOADING");
            webView.reload();
            lastRefreshTime = currTime;
        }
    }
    
    /**
     * Loads the given url in web view.
     */
    public void loadUrl(String url) {
        
        webView.clearCache(true); // TODO: ??
        /* Not reqd
        webView.clearHistory();   // TODO: ??
        */
        webView.getSettings().setJavaScriptEnabled(true);
        
        webView.getSettings().setRenderPriority(RenderPriority.HIGH);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);  
        webView.addJavascriptInterface(this, "Android");
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // webView.getSettings().setLightTouchEnabled(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);     // This help load a bigger page so person can scroll down.
        // webView.getSettings().setLoadWithOverviewMode(true);  // Don't use this, else it looks too small on calendar and only little bit of data.
        webView.getSettings().setPluginsEnabled(true);

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            /* Display links in separate browser, as backPressed is not supported for fragments */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent in = new Intent (Intent.ACTION_VIEW , Uri.parse(url));
                startActivity(in);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
            /*
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
            */
        });

        webView.loadUrl(url);
        lastRefreshTime = System.currentTimeMillis();
    }
    
    /**
     * If visible toggles the Home Caret appropriately.
     * (Loads the url again ??)
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if(webView != null) webView.loadUrl("javascript:onBeforeActive()");
            if(webView != null) webView.loadUrl("javascript:onAfterActive()");
            ((MainActivity) getActivity()).toggleHomeUpCaret(false);
            ((MainActivity) getActivity()).isHomeTab = false;
        } else {
        }
    }
    
    /* TBR
    public void onStart() {
        super.onStart();
        Log.d(LOG_MODULE, "*** onStart" + getTag());
    }

    public void onResume() {
        super.onResume();
        Log.d(LOG_MODULE, "*** onResume" + getTag());
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(LOG_MODULE, "*** onAttach" + getId());
    }
    public void onPause() {
        super.onPause();
        Log.d(LOG_MODULE, "*** onPause" + getTag());
    }

    public void onStop() {
        super.onStop();
        Log.d(LOG_MODULE, "*** onStop" + getTag());
    }
    */

}
