package edu.uml.cs.isense.carphysicsv2.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.isense.carphysicsv2.CarRampPhysicsV2;
import edu.uml.cs.isense.carphysicsv2.R;
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

	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    
	    // Check which radio button was clicked
	    length = ((RadioButton) view).getText().toString().substring(0, ((RadioButton) view).getText().toString().indexOf(" "));
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.single_input_template);

		setTitle(getIntent().getExtras().getString("title"));
		TextView messageBox = (TextView) findViewById(R.id.messageBox2);
		messageBox.setText(getIntent().getExtras().getString("message"));

		pos = (Button) findViewById(R.id.positive2);
		neg = (Button) findViewById(R.id.negative2);
		RadioButton defaultR;
		
		SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH",
				0);
		length = String.valueOf(prefs.getInt("length", 10));
		
		switch(indexOfLength()){
		
			case 0:
				defaultR = (RadioButton) findViewById(R.id.radio0);
				break;
			case 1:
				defaultR = (RadioButton) findViewById(R.id.radio1);
				break;
			case 2:
				defaultR = (RadioButton) findViewById(R.id.radio2);
				break;
			case 3:
				defaultR = (RadioButton) findViewById(R.id.radio3);
				break;
			case 4:
				defaultR = (RadioButton) findViewById(R.id.radio4);
				break;
			default:
				defaultR = (RadioButton) findViewById(R.id.radio5);
				break;
		}
		
		defaultR.setChecked(true);

		w = new Waffle(CarRampPhysicsV2.mContext);


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
