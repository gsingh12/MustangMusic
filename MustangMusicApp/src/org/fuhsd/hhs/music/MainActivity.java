package org.fuhsd.hhs.music;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;

import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This is the Main Activity for the <b>Mustang Music</b> app.
 * It hosts multiple fragments with a Tab mechanism for switching between these.
 */
public class MainActivity extends SherlockFragmentActivity {

    // Log Module
    private static final String LOG_MODULE = "MainActivity";

    private static final int NUM_ITEMS = 5;
    public ActionBar actionBar;
    boolean isHomeTab = true;
    private boolean isHome  = false;
    
    //
    public final int CALL_NATIVE_HANDLER = 1;

    // splash screen
    private RelativeLayout splashScreen;
    private TextView loadingMessage;
    private ProgressBar progressbar;
    
    // timer
    final long futureTimeInMiliSec = 120*1000;
    final long CountDownIntervalInMiliSec = 1*1000;
    LoadingTimer loadingTimer = null;

    // fragments
    private FragmentManager fragmentManager;  
    private Fragment homeFragment;
    private Fragment calendarFragment;
    private Fragment blastsFragment;
    private Fragment photosFragment;
    private Fragment socialFragment;

    // View Pager
    private MyFragmentAdapter fragmentAdapter;
    private MyViewPager viewPager;
    
