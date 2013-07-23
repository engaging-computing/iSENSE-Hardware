package edu.uml.cs.isense.datawalk_v2.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.uml.cs.isense.R;


public class NoConnect extends Activity {

	public static final int RESULT_LEAVE = 2; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_connect);
		
		//getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		
		
		final Button no = (Button) findViewById(R.id.no_connect_try_again);
		no.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		

		final Button yesSave = (Button) findViewById(R.id.yes_save);
		yesSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
				setResult(RESULT_OK);
				finish();
			}
		});
	}//ends onCreate
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_LEAVE);
		finish();
	}//ends onBackPressed
	
}//ends noConnect class