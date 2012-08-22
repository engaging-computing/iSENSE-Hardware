package edu.uml.cs.isense.uppt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

public class Experiment extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.experiment);
		
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		final SharedPreferences mPrefs = getSharedPreferences("eid", 0);
		final EditText eid = (EditText) findViewById(R.id.experimentInput);
		eid.setText(mPrefs.getString("eid", ""));
		
		final Button ok = (Button) findViewById(R.id.experiment_ok);
		ok.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (eid.length() == 0) {
					setResult(RESULT_CANCELED);
					finish();
				} else {
					Intent send = new Intent(Experiment.this, Main.class);
					send.putExtra("eid", eid.getText().toString());
					setResult(RESULT_OK, send);
					finish();
				}
			}	
		});
		
		final Button cancel = (Button) findViewById(R.id.experiment_cancel);
		cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}	
		});
		
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}
}
