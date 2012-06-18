package edu.uml.cs.pincomm;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExperimentRow extends LinearLayout {

	public ExperimentRow(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.experiment_row, this, true);

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.exprow, 0, 0);

		TextView title = (TextView) findViewById(R.id.toptext);
		String titleText = array.getString(R.styleable.exprow_name);
		if (titleText != null) {
			title.setText(titleText);
		}
		
		TextView tease = (TextView) findViewById(R.id.bottomtext);
		String teaseText = array.getString(R.styleable.exprow_desc);
		if (teaseText != null) {
			tease.setText(teaseText);
		}

		array.recycle();
	}
	
	public void setName(String newName) {
		TextView title = (TextView) findViewById(R.id.toptext);
		if (newName != null) {
			title.setText(newName);
		}
	}
	
	public void setDesc(String newDesc) {
		TextView desc = (TextView) findViewById(R.id.bottomtext);
		if (newDesc != null) {
			desc.setText(newDesc);
		}
	}
	
	public void setLayoutBg(int color) {
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.exprow_layout);
		myLayout.setBackgroundColor(color);
	}
	
	public void setClickable(View.OnClickListener listener) {
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.exprow_layout);
		TextView name = (TextView) findViewById(R.id.toptext);
		TextView desc = (TextView) findViewById(R.id.bottomtext);
		myLayout.setOnClickListener(listener);
		name.setOnClickListener(listener);
		desc.setOnClickListener(listener);
	}
}
