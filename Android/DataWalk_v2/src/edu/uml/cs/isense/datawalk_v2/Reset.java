package edu.uml.cs.isense.datawalk_v2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import edu.uml.cs.isense.R;

public class Reset extends Activity {
	Button resetYes, resetNo;
	
	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		this.setContentView(R.layout.reset);
		
		setTitle("Are You Sure?");
		resetYes = (Button) findViewById(R.id.reset);
		resetNo = (Button) findViewById(R.id.noreset);
		
		resetYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO
				// This is what happens when the reset button is clicked...
				int length = 10;
				SharedPreferences prefs = getSharedPreferences("RECORD_LENGTH",
						0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("length", length);
				editor.commit();
				setResult(RESULT_OK);
				finish();
			}
		});//ends onClickListener for resetButton Yes
		
		resetNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// This is what happens when the person says that they no longer want to reset settings
				setResult(RESULT_CANCELED);
				finish();
			}
		});//ends onClickListener for resetButton No
		
	}//ends onCreate

	
	
	
	
}//ends Reset class
