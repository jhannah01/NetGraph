package com.blueodin.netgraph.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SearchViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;

import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.blueodin.netgraph.R;
import com.blueodin.netgraph.MainActivity.TabFragment;
import com.blueodin.netgraph.data.Network;
import com.blueodin.netgraph.data.Reading;
import com.blueodin.netgraph.graphs.SignalChart;
import com.blueodin.netgraph.graphs.SignalGraph;

import org.apache.http.entity.ContentProducer;

import java.util.List;

public class OverviewTabFragment extends TabFragment implements OnQueryTextListener, LoaderCallbacks<Cursor>, OnCloseListener {
	private static final String SELECTED_LIST_ITEM_POSITION = "selected_list_item_position";
	private static final int LOADER_ID = 0x20;

	public OverviewTabFragment() { }

	private SignalGraph mSignalGraph;
	private SignalChart mSignalChart;
	private ListView mListView;
	private NetworkListAdapter mListAdapter;
	private LinearLayout mLayoutGraphs;
	private String mCurFilter;
	private NetworkSearchView mSearchView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		mListAdapter = new NetworkListAdapter(getActivity());
		
		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSignalGraph = new SignalGraph(getActivity());
		mSignalChart = new SignalChart(getActivity());
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_LIST_ITEM_POSITION, mListView.getSelectedItemPosition());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View tabView = inflater.inflate(R.layout.overview_tab_fragment, container, false);

		mLayoutGraphs = (LinearLayout)tabView.findViewById(R.id.layout_overview_graphs);
		
		mListView = (ListView)tabView.findViewById(R.id.list_overview_networks);
		mListView.setAdapter(mListAdapter);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setItemsCanFocus(true);
		//mListView.setSelector(android.R.drawable.list_selector_background);
		
		mListView.setItemChecked(0, true);
		
		if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_LIST_ITEM_POSITION)) {
			int idx = savedInstanceState.getInt(SELECTED_LIST_ITEM_POSITION);
			if(idx != ListView.INVALID_POSITION)
				mListView.setSelection(idx);
		}
		
		mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Network network = new Network();
				network.loadFromCursor((Cursor)mListAdapter.getItem(position));
				List<Reading> readings = network.getReadings();
				
				Toast.makeText(getActivity(), String.format("Selected Network for %s (%s) with %d records", network.ssid, network.bssid, readings.size()), Toast.LENGTH_SHORT).show();
				
				mSignalGraph.reset(readings);
				mSignalChart.reset(readings);
				mLayoutGraphs.removeAllViews();
				mLayoutGraphs.addView(mSignalGraph.getGraphView());
				mLayoutGraphs.addView(mSignalChart.getChartView());		
				
				mListView.setItemChecked(position, true);
			}
		
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				mLayoutGraphs.removeAllViews();
				mSignalGraph.clear();
				mSignalChart.clear();
			}
		});
		
		return tabView;
	}

	@Override
	public void onLoadNetworks(List<Network> networks) {
		
	}

	@Override
	public void onLoadReadings(List<Reading> readings) {
		mSignalGraph.add(readings);
		mSignalChart.add(readings);
	}

	@Override
	public void onClearNetworks() {
		clear();
	}

	@Override
	public void onClearReadings() {
		clear();
	}
	
	public void clear() {
		mSignalGraph.clear();
		mSignalChart.clear();
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
		mListAdapter.getFilter().filter(mCurFilter);
		return true;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        mSearchView = new NetworkSearchView(getActivity());
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconifiedByDefault(true);
        item.setActionView(mSearchView);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String selection = null;
		String[] selectionArgs = null;
		
		if (mCurFilter != null) {
			selection = "ssid LIKE %?%";
			selectionArgs = new String[] { mCurFilter };
		}
		
		return new CursorLoader(getActivity(), ContentProvider.createUri(Network.class, null), null, selection, selectionArgs, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}
	
	public static class NetworkSearchView extends SearchView {
		public NetworkSearchView(Context context) {
            super(context);
        }
		
		@Override
	    public void onActionViewCollapsed() {
	    	setQuery("", false);
	        super.onActionViewCollapsed();
	    }
	}

	@Override
	public boolean onClose() {
		if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        return true;
	}
}