package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.R;

public class SingleInputDialogTemplate extends Activity {

	Button pos, neg ;
	EditText input ;
	
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.single_input_template);
		
		setTitle(getIntent().getExtras().getString("title"));
		TextView messageBox = (TextView) findViewById(R.id.messageBox2);
		messageBox.setText(getIntent().getExtras().getString("message"));
		
		pos = (Button) findViewById(R.id.positive2);
		neg = (Button) findViewById(R.id.negative2);
		input = (EditText) findViewById(R.id.editText1);
		
		pos.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("input", input.getText().toString());
				setResult(RESULT_OK, i);
				finish();
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