    private Boolean creating = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        synchronized(creating) {
            creating = true;
        }
        try {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  // This displays spinner or action bar.
            setContentView(R.layout.main);
            
            // Action Bar hidden for splash screen
            actionBar = getSupportActionBar();
            hideActionBar();
            
            // Splash Screen
            splashScreen = (RelativeLayout) findViewById(R.id.splashScreen);
            loadingMessage = (TextView) findViewById(R.id.loadingMessage);
            // ImageView splashImageView = (ImageView) findViewById(R.id.splashImageView);
            progressbar = (ProgressBar) findViewById(R.id.progressbar);

            // displayHeight = ((WindowManager) _context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
            // displayWidth = ((WindowManager) _context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();

            splashScreen.setVisibility(View.VISIBLE);
            splashScreen.bringToFront();
            loadingMessage.bringToFront();
            progressbar.bringToFront();

            // Setup loading timer
            try {
                while(loadingTimer != null) {
                    loadingTimer.cancel();
                    loadingTimer = null;
                }
                loadingTimer = new LoadingTimer(futureTimeInMiliSec, CountDownIntervalInMiliSec);
                loadingTimer.start();
            } catch (Exception e) {
                if(e != null && e.getMessage() != null) Log.e(LOG_MODULE, e.getMessage());
            }

            // Create all fragments
            fragmentManager = getSupportFragmentManager();
            String[] urls = new String[] {Constants.URL_HOME, Constants.URL_HOME2};
            homeFragment = MultiLinkFragment.newInstance(urls);
            calendarFragment = LinkFragment.newInstance(Constants.URL_CALENDAR);
            blastsFragment = ItemListFragment.newInstance(Constants.ACT_BLASTS);
            photosFragment = ItemListFragment.newInstance(Constants.ACT_PHOTOS);
            socialFragment = LinkFragment.newInstance(Constants.URL_SOCIAL);

            homeFragment.setRetainInstance(true);
            calendarFragment.setRetainInstance(true);
            blastsFragment.setRetainInstance(true);
            photosFragment.setRetainInstance(true);
            socialFragment.setRetainInstance(true);

            // Set view pager and Fragment Adapter
            fragmentAdapter = new MyFragmentAdapter(fragmentManager);
            viewPager = (MyViewPager)findViewById(R.id.pager);
            viewPager.setOffscreenPageLimit(5);    // IMPORTANT
            viewPager.setAdapter(fragmentAdapter);
            
            // Add tabs in Action Bar.
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            // TBR: mActionBar.setDisplayShowTitleEnabled(false);
            
            Tab tab = actionBar.newTab()/*.setText("ONE")*/.setTabListener(new MyTabListener(fragmentAdapter, Constants.ACT_HOME + "", viewPager));
            tab.setCustomView(R.layout.tab_home);
            actionBar.addTab(tab);
            
            tab = actionBar.newTab().setTabListener(new MyTabListener(fragmentAdapter, Constants.ACT_CALENDAR + "", viewPager));
            tab.setCustomView(R.layout.tab_calendar);
            actionBar.addTab(tab);
            
            tab = actionBar.newTab().setTabListener(new MyTabListener(fragmentAdapter, Constants.ACT_BLASTS + "", viewPager));
            tab.setCustomView(R.layout.tab_blasts);
            actionBar.addTab(tab);
            
            tab = actionBar.newTab().setTabListener(new MyTabListener(fragmentAdapter, Constants.ACT_PHOTOS + "", viewPager));
            tab.setCustomView(R.layout.tab_photos);
            actionBar.addTab(tab);
            
            tab = actionBar.newTab().setTabListener(new MyTabListener(fragmentAdapter, Constants.ACT_SOCIAL + "", viewPager));
            tab.setCustomView(R.layout.tab_social);
            actionBar.addTab(tab);

            /* This Does not work
            View title = getWindow().findViewById(android.R.id.title);
            View titleBar = (View) title.getParent();
            titleBar.setBackgroundColor(Color.RED);
            */
            
            synchronized(creating) {
                creating = false;
            }

        } catch (Exception e) {
            Log.e("ViewPager", e.toString());
        }
    }
    
    /**
     * Called when the activity creation is complete.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSupportProgressBarIndeterminateVisibility(false);
    }

    /**
     * When app is detroyed.
     */
    @Override
    protected void onDestroy() {
        /*
        if(receiver != null) {
            getContext().unregisterReceiver(receiver);
        }
        */
        super.onDestroy();

        Log.d(LOG_MODULE, "*** onDestroy");
        // tToast("onDestroy.");
    }

    /**
     * Tab listener than changes the page in View pager
     */
    public class MyTabListener implements ActionBar.TabListener {
        private Fragment mFragment;
        // private final Activity mActivity;
        private FragmentPagerAdapter mFragmentAdapter;
        private final String mTag;
        private ViewPager mPager;

        public MyTabListener(FragmentPagerAdapter fragmentAdapter, String tag, ViewPager pager) {
            // mActivity = activity;
            mFragmentAdapter = fragmentAdapter;
            mTag = tag;
            mPager = pager;
        }

        /* The following are each of the ActionBar.TabListener callbacks */
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            /* This is not required
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }*/

            int tag = Integer.parseInt(mTag);
            mPager.setCurrentItem(tag);
            isHomeTab = (tag == Constants.ACT_HOME);
            
            switch (tag) {
            case Constants.ACT_HOME:
                synchronized(creating) {
                    if (!creating) {
                        ((MultiLinkFragment)mFragmentAdapter.getItem(tag)).refresh();
                    }
                }
                break;
            case Constants.ACT_CALENDAR:
            case Constants.ACT_SOCIAL:
                ((LinkFragment)mFragmentAdapter.getItem(tag)).refresh();
                break;
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            /* This is not required
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
            */
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }
    }

    /**
     * Fragment Pager adapter that displays the correct fragment based on selected tab position.
     */
    public class MyFragmentAdapter extends FragmentPagerAdapter {
        private ActionBar mActionBar;

        private Fragment[] fragments = {homeFragment, calendarFragment, blastsFragment, photosFragment, socialFragment};
        
        public MyFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            //mActionBar.getTabAt(position).select();

            return fragments[position];
            /* TBR
            switch (position) {
            case Constants.ACT_HOME:
                //return ArrayListFragment.newInstance(position);
                //return new LinkFragment(Constants.URL_HOME);
                return fragments[position];
            }
            */
        }
    }

    /**
     * Displays the Home screen
     */
    public void showHomeTab(){
        viewPager.setCurrentItem(Constants.ACT_HOME, true);
        actionBar.setSelectedNavigationItem(Constants.ACT_HOME);
        isHomeTab = true;
    }

    /**
     * This helps the Up caret to work correctly 
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if(!isHome) {
                showHomeTab();
            }
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Back pressing takes back to home tab or closes the app.
     */
    @Override
    public void onBackPressed() {
        if(!isHomeTab){
            showHomeTab();
        } else {
            finish();
        }
    }
    
    /**
     * In future if we add About screen, should will take the user to the previously opened tab or Home tab.
     */
    public void openPreviousTab(HashMap<String, String> hashmap) {
        showHomeTab();
    }

   /**
    * Thi toggles the Up Caret on ActionBar.
    * @param isHome true if current fragment is Home tab. False otherwise.
    */
   public void toggleHomeUpCaret(boolean isHome) {
       if(actionBar == null) {
           actionBar = getSupportActionBar();
       }
       actionBar.setDisplayHomeAsUpEnabled(!isHome);
       this.isHome = isHome;
   }

   /**
    * First Webview on Home tab calls this method after completion.
    * Action Bar is shown; Timer is cancelled; and splash page is removed.
    */
   public void finishLoad(HashMap<String, String> hashmap) {
       Log.d(LOG_MODULE, "*** Loading Finished.");
       
       showActionBar();

       try{
           while(loadingTimer != null){
               loadingTimer.cancel();
               loadingTimer = null;
           }
       }catch (Exception e) {
           if(e != null && e.getMessage() != null) Log.e(LOG_MODULE, e.getMessage());
       }
       
       splashScreen.setVisibility(View.GONE);
       // TBR: showLoading = false;
       progressbar = null;
       splashScreen = null;
       loadingMessage = null;
   }
   
   public void hideActionBar() {
       actionBar.hide();
   }

   public void showActionBar() {
       actionBar.show();
   }

   /**
    * This method updates the loading message
    */
   public void updateLoadingMessage(String text){
       loadingMessage.bringToFront();
       loadingMessage.setText(text);
   }
   
    /**
     * This class changes the loading message on the splash screen after each specified time interval
     */
    public class LoadingTimer extends CountDownTimer{

        public LoadingTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            updateLoadingMessage(getString(R.string.loading_message_10));
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long seconds = millisUntilFinished/1000;
            if(seconds >= 5 && seconds <= 10){
                updateLoadingMessage(getString(R.string.loading_message_9));
            }else if(seconds > 10 && seconds <= 20){
                updateLoadingMessage(getString(R.string.loading_message_8));
            }else if(seconds > 20 && seconds <= 30){
                updateLoadingMessage(getString(R.string.loading_message_7));
            }else if(seconds > 30 && seconds <= 45){
                updateLoadingMessage(getString(R.string.loading_message_6));
            }else if(seconds > 45 && seconds <= 60){
                updateLoadingMessage(getString(R.string.loading_message_5));
            }else if(seconds > 60 && seconds <= 75){        
                updateLoadingMessage(getString(R.string.loading_message_4));
            }else if(seconds > 75 && seconds <= 90){        
                updateLoadingMessage(getString(R.string.loading_message_3));
            }else if(seconds > 90 && seconds <= 105){
                updateLoadingMessage(getString(R.string.loading_message_2));
            }else if(seconds > 105 && seconds <= 120){
                updateLoadingMessage(getString(R.string.loading_message_1));
            }
        }
    }
    
    /* TBR
    public void onStart() {
        super.onStart();
        Log.d(LOG_MODULE, "*** onStart");
    }

    public void onRestart() {
        super.onRestart();
        Log.d(LOG_MODULE, "*** onRestart");
    }

    public void onResume() {
        super.onResume();
        Log.d(LOG_MODULE, "*** onResume");
    }

    public void onPause() {
        super.onPause();
        Log.d(LOG_MODULE, "*** onPause");
    }

    public void onStop() {
        super.onStop();
        Log.d(LOG_MODULE, "*** onStop");
    }
    */
    
}