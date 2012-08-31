package edu.uml.cs.isense.uppt;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SdCardFailure extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sd_card_failure);
		
		//getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		final Button back = (Button) findViewById(R.id.sd_card_failure_back);
		back.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}	
		});
		
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
}
