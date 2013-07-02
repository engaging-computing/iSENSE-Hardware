package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import edu.uml.cs.isense.R;

public class ResetToDefaults extends Activity {
	
	Button reset, noreset;
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		this.setContentView(R.layout.reset);
		
		setTitle("Are You Sure?");
		
		reset = (Button) findViewById(R.id.reset);
		noreset = (Button) findViewById(R.id.noreset);
		
		reset.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int length = 10;
				SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH",
						0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("length", length);
				editor.commit();
				setResult(RESULT_OK);
				finish();
				
			}
		});
		
		noreset.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
				
			}
		});
				
		
	}

}
