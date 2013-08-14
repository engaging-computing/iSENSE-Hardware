package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.waffle.Waffle;

public class CarRampLoginActivity extends Activity {

	Button ok, cancel;
	EditText user, pass;
	API api;
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
		
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				CarRampPhysicsV2.mContext,
				CarRampPhysicsV2.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		user.setText(mPrefs.getString("username", ""));
		pass.setText(mPrefs.getString("password", ""));

		loggedInAs = (TextView) findViewById(R.id.loginStatus);

		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if (end > start) {

					char[] acceptedChars = new char[] { 'a', 'b', 'c', 'd',
							'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
							'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
							'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
							'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
							'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1',
							'2', '3', '4', '5', '6', '7', '8', '9', '@', '.',
							'_', '-', '(', ')', ',' };

					for (int index = start; index < end; index++) {
						if (!new String(acceptedChars).contains(String
								.valueOf(source.charAt(index)))) {
							return "";
						}
					}
				}
				return null;
			}

		};
		user.setFilters(filters);

		w = new Waffle(CarRampPhysicsV2.mContext);

		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				uName = user.getText().toString();
				password = pass.getText().toString();

				api = API.getInstance(getApplicationContext());
				
				new LoginTask().execute();

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
	
	public class LoginTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			Boolean success = api.createSession(uName, password);
			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (api.hasConnectivity()) {
				if (result) {			
					w.make("Login as " + uName + " successful.",
							Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
					
					final SharedPreferences mPrefs = new ObscuredSharedPreferences(
							CarRampPhysicsV2.mContext,
							CarRampPhysicsV2.mContext.getSharedPreferences("USER_INFO",
									Context.MODE_PRIVATE));
					SharedPreferences.Editor mEdit = mPrefs.edit();
					mEdit.putString("username", uName).commit();
					mEdit.putString("password", password).commit();
					
					Intent i = new Intent();
					i.putExtra("username", uName);
					setResult(RESULT_OK, i);
					finish();
				} else {
					w.make("Login as " + uName + " failed.",
							Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
					setResult(RESULT_CANCELED);
					finish();
				}

			} else {
				w.make("Cannot login due to lack of internet connection. Please try again later.",
						Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
				setResult(RESULT_CANCELED);
				finish();
			}
			
		}
		
		

	}
}
