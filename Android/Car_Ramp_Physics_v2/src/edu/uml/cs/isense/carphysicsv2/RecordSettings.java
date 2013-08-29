package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckedTextView;

public class RecordSettings extends Activity {

	private boolean Xchecked = false, Ychecked = true, Zchecked = false, Magchecked = false ;
	private CheckedTextView XcheckBox;
	private CheckedTextView YcheckBox;
	private CheckedTextView ZcheckBox;
	private CheckedTextView MagcheckBox;
	public static String RECORD_SETTINGS = "RECORD_SETTINGS";
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.record_settings);
		
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		XcheckBox = (CheckedTextView) findViewById(R.id.checkedTextView1);
		YcheckBox = (CheckedTextView) findViewById(R.id.checkedTextView2);
		ZcheckBox = (CheckedTextView) findViewById(R.id.checkedTextView3);
		MagcheckBox = (CheckedTextView) findViewById(R.id.checkedTextView4);
		
		final Button DoneButton = (Button) findViewById(R.id.loginButton);
		
		setTitle("Select Accelerometer Fields to Record");
		
		XcheckBox.setChecked(Xchecked);
		YcheckBox.setChecked(Ychecked);
		ZcheckBox.setChecked(Zchecked);
		MagcheckBox.setChecked(Magchecked);
		
		XcheckBox.setCheckMarkDrawable(R.drawable.red_x);
		ZcheckBox.setCheckMarkDrawable(R.drawable.red_x);
		MagcheckBox.setCheckMarkDrawable(R.drawable.red_x);
		YcheckBox.setCheckMarkDrawable(R.drawable.checkmark);
		
		XcheckBox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (Xchecked){
					XcheckBox.setCheckMarkDrawable(R.drawable.red_x);
				} else {
					XcheckBox.setCheckMarkDrawable(R.drawable.checkmark);
				}
				Xchecked = !Xchecked ;
				
			}
		});
		
		YcheckBox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (Ychecked){
					YcheckBox.setCheckMarkDrawable(R.drawable.red_x);
				} else {
					YcheckBox.setCheckMarkDrawable(R.drawable.checkmark);
				}
				Ychecked = !Ychecked ;
				YcheckBox.setChecked(Ychecked);
			}
		});
		
		ZcheckBox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (Zchecked){
					ZcheckBox.setCheckMarkDrawable(R.drawable.red_x);
				} else {
					ZcheckBox.setCheckMarkDrawable(R.drawable.checkmark);
				}
				Zchecked = !Zchecked ;
				ZcheckBox.setChecked(Zchecked);

			}
		});
		
		MagcheckBox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (Magchecked){
					MagcheckBox.setCheckMarkDrawable(R.drawable.red_x);
				} else {
					MagcheckBox.setCheckMarkDrawable(R.drawable.checkmark);
				}
				Magchecked = !Magchecked ;
				MagcheckBox.setChecked(Magchecked);
			}
		});
		
		DoneButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = getSharedPreferences(RECORD_SETTINGS, 0) ;
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("X", Xchecked);
				editor.putBoolean("Y", Ychecked);
				editor.putBoolean("Z", Zchecked);
				editor.putBoolean("Magnitude", Magchecked);
				editor.commit();
				finish();
				
			}
		});
		
		
		
	}
	
	public void onResume() {
		
		super.onResume();
		
		SharedPreferences prefs = getSharedPreferences(RecordSettings.RECORD_SETTINGS, 0) ;
		
		Xchecked = prefs.getBoolean("X", false);
		Ychecked = prefs.getBoolean("Y", true);
		Zchecked = prefs.getBoolean("Z", false);
		Magchecked = prefs.getBoolean("Magnitude", false);
		
		XcheckBox.setChecked(Xchecked);
		YcheckBox.setChecked(Ychecked);
		ZcheckBox.setChecked(Zchecked);
		MagcheckBox.setChecked(Magchecked);
		
		checked(Xchecked,XcheckBox);
		checked(Ychecked,YcheckBox);
		checked(Zchecked,ZcheckBox);
		checked(Magchecked,MagcheckBox);
		
	}
	
	private void checked(boolean isChecked, CheckedTextView box){
		if (isChecked)
			box.setCheckMarkDrawable(R.drawable.checkmark);
		else
			box.setCheckMarkDrawable(R.drawable.red_x);
	}
}
