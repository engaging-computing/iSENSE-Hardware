package edu.uml.cs.isense.collector.splash;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.waffle.Waffle;

public class ProjectCreate extends Activity {

	public static Context mContext;
	public static Waffle w;
	
	private Spinner fieldSpin;
	private LinearLayout fieldScroll;
	
	private static final int FIELD_TYPE_TIMESTAMP 	= 0;
	private static final int FIELD_TYPE_NUMBER	  	= 1;
	private static final int FIELD_TYPE_TEXT		= 2;
	private static final int FIELD_TYPE_LOCATION	= 3;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_create);

		mContext = this;
		w = new Waffle(mContext);
		
		// Action bar customization for API >= 14
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			ActionBar bar = getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#111133")));
			bar.setIcon(getResources()
					.getDrawable(R.drawable.rsense_logo_right));
			bar.setDisplayShowTitleEnabled(false);
			int actionBarTitleId = Resources.getSystem().getIdentifier(
					"action_bar_title", "id", "android");
			if (actionBarTitleId > 0) {
				TextView title = (TextView) findViewById(actionBarTitleId);
				if (title != null) {
					title.setTextColor(Color.WHITE);
					title.setTextSize(24.0f);
				}
			}
		}

		// Set listeners for the buttons
		final Button cancel = (Button) findViewById(R.id.project_create_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		final Button ok = (Button) findViewById(R.id.project_create_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// stuff
			}
		});
		
		fieldSpin = (Spinner) findViewById(R.id.project_create_fields_spinner);
		fieldSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				addFieldType(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		fieldScroll = (LinearLayout) findViewById(R.id.project_create_fields_view);

	}
	
	private void addFieldType(int tag) {
		
		View v = null;
		
		if (tag == FIELD_TYPE_LOCATION) {
			v = View.inflate(mContext, R.layout.project_field_location, null);
		} else {
			v = View.inflate(mContext, R.layout.project_field, null);
		}
		
		v.setTag(tag);
		
		switch(tag) {
		case FIELD_TYPE_TIMESTAMP:
			EditText time = (EditText) v.findViewById(R.id.project_field_name);
			time.setEnabled(false);
			time.setText("Time");
			time.setBackgroundColor(Color.TRANSPARENT);
			time.setTextColor(Color.BLACK);
			break;
			
		// TODO - the other cases (e.g. time have units? text def. has no units, change label hints, etc.)
			
		default:
			break;
		
		}
		
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutParams.setMargins(1, 1, 1, 1);
		
		fieldScroll.addView(v, layoutParams);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}