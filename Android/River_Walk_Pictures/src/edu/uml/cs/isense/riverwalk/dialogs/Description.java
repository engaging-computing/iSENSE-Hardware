package edu.uml.cs.isense.riverwalk.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import edu.uml.cs.isense.riverwalk.R;

public class Description extends Activity {
	
	public static final String RADIO_SELECTION = "radio_selection";
	
	private RadioButton rb1;
	private RadioButton rb2;
	private RadioButton rb3;
	private RadioButton rb4;
	private RadioButton rb5;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.description);
		
		rb1 = (RadioButton) findViewById(R.id.radioButton1);
		rb2 = (RadioButton) findViewById(R.id.radioButton2);
		rb3 = (RadioButton) findViewById(R.id.radioButton3);
		rb4 = (RadioButton) findViewById(R.id.radioButton4);
		rb5 = (RadioButton) findViewById(R.id.radioButton5);
		
		if (android.os.Build.VERSION.SDK_INT >= 11)
			setFinishOnTouchOutside(false);
		
		final Button ok = (Button) findViewById(R.id.description_okay);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int selection = getRadioSelection();
				if (selection != -1) {
					Intent ret = new Intent();
					ret.putExtra(RADIO_SELECTION, selection);
					setResult(RESULT_OK, ret);
					finish();
				} else {
					// fail
				}
				
			}
		});
		
		
	}
	
	@Override
	public void onBackPressed() {}
	
	int getRadioSelection() {
		if (rb1.isChecked())
			return 1;
		else if (rb2.isChecked())
			return 2;
		else if (rb3.isChecked())
			return 3;
		else if (rb4.isChecked())
			return 4;
		else if (rb5.isChecked())
			return 5;
		
		return -1;
		
	}
	
}