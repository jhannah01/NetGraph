package com.blueodin.netgraph.data;

import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.michaelpardo.chartview.widget.LinearSeries;

import java.util.ArrayList;
import java.util.List;

@Table(name="readings")
public class Reading extends Model implements Parcelable, Comparable<Reading> {
	@Column(name="bssid", notNull=true)
	public String bssid;
	@Column(name="level")
	public int level;
	@Column(name="timestamp")
	public long timestamp;
	
	public Network getNetwork() {
		return new Select()
			.distinct()
			.from(Reading.class)
			.where("bssid == ?", this.bssid)
			.executeSingle();
	}
	
	public Reading() {
		super();
	}
	
	public Reading(String bssid, int level) {
		super();
		this.bssid = bssid;
		this.level = level;
		this.timestamp = System.currentTimeMillis();
	}
	
	private Reading(Parcel src) {
		super();
		this.bssid = src.readString();
		this.level = src.readInt();
		this.timestamp = src.readLong();
	}

	public GraphViewData getGraphData() {
		return new GraphViewData(this.timestamp, this.level);
	}
	
	public LinearSeries.LinearPoint getChartPoint() {
		return new LinearSeries.LinearPoint(this.timestamp, this.level);
	}
	
	public static GraphViewData[] buildGraphDataAray(List<Reading> readings) {
		GraphViewData[] data = new GraphViewData[readings.size()];
		
		for(int i=0 ; i < readings.size() ; i++)
			data[i] = readings.get(i).getGraphData();
		
		return data;
	}
	
	public static List<LinearSeries.LinearPoint> buildChartDataList(List<Reading> readings) {
		List<LinearSeries.LinearPoint> points = new ArrayList<LinearSeries.LinearPoint>();
		
		for(Reading reading : readings)
			points.add(reading.getChartPoint());
		
		return points;
	}
	
	public static int getCount(String bssid) {
		Cursor data = Cache.openDatabase().rawQuery(String.format("SELECT COUNT(_id) AS c FROM readings WHERE bssid = '%s'", bssid), null);
		
		if(data == null)
			return 0;
		
		if(!data.moveToFirst()) {
			data.close();
			return 0;
		}
		
		int c = data.getInt(data.getColumnIndex("c"));
		
		data.close();
		
		return c;
	}
	
	public static int getCount() {
		Cursor data = Cache.openDatabase().rawQuery("SELECT COUNT(_id) AS c FROM readings", null);
		
		if(data == null)
			return 0;
		
		if(!data.moveToFirst()) {
			data.close();
			return 0;
		}
		
		int c = data.getInt(data.getColumnIndex("c"));
		
		data.close();
		
		return c;
	}

	@Override
	public int compareTo(Reading another) {
		if(this.timestamp < another.timestamp)
			return -1;
		else if(this.timestamp > another.timestamp)
			return 1;
		
		return 0;
	}
	
	public static final Creator<Reading> CREATOR = new Creator<Reading>() {
        public Reading createFromParcel(Parcel in) {
            return new Reading(in);
        }
        
        public Reading[] newArray(int size) {
            return new Reading[size];
        }
    };

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.bssid);
		dest.writeInt(this.level);
		dest.writeLong(this.timestamp);
	}
}
