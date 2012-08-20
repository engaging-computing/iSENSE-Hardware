package edu.uml.cs.raac;

import android.app.Activity;
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
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("group_name", namefield.getText().toString());
				editor.putBoolean("firstrun", false);
				editor.commit();
			}
		}

		finish();
	}	
}
