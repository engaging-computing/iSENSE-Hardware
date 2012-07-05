package edu.uml.cs.pincomm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class FinalizeUpload extends Activity implements OnClickListener, TextWatcher {

	Button btnUp, btnCancel, btnGeo;
	EditText desc, street, city;
	String myDesc;
	String myStreet;
	String myCity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.uploadfinalizebox);
		
		SharedPreferences myPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
				
		desc = (EditText) findViewById(R.id.descField);
		street = (EditText) findViewById(R.id.streetfield);
		city = (EditText) findViewById(R.id.cityField);
		
		btnUp = (Button) findViewById(R.id.btnUpload);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnGeo = (Button) findViewById(R.id.btn_geolocate);
		
		btnUp.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		btnGeo.setOnClickListener(this);
		
		desc.addTextChangedListener(this);
		street.addTextChangedListener(this);
		city.addTextChangedListener(this);
		
		btnUp.setEnabled(false);
		
	}
	
	@Override
	public void onClick(View v) {
		
		if (v == btnUp) {
		
			myDesc = desc.getText().toString();
			myStreet = street.getText().toString();
			myCity = city.getText().toString();
			
			Intent result = new Intent();
			result.putExtra("myDesc", myDesc);
			result.putExtra("myStreet", myStreet);
			result.putExtra("myCity", myCity);
			
	        setResult(RESULT_OK, result);
			finish();
			
		} else if ( v == btnCancel ) {
			Intent result = new Intent();
			setResult(RESULT_CANCELED,result);
			finish();
		}
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
		myDesc = desc.getText().toString();
		myStreet = street.getText().toString();
		myCity = city.getText().toString();
		
		if (myDesc.equals("") || myStreet.equals("") || myCity.equals("")) {
			btnUp.setEnabled(false);
		} else{
			btnUp.setEnabled(true);
		}
		
	}

}
