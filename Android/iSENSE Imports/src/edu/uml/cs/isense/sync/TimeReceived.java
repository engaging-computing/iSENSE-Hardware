package edu.uml.cs.isense.sync;

import edu.uml.cs.isense.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class TimeReceived extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_received);

		getWindow().setLayout(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		Bundle extras = getIntent().getExtras();
		String timeRec = extras.getString("timeReceived");
		long timeOffset = extras.getLong("timeOffset");

		final Button done = (Button) findViewById(R.id.time_received_done);
		done.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});

		final TextView text = (TextView) findViewById(R.id.time_received_text);
		text.setText("You have received the time " + timeRec
				+ " from the host device, which is an offset of " + timeOffset
				+ " milliseconds from your local clock.");

	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}

}