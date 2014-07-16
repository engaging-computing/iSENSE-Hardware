package edu.uml.cs.isense.canobiev2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class Summary extends Activity {

	private String seconds = "", minutes = "", 
			date = "", points = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.summary);
		
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		Bundle extras = getIntent().getExtras();
		seconds = extras.getString("seconds");
		minutes = extras.getString("minutes");
		date    = extras.getString("date");
		points  = extras.getString("points");
		
		final TextView message = (TextView) findViewById(R.id.summary_text);
		message.setText("Elapsed time: " + minutes + ":"
				+ seconds
				+ "\n" + "Data points: " + points
				+ "\n" + "End date and time:\n"
				+ date + "\n" );
		
		final Button ok = (Button) findViewById(R.id.summary_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}
	
}