package edu.uml.cs.isense.collector.dialogs;

import android.annotation.SuppressLint;
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

@SuppressLint("NewApi")
public class Description extends Activity {
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.description);
		
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 11) {
			this.setFinishOnTouchOutside(false);
		}
		
		final EditText et = (EditText) findViewById(R.id.description_input);
		
		final Button upload = (Button) findViewById(R.id.description_upload);
		upload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iRet = new Intent(Description.this, DataCollector.class);
				iRet.putExtra("description", et.getText().toString());
				setResult(RESULT_OK, iRet);
				finish();
			}
		});
		
		final Button delete = (Button) findViewById(R.id.description_delete);
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
	}
	
	@Override
	public void onBackPressed() {
	}
	
}
