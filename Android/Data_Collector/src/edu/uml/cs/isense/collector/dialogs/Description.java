package edu.uml.cs.isense.collector.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.collector.DataCollector;
import edu.uml.cs.isense.collector.R;

public class Description extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.description);
		
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		final EditText et = (EditText) findViewById(R.id.description_input);
		
		final Button yes = (Button) findViewById(R.id.description_upload);
		yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iRet = new Intent(Description.this, DataCollector.class);
				iRet.putExtra("description", et.getText().toString());
				setResult(RESULT_OK, iRet);
				finish();
			}
		});
		
		final Button no = (Button) findViewById(R.id.description_cancel);
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
		//setResult(RESULT_CANCELED);
		//finish();
	}
	
}
