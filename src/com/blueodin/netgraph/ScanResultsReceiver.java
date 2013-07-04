package com.blueodin.netgraph;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.blueodin.netgraph.data.Network;
import com.blueodin.netgraph.data.Reading;

import java.util.ArrayList;
import java.util.List;

public abstract class ScanResultsReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (!intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
			return;

		(new ScanResultTask()).execute((WifiManager)context.getSystemService(Context.WIFI_SERVICE));
	}
	
	protected abstract void updateResults(List<Reading> readings);
	
	private class ScanResultTask extends AsyncTask<WifiManager, Void, List<Reading>> {
		@Override
		protected List<Reading> doInBackground(WifiManager... params) {
			List<Reading> results = new ArrayList<Reading>();
			
			for(ScanResult result : params[0].getScanResults()) {
				Network network = Network.getByBSSID(result.BSSID);
				
				if(network == null) {
					network = new Network(result.BSSID, result.SSID, result.capabilities, result.frequency);
					network.save();
				}
				
				Reading reading = new Reading(result.BSSID, result.level);
				reading.save();
				results.add(reading);
			}
			
			return results;
		}
		
		@Override
		protected void onPostExecute(List<Reading> readings) {
			updateResults(readings);
		}
	}
}
