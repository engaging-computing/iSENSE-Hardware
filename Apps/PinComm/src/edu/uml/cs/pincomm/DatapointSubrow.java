package edu.uml.cs.pincomm;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DatapointSubrow extends LinearLayout {

	public DatapointSubrow(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.datapoint_subrow, this, true);

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.datasubrow, 0, 0);
		
		TextView label = (TextView) findViewById(R.id.label_sensor);
		String labelText = array.getString(R.styleable.datasubrow_label);
		if (labelText != null) {
			label.setText(labelText);
		}
		
		TextView data = (TextView) findViewById(R.id.label_data);
		String dataText = array.getString(R.styleable.datasubrow_data);
		if (dataText != null) {
			data.setText(dataText);
		}

		array.recycle();
	}
	
	public void setLabel(String newLabel) {
		TextView label = (TextView) findViewById(R.id.label_sensor);
		if (newLabel != null) {
			label.setText(newLabel);
		}
	}
	
	public void setData(String newData) {
		TextView data = (TextView) findViewById(R.id.label_data);
		if (newData != null) {
			data.setText(newData);
		}
	}
	
	public void setClickListener(View.OnClickListener listener) {
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.datasubrow_layout);
		TextView label = (TextView) findViewById(R.id.label_sensor);
		TextView data = (TextView) findViewById(R.id.label_data);
		myLayout.setOnClickListener(listener);
		label.setOnClickListener(listener);
		data.setOnClickListener(listener);
	}
}
