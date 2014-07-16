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

		array.recycle();
	}
	
	public void addField(String fieldName, String fieldData) {
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.datarow_layout);
		edu.uml.cs.pincomm.DatapointSubrow newRow = new edu.uml.cs.pincomm.DatapointSubrow(getContext(), null);
		newRow.setLabel(fieldName);
		newRow.setData(fieldData);
		myLayout.addView(newRow);
	}
	
	public void setLabel(String newLabel) {
		TextView topLabel = (TextView) findViewById(R.id.label_datapoint);
		if (newLabel != null) {
			topLabel.setText(newLabel);
		}
	}
	
	public void setLayoutBg(int color) {
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.datarow_layout);
		myLayout.setBackgroundColor(color);
	}
	
	public void setClickListener(View.OnClickListener listener) {
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.datarow_layout);
		TextView topLabel = (TextView) findViewById(R.id.label_datapoint);
		myLayout.setOnClickListener(listener);
		topLabel.setOnClickListener(listener);
	}
}
