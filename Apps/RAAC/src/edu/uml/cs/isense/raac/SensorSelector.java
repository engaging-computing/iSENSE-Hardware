package edu.uml.cs.isense.raac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import edu.uml.cs.isense.pinports.exceptions.NoConnectionException;
import edu.uml.cs.isense.pinports.pincushion.PinComm;

public class SensorSelector extends Activity implements OnClickListener {

	RadioButton tempBtn, phBtn;
	Button btnOk, btnCancel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensors_selector);

		int currentSensor;
		try {
			currentSensor = MainActivity.ppi.getSetting(PinComm.BTA1);
		} catch (NoConnectionException e) {
			Toast.makeText(this, "Unable to find the PINPoint. Make sure it's turned on and within range of the tablet.", Toast.LENGTH_LONG).show();
			return;
		}
		
		tempBtn = (RadioButton) findViewById(R.id.radioTemp);
		phBtn = (RadioButton) findViewById(R.id.radioPh);
		btnOk = (Button) findViewById(R.id.btn_okay);
		btnCancel = (Button) findViewById(R.id.btn_cancel);

		btnOk.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		
		if(currentSensor == 1) {
			tempBtn.setChecked(true);
			phBtn.setChecked(false);
		} else {
			tempBtn.setChecked(false);
			phBtn.setChecked(true);
		}

	}

	@Override
	public void onClick(View view) {
		if(view == btnCancel) {
			Intent result = new Intent();
			setResult(RESULT_CANCELED,result);
			finish();
		} else if(view==btnOk) {
			if(tempBtn.isChecked()) {
				MainActivity.ppi.setSetting(PinComm.BTA1, 1);
			} else {
				MainActivity.ppi.setSetting(PinComm.BTA1, 24);
			}
			finish();
		}
	}

}
