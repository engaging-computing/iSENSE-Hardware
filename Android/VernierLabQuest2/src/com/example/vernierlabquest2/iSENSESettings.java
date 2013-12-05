package com.example.vernierlabquest2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;
import edu.uml.cs.isense.waffle.Waffle;

public class iSENSESettings extends Activity {
	private String tag = "iSENSESettings";
	private Button save;
	private Button cancel;
	private Button QRCode;
	private EditText isense_user;
	private EditText isense_pass;
	private EditText isense_expid;
	private ToggleButton isense_dev_mode;
	private Waffle w;
	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.isense_settings);
		w = new Waffle(this);
		
		save = (Button) findViewById(R.id.isense_save);
		cancel = (Button) findViewById(R.id.isense_cancel);
		QRCode = (Button) findViewById(R.id.isense_scan_QR);
		isense_user = (EditText) findViewById(R.id.isense_user);
		isense_pass = (EditText) findViewById(R.id.isense_pass);
		isense_expid = (EditText) findViewById(R.id.isense_expid);
		isense_dev_mode = (ToggleButton) findViewById(R.id.isense_dev_mode);

		sp = getSharedPreferences("isense_settings", 0);
		isense_user.setText(sp.getString("isense_user", ""));
		isense_pass.setText(sp.getString("isense_pass", ""));
		isense_expid.setText(sp.getString("isense_expid", ""));
		isense_dev_mode.setChecked(sp.getLong("isense_dev_mode", 0) == 1);
		QRCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				w.make("Feature Disabled",Waffle.LENGTH_SHORT,Waffle.IMAGE_X);
			}
			
		});
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences.Editor e = sp.edit();
				e.putString("isense_user", isense_user.getText().toString());
				e.putString("isense_pass", isense_pass.getText().toString());
				e.putString("isense_expid", isense_expid.getText().toString());
				if (isense_dev_mode.isChecked()) {
					e.putLong("isense_dev_mode", 1);
				} else {
					e.putLong("isense_dev_mode", 0);
				}
				e.commit();
				Log.v(tag,"Saved");
				setResult(RESULT_OK);
				finish();
			}

		});
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.v(tag, "Canceled");
				setResult(RESULT_CANCELED);
				finish();
			}

		});
	}

}
