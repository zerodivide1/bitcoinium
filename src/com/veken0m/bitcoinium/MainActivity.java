
package com.veken0m.bitcoinium;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.veken0m.bitcoinium.exchanges.BTCChinaFragment;
import com.veken0m.bitcoinium.exchanges.BTCEFragment;
import com.veken0m.bitcoinium.exchanges.BitcurexFragment;
import com.veken0m.bitcoinium.exchanges.BitstampFragment;
import com.veken0m.bitcoinium.exchanges.CampBXFragment;
import com.veken0m.bitcoinium.exchanges.KrakenFragment;
import com.veken0m.bitcoinium.exchanges.MtGoxFragment;
import com.veken0m.bitcoinium.exchanges.VirtExFragment;
import com.veken0m.bitcoinium.utils.KarmaAdsUtils;

/**
 * @author Michael Lagacé a.k.a. veken0m
 * @version 1.7.0 Aug 29 2013
 */
public class MainActivity extends SherlockFragmentActivity {
    ViewPager mViewPager;
    ActionBar actionbar;
    TabsAdapter tabsAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initTabbedActionBar();
        selectTabViaBundle();

        //KarmaAdsUtils.initAd(this);
    }
    
    public void onResume() {
        super.onResume();
        selectTabViaBundle();
    }
    
    public void selectTabViaBundle(){
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            selectTab(extras.getString("exchangeKey"));
        }
    }
    
    public void initTabbedActionBar(){
        mViewPager = (ViewPager) findViewById(R.id.pager);

        // ActionBar gets initiated
        actionbar = getSupportActionBar();

        // Tell the ActionBar we want to use Tabs
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionbar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_color)));
        //actionbar.setBackgroundDrawable(color);

        // Create the actionbar tabs
        tabsAdapter = new TabsAdapter(this, actionbar, mViewPager);
        if(prefs.getBoolean("mtgoxShowPref", true))
            addTab(actionbar, tabsAdapter, R.drawable.mtgoxlogo, MtGoxFragment.class, "mtgox");
        if(prefs.getBoolean("virtexShowPref", true))
            addTab(actionbar, tabsAdapter, R.drawable.virtexlogo, VirtExFragment.class, "virtex");
        if(prefs.getBoolean("btceShowPref", true))
            addTab(actionbar, tabsAdapter, R.drawable.btcelogo, BTCEFragment.class, "btce");
        if(prefs.getBoolean("bitstampShowPref", true))
            addTab(actionbar, tabsAdapter, R.drawable.bitstamplogo, BitstampFragment.class, "bitstamp");
        if(prefs.getBoolean("campbxShowPref", true))
            addTab(actionbar, tabsAdapter, R.drawable.campbxlogo, CampBXFragment.class, "campbx");
        if(prefs.getBoolean("btcchinaShowPref", true))
            addTab(actionbar, tabsAdapter, R.drawable.btcchinalogo, BTCChinaFragment.class, "btcchina");
        if(prefs.getBoolean("bitcurexShowPref", true))
            addTab(actionbar, tabsAdapter, R.drawable.bitcurexlogo, BitcurexFragment.class, "bitcurex");
        addTab(actionbar, tabsAdapter, R.drawable.krakenlogo, KrakenFragment.class, "kraken");

        selectTab();
        actionbar.show();
    }
    
    private void addTab(ActionBar actionbar, TabsAdapter tabsAdapter, int logoResource, Class<? extends Fragment> viewFragment, String identity) {
        ActionBar.Tab tab = actionbar.newTab().setIcon(logoResource);
        tabsAdapter.addTab(tab, viewFragment, null, identity);
    }
    
    private void selectTab(){
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        try {
            String preferredExchange = prefs.getString("favExchangePref", "mtgox");
            //Check if moving from integer index
            if(preferredExchange.matches("\\d+")) {
                int preferredExchangeNum = Integer.parseInt(preferredExchange);
                actionbar.setSelectedNavigationItem(preferredExchangeNum);
                
                //Migrate to the newer tag index
                String[] exchangeMap = getResources().getStringArray(R.array.exchangeMigration);
                Editor editor = prefs.edit();
                editor.putString("favExchangePref", exchangeMap[preferredExchangeNum]);
                editor.commit();
            } else {
                selectTab(preferredExchange);
            }
        } catch (Exception e) {
            // If preference is not set a valid integer set to "0"
            Editor editor = prefs.edit();
            editor.putString("favExchangePref", "mtgox");
            editor.commit();
        }
    }

    private void selectTab(String key){
        try{
            int tabIndex = tabsAdapter.getIndexForIdentity(key);
            if(tabIndex >= 0)
                actionbar.setSelectedNavigationItem(tabIndex);
            else
                actionbar.setSelectedNavigationItem(0);
        } catch (Exception e){
            selectTab();
        }
    }
    

    /**
     * Obtained from: https://gist.github.com/2424383 This is a helper class
     * that implements the management of tabs and all details of connecting a
     * ViewPager with associated TabHost. It relies on a trick. Normally a tab
     * host has a simple API for supplying a View or Intent that each tab will
     * show. This is not sufficient for switching between pages. So instead we
     * make the content part of the tab host 0dp high (it is not shown) and the
     * TabsAdapter supplies its own dummy view to show as the tab content. It
     * listens to changes in tabs, and takes care of switch to the correct paged
     * in the ViewPager whenever the selected tab changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter implements
            ViewPager.OnPageChangeListener, ActionBar.TabListener {
        private final Context mContext;
        private final ActionBar mBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private final String ident;

            TabInfo(Class<?> _class, Bundle _args, String _ident) {
                clss = _class;
                args = _args;
                ident = _ident;
            }
        }

        public TabsAdapter(SherlockFragmentActivity activity, ActionBar bar,
                ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mBar = bar;
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<? extends Fragment> clss,
                Bundle args, String ident) {
            TabInfo info = new TabInfo(clss, args, ident);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(),
                    info.args);
        }
        
        public int getIndexForIdentity(String identity) {
            for(int i=0; i < mTabs.size(); i++) {
                TabInfo info = mTabs.get(i);
                if(identity.equals(info.ident))
                    return i;
            }
            return -1;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mBar.setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i = 0; i < mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
        }
        if (item.getItemId() == R.id.price_alarm_preferences) {
            startActivity(new Intent(this, PriceAlarmPreferencesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
