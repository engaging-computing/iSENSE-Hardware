package edu.uml.cs.isense.datawalk_v2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Spinner;

import edu.uml.cs.isense.R;

public class PrefsTwoClone extends Activity{
	private Spinner spinner1;
	
	
	@Override
	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.spinner);
	
		addListenerOnButton();
		addListenerOnSpinnerItemSelection();
	
	}
	public void addListenerOnSpinnerItemSelection(){
		spinner1 = (Spinner)findViewById(R.id.spinner1);
		spinner1.setSelection(3);
		spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
	}
	
	public void addListenerOnButton(){
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		
	}
}//ends About class
