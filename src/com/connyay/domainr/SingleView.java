package com.connyay.domainr;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.connyay.domainr.gson.GsonTransformer;
import com.connyay.domainr.gson.Result;
import com.connyay.domainr.support.LoaderCustomSupport;

/**
 * Demonstrates combining a TabHost with a ViewPager to implement a tab UI that
 * switches between tabs and also allows the user to perform horizontal flicks
 * to move between the tabs.
 */
public class SingleView extends SherlockFragmentActivity {
    private AQuery aq = new AQuery(this);
    long expire = 60 * 60 * 1000;

    TabHost mTabHost;
    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    Result res;
    TextView singleDomain, singlePreAvail, singleAvail;

    String domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);

	setContentView(R.layout.view);
	Intent intent = getIntent();
	domain = intent.getStringExtra("domain");
	getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	mTabHost = (TabHost) findViewById(android.R.id.tabhost);
	mTabHost.setup();

	if (savedInstanceState != null) {
	    mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
	    domain = savedInstanceState.getString("domain");
	}
	singleDomain = (TextView) findViewById(R.id.single_domain);
	singlePreAvail = (TextView) findViewById(R.id.single_pre_available);
	singleAvail = (TextView) findViewById(R.id.single_available);
	singleDomain.setText(domain);
	buildResult();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putString("tab", mTabHost.getCurrentTabTag());
	outState.putString("domain", domain);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
	    finish();
	    return true;
	}

	return super.onOptionsItemSelected(item);
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost. It relies on a
     * trick. Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show. This is not sufficient for switching
     * between pages. So instead we make the content part of the tab host 0dp
     * high (it is not shown) and the TabsAdapter supplies its own dummy view to
     * show as the tab content. It listens to changes in tabs, and takes care of
     * switch to the correct paged in the ViewPager whenever the selected tab
     * changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter implements
	    TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
	private final Context mContext;
	private final TabHost mTabHost;
	private final ViewPager mViewPager;
	private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

	static final class TabInfo {
	    private final String tag;
	    private final Class<?> clss;
	    private final Bundle args;

	    TabInfo(String _tag, Class<?> _class, Bundle _args) {
		tag = _tag;
		clss = _class;
		args = _args;
	    }
	}

	static class DummyTabFactory implements TabHost.TabContentFactory {
	    private final Context mContext;

	    public DummyTabFactory(Context context) {
		mContext = context;
	    }

	    @Override
	    public View createTabContent(String tag) {
		View v = new View(mContext);
		v.setMinimumWidth(0);
		v.setMinimumHeight(0);
		return v;
	    }
	}

	public TabsAdapter(FragmentActivity activity, TabHost tabHost,
		ViewPager pager) {
	    super(activity.getSupportFragmentManager());
	    mContext = activity;
	    mTabHost = tabHost;
	    mViewPager = pager;
	    mTabHost.setOnTabChangedListener(this);
	    mViewPager.setAdapter(this);
	    mViewPager.setOnPageChangeListener(this);
	}

	public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
	    tabSpec.setContent(new DummyTabFactory(mContext));
	    String tag = tabSpec.getTag();

	    TabInfo info = new TabInfo(tag, clss, args);
	    mTabs.add(info);
	    mTabHost.addTab(tabSpec);
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

	@Override
	public void onTabChanged(String tabId) {
	    int position = mTabHost.getCurrentTab();
	    mViewPager.setCurrentItem(position);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
		int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
	    // Unfortunately when TabHost changes the current tab, it kindly
	    // also takes care of putting focus on it when not in touch mode.
	    // The jerk.
	    // This hack tries to prevent this from pulling focus out of our
	    // ViewPager.
	    TabWidget widget = mTabHost.getTabWidget();
	    int oldFocusability = widget.getDescendantFocusability();
	    widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
	    mTabHost.setCurrentTab(position);
	    widget.setDescendantFocusability(oldFocusability);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}
    }

    public void buildResult() {

	String url = "http://domai.nr/api/json/info?client_id=domainr-android&q="
		+ domain;
	GsonTransformer t = new GsonTransformer();

	aq.transformer(t).ajax(url, Result.class, expire,
		new AjaxCallback<Result>() {
		    public void callback(String url, Result result,
			    AjaxStatus status) {
			if (status.getCode() == 200) {
			    // good response
			    processResult(result);
			}

		    }
		});
    }

    public void processResult(Result result) {

	String availability = result.getAvailability();
	if (availability.equals("available")) {
	    singleAvail.setTextColor(getResources().getColor(
		    R.color.domainr_green));
	    singleAvail.setText("Available!");
	} else if (availability.equals("taken")) {
	    singleAvail.setTextColor(getResources().getColor(
		    R.color.domainr_blue));
	    singlePreAvail.setText("This domain is ");
	    singleAvail.setText("taken.");

	} else if (availability.equals("tld")
		|| availability.equals("unavailable")) {
	    singleAvail.setTextColor(getResources().getColor(
		    R.color.domainr_red));
	    singleAvail.setText("Unavailable.");
	} else if (availability.equals("maybe")) {
	    singleAvail.setTextColor(getResources().getColor(
		    R.color.domainr_green));
	    singleAvail.setText("Possibly Available.");
	}

	mViewPager = (ViewPager) findViewById(R.id.pager);
	mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
	Bundle a = new Bundle();
	a.putParcelableArray("regs", result.getRegistrars());
	mTabsAdapter.addTab(
		mTabHost.newTabSpec("register").setIndicator("Register"),
		LoaderCustomSupport.RegistrarListFragment.class, a);
	Bundle b = new Bundle();
	b.putString("wiki", result.getTld().getWikipedia_url());
	b.putString("iana", result.getTld().getIana_url());
	b.putString("www", result.getWww_url());
	b.putString("whois", result.getWhois_url());
	b.putString("domain", result.getDomain());
	mTabsAdapter.addTab(
		mTabHost.newTabSpec("moreinfo").setIndicator("More Info"),
		MoreInfoFragment.class, b);
    }
}