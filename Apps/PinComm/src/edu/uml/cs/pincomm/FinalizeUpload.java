package edu.uml.cs.pincomm;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class FinalizeUpload extends Activity implements OnClickListener, TextWatcher, OnCheckedChangeListener {

	Button btnUp, btnCancel;
	CheckBox btnGeo;
	EditText name, desc, street, city;
	String myDesc;
	String myStreet;
	String myCity;
	String myName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.uploadfinalizebox);

		name = (EditText) findViewById(R.id.nameField);
		desc = (EditText) findViewById(R.id.descField);
		street = (EditText) findViewById(R.id.streetfield);
		city = (EditText) findViewById(R.id.cityField);

		btnUp = (Button) findViewById(R.id.btnUpload);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnGeo = (CheckBox) findViewById(R.id.btn_geolocate);

		btnUp.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		btnGeo.setOnCheckedChangeListener(this);

		name.addTextChangedListener(this);
		desc.addTextChangedListener(this);
		street.addTextChangedListener(this);
		city.addTextChangedListener(this);

		btnUp.setEnabled(false);

	}

	@Override
	public void onClick(View v) {

		if (v == btnUp) {

			myName = name.getText().toString();
			myDesc = desc.getText().toString();
			myStreet = street.getText().toString();
			myCity = city.getText().toString();

			Intent result = new Intent();
			result.putExtra("myName", myName);
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

	public void setLocationFields(Location newLocation) {
		
		double latitude = newLocation.getLatitude();
        double longitude = newLocation.getLongitude();
        
        Geocoder gc = new Geocoder(this, Locale.getDefault());
        try {
			List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
			street.setText(addresses.get(0).getAddressLine(0));
			city.setText(addresses.get(0).getAddressLine(1));
		} catch (IOException e) {
			e.printStackTrace();
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

		myName = name.getText().toString();
		myDesc = desc.getText().toString();
		myStreet = street.getText().toString();
		myCity = city.getText().toString();

		if (myName.equals("") || myDesc.equals("") || myStreet.equals("") || myCity.equals("")) {
			btnUp.setEnabled(false);
		} else{
			btnUp.setEnabled(true);
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if ( buttonView == btnGeo) {
			if(isChecked) {
				LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

				LocationListener locationListener = new LocationListener() {
					public void onLocationChanged(Location location) {
						// Called when a new location is found by the network location provider.
						setLocationFields(location);
					}

					public void onStatusChanged(String provider, int status, Bundle extras) {}

					public void onProviderEnabled(String provider) {}

					public void onProviderDisabled(String provider) {}
				};

				// Register the listener with the Location Manager to receive location updates
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
				
				street.setEnabled(false);
				city.setEnabled(false);
				
				Toast.makeText(this, "Finding location, please wait...", Toast.LENGTH_LONG).show(); 
			} else {
				street.setEnabled(true);
				city.setEnabled(true);
			}
		}
	}

}
