package org.fuhsd.hhs.music;

import org.fuhsd.hhs.music.R;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.RenderPriority;
import android.widget.RelativeLayout;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.Uri;
import android.net.http.SslError;

/**
 * This activity displays the link for the item selected from the ItemList.
 * It displays all the links clicked from it, also in the same web view.
 */
public class ItemLinkActivity extends SherlockActivity {
    private static final String LOG_MODULE = "ItemLinkActivity";
    private String url = null;
    private RelativeLayout loadingContainer = null;
    private ShareActionProvider shareActionProvider;
    private boolean isShare = false;

    private WebView webView;
    
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_itemlink);
        
        loadingContainer = (RelativeLayout) findViewById(R.id.loadingContainer);

        webView = ((WebView)findViewById(R.id.webview));
        /* Not reqd
        webView.clearCache(true); // TODO: ??
        webView.clearHistory();   // TODO: ??
        */
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(RenderPriority.HIGH);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);  // Not LOAD_CACHE_ELSE_NETWORK
        webView.getSettings().setAppCacheEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);  
        webView.getSettings().setLightTouchEnabled(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setPluginsEnabled(true);
        
        /* Not reqd
        webView.addJavascriptInterface(this, "Android");
        webView.getSettings().setDomStorageEnabled(true);
        */

        webView.setWebViewClient(new WebViewClient() {
            /* Open all links inside this activity, except for Videos */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.endsWith(".mp4")){
                    Intent in = new Intent (Intent.ACTION_VIEW , Uri.parse(url));
                    startActivity(in);
                    return true;
                } else {
                    Log.i(LOG_MODULE, "url | " + url);
                    setUrl(url);
                    view.loadUrl(url);
                    return true;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loadingContainer.bringToFront();
                loadingContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadingContainer.setVisibility(View.GONE);
                if(isShare) {
                    setShareIntent();
                }
                
                setUrl(webView.getUrl());
                Log.i(LOG_MODULE, "onPageFinished url: " + url);
            }

            // This is required otherwise blasts items do not show up.
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
        });

        if(getIntent().getExtras() != null) {
            url = getIntent().getExtras().getString(Constants.KEY_ITEM_LINK_URL);
            if(url != null){
                webView.loadUrl(url);
            }

            if(getIntent().hasExtra("share")) {
                isShare  = getIntent().getExtras().getBoolean(Constants.KEY_SHARE);
            }
            
            if(getIntent().hasExtra("title")) {
                if(getIntent().getExtras().getString(Constants.KEY_TITLE) != null) {
                    setTitle(getIntent().getExtras().getString("title"));
                }
            }
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
    
    private void setUrl(String url) {
        this.url = url;
    }

    private void setShareIntent() {
        if(url != null) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, url);

            if(shareActionProvider != null) {
                shareActionProvider.setShareIntent(sharingIntent);
            }
        }
    }

    /**
     * This helps the Up caret to work correctly 
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            setResult(RESULT_CANCELED, new Intent());
            finish();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }       
    }
    
    /**
     * Back pressing takes back to pevious url in web view.
     */
    @Override
    public void onBackPressed() {
        Log.d(LOG_MODULE, "onBackPressed: " + webView.getUrl() + ", " + webView.canGoBack());
        try {
            if(webView.canGoBack()) {
                webView.goBack();
                setUrl(webView.getUrl());
                Log.i(LOG_MODULE, "onBackPreassed");
            } else {
                finish();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }       
    }

}
