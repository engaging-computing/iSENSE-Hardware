package com.example.vernierlabquest2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LabQuestSettings extends Activity {

	private Button save;
	private Button cancel;
	private EditText labquest_ip;
	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.labquest_settings);
		
		save = (Button) findViewById(R.id.labquest_save);
		cancel = (Button) findViewById(R.id.labquest_cancel);
		labquest_ip = (EditText) findViewById(R.id.labquest_ip);
		
		sp = getSharedPreferences("labquest_settings", 0);
		labquest_ip.setText(sp.getString("labquest_ip", ""));
		
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences.Editor e = sp.edit();
				e.putString("labquest_ip", labquest_ip.getText().toString());
				e.commit();
				setResult(RESULT_OK);
				finish();
			}

		});
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}

		});
	}

}
