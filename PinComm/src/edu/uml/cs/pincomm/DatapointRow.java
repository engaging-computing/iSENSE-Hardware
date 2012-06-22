package edu.uml.cs.pincomm;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DatapointRow extends LinearLayout {

	public DatapointRow(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.datapoint_row, this, true);

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.datarow, 0, 0);

		TextView topLabel = (TextView) findViewById(R.id.label_datapoint);
		String labelText = array.getString(R.styleable.datarow_toplabel);
		if (labelText != null) {
			topLabel.setText(labelText);
		}
		
		TextView recordTime = (TextView) findViewById(R.id.label_timedata);
		String timeText = array.getString(R.styleable.datarow_time);
		if (timeText != null) {
			recordTime.setText(timeText);
		}
		
		TextView sensor1Name = (TextView) findViewById(R.id.label_sensor1);
		String sensorText = array.getString(R.styleable.datarow_sensor1name);
		if (sensorText != null) {
			sensor1Name.setText(sensorText);
		}
		
		TextView sensor1Data = (TextView) findViewById(R.id.label_sensor1data);
		String sensorDataText = array.getString(R.styleable.datarow_sensor1data);
		if (sensorDataText != null) {
			sensor1Data.setText(sensorDataText);
		}

		array.recycle();
	}
	
	public void setLabel(String newLabel) {
		TextView topLabel = (TextView) findViewById(R.id.label_datapoint);
		if (newLabel != null) {
			topLabel.setText(newLabel);
		}
	}
	
	public void setTime(String newTime) {
		TextView time = (TextView) findViewById(R.id.label_timedata);
		if (newTime != null) {
			time.setText(newTime);
		}
	}
	
	public void setSensor1Name(String newName) {
		TextView sensor1Name = (TextView) findViewById(R.id.label_sensor1);
		if (newName!= null) {
			sensor1Name.setText(newName);
		}
	}
	
	public void setSensor1Data(String newData) {
		TextView sensor1Data = (TextView) findViewById(R.id.label_sensor1data);
		if (newData!= null) {
			sensor1Data.setText(newData);
		}
	}
	
	public void setLayoutBg(int color) {
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.datarow_layout);
		myLayout.setBackgroundColor(color);
	}
	
	public void setClickListener(View.OnClickListener listener) {
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.datarow_layout);
		TextView topLabel = (TextView) findViewById(R.id.label_datapoint);
		TextView recordTime = (TextView) findViewById(R.id.label_timedata);
		TextView sensor1Name = (TextView) findViewById(R.id.label_sensor1);
		TextView sensor1Data = (TextView) findViewById(R.id.label_sensor1data);
		myLayout.setOnClickListener(listener);
		topLabel.setOnClickListener(listener);
		recordTime.setOnClickListener(listener);
		sensor1Name.setOnClickListener(listener);
		sensor1Data.setOnClickListener(listener);
	}
}
