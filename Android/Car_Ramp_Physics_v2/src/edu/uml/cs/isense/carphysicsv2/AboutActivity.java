package edu.uml.cs.isense.carphysicsv2;

import edu.uml.cs.isense.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends Activity {

	private Button okButton;
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.about);
		
		okButton = (Button) findViewById(R.id.loginButton);
		
		okButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
	}
	
}
