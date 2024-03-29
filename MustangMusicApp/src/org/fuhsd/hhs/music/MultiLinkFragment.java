package org.fuhsd.hhs.music;

import android.text.Html;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;

import org.fuhsd.hhs.music.R;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * This is a generic Fragment that shows a fragment that contains multiple (2) web views to show different links/urls.
 * It handles HOME tab for Mustang Music App. It also handles call and email buttons on the footer.
 */
public class MultiLinkFragment extends SherlockFragment {

    // Log Module
    private final String LOG_MODULE = "MultiLinkFragment";
    
    protected WebView webView1;
    protected WebView webView2;
    protected long lastRefreshTime = 0;
    
    /**
     * Instance of this Fragment is created by the main activity.
     */
    public static MultiLinkFragment newInstance(String[] linkUrls) {
        MultiLinkFragment frag = new MultiLinkFragment();
        Bundle bdl = new Bundle(1);
        bdl.putStringArray(Constants.KEY_LINK_URL, linkUrls);
        frag.setArguments(bdl);
        return frag;
    }
    
    /**
     * On creating View, it inflates the xml for this fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = (View) inflater.inflate(R.layout.fragment_multilink, container, false);
        webView1 = (WebView) view.findViewById(R.id.webview1);
        webView2 = (WebView) view.findViewById(R.id.webview2);
        return view;
    }

    /**
     * On activity created, it loads multiple urls.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (savedInstanceState != null) {
            lastRefreshTime = savedInstanceState.getLong("lastRefreshTime");
        }
        if (lastRefreshTime == 0) {
            lastRefreshTime = System.currentTimeMillis();
        }

        String[] linkUrls = getArguments().getStringArray(Constants.KEY_LINK_URL);
        if (linkUrls != null) {
            loadUrl(getView(), linkUrls);
        }
        TextView tvFooterText = (TextView)getView().findViewById(R.id.footertext);
        tvFooterText.setText(Html.fromHtml(getString(R.string.footertext)));
        
        // Call button on footer
        Button btnCall = (Button) getView().findViewById(R.id.btnCall);
        btnCall.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String toDial="tel:" + getString(R.string.mm_tel);
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(toDial)));
            }
        });

        // Email button on Footer
        Button btnEmail = (Button) getView().findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                String[] recipients = new String[]{getString(R.string.mm_email_recipient), "",};
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mm_email_subject));
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mm_email_text));
                emailIntent.setType("text/plain");
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        });

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
        Log.d(LOG_MODULE, "MultiLinkFragment Refresh request came in");
        long currTime = System.currentTimeMillis();
        if (currTime > (lastRefreshTime + Constants.LINK_EXPIRY_MILLIS)) {
            // webView.loadUrl(webView.getUrl());
            Log.i(LOG_MODULE, "MultiLinkFragment RELOADING");
            webView2.reload();
            lastRefreshTime = currTime;
        }
    }
    
    public void loadUrl(String[] urls) {
        loadUrl(getView(), urls);
    }
    
    public void loadUrl(View v, String[] urls) {
        
        /*
        Log.d(LOG_MODULE, "urls[0]" + urls[0]);
        if (urls.length > 1) {
            Log.d(LOG_MODULE, "urls[1]" + urls[1]);
        }
        */

        //  Load first url into first web view
        /* Not reqd
        webView1.clearCache(true); // TODO: ??
        webView1.clearHistory();   // TODO: ??
        */
        
        webView1.getSettings().setJavaScriptEnabled(true);
        webView1.getSettings().setRenderPriority(RenderPriority.HIGH);
        webView1.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);  
        webView1.addJavascriptInterface(this, "Android");
        webView1.getSettings().setDomStorageEnabled(true);
        webView1.getSettings().setAppCacheEnabled(true);
        webView1.getSettings().setSupportZoom(false);
        webView1.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView1.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
            
        });
        webView1.setWebViewClient(new WebViewClient(){
            /* TBR
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
            */
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                Log.d(LOG_MODULE, "onPageFinished");
                ((MainActivity)getActivity()).finishLoad(null);
            }
        });

        if (urls.length > 0) {
            webView1.loadUrl(urls[0]);
        }

        //  Load second url into second web view
        
        webView2.clearCache(true);
        /* Not reqd
        webView2.clearHistory();   // TODO: ??
        */
        webView2.getSettings().setJavaScriptEnabled(true);

        webView2.getSettings().setRenderPriority(RenderPriority.HIGH);
        webView2.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);  
        webView2.addJavascriptInterface(this, "Android");

        webView2.getSettings().setDomStorageEnabled(true);
        webView2.getSettings().setAppCacheEnabled(true);
        webView2.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // webView2.getSettings().setLightTouchEnabled(false);
        webView2.getSettings().setSupportZoom(false);
        webView2.getSettings().setUseWideViewPort(true);
        // webView2.getSettings().setLoadWithOverviewMode(true);
        webView2.getSettings().setPluginsEnabled(true);

        webView2.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
        webView2.setWebViewClient(new WebViewClient() {
            /* Display links in separate browser */
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

        if (urls.length > 1) {
            webView2.loadUrl(urls[1]);
            lastRefreshTime = System.currentTimeMillis();
        }
    }

    /**
     * If visible toggles the Home Caret appropriately.
     * (Loads the url again ??)
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if(webView1 != null) webView1.loadUrl("javascript:onBeforeActive()");
            if(webView1 != null) webView1.loadUrl("javascript:onAfterActive()");
            if(webView2 != null) webView2.loadUrl("javascript:onBeforeActive()");
            if(webView2 != null) webView2.loadUrl("javascript:onAfterActive()");
            ((MainActivity) getActivity()).toggleHomeUpCaret(true);
            ((MainActivity) getActivity()).isHomeTab = true;
        } else {
        }
    }
    
    /* TBR
    public void callNative(String jsonString){
        if((MainActivity) getActivity() != null) {
            ((MainActivity) getActivity()).callNative(jsonString);
        }
    }
    */
}
