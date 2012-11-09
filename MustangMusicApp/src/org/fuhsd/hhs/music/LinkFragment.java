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
    
    private static final String LOG_MODULE = "LinkFragment";
    WebView webView;
    
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
        String linkUrl = getArguments().getString(Constants.KEY_LINK_URL);
        if (linkUrl != null) {
            loadUrl(linkUrl);
        }
    }
    
    /**
     * Loads the given url in web view.
     */
    public void loadUrl(String url) {
        
        /* Not reqd
        webView.clearCache(true); // TODO: ??
        webView.clearHistory();   // TODO: ??
        */
        webView.getSettings().setJavaScriptEnabled(true);
        
        webView.getSettings().setRenderPriority(RenderPriority.HIGH);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);  
        webView.addJavascriptInterface(this, "Android");
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setVerticalScrollBarEnabled(true);
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
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
        });

        webView.loadUrl(url);
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
    
}
