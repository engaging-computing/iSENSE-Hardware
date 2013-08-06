package edu.uml.cs.isense.datawalk_v2;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import edu.uml.cs.isense.datawalk_v2.R;
public class Help extends Activity{

	private Button okButton;
	
	@Override
	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.help_dialog);
		this.setTitle("How to Create a New Experiment: ");
		okButton = (Button) findViewById(R.id.helpOkButton);
		
		okButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	
	
	
	}

}//ends Help class
