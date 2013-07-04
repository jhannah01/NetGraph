package com.blueodin.netgraph.graphs;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blueodin.netgraph.R;
import com.blueodin.netgraph.R.color;
import com.blueodin.netgraph.data.Reading;
import com.michaelpardo.chartview.widget.ChartView;
import com.michaelpardo.chartview.widget.LabelAdapter;
import com.michaelpardo.chartview.widget.LinearSeries;

import java.util.Collections;
import java.util.List;

public class SignalChart {
	private Context mContext;
	private ChartView mChartView;
	private LinearSeries mSeries = null;
	
	private abstract class BaseLabelAdapter extends LabelAdapter {
		protected abstract String formatValue(Double value);

		protected abstract int getGravity(int position);

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView labelTextView;

			if (convertView == null)
				convertView = new TextView(mContext);

			labelTextView = (TextView) convertView;

			labelTextView.setGravity(getGravity(position));
			labelTextView.setPadding(8, 0, 8, 0);
			labelTextView.setText(formatValue(getItem(position)));

			return labelTextView;
		}
	}
	
	private class LevelLabelAdapter extends BaseLabelAdapter {
		@Override
		protected String formatValue(Double value) {
			return String.format("%d dBm", value.intValue());
		}
		
		@Override
		protected int getGravity(int position) {
			int gravity = Gravity.RIGHT;
			
			if(position == 0)
				gravity |= Gravity.BOTTOM;
			else if(position == (getCount()-1))
				gravity |= Gravity.TOP;
			else
				gravity |= Gravity.CENTER;
			
			return gravity;
		}
	}

	private class TimestampLabelAdapter extends BaseLabelAdapter {
		@Override
		protected String formatValue(Double value) {
			return DateUtils.getRelativeTimeSpanString(value.longValue(),
					System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS,
					DateUtils.FORMAT_ABBREV_ALL).toString();
		}

		protected int getGravity(int position) {
			int gravity = Gravity.CENTER;

			if (position == 0)
				gravity |= Gravity.LEFT;
			else if (position == getCount() - 1)
				gravity |= Gravity.RIGHT;

			return gravity;

		}
	}
	
	public SignalChart(Context context) {
		mContext = context;
		
		mChartView = new ChartView(context);
		mChartView.setGridLineWidth(2);
		mChartView.setGridLineColor(context.getResources().getColor(android.R.color.darker_gray));
		
		mChartView.setLabelAdapter(new TimestampLabelAdapter(), ChartView.POSITION_BOTTOM);
		mChartView.setLabelAdapter(new LevelLabelAdapter(), ChartView.POSITION_LEFT);
		
		//mChartView.setGridStepX(5 * 60 * 1000);
		//mChartView.setGridStepY();
	}
	
	private void setupSeries(List<Reading> readings) {
		if(mSeries != null)
			mChartView.clearSeries();
		
		mSeries = new LinearSeries();
		mSeries.setLineColor(mContext.getResources().getColor(R.color.graph_line));
		mSeries.setLineWidth(2);
		mSeries.setPoints(Reading.buildChartDataList(readings));
		mChartView.addSeries(mSeries);
	}
	
	public void reset(List<Reading> readings) {
		if(mSeries != null)
			mChartView.clearSeries();
		
		setupSeries(readings);
	}
	
	public void add(Reading reading) {
		add(Collections.singletonList(reading));
	}
	
	public void add(List<Reading> readings) {
		if(mSeries == null)
			setupSeries(readings);
		else {
			for(Reading reading : readings)
				mSeries.addPoint(reading.getChartPoint());
			mChartView.invalidate();
		}
	}
	
	public void clear() {
		mChartView.clearSeries();
		mSeries = null;
		mChartView.invalidate();
	}
	
	public ChartView getChartView() {
		return mChartView;
	}
}
