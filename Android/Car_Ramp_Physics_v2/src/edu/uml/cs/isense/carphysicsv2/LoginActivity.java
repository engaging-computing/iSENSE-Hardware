package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.waffle.Waffle;

public class LoginActivity extends Activity {

	Button ok, cancel;
	EditText user, pass;
	RestAPI rapi;
	TextView loggedInAs;
	Waffle w;
	public static String uName;
	public static String password;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.login);

		ok = (Button) findViewById(R.id.loginButton);
		cancel = (Button) findViewById(R.id.cancelButton);
		user = (EditText) findViewById(R.id.userNameEditText);
		pass = (EditText) findViewById(R.id.passwordEditText);

		loggedInAs = (TextView) findViewById(R.id.loginStatus);

		w = new Waffle(CarRampPhysicsV2.mContext);

		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				uName = user.getText().toString();
				password = pass.getText().toString();

				rapi = RestAPI.getInstance();

				if (rapi.isConnectedToInternet()) {
					boolean success = rapi.login(uName, password);
					if (success) {
						w.make("Login as " + uName + " successful.",
								Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
						Intent i = new Intent();
						i.putExtra("username", uName);
						setResult(RESULT_OK, i);
						finish();

					} else {
						w.make("Incorrect login credentials. Please try again.",
								Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
					}
				}

			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

}
