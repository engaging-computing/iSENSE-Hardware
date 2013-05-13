package edu.uml.cs.isense.raac;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SetName extends Activity implements OnClickListener {
	Button ok;
	EditText namefield;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_experiment);

		namefield = (EditText) findViewById(R.id.new_name);
		ok     = (Button)   findViewById(R.id.name_ok);
		ok.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == ok) {
			if (namefield.getText().toString().equals(""))
				;
			else {
				Intent result = new Intent();
				result.putExtra("groupname",namefield.getText().toString());
				setResult(RESULT_OK, result);
				finish();
			}
		}
	}	
}
