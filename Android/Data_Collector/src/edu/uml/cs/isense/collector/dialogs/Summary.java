package edu.uml.cs.isense.collector.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;

public class Summary extends Activity {

	private String millis = "", seconds = "", minutes = "", 
			append = "", date = "", points = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.summary);
		
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		Bundle extras = getIntent().getExtras();
		millis  = extras.getString("millis");
		seconds = extras.getString("seconds");
		minutes = extras.getString("minutes");
		append  = extras.getString("append");
		date    = extras.getString("date");
		points  = extras.getString("points");
		
		final TextView message = (TextView) findViewById(R.id.summary_text);
		message.setText("Elapsed time: " + minutes + ":"
				+ seconds + "." + millis
				+ "\n" + "Data points: " + points
				+ "\n" + "End date and time:\n"
				+ date + "\n" + append);
		
		final Button ok = (Button) findViewById(R.id.summary_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}
	
}