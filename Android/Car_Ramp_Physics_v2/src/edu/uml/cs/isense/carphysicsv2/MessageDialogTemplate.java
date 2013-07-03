package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.carphysicsv2.R;

public class MessageDialogTemplate extends Activity {

	Button pos, neg;

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.message_template);

		setTitle(getIntent().getExtras().getString("title"));
		TextView messageBox = (TextView) findViewById(R.id.messageBox);
		messageBox.setText(getIntent().getExtras().getString("message"));

		pos = (Button) findViewById(R.id.positive);
		neg = (Button) findViewById(R.id.negative);

		pos.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				setResult(RESULT_OK);
				finish();

			}
		});

		neg.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
				finish();

			}
		});

	}

}
