package com.blueodin.netgraph.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.blueodin.netgraph.R;
import com.blueodin.netgraph.data.Network;
import com.blueodin.netgraph.data.Reading;

import java.util.List;

public class NetworkListAdapter extends ArrayAdapter<Network> {
	private LayoutInflater mLayoutInflater;
	
	public NetworkListAdapter(Context context) {
		super(context, android.R.id.text1);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		refresh();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Network network = getItem(position);
		NetworkRow networkRow;
		
		if(convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.network_row, parent, false);
			networkRow = NetworkRow.setupNetworkRow(convertView);
		} else
			networkRow = (NetworkRow)convertView.getTag();

		networkRow.update(network);
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	public void refresh() {
		(new AsyncTask<Void, Void, List<Network>>() {
			@Override
			protected List<Network> doInBackground(Void... params) {
				return new Select()
					.all()
					.from(Network.class)
					.execute();
			}
			
			@Override
			protected void onPostExecute(List<Network> result) {
				NetworkListAdapter.this.clear();
				NetworkListAdapter.this.addAll(result);
			}
		}).execute();
	}
	
	private static class NetworkRow {
		public TextView textSSID;
		public TextView textBSSID;
		public TextView textCount;
		
		public static NetworkRow setupNetworkRow(View rowView) {
			NetworkRow networkRow = new NetworkRow(rowView);
			rowView.setTag(networkRow);
			return networkRow;
		}
		
		public NetworkRow(View rowView) {
			textSSID = (TextView) rowView.findViewById(R.id.text_network_ssid);
			textBSSID = (TextView) rowView.findViewById(R.id.text_network_bssid);
			textCount = (TextView) rowView.findViewById(R.id.text_network_count);
		}
		
		public void update(Network network) {
			textSSID.setText(network.ssid);
			textBSSID.setText(network.bssid);
			textCount.setText(String.format("%d", Reading.getCount(network.bssid)));
		}
	}
}
