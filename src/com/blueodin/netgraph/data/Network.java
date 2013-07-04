package com.blueodin.netgraph.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.query.Sqlable;

import java.util.List;

@Table(name="networks")
public class Network extends Model implements Parcelable {
	@Column(name="bssid", notNull=true, unique=true)
	public String bssid;
	@Column(name="ssid")
	public String ssid;
	@Column(name="capabilities")
	public String capabilities;
	@Column(name="frequency")
	public int frequency;
	
	public List<Reading> getReadings() {
		return getMany(Reading.class, "bssid");
	}
	
	public Network() {
		super();
	}
	
	private Network(Parcel src) {
		super();
		this.bssid = src.readString();
		this.ssid = src.readString();
		this.capabilities = src.readString();
		this.frequency = src.readInt();
	}
	
	public Network(String bssid, String ssid, String capabilities, int frequency) {
		super();
		this.bssid = bssid;
		this.ssid = ssid;
		this.capabilities = capabilities;
		this.frequency = frequency;
	}

	public static Network getByBSSID(String bssid) {
		return new Select()
			.from(Network.class)
			.where("bssid == ?", bssid)
			.executeSingle();
	}
	
	public static int getCount() {
		Cursor data = Cache.openDatabase().rawQuery("SELECT COUNT(_id) AS c FROM networks", null);
		
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
	
	public static final Creator<Network> CREATOR = new Creator<Network>() {
        public Network createFromParcel(Parcel in) {
            return new Network(in);
        }
        
        public Network[] newArray(int size) {
            return new Network[size];
        }
    };

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.bssid);
		dest.writeString(ssid);
		dest.writeString(this.capabilities);
		dest.writeInt(this.frequency);
	}
}
