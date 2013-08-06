package edu.uml.cs.isense.datawalk_v2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;

import edu.uml.cs.isense.datawalk_v2.R;

public class PrefsTwoClone extends Activity{
	private Spinner spinner1;
	
	
	@Override
	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.spinner);
	
		/* final Button noSpinner = (Button) findViewById(R.id.cancelSpinnerB);
		noSpinner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});*/
		
		final Button yesSaveSpinner = (Button) findViewById(R.id.confirmSpinnerB);
		yesSaveSpinner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
				setResult(RESULT_OK);
				finish();
			}
		});		
				
		addListenerOnButton();
		addListenerOnSpinnerItemSelection();
		
	}


	public void addListenerOnSpinnerItemSelection(){
		spinner1 = (Spinner)findViewById(R.id.spinner1);
		spinner1.setSelection(CustomOnItemSelectedListener.savedValueInt);
		spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
	}
	
	public void addListenerOnButton(){
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		
	}
}//ends About class
