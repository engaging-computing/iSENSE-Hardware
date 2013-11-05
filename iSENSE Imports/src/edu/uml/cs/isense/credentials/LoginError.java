package edu.uml.cs.isense.credentials;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.R;

public class LoginError extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_error);
		
		Bundle extras = getIntent().getExtras();
		String message = extras.getString(Login.INTENT_KEY_MESSAGE);
		
		final TextView error = (TextView) findViewById(R.id.textview_show_error);
		error.setText(message);
		
		final Button ok = (Button) findViewById(R.id.button_ok);
		ok.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
		
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}
}
