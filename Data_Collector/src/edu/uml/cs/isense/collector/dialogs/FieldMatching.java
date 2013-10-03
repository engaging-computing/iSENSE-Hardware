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
import edu.uml.cs.isense.dfm.SensorCompatibility;
import edu.uml.cs.isense.dfm.SensorCompatibility.SensorTypes;
import edu.uml.cs.isense.supplements.OrientationManager;

public class FieldMatching extends Activity implements OnClickListener {

	SensorCompatibility sensors;
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
		sensors = Step1Setup.sc;

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
				
				TextView compat = (TextView) v.findViewById(R.id.field_match_cell_compatible);
				Spinner selector = (Spinner) v.findViewById(R.id.field_match_cell_spinner);
				
				if (field.contains(getString(R.string.null_string))) {
					setCompatibilityAndSelection(compat, selector, -1);
				} else if (field.equals(getString(R.string.time))) {
					setCompatibilityAndSelection(compat, selector, Fields.TIME);
				} else if (field.equals(getString(R.string.latitude))) {
					setCompatibilityAndSelection(compat, selector, Fields.LATITUDE);
				} else if (field.equals(getString(R.string.longitude))) {
					setCompatibilityAndSelection(compat, selector, Fields.LONGITUDE);
				} else if (field.equals(getString(R.string.accel_x))) {
					setCompatibilityAndSelection(compat, selector, Fields.ACCEL_X, SensorTypes.ACCELEROMETER);
				} else if (field.equals(getString(R.string.accel_y))) {
					setCompatibilityAndSelection(compat, selector, Fields.ACCEL_Y, SensorTypes.ACCELEROMETER);
				} else if (field.equals(getString(R.string.accel_z))) {
					setCompatibilityAndSelection(compat, selector, Fields.ACCEL_Z, SensorTypes.ACCELEROMETER);
				} else if (field.equals(getString(R.string.accel_total))) {
					setCompatibilityAndSelection(compat, selector, Fields.ACCEL_TOTAL, SensorTypes.ACCELEROMETER);
				} else if (field.equals(getString(R.string.magnetic_x))) {
					setCompatibilityAndSelection(compat, selector, Fields.MAG_X, SensorTypes.MAGNETIC_FIELD);
				} else if (field.equals(getString(R.string.magnetic_y))) {
					setCompatibilityAndSelection(compat, selector, Fields.MAG_Y, SensorTypes.MAGNETIC_FIELD);
				} else if (field.equals(getString(R.string.magnetic_z))) {
					setCompatibilityAndSelection(compat, selector, Fields.MAG_Z, SensorTypes.MAGNETIC_FIELD);
				} else if (field.equals(getString(R.string.magnetic_total))) {
					setCompatibilityAndSelection(compat, selector, Fields.MAG_TOTAL, SensorTypes.MAGNETIC_FIELD);
				} else if (field.equals(getString(R.string.heading_deg))) {
					setCompatibilityAndSelection(compat, selector, Fields.HEADING_DEG, SensorTypes.ORIENTATION);
				} else if (field.equals(getString(R.string.heading_rad))) {
					setCompatibilityAndSelection(compat, selector, Fields.HEADING_RAD, SensorTypes.ORIENTATION);
				} else if (field.equals(getString(R.string.temperature_c))) {
					setCompatibilityAndSelection(compat, selector, Fields.TEMPERATURE_C, SensorTypes.AMBIENT_TEMPERATURE);
				} else if (field.equals(getString(R.string.temperature_f))) {
					setCompatibilityAndSelection(compat, selector, Fields.TEMPERATURE_F, SensorTypes.AMBIENT_TEMPERATURE);
				} else if (field.equals(getString(R.string.temperature_k))) {
					setCompatibilityAndSelection(compat, selector, Fields.TEMPERATURE_K, SensorTypes.AMBIENT_TEMPERATURE);
				} else if (field.equals(getString(R.string.pressure))) {
					setCompatibilityAndSelection(compat, selector, Fields.PRESSURE, SensorTypes.PRESSURE);
				} else if (field.equals(getString(R.string.luminous_flux))) {
					setCompatibilityAndSelection(compat, selector, Fields.LIGHT, SensorTypes.LIGHT);
				} else if (field.equals(getString(R.string.altitude))) {
					setCompatibilityAndSelection(compat, selector, Fields.ALTITUDE, SensorTypes.PRESSURE, SensorTypes.AMBIENT_TEMPERATURE);
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
				errorTV.setText(getString(R.string.noCompatibleFields));
				compatible = false;
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
	
	// Always compatible
	void setCompatibilityAndSelection(TextView compat, Spinner selector, int index) {
		if (index == -1) {
			compat.setTextColor(Color.parseColor("#00AA00"));
			compat.setText("");
			selector.setSelection(index + 1);
		} else {
			compat.setTextColor(Color.parseColor("#00AA00"));
			compat.setText(R.string.compatible);
			selector.setSelection(index + 1);
		}
		
	}
	
	// Check compatibility against SensorTypes
	void setCompatibilityAndSelection(TextView compat, Spinner selector, int index, SensorTypes sensor) {
		if (sensors.isCompatible(sensor)) {
			compat.setTextColor(Color.parseColor("#00AA00"));
			compat.setText(R.string.compatible);
			selector.setSelection(index + 1);
		} else {
			compat.setTextColor(Color.parseColor("#AA0000"));
			compat.setText(R.string.incompatible);
			selector.setSelection(index + 1);
		}
	}
	
	// Double compatibility check for fields like altitude
	void setCompatibilityAndSelection(TextView compat, Spinner selector, int index, SensorTypes sensor1, SensorTypes sensor2) {
		if (sensors.isCompatible(sensor1) && sensors.isCompatible(sensor2)) {
			compat.setTextColor(Color.parseColor("#00AA00"));
			compat.setText(R.string.compatible);
			selector.setSelection(index + 1);
		} else {
			compat.setTextColor(Color.parseColor("#AA0000"));
			compat.setText(R.string.incompatible);
			selector.setSelection(index + 1);
		}
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
		//setAcceptedFields();
		setResult(RESULT_CANCELED);
		OrientationManager.enableRotation((Activity) mContext);
		finish();
	}

}