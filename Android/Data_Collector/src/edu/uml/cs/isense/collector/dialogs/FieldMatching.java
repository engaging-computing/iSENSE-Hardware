package edu.uml.cs.isense.collector.dialogs;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.supplements.OrientationManager;

public class FieldMatching extends Activity implements OnClickListener {

	LinearLayout scrollViewLayout;
	public static LinkedList<String> acceptedFields;
	
	public static boolean compatible;
	private int nullViewCount = 0;
	private boolean isEmpty = false;
	private TextView errorTV;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.field_matching);
		
		mContext = this;
		compatible = true;
		
		OrientationManager.disableRotation((Activity) mContext);
		
		LinkedList<String> fields = Step1Setup.dfm.getOrderList();

		scrollViewLayout = (LinearLayout) findViewById(R.id.field_matching_view);

		if (fields.isEmpty()) {
			isEmpty = true;
		} else {
			for (String field : fields) {
				View v = View.inflate(mContext, R.layout.field_match_cell, null);

				TextView name = (TextView) v.findViewById(R.id.field_match_cell_name);
				if (field.contains(getString(R.string.null_string))) {
					String subStr = field.replace(getString(R.string.null_string), "");
					name.setText(subStr);
				} else
					name.setText(field);
				
				Spinner selector = (Spinner) v.findViewById(R.id.field_match_cell_spinner);
				
				if (field.contains(getString(R.string.null_string))) {
					selector.setSelection(0);
				} else if (field.equals(getString(R.string.time))) {
					selector.setSelection(Fields.TIME + 1);
				} else if (field.equals(getString(R.string.latitude))) {
					selector.setSelection(Fields.LATITUDE + 1);
				} else if (field.equals(getString(R.string.longitude))) {
					selector.setSelection(Fields.LONGITUDE + 1);
				} else if (field.equals(getString(R.string.accel_x))) {
					selector.setSelection(Fields.ACCEL_X + 1);
				} else if (field.equals(getString(R.string.accel_y))) {
					selector.setSelection(Fields.ACCEL_Y + 1);
				} else if (field.equals(getString(R.string.accel_z))) {
					selector.setSelection(Fields.ACCEL_Z + 1);
				} else if (field.equals(getString(R.string.accel_total))) {
					selector.setSelection(Fields.ACCEL_TOTAL + 1);
				} else if (field.equals(getString(R.string.magnetic_x))) {
					selector.setSelection(Fields.MAG_X);
				} else if (field.equals(getString(R.string.magnetic_y))) {
					selector.setSelection(Fields.MAG_Y + 1);
				} else if (field.equals(getString(R.string.magnetic_z))) {
					selector.setSelection(Fields.MAG_Z + 1);
				} else if (field.equals(getString(R.string.magnetic_total))) {
					selector.setSelection(Fields.MAG_TOTAL + 1);
				} else if (field.equals(getString(R.string.heading_deg))) {
					selector.setSelection(Fields.HEADING_DEG + 1);
				} else if (field.equals(getString(R.string.heading_rad))) {
					selector.setSelection(Fields.HEADING_RAD + 1);
				} else if (field.equals(getString(R.string.temperature_c))) {
					selector.setSelection(Fields.TEMPERATURE_C + 1);
				} else if (field.equals(getString(R.string.temperature_f))) {
					selector.setSelection(Fields.TEMPERATURE_F + 1);
				} else if (field.equals(getString(R.string.temperature_k))) {
					selector.setSelection(Fields.TEMPERATURE_K + 1);
				} else if (field.equals(getString(R.string.pressure))) {
					selector.setSelection(Fields.PRESSURE + 1);
				} else if (field.equals(getString(R.string.luminous_flux))) {
					selector.setSelection(Fields.LIGHT + 1);
				} else if (field.equals(getString(R.string.altitude))) {
					selector.setSelection(Fields.ALTITUDE + 1);
				}

				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					     LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

				layoutParams.setMargins(1, 1, 1, 1);
				
				scrollViewLayout.addView(v, layoutParams);
				v.setOnClickListener(this);

				if (field.equals(getString(R.string.null_string))) {
					nullViewCount++;
				}
			}

		}

		if (nullViewCount == scrollViewLayout.getChildCount()) {
			errorTV = new TextView(mContext);
			errorTV.setBackgroundColor(Color.TRANSPARENT);
			if (isEmpty) errorTV.setText(getString(R.string.invalidProject));
			else {
				compatible = false;
				errorTV.setText(getString(R.string.noCompatibleFields));
			}
			scrollViewLayout.addView(errorTV);
			
			TextView topText = (TextView) findViewById(R.id.field_matching_text);
			topText.setText("");
			ScrollView fieldScroll = (ScrollView) findViewById(R.id.field_matching_scroll);
			fieldScroll.setBackgroundColor(Color.TRANSPARENT);
			
			isEmpty = false;
		}

		Button back = (Button) findViewById(R.id.field_matching_back);
		back.setOnClickListener(this);
		
		Button okay = (Button) findViewById(R.id.field_matching_ok);
		okay.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.field_matching_ok:
			setAcceptedFields();
			setResult(RESULT_OK);
			OrientationManager.enableRotation((Activity) mContext);
			finish();
			break;
			
		case R.id.field_matching_back:
			setResult(RESULT_CANCELED);
			OrientationManager.enableRotation((Activity) mContext);
			finish();
			break;
		}

	}

	private void setAcceptedFields() {
		acceptedFields = new LinkedList<String>();
		scrollViewLayout.removeView(errorTV);
		
		for (int i = 0; i < scrollViewLayout.getChildCount(); i++) {
			View v = scrollViewLayout.getChildAt(i);

			Spinner selector = (Spinner) v.findViewById(R.id.field_match_cell_spinner);
			if (selector.getSelectedItemPosition() != 0)
				acceptedFields.add((String) selector.getSelectedItem());
			else
				acceptedFields.add(getString(R.string.null_string));	
			
		}
		
		buildPrefsString();

	}
	
	private void buildPrefsString() {
		SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
		SharedPreferences.Editor mEdit = mPrefs.edit();
		
		StringBuilder sb = new StringBuilder();
		
		for (String s : acceptedFields) {
			sb.append(s).append(",");
		}
		
		String prefString = sb.toString();
		if(prefString.length() == 0)
			return;
		prefString = prefString.substring(0, prefString.length() - 1);
		
		mEdit.putString("accepted_fields", prefString).commit();
		mEdit.putString("accepted_proj", mPrefs.getString("project_id", "-1")).commit();
		
		System.out.println("Accepted: " + prefString);
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		OrientationManager.enableRotation((Activity) mContext);
		finish();
	}

}