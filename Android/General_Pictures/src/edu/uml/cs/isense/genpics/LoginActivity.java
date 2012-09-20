package edu.uml.cs.isense.genpics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.uml.cs.isense.comm.RestAPI;

public class LoginActivity extends Activity implements OnClickListener {

	private RestAPI rapi;
	private Context mContext;

	private Button okay;
	private Button cancel;
	private EditText usernameInput;
	private EditText passwordInput;

	static final public int LOGIN_SUCCESSFULL = 1;
	static final public int LOGIN_FAILED = 0;
	static final public int LOGIN_CANCELED = -1;

	private static boolean dontToastMeTwice = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logindialog);

		Main.initialLoginStatus = false;

		mContext = this;
		rapi = RestAPI.getInstance();

		okay = (Button) findViewById(R.id.okay);
		cancel = (Button) findViewById(R.id.cancel);

		okay.setOnClickListener(this);
		cancel.setOnClickListener(this);

		usernameInput = (EditText) findViewById(R.id.usernameInput);
		passwordInput = (EditText) findViewById(R.id.passwordInput);

	}

	@Override
	public void onClick(View v) {

		if (v == okay) {

			if (usernameInput.getText().length() != 0
					&& passwordInput.getText().length() != 0) {

				boolean success = rapi.login(
						usernameInput.getText().toString(), passwordInput
								.getText().toString());

				if (success) {
					makeToast("Login Successful!", Toast.LENGTH_SHORT);

					final SharedPreferences mPrefs = new ObscuredSharedPreferences(
							Main.mContext,
							Main.mContext.getSharedPreferences("USER_INFO",
									Context.MODE_PRIVATE));

					mPrefs.edit()
							.putString("username",
									usernameInput.getText().toString())
							.commit();
					mPrefs.edit()
							.putString("password",
									passwordInput.getText().toString())
							.commit();

					setResult(LoginActivity.RESULT_OK);
					LoginActivity.this.finish();

				} else {
					Log.e("tag", "Login Failed");
					makeToast("Login failed!\nWas your username and password correct?\nAre you connected to the internet?", Toast.LENGTH_LONG);
				}
			} else
				makeToast("Please enter a username and password.",
						Toast.LENGTH_SHORT);
		} else if (v == cancel) {
			setResult(LoginActivity.RESULT_CANCELED);
			LoginActivity.this.finish();
		}

	}

	// Easy method to create and show Toast messages, using the NoToastTwiceTask
	public void makeToast(String message, int length) {
		if (length == Toast.LENGTH_SHORT) {
			if (!dontToastMeTwice) {
				Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
				new NoToastTwiceTask().execute();
			}
		} else if (length == Toast.LENGTH_LONG) {
			if (!dontToastMeTwice) {
				Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
				new NoToastTwiceTask().execute();
			}
		}
	}

	// Prevents toasts from being queued infinitesimally
	private class NoToastTwiceTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPreExecute() {
			dontToastMeTwice = true;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			try {
				Thread.sleep(3500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			dontToastMeTwice = false;
		}
	}

}
