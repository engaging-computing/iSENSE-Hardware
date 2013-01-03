package com.example.vernierlabquest2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;
import edu.uml.cs.isense.waffle.Waffle;

public class LabQuestSettings extends Activity {
	private String tag = "LabQuestSettings";
	private Button save;
	private Button cancel;
	private EditText labquest_ip;
	private ToggleButton labquest_manual_field_match;
	private Button QRCode;
	private Button labquest_manual_field_match_conf;
	private Waffle w;
	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.labquest_settings);
		w = new Waffle(this);

		save = (Button) findViewById(R.id.labquest_save);
		cancel = (Button) findViewById(R.id.labquest_cancel);
		labquest_ip = (EditText) findViewById(R.id.labquest_ip);
		QRCode = (Button) findViewById(R.id.labquest_scan_QR);
		labquest_manual_field_match = (ToggleButton) findViewById(R.id.labquest_manual_field_match);
		labquest_manual_field_match_conf = (Button) findViewById(R.id.labquest_manual_field_match_conf);
		sp = getSharedPreferences("labquest_settings", 0);
		labquest_ip.setText(sp.getString("labquest_ip", ""));
		labquest_manual_field_match.setChecked(sp.getLong("labquest_manual_field_match", 0) == 1);

		QRCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				w.make("Feature Disabled", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
			}

		});
		labquest_manual_field_match_conf.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.v(tag, "Start Activity LabQuestFieldMatching");
				Intent i = new Intent(LabQuestSettings.this, LabQuestFieldMatching.class);
				startActivity(i);
			}
			
		});

		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences.Editor e = sp.edit();
				e.putString("labquest_ip", labquest_ip.getText().toString());
				if (labquest_manual_field_match.isChecked()) {
					e.putLong("labquest_manual_field_match", 1);
				} else {
					e.putLong("labquest_manual_field_match", 0);
				}
				e.commit();
				Log.v(tag, "Saved");
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
