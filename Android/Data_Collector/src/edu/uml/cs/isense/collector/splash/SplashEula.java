package edu.uml.cs.isense.collector.splash;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import edu.uml.cs.isense.collector.R;

public class SplashEula extends Activity {

	public static boolean eulaIsChecked = false;
	
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_eula);
        
        final CheckBox eulaCheck = (CheckBox) findViewById(R.id.eula_check_box);
        eulaCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				eulaIsChecked = eulaCheck.isChecked();
			}
        });
        
    }
    
}
