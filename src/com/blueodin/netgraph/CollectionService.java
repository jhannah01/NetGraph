package com.blueodin.netgraph;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

public class CollectionService extends Service {
	private static final String TAG = "CollectionService";
	private static final int DEFAULT_SCAN_INTERVAL = (4 * 1000);
	private static final int NOTIFICATION_ID = 1;
	
	public static final String ACTION_NEW_SENSOR_READING = "com.blueodin.netgraph.NEW_SENSOR_READING";
	public static final String EXTRA_NEW_READINGS = "new_readings";
	
	private final IBinder mBinder = new CollectionServiceBinder();
	
	private NotificationManager mNotificationManager;
	private WifiScanManager mWifiScanManager;

	private boolean mIsCollecting = false;
		
	public CollectionService() { }

	@Override
	public void onCreate() {
		super.onCreate();
		this.mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		this.mWifiScanManager = new WifiScanManager(this, DEFAULT_SCAN_INTERVAL);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startCollection();
		return START_STICKY;
	}
	
	public boolean isCollecting() {
		return mIsCollecting;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(mIsCollecting)
			stopCollection();
	}

	private void showNotification(String textContent) {
		mNotificationManager.notify(NOTIFICATION_ID, getNotificationBuilder(textContent).build());
	}
	
	private NotificationCompat.Builder getNotificationBuilder(String textContent) {
		Intent mainIntent = new Intent(this, MainActivity.class);
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mainIntent.putExtra(MainActivity.PARAM_FROM_SERVICE, true);
		
		return (new NotificationCompat.Builder(this))
			.setContentTitle("NetGraph")
			.setContentText(textContent)
			.setSmallIcon(R.drawable.ic_stat_service)
			.setContentIntent(PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT))
			.setWhen(System.currentTimeMillis())
			.setAutoCancel(false)
			.setOngoing(true);
	}
	
	public void updateNotification(String subText) {
		updateNotification("", subText);
	}
	
	public void updateNotification(String tickerText, String subText) {
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
		
		notificationBuilder
			.setSubText(subText)
			.setWhen(System.currentTimeMillis());
		
		if(!TextUtils.isEmpty(tickerText))
			notificationBuilder.setTicker(tickerText);
		
		mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}
	
	private void cancelNotification() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
	
	public void startCollection() {
		if(mIsCollecting)
			return;

		mWifiScanManager.start();
		mIsCollecting = true;
		
		Log.i(TAG, "Network collection started.");
		
		showNotification("Collection service running");
	}
	
	public void stopCollection() {
		if(!mIsCollecting)
			return;
		
		mWifiScanManager.stop();
		mIsCollecting = false;
		
		Log.i(TAG, "Network collection stopped.");
		
		cancelNotification();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class CollectionServiceBinder extends Binder {
        CollectionService getService() {
            return CollectionService.this;
        }
    }

	public void toggleMonitoring() {
		if(mIsCollecting)
			stopCollection();
		else
			startCollection();
	}
}