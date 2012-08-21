package edu.uml.cs.isense.uppt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;

public class LoginActivity extends Activity {

	private Context mContext;
	
	private RestAPI rapi;

	static final public int NAME_SUCCESSFUL = 1;
	static final public int NAME_FAILED = 0;
	static final public int NAME_CANCELED = -1;
	
	private static final int ERROR_REQUESTED = 100;

	boolean success;
	
	private String message = "";
	private String returnCode = "";

	private static final String unknownUser    = "Connection to internet has been found, but the username or password was incorrect.  Please try again.";
	private static final String noConnection   = "No connection to internet through either wifi or mobile found.  Please enable one to continue, then try again."; 
	private static final String defaultMessage = "Was your username and password correct?\nAre you connected to the internet?\nPlease try again.";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logindialog);

		mContext = this;
		
		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		final EditText username = (EditText) findViewById(R.id.usernameInput);
		final EditText password = (EditText) findViewById(R.id.passwordInput);
		final Button ok = (Button) findViewById(R.id.login_ok);
		final Button cancel = (Button) findViewById(R.id.login_cancel);
		
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				   Main.mContext, Main.mContext
				   .getSharedPreferences("USER_INFO", Context.MODE_PRIVATE));
		
		username.setText(mPrefs.getString("username", ""));
		password.setText(mPrefs.getString("password", ""));

		ok.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				success = rapi.login(username.getText().toString(), password.getText().toString());
       			   
				if (success) {
					final SharedPreferences mPrefs = new ObscuredSharedPreferences(
							Main.mContext, Main.mContext
 						   .getSharedPreferences("USER_INFO", Context.MODE_PRIVATE));
 				   	mPrefs.edit().putString("username", username.getText().toString()).commit();
 				   	mPrefs.edit().putString("password", password.getText().toString()).commit();
 				   	
 				   	returnCode = "Success";
 				   	Intent ret = new Intent(LoginActivity.this, Main.class);
 				   	ret.putExtra("returnCode", returnCode);
 				   	setResult(RESULT_OK, ret);
 				   	finish();
				} else {
					showFailure();
				}
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();		
			}		
		});

	}
	
	private void showFailure() {
		
		if(rapi.connection == "NONE") {     
			message = noConnection;
		}
		else if(rapi.connection == "600") {
			message = unknownUser;
		}
		else if(rapi.connection == "") {
			message = unknownUser;
		}
		else {
			message = defaultMessage;
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
			Intent ret = new Intent(LoginActivity.this, Main.class);
			ret.putExtra("returnCode", codeFromError);
			setResult(RESULT_OK, ret);
			finish();
		}
	}

}
