package com.blueodin.netgraph.graphs;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.blueodin.netgraph.data.Reading;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignalGraph {
	private Context mContext;
	private BarGraphView mGraphView;
	private GraphViewSeries mSeries = null;
	
	public SignalGraph(Context context) {
		mContext = context;
		
		setupGraphView();
	}
	
	private void setupGraphView() {
		mGraphView = new BarGraphView(mContext, "Sigal Graph") {
			@Override
			protected String formatLabel(double value, boolean isValueX) {
				if(!isValueX)
					return String.format("%d dBm", (int)value);
				
				long timestamp = (long)value;
				
				long offset = System.currentTimeMillis() - timestamp;
				
				if(offset > (24 * 60 * 60 * 1000))
					return DateFormat.format("MMM h:mmaa", timestamp).toString();
				else if(offset < 1000)
					return String.format("%d ms", timestamp);
				
				return DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();
			}
		};
		
		GraphViewStyle graphViewStyle = mGraphView.getGraphViewStyle();
		graphViewStyle.setHorizontalLabelsColor(mContext.getResources().getColor(android.R.color.primary_text_light));
		graphViewStyle.setVerticalLabelsColor(mContext.getResources().getColor(android.R.color.primary_text_light));
		graphViewStyle.setGridColor(mContext.getResources().getColor(android.R.color.darker_gray));
		mGraphView.setGraphViewStyle(graphViewStyle);
		
		mGraphView.setScrollable(true);
		mGraphView.setScalable(true);
	}
	
	public void reset(List<Reading> readings) {
		if(mSeries == null)
			setupSeries(readings);
		else
			mSeries.resetData(Reading.buildGraphDataAray(readings));
	}
	
	public void add(Reading reading) {
		add(Collections.singletonList(reading));
	}
	
	public void add(List<Reading> readings) {
		if(mSeries == null)
			setupSeries(readings);
		else {
			for(Reading reading : readings)
				mSeries.appendData(reading.getGraphData(), true);
		}
	}
	
	public void clear() {
		if(mSeries != null) {
			mGraphView.removeSeries(mSeries);
			mSeries = null;
		}
	}

	private void setupSeries(List<Reading> readings) {
		final int defaultColor = mContext.getResources().getColor(android.R.color.holo_green_dark);
		final int highColor = mContext.getResources().getColor(android.R.color.holo_orange_dark);
		GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle();
		seriesStyle.setValueDependentColor(new ValueDependentColor() {
			@Override
			public int get(GraphViewData data) {
				if(data.valueY > getAverageLevel())
					return highColor;
				
				return defaultColor;
			}
		});
		
		mSeries = new GraphViewSeries("Readings", seriesStyle, Reading.buildGraphDataAray(readings));
		
		if(readings.size() > 0)
			mGraphView.addSeries(mSeries);
		
		updateViewPort();
	}
	
	public int getAverageLevel() {
		int avg = 0;
		
		if((mSeries == null) || (mSeries.getCount() == 0))
			return 0;
		
		for(GraphViewData data : mSeries.getData())
			avg += (int)data.valueY;
		
		avg /= mSeries.getCount();
		
		return avg;
	}
	
	public void updateViewPort() {
		mGraphView.setViewPort(System.currentTimeMillis() - (5 * 60 * 1000), (5 * 60 * 1000));
	}
	
	public BarGraphView getGraphView() {
		return mGraphView;
	}
}
