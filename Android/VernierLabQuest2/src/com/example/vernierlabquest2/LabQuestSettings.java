package com.example.vernierlabquest2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.waffle.Waffle;

public class LabQuestSettings extends Activity {
	private String tag = "LabQuestSettings";
	private Button save;
	private Button cancel;
	private EditText labquest_ip;
	private Button QRCode;
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
		sp = getSharedPreferences("labquest_settings", 0);
		labquest_ip.setText(sp.getString("labquest_ip", ""));

		QRCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				w.make("Feature Disabled", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
			}

		});

		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences.Editor e = sp.edit();
				e.putString("labquest_ip", labquest_ip.getText().toString());
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
