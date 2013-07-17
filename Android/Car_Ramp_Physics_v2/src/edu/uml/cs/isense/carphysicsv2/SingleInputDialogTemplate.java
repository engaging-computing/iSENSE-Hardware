package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.carphysicsv2.R;
import edu.uml.cs.isense.waffle.Waffle;

public class SingleInputDialogTemplate extends Activity {

	Button pos, neg ;
	EditText input ;
	Waffle w;
	
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.single_input_template);
		
		setTitle(getIntent().getExtras().getString("title"));
		TextView messageBox = (TextView) findViewById(R.id.messageBox2);
		messageBox.setText(getIntent().getExtras().getString("message"));
		
		pos = (Button) findViewById(R.id.positive2);
		neg = (Button) findViewById(R.id.negative2);
		input = (EditText) findViewById(R.id.editText1);
		
		w = new Waffle(CarRampPhysicsV2.mContext);
		
		pos.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String in = input.getText().toString();
				int inInt = Integer.parseInt(in);
				
				if (inInt <= 600 && inInt > 0) {
					Intent i = new Intent();
					i.putExtra("input", in );
					setResult(RESULT_OK, i);
					finish();
				} else {
					w.make("Input is not between 0 and 10 minutes. Try again.", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
				}
				
			}
		});
		
		neg.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
				
			}
		});
	}
	
}
