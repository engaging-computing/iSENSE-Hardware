package edu.uml.cs.pincomm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity implements OnClickListener, TextWatcher {

	Button loginButton;
	EditText username, password;
	String myUsername;
	String myPass;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.loginbox);
		
		SharedPreferences myPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
				
		username = (EditText) findViewById(R.id.usernameField);
		password = (EditText) findViewById(R.id.passwordField);
		loginButton = (Button) findViewById(R.id.loginBtn);
		
		loginButton.setOnClickListener(this);
		username.addTextChangedListener(this);
		password.addTextChangedListener(this);
		
		loginButton.setEnabled(false);
		
		username.setText (myPrefs.getString("isense_user", ""));
		password.setText (myPrefs.getString("isense_pass", ""));
		
	}
	
	@Override
	public void onClick(View v) {
		
		if (v == loginButton) {
		
			myUsername = username.getText().toString();
			myPass = password.getText().toString();
			
			Intent result = new Intent();
			result.putExtra("myUsername", myUsername);
			result.putExtra("myPass", myPass);
			
	        setResult(RESULT_OK, result);
			finish();
			
		}
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
		myUsername = username.getText().toString();
		myPass = password.getText().toString();
		
		if (myUsername.equals("") || myPass.equals("")) {
			loginButton.setEnabled(false);
		} else{
			loginButton.setEnabled(true);
		}
		
	}

}
