package edu.uml.cs.isense.collector.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.uml.cs.isense.collector.R;

public class NeedConnectivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.need_connectivity);
		
		final Button ok = (Button) findViewById(R.id.no_connectivity_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
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