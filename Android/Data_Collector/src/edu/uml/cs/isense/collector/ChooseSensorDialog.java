package edu.uml.cs.isense.collector;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.SensorCompatibility.SensorTypes;

public class ChooseSensorDialog extends Activity implements OnClickListener {

	SensorCompatibility sensors;
	LinearLayout scrollViewLayout;
	public static LinkedList<String> acceptedFields;
	
	private int nullViewCount = 0;
	private boolean isEmpty = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choosesensor);

		Context mContext = this;

		LinkedList<String> fields = DataCollector.dfm.order;

		sensors = DataCollector.sc;

		scrollViewLayout = (LinearLayout) findViewById(R.id.sensorscrollview);

		if (fields.isEmpty()) {
			isEmpty = true;
		} else {
			for (String field : fields) {
				View v = View.inflate(mContext, R.layout.sensorelement, null);

				CheckedTextView ctv = (CheckedTextView) v
						.findViewById(R.id.sensorlabel);
				ctv.setText(field);

				TextView tv = (TextView) v.findViewById(R.id.subsensorlabel);

				if (field.equals(getString(R.string.null_string))
						|| field.equals(getString(R.string.time))
						|| field.equals(getString(R.string.latitude))
						|| field.equals(getString(R.string.longitude))) {
					setCompatibility(tv, ctv);

				} else if (field.equals(getString(R.string.accel_x))
						|| field.equals(getString(R.string.accel_y))
						|| field.equals(getString(R.string.accel_z))
						|| field.equals(getString(R.string.accel_total))) {
					setCompatibility(tv, ctv, SensorTypes.ACCELEROMETER);

				} else if (field.equals(getString(R.string.magnetic_x))
						|| field.equals(getString(R.string.magnetic_y))
						|| field.equals(getString(R.string.magnetic_z))
						|| field.equals(getString(R.string.magnetic_total))) {
					setCompatibility(tv, ctv, SensorTypes.MAGNETIC_FIELD);

				} else if (field.equals(getString(R.string.heading_deg))
						|| field.equals(getString(R.string.heading_rad))) {
					setCompatibility(tv, ctv, SensorTypes.ORIENTATION);

				} else if (field.equals(getString(R.string.temperature))) {
					setCompatibility(tv, ctv, SensorTypes.AMBIENT_TEMPERATURE);

				} else if (field.equals(getString(R.string.pressure))) {
					setCompatibility(tv, ctv, SensorTypes.PRESSURE);

				} else if (field.equals(getString(R.string.luminous_flux))) {
					setCompatibility(tv, ctv, SensorTypes.LIGHT);

				} else if (field.equals(getString(R.string.altitude))) {
					setCompatibility(tv, ctv, SensorTypes.PRESSURE,
							SensorTypes.AMBIENT_TEMPERATURE);
				}

				scrollViewLayout.addView(v);
				v.setOnClickListener(this);

				if (field.equals(getString(R.string.null_string))) {
					nullViewCount++;
					v.setVisibility(View.GONE);
				}
			}

		}

		if (nullViewCount == scrollViewLayout.getChildCount()) {
			TextView tv = new TextView(mContext);
			if (isEmpty) tv.setText(getString(R.string.invalidExperiment));
			else tv.setText(getString(R.string.noCompatibleFields));
			scrollViewLayout.addView(tv);
			
			isEmpty = false;
		}

		Button okay = (Button) findViewById(R.id.sensor_ok);
		okay.setOnClickListener(this);

		Button back = (Button) findViewById(R.id.sensor_back);
		back.setOnClickListener(this);

	}

	// Automatically Compatible
	void setCompatibility(TextView tv, CheckedTextView ctv) {
		tv.setTextColor(Color.GREEN);
		tv.setText(R.string.compatible);
		ctv.setChecked(true);
	}

	// Check compatibility against SensorTypes
	void setCompatibility(TextView tv, CheckedTextView ctv, SensorTypes sensor) {
		if (sensors.isCompatible(sensor)) {
			tv.setTextColor(Color.GREEN);
			tv.setText(R.string.compatible);
			ctv.setChecked(true);
		} else {
			tv.setTextColor(Color.RED);
			tv.setText(R.string.incompatible);
			ctv.setChecked(false);
			ctv.setCheckMarkDrawable(R.drawable.red_x);
		}
	}

	// Double compatibility check for fields like altitude
	void setCompatibility(TextView tv, CheckedTextView ctv,
			SensorTypes sensor1, SensorTypes sensor2) {
		if (sensors.isCompatible(sensor1) && sensors.isCompatible(sensor2)) {
			tv.setTextColor(Color.GREEN);
			tv.setText(R.string.compatible);
			ctv.setChecked(true);
		} else {
			tv.setTextColor(Color.RED);
			tv.setText(R.string.incompatible);
			ctv.setChecked(false);
			ctv.setCheckMarkDrawable(R.drawable.red_x);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.sensor_ok:
			setAcceptedFields();
			setResult(RESULT_OK);
			finish();
			break;

		case R.id.sensor_back:
			setResult(RESULT_CANCELED);
			finish();
			break;

		case R.id.check_layout:
			CheckedTextView ctv = (CheckedTextView) v
					.findViewById(R.id.sensorlabel);
			if (ctv.isChecked())
				ctv.setCheckMarkDrawable(R.drawable.red_x);
			else
				ctv.setCheckMarkDrawable(R.drawable.bluechecksmall);
			ctv.toggle();
			break;
		}

	}

	private void setAcceptedFields() {
		acceptedFields = new LinkedList<String>();
		for (int i = 0; i < scrollViewLayout.getChildCount(); i++) {
			View v = scrollViewLayout.getChildAt(i);

			CheckedTextView ctv = (CheckedTextView) v
					.findViewById(R.id.sensorlabel);
			if (ctv.isChecked())
				acceptedFields.add(ctv.getText().toString());
			else
				acceptedFields.add(getString(R.string.null_string));
		}

	}

}
