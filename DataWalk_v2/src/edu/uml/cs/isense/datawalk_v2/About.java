package edu.uml.cs.isense.datawalk_v2;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import edu.uml.cs.isense.R;
public class About extends Activity{

	private Button okButton;
	
	@Override
	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.about);
	
		okButton = (Button) findViewById(R.id.loginButton);
		
		okButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	
	
	
	}

}//ends About class