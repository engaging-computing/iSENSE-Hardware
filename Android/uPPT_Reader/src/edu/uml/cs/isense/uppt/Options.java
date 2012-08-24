package edu.uml.cs.isense.uppt;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Options extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options);
		
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		final SharedPreferences mPrefs = getSharedPreferences("swipe", 0);
		
		final CheckBox swipe = (CheckBox) findViewById(R.id.no_swipe_check);
		swipe.setChecked(mPrefs.getBoolean("swipe", true));
		swipe.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				final SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putBoolean("swipe", isChecked).commit();
				
			}		
		});
		
		final Button back = (Button) findViewById(R.id.options_back);
		back.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
		
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
}
