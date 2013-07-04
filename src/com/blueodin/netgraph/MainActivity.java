package com.blueodin.netgraph;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.blueodin.netgraph.CollectionService.CollectionServiceBinder;
import com.blueodin.netgraph.data.Network;
import com.blueodin.netgraph.data.Reading;
import com.blueodin.netgraph.fragments.OverviewTabFragment;
import com.blueodin.netgraph.graphs.SignalChart;
import com.blueodin.netgraph.graphs.SignalGraph;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
	private static final String TAB_OVERVIEW = "Overview";
	private static final String TAB_HISTORICAL = "Historical";
	private static final String TAB_QUERY = "Query";
	private static final String SELECTED_NAVIGATION_INDEX = "selected_navigation_index";
	public static final String PARAM_FROM_SERVICE = "param_from_service";

	private ActionBar mActionBar;
	private MenuItem mToggleMenuItem = null;

	private boolean mBound = false;
	private CollectionService mService = null;
	private TextView mTextNetworksCount;
	private TextView mTextReadingsCount;
	
	private List<Network> mNetworks = new ArrayList<Network>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mActionBar = getActionBar();
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mTextNetworksCount = (TextView) findViewById(R.id.text_main_networks);
		mTextReadingsCount = (TextView) findViewById(R.id.text_main_readings);

		FragmentManager fm = getSupportFragmentManager();

		mActionBar.addTab(mActionBar
				.newTab()
				.setText(TAB_OVERVIEW)
				.setTabListener(
						new TabListener<OverviewTabFragment>(fm, this,
								TAB_OVERVIEW, OverviewTabFragment.class, mActionBar)));

		mActionBar.addTab(mActionBar
				.newTab()
				.setText(TAB_HISTORICAL)
				.setTabListener(
						new TabListener<HistoricalTabFragment>(fm, this,
								TAB_HISTORICAL, HistoricalTabFragment.class, mActionBar)));

		mActionBar.addTab(mActionBar
				.newTab()
				.setText(TAB_QUERY)
				.setTabListener(
						new TabListener<QueryTabFragment>(fm, this, TAB_QUERY,
								QueryTabFragment.class, mActionBar)));

		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(SELECTED_NAVIGATION_INDEX))
			mActionBar.setSelectedNavigationItem(savedInstanceState
					.getInt(SELECTED_NAVIGATION_INDEX));
		
		refreshReadings();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateToggleMenuItem();
		registerReceiver(mScanResultsReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mScanResultsReceiver);
	}

	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, CollectionService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_NAVIGATION_INDEX,
				mActionBar.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mToggleMenuItem = menu.findItem(R.id.action_toggle_collection);
		updateToggleMenuItem();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_toggle_collection:
			toggleMonitoring();
			return true;
		case R.id.action_reset:
			refreshReadings();
			return true;
		case R.id.action_settings:
			// TODO: Make settings
			return true;
		case R.id.action_exit:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	

	public static class HistoricalTabFragment extends TabFragment {
		public HistoricalTabFragment() { }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View tabView = inflater.inflate(R.layout.historical_tab_fragment,
					container, false);

			return tabView;
		}

		@Override
		public void onLoadNetworks(List<Network> networks) {
			
		}

		@Override
		public void onLoadReadings(List<Reading> readings) {
			
		}

		@Override
		public void onClearNetworks() {
			
		}

		@Override
		public void onClearReadings() {
			
		}
	}

	public static class QueryTabFragment extends TabFragment {
		public QueryTabFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View tabView = inflater.inflate(R.layout.query_tab_fragment,
					container, false);

			return tabView;
		}

		@Override
		public void onLoadNetworks(List<Network> networks) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLoadReadings(List<Reading> readings) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onClearNetworks() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onClearReadings() {
			// TODO Auto-generated method stub
			
		}
	}

	public static abstract class TabFragment extends Fragment implements OnNetworkDataCallback {
		private List<Reading> mReadings = new ArrayList<Reading>();
		private List<Network> mNetworks = new ArrayList<Network>();
		
		public TabFragment() { }
		
		public List<Reading> getReadings() {
			return mReadings;
		}
		
		public List<Network> getNetworks() {
			return mNetworks;
		}
	}

	public static class TabListener<T extends Fragment> implements
			ActionBar.TabListener {
		private Activity mActivity;
		private String mTag;
		private Class<T> mClass;
		private Fragment mFragment;
		private FragmentManager mFragmentManager;
		private ActionBar mActionBar;

		public TabListener(FragmentManager fragmentManager, Activity activity,
				String tag, Class<T> cls, ActionBar actionBar) {
			mFragmentManager = fragmentManager;
			mActivity = activity;
			mTag = tag;
			mClass = cls;
			mActionBar = actionBar;
		}

		@Override
		public void onTabSelected(Tab tab,
				android.app.FragmentTransaction otherTransaction) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();

			if (mFragment == null) {
				mFragment = Fragment.instantiate(mActivity, mClass.getName());
				ft.add(R.id.frame_main_content, mFragment, mTag);
			} else
				ft.attach(mFragment);

			tab.setTag(mFragment);
			ft.commit();
			
			mActionBar.setSubtitle(tab.getText());
		}

		public void onTabUnselected(Tab tab,
				android.app.FragmentTransaction otherTransaction) {
			if (mFragment != null) {
				tab.setTag(null);
				mFragmentManager.beginTransaction().detach(mFragment).commit();
			}
		}

		public void onTabReselected(Tab tab,
				android.app.FragmentTransaction otherTransaction) {

		}
	}

	private TabFragment getTabFragment() {
		return (TabFragment) mActionBar.getSelectedTab().getTag();
	}

	private ScanResultsReceiver mScanResultsReceiver = new ScanResultsReceiver() {
		@Override
		protected void updateResults(List<Reading> readings) {
			getTabFragment().onLoadReadings(readings);
			updateCounts();
		}
	};

	public void toggleMonitoring() {
		if (mBound)
			mService.toggleMonitoring();

		updateToggleMenuItem();
	}

	private void updateToggleMenuItem() {
		if (mToggleMenuItem == null)
			return;

		if (!mBound || !mService.isCollecting()) {
			mToggleMenuItem.setIcon(R.drawable.ic_action_start);
			mToggleMenuItem.setTitle("Start Collection Service");
			mToggleMenuItem.setChecked(false);
		} else {
			mToggleMenuItem.setIcon(R.drawable.ic_action_stop);
			mToggleMenuItem.setTitle("Stop Collection Service");
			mToggleMenuItem.setChecked(true);
		}
	}

	private void updateCounts() {
		mTextNetworksCount.setText(String.valueOf(Network.getCount()));
		mTextReadingsCount.setText(String.valueOf(Reading.getCount()));
	}

	public void refreshReadings() {
		(new AsyncTask<Void, Void, List<Reading>>() {
			@Override
			protected List<Reading> doInBackground(Void... params) {
				return (new Select().all().from(Reading.class).execute());
			}

			@Override
			protected void onPostExecute(List<Reading> readings) {
				getTabFragment().onLoadReadings(readings);
				updateCounts();
			}

			@Override
			protected void onCancelled() {
				getTabFragment().onClearReadings();
			}
		}).execute();
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			CollectionServiceBinder binder = (CollectionService.CollectionServiceBinder) service;
			mService = binder.getService();
			mBound = true;
			updateToggleMenuItem();
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};
	
	public interface OnNetworkDataCallback {
		public void onLoadNetworks(List<Network> networks);
		public void onLoadReadings(List<Reading> readings);
		public void onClearNetworks();
		public void onClearReadings();
	}
}