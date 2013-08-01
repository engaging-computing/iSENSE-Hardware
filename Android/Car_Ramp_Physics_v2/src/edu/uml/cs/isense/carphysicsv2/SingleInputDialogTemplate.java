package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.isense.waffle.Waffle;

public class SingleInputDialogTemplate extends Activity {

	Button pos, neg;
	EditText input;
	Waffle w;
	Spinner spinner;
	String length;
	
	private int indexOfLength(){
		if (length.equalsIgnoreCase("1"))
			return 0;
		else if (length.equalsIgnoreCase("2"))
			return 1;
		else if (length.equalsIgnoreCase("5"))
			return 2;
		else if (length.equalsIgnoreCase("10"))
			return 3;
		else if (length.equalsIgnoreCase("30"))
			return 4;
		else
			return 5;
	}

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.single_input_template);

		setTitle(getIntent().getExtras().getString("title"));
		TextView messageBox = (TextView) findViewById(R.id.messageBox2);
		messageBox.setText(getIntent().getExtras().getString("message"));

		pos = (Button) findViewById(R.id.positive2);
		neg = (Button) findViewById(R.id.negative2);
		spinner = (Spinner) findViewById(R.id.length_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.lengths_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH",
				0);
		length = String.valueOf(prefs.getInt("length", 10));
		
		spinner.setSelection(this.indexOfLength(), true);

		w = new Waffle(CarRampPhysicsV2.mContext);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				length = parent.getItemAtPosition(pos).toString().substring(0, parent.getItemAtPosition(pos).toString().indexOf(" "));
			}

			public void onNothingSelected(AdapterView<?> parent) {
				length = "10";
			}
		});

		pos.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

					Intent i = new Intent();
					i.putExtra("input", length);
					setResult(RESULT_OK, i);
					finish();

			}
		});

		neg.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();

			}
		});
	}

}
