package edu.uml.cs.isense.collector.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;

public class LoginError extends Activity {

	private String message;
	private String returnCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_show_error);
		
		Bundle extras = getIntent().getExtras();
		message = extras.getString("message");
		returnCode = extras.getString("returnCode");
		
		final TextView error = (TextView) findViewById(R.id.show_error_textview);
		error.setText(message);
		
		final Button ok = (Button) findViewById(R.id.login_show_error_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent ret = new Intent(LoginError.this, LoginActivity.class);
				ret.putExtra("returnCode", returnCode);
				setResult(RESULT_OK, ret);
				finish();
			}
		});
		
	}
	
	@Override
	public void onBackPressed() {
		Intent ret = new Intent(LoginError.this, LoginActivity.class);
		ret.putExtra("returnCode", returnCode);
		setResult(RESULT_OK, ret);
		finish();
	}
}
