package org.fuhsd.hhs.music;

import java.io.PrintStream;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * Executes Http request with against external servers that normally provides RSS feeds.
 * It using a sepcified parser to parse the response and returns the list of items.
 */
public class HttpRequests {
    
    private static final String LOG_MODULE = "HttpRequests";

    public List<Item> returnLinkData(String url, Parser parser) {
        // Log.d(LOG_MODULE, "*** returnLinkData(): " + url);

        DefaultHttpClient defHttpClient = new DefaultHttpClient();
        HttpProtocolParams.setUseExpectContinue(defHttpClient.getParams(), false);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "text/xml; charset=utf-8");
        HttpConnectionParams.setConnectionTimeout(defHttpClient.getParams(), 20000);
        
        List<Item> list = null;
        try {
            // Execute hhtp get
            HttpResponse response = defHttpClient.execute(httpGet);

            // Get response and parse it also
            if (response != null) {
                String str = EntityUtils.toString(response.getEntity());
                parser.setContent(str);
                list = parser.parseXML();
                
                // Log.d(LOG_MODULE, "*** this.d in Httprequest after parse: " + list);
            }
        } catch (Exception ex) {
                Log.e("hhsmusic", "*** ex" + ex);
                ex.printStackTrace();
                list = null;
        }
        return list;
    }
}
