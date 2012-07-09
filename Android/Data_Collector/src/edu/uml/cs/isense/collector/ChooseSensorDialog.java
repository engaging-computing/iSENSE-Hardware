package edu.uml.cs.isense.collector;

import java.util.LinkedList;

import edu.uml.cs.isense.collector.SensorCompatibility.SensorTypes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class ChooseSensorDialog extends Activity {

	SensorCompatibility sensors;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choosesensor);
		
		Context mContext = this;
		
		Bundle extras = getIntent().getExtras();
		LinkedList<String> fields = (LinkedList<String>) extras.get("fields");
		sensors = (SensorCompatibility) extras.get("sensors");
		
		View scrollViewLayout = findViewById(R.id.sensorscrollview);
	
		for (String field : fields) {
			if (field.equals(getString(R.string.accel_x)) ||
				field.equals(getString(R.string.accel_y)) ||
				field.equals(getString(R.string.accel_z)) ||
				field.equals(getString(R.string.accel_total))) {
					View v = View.inflate(mContext, R.layout.sensorelement, null);
					CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.sensorlabel);
					ctv.setText(field);
					TextView tv = (TextView) v.findViewById(R.id.subsensorlabel);
					setCompatibility(tv, SensorTypes.ACCELEROMETER);
			}
				
				
					
		}
	}
	
	void setCompatibility(TextView tv, SensorTypes sensor) {
		if (sensor == SensorTypes.ACCELEROMETER) {
				if (sensors.isCompatible(sensor));
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	

}
