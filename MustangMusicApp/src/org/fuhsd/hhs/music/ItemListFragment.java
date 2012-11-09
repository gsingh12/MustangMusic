package org.fuhsd.hhs.music;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

import org.fuhsd.hhs.music.R;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * This is a generic List Fragment that shows the list of select-able Items.
 * It handles BLASTS and PHOTOS tabs for Mustang Music App.
 */
public class ItemListFragment extends SherlockListFragment /*Fragment*/ {
    
    protected List<Item> itemList = null;
    protected LoadingItemListTask lilTask;

    private static final String LOG_MODULE = "ItemListFragment";
    
    /**
     * Instance of this Fragment is created by the main activity.
     */
    public static ItemListFragment newInstance(int keyActivity) {
        ItemListFragment frag = new ItemListFragment();
        Bundle bdl = new Bundle(1);
        bdl.putInt(Constants.KEY_TAB, keyActivity);
        frag.setArguments(bdl);
        return frag;
    }
    
    /**
     * On creating View, it inflates the xml for this fragment.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (View) inflater.inflate(R.layout.fragment_itemlist, container, false);
    }

    /**
     * On activity created, it shows the list of items (that is computing in an asynchronous task).
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int keyActivity = getArguments().getInt(Constants.KEY_TAB);
        if (keyActivity != -1) {
            showList(getView(), keyActivity);
        }
    }
    
    public void showList(int keyActivity) {
        showList(getView(), keyActivity);
    }

    /**
     * List is procured in an asynchronous task, and then displayed using a Lst adapter.
     * @param v
     * @param keyActivity
     */
    public void showList(View v, int keyActivity) {
        
        // Create an item list adapter
        ItemListAdapter listAdapter = new ItemListAdapter(keyActivity);
        
        // Create asynchronous task to fetch Items
        // lilTask = new LoadingItemListTask(getView().getContext(), listAdapter);
        // lilTask = new LoadingItemListTask(v.getContext(), listAdapter);
        lilTask = createLoadingItemListTask(v.getContext(), listAdapter);
        
        // Execute background task
        List currItemist = (List) null; // TODO: getLastNonConfigurationInstance();
        String[]  urls = null;
        switch (keyActivity) {
        case Constants.ACT_BLASTS:
            //setTitle(R.string.title_activity_blasts);
            if (currItemist == null) {
                urls = new String[] {Constants.URL_BLASTS, ParseNewsLinks.class.getName()};
                lilTask.execute(urls);
            }
            break;
        case Constants.ACT_CALENDAR: // TBR
            //setTitle(R.string.title_activity_calendar);
            if (currItemist == null) {
                urls = new String[] {Constants.URL_CALENDAR, ParsePhotoLinks.class.getName()}; // ??
                lilTask.execute(urls);
            }
            break;
        case Constants.ACT_PHOTOS:
            //setTitle(R.string.title_activity_photos);
            if (currItemist == null) {
                urls = new String[] {Constants.URL_PHOTOS, ParsePhotoLinks.class.getName()};
                lilTask.execute(urls);
            }
            break;
        }

        /* TBR
        String url = urls[0];
        String parserClassName = urls[1];
        Parser parser = null;
        try {
            parser = (Parser) Class.forName(parserClassName).newInstance();
        } catch (Exception e) {
            return; // todo
        }
        itemList = new HttpRequests().returnLinkData(url, parser);
        */
        
        // Set List Adapter
        setListAdapter(listAdapter);
        // TBR setAdapter(v, listAdapter);
    }
    
    /**
     * If visible toggles the Home Caret appropriately.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            ((MainActivity) getActivity()).toggleHomeUpCaret(false);
            ((MainActivity) getActivity()).isHomeTab = false;
        } else {
        }
    }

    protected LoadingItemListTask createLoadingItemListTask(Context context, BaseAdapter adapter) {
        return new LoadingItemListTask(context, adapter);
    }

    /**
     * On clicking any utem in the lists display it in the ItemLinkActivity
     */
    public void onListItemClick(ListView paramListView, View paramView, int index, long paramLong) {
        
        Intent intent = new Intent(getView().getContext(), ItemLinkActivity.class);
        // Intent intent = new Intent(getView().getContext(), LinkActivity.class);
        
        Item item = (Item)itemList.get(index);
        String itemLink = item.getLink();
        // Log.d(LOG_MODULE, "Item Link="+itemLink);

        // Ask back the MainActivity to start a new activity.
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_ITEM_LINK_URL, itemLink);
        /* TBR
        bundle.putString(Constants.KEY_ITEM_LINK_URL, 
        "https://www.google.com/calendar/embed?showTitle=0&showPrint=0&showTabs=0&showCalendars=0&showTz=0&mode=AGENDA&width=300&height=300&wkst=1&src=eieiglnt66gtoi9aspvj38dlv4@group.calendar.google.com&ctz=America/Los_Angeles&color=#406627&bgcolor=#ccf29b");
        */

        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    public Object onRetainNonConfigurationInstance() {
        return itemList;
    }

    /**
     * Item List Adapter
     */
    protected class ItemListAdapter extends BaseAdapter {
        private int keyActivity;
        private ItemListAdapter(int keyActivity) {
            this.keyActivity = keyActivity;
        }

        public int getCount() {
            // Q: Is this the correct place to find the end of lilTask and get the results. 
            itemList = lilTask.returnList();
            if (itemList != null) {
                return itemList.size();
            }
            return 0;
        }

        public Object getItem(int index) {
            return ((Item)itemList.get(index)).getName();
        }

        public long getItemId(int paramInt) {
            return 0L;
        }

        public View getView(int index, View convertView, ViewGroup parent) {
            View row = convertView;
            
            if (row == null) {
                // Inflate view from item.xml or item_withdate.xml
                if (keyActivity == Constants.ACT_BLASTS) {
                    row = LayoutInflater.from(getActivity()).inflate(R.layout.item, null);
                } else if (keyActivity == Constants.ACT_CALENDAR) { // ??
                    row = LayoutInflater.from(getActivity()).inflate(R.layout.item_withdate, null);
                } else if (keyActivity == Constants.ACT_PHOTOS) {
                    row = LayoutInflater.from(getActivity()).inflate(R.layout.item, null);
                }
            }
            // Fill in values in this view
            Item item = (Item)itemList.get(index);
            ((TextView)row.findViewById(R.id.text)).setText(item.getName());
            if (keyActivity == Constants.ACT_CALENDAR) {
                ((TextView)row.findViewById(R.id.date)).setText("Date: " + item.getDate());
            }
            ((TextView)row.findViewById(R.id.arrows)).setText(">");
            
            return row;
        }
    }
}
