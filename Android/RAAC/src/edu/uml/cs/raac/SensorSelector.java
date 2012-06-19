package edu.uml.cs.raac;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SensorSelector extends Activity implements OnClickListener, OnItemSelectedListener {

	Spinner bta1, bta2, mini1, mini2;
	Button btnOk, btnCancel;

	String return1, return2, return3, return4;
	String retName1, retName2, retName3, retName4;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensors_selector);

		SharedPreferences prefs = getSharedPreferences("SENSORS", 0);
		
		bta1 = (Spinner) findViewById(R.id.btaSpin1);
		bta2 = (Spinner) findViewById(R.id.btaSpin2);
		mini1 = (Spinner) findViewById(R.id.miniSpin1);
		mini2 = (Spinner) findViewById(R.id.miniSpin2);
		btnOk = (Button) findViewById(R.id.btn_okay);
		btnCancel = (Button) findViewById(R.id.btn_cancel);

		bta1.setOnItemSelectedListener(this);
		bta2.setOnItemSelectedListener(this);
		mini1.setOnItemSelectedListener(this);
		mini2.setOnItemSelectedListener(this);
		btnOk.setOnClickListener(this);
		btnCancel.setOnClickListener(this);

		ArrayAdapter<CharSequence> btaAdapter = ArrayAdapter.createFromResource(
				this, R.array.btasensors, android.R.layout.simple_spinner_item);
		btaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<CharSequence> miniAdapter = ArrayAdapter.createFromResource(
				this, R.array.minisensors, android.R.layout.simple_spinner_item);
		miniAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		bta1.setAdapter(btaAdapter);
		bta2.setAdapter(btaAdapter);
		mini1.setAdapter(miniAdapter);
		mini2.setAdapter(miniAdapter);
		
		bta1.setSelection(prefs.getInt("bta1pos", 0));
		bta2.setSelection(prefs.getInt("bta2pos", 0));
		mini1.setSelection(prefs.getInt("mini1pos", 0));
		mini2.setSelection(prefs.getInt("mini2pos", 0));

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		
		if(parent == bta1) {
			return1 = getFormula(parent.getItemAtPosition(position).toString());
			retName1 = parent.getItemAtPosition(position).toString();
		} else if(parent == bta2) {
			return2 = getFormula(parent.getItemAtPosition(position).toString());
			retName2 = parent.getItemAtPosition(position).toString();
		} else if(parent == mini1) {
			return3 = getFormula(parent.getItemAtPosition(position).toString());
			retName3 = parent.getItemAtPosition(position).toString();
		} else if(parent == mini2) {
			return4 = getFormula(parent.getItemAtPosition(position).toString());
			retName4 = parent.getItemAtPosition(position).toString();
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View view) {
		if(view == btnCancel) {
			Intent result = new Intent();
			setResult(RESULT_CANCELED,result);
			finish();
		} else {
			SharedPreferences prefs = getSharedPreferences("SENSORS", 0);
			SharedPreferences.Editor editor = prefs.edit();
			
			editor.putInt("bta1pos", bta1.getSelectedItemPosition());
			editor.putInt("bta2pos", bta2.getSelectedItemPosition());
			editor.putInt("mini1pos", mini1.getSelectedItemPosition());
			editor.putInt("mini2pos", mini2.getSelectedItemPosition());
			
			editor.commit();
			
			Intent result = new Intent();
			result.putExtra("bta1", return1);
			result.putExtra("bta2", return2);
			result.putExtra("mini1", return3);
			result.putExtra("mini2", return4);
			result.putExtra("btaname1", retName1);
			result.putExtra("btaname2", retName2);
			result.putExtra("mininame1", retName3);
			result.putExtra("mininame2", retName4);
			
			setResult(RESULT_OK,result);
			finish();
		}
	}

	private String getFormula(String sensor) {
		if(sensor.equals("Counter Type")) {
			return "x";
		} else if(sensor.equals("Voltage")) {
			return "x*3.3*(3/2)/1023";
		} else if(sensor.equals("Analog to Digital")) {
			return "x";
		} else if(sensor.equals("Vernier UV(A) Sensor")) {
			return "((3940*x*3)/(1024))";
		} else if(sensor.equals("Vernier Carbon Dioxide Gas Sensor (Low Range)")) {
			return "2500*x*3.3*(3/2)/1023";
		} else if(sensor.equals("Vernier Carbon Dioxide Gas Sensor (High Range)")) {
			return "25000*x*3.3*(3/2)/1023";
		} else if(sensor.equals("Vernier pH Sensor")) {
			return "-0.0185*x+13.769";
		} else if(sensor.equals("Vernier Dissolved Oxygen Probe")) {
			return "-.327+3.27*(x*3.3*(3/2)/1023)";
		} else if(sensor.equals("Vernier Salinity Sensor")) {
			return "16.3*(x*3.3*(3/2)/1023)";
		} else if(sensor.equals("Vernier Flow Rate Sensor")) {
			return "1*(x*3.3*(3/2)/1023)";
		} else if(sensor.equals("Vernier Stainless Steel Temperature Probe")) {
			return "-33.47 * ln (x) + 213.85";
		} else if(sensor.equals("Vernier Conductivity Probe (20000)")) {
			return "(x*3.3*(3/2)/1023)*7819";
		} else if(sensor.equals("Vernier Conductivity Probe (2000)")) {
			return "(x*3.3*(3/2)/1023)*781.9";
		} else if(sensor.equals("Vernier Conductivity Probe (200)")) {
			return "(x*3.3*(3/2)/1023)*78.19";
		} else if(sensor.equals("Pinpoint Temperature Probe")) {
			return "-40.17 * ln((x*3.3*(3/2)/1023)) + 64.03";
		} else if(sensor.equals("Vernier Turbidity Sensor [BLUE]")) {
			return "0.5814*x - 69.767";
		} else if(sensor.equals("Vernier Turbidity Sensor [RED]")) {
			return "0.5291*x - 87.831";
		} else if(sensor.equals("Vernier Turbidity Sensor [WHITE]")) {
			return "0.5556*x - 66.667";
		} else if(sensor.equals("Vernier Turbidity Sensor [GREEN]")) {
			return "0.5435*x - 79.891";
		} else if(sensor.equals("Current Sensor")) {
			return "((x*3.3*(3/2)/1023)-2.5)/5";
		} else if(sensor.equals("Voltage Sensor")) {
			return "((x*3.3*(3/2)/1023)-2.5)*2";
		} else if(sensor.equals("Voltage Sensor (-7.5V to 7.5V)")) {
			return "((x*3.3*(3/2)/1023)-2.5)*3";
		} else {
			return "x";
		}
	}

}
