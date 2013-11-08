package edu.uml.cs.isense.riverwalk.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.riverwalk.Main;
import edu.uml.cs.isense.riverwalk.R;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.waffle.Waffle;

public class LoginActivity extends Activity implements OnClickListener {

	private API api;
	private Context mContext;

	private Button okay;
	private Button cancel;
	private EditText usernameInput;
	private EditText passwordInput;

	static final public int LOGIN_SUCCESSFULL = 1;
	static final public int LOGIN_FAILED = 0;
	static final public int LOGIN_CANCELED = -1;

	private static Waffle w;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logindialog);

		Main.initialLoginStatus = false;

		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				Main.mContext, Main.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		mContext = this;
		api = API.getInstance(mContext);

		w = new Waffle(mContext);

		okay = (Button) findViewById(R.id.okay);
		cancel = (Button) findViewById(R.id.cancel);

		okay.setOnClickListener(this);
		cancel.setOnClickListener(this);

		usernameInput = (EditText) findViewById(R.id.usernameInput);
		passwordInput = (EditText) findViewById(R.id.passwordInput);

		usernameInput.setText(mPrefs.getString("username", ""));
		passwordInput.setText(mPrefs.getString("password", ""));

	}

	@Override
	public void onClick(View v) {

		if (v == okay) {

			if (usernameInput.getText().length() != 0
					&& passwordInput.getText().length() != 0) {

				new LoginTask().execute();

			} else
				w.make("Please enter a username and password.",
						Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
		} else if (v == cancel) {
			setResult(LoginActivity.RESULT_CANCELED);
			LoginActivity.this.finish();
		}

	}

	// Attempts to login with current user information
	private class LoginTask extends AsyncTask<Void, Integer, Void> {

		boolean success;

		@Override
		protected Void doInBackground(Void... params) {

			success = api.createSession(usernameInput.getText().toString(),
					passwordInput.getText().toString());
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			if (success) {
				w.make("Login Successful!", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);

				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						Main.mContext, Main.mContext.getSharedPreferences(
								"USER_INFO", Context.MODE_PRIVATE));

				mPrefs.edit()
						.putString("username",
								usernameInput.getText().toString()).commit();
				mPrefs.edit()
						.putString("password",
								passwordInput.getText().toString()).commit();

				setResult(LoginActivity.RESULT_OK);
				LoginActivity.this.finish();

			} else {
				w.make("Login failed.  Check internet connectivity and your username/password.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			}
		}

	}

}
