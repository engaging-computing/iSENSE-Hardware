package edu.uml.cs.isense.canobiev2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;

public class LoginActivity extends Activity {

	private Context mContext;

	private API api;

	private final int ERROR_REQUESTED = 100;

	boolean success;

	private String message = "";
	private String returnCode = "";

	private EditText username, password;

	private static final String unknownUser = "Connection to internet has been found, but the username or password was incorrect.  Please try again.";
	private static final String noConnection = "No connection to internet through either wifi or mobile found.  Please enable one to continue, then try again.";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logindialog);

		mContext = this;

		api = API.getInstance(mContext);

		username = (EditText) findViewById(R.id.usernameInput);
		password = (EditText) findViewById(R.id.passwordInput);
		final Button ok = (Button) findViewById(R.id.login_ok);
		final Button cancel = (Button) findViewById(R.id.login_cancel);

		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				AmusementPark.mContext, AmusementPark.mContext.getSharedPreferences(
						"USER_INFO", Context.MODE_PRIVATE));

		username.setText(mPrefs.getString("username", ""));
		password.setText(mPrefs.getString("password", ""));

		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new LoginTask().execute();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

	private void showFailure() {

		if (api.hasConnectivity()) {
			message = unknownUser;
		} else {
			message = noConnection;
		}

		returnCode = "Failed";

		Intent showError = new Intent(mContext, LoginError.class);
		showError.putExtra("message", message);
		showError.putExtra("returnCode", returnCode);
		startActivityForResult(showError, ERROR_REQUESTED);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == ERROR_REQUESTED) {
			String codeFromError = data.getStringExtra("returnCode");
			Intent ret = new Intent(LoginActivity.this, AmusementPark.class);
			ret.putExtra("returnCode", codeFromError);
			setResult(RESULT_OK, ret);
			finish();
		}
	}

	private class LoginTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			success = api.createSession(username.getText().toString(), password
					.getText().toString());
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			if (success) {
				final SharedPreferences mPrefs = new ObscuredSharedPreferences(
						AmusementPark.mContext, AmusementPark.mContext.getSharedPreferences(
								"USER_INFO", Context.MODE_PRIVATE));
				mPrefs.edit()
						.putString("username", username.getText().toString())
						.commit();
				mPrefs.edit()
						.putString("password", password.getText().toString())
						.commit();

				returnCode = "Success";
				Intent ret = new Intent(LoginActivity.this, AmusementPark.class);
				ret.putExtra("returnCode", returnCode);
				setResult(RESULT_OK, ret);
				finish();
			} else {
				showFailure();
			}
		}

	}

}
