package org.fuhsd.hhs.music;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Toast;
import java.util.LinkedList;
import java.util.List;

/**
 * Async Task for loading items from a given url.
 * It takes help from HttpRequests calls get content from the given url and then parse it using given parser.
 *
 */
public class LoadingItemListTask extends AsyncTask<String, Integer, Integer> {
    
    private static final String LOG_MODULE = "LoadingItemListTask";

    protected Context context;
    protected BaseAdapter adapter;
    
    protected ProgressDialog dialog;
    protected List<Item> itemList = null;

    /**
     */
    LoadingItemListTask(Context context, BaseAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    protected void onPreExecute() {
        /*
        dialog = ProgressDialog.show(context, "", "Loading. Please wait...", true);
        dialog.setCancelable(true);
        */
    }

    /**
     * Gets that parameters for url and parser
     */
    protected Integer doInBackground(String[] params) {
        // Get item List by calling Http Request
        
        String url = params[0];
        String parserClassName = params[1];
        Parser parser = null;
        try {
            parser = (Parser) Class.forName(parserClassName).newInstance();
        } catch (Exception e) {
            // TODO
            return Integer.valueOf(-1);
        }
        
        itemList = new HttpRequests().returnLinkData(url, parser);
        // Log.d(LOG_MODULE, "*** lcl: itemList" + itemList);
        
        if (this.itemList == null) {
            return Integer.valueOf(-1);
        } else {
            return Integer.valueOf(1);
        }
    }

    /**
     * At the end of the task, notify the adapter
     */
    protected void onPostExecute(Integer paramInteger) {
        // dialog.cancel();
        
        switch (paramInteger.intValue()) {
        case -1:
            Toast.makeText(context, "Connection cannot be established", 100000).show();
            break;
        case 1:
            this.adapter.notifyDataSetChanged();
            break;
        }
    }

    /**
     * Provides the retrieved list of items
     * @return
     */
    public List<Item> returnList() {
        return itemList;
    }
}
