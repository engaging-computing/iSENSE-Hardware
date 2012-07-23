package edu.uml.cs.isense.collector;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class NoGps extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_gps);
		
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		final Button yes = (Button) findViewById(R.id.no_gps_yes);
		yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
		
		final Button no = (Button) findViewById(R.id.no_gps_no);
		no.setOnClickListener(new OnClickListener() {
			@Override
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