package edu.uml.cs.isense.datawalk_v2;


import android.app.Activity;
import android.content.Context;
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

public class LoginIsense extends Activity{
	
	Button ok,cancel;
	EditText user,pass;
	API api;
	TextView loggedInAs;
	Waffle w;
	
	private String username;
	private String password;
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.login_website);
		
		ok = (Button) findViewById(R.id.loginB);
		cancel = (Button) findViewById(R.id.cancelB);
		user = (EditText) findViewById(R.id.userNameET);
		pass = (EditText) findViewById(R.id.passwordET);
		loggedInAs = (TextView) findViewById(R.id.loginStatus);
		
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				   DataWalk.mContext, DataWalk.mContext
				   .getSharedPreferences("USER_INFO", Context.MODE_PRIVATE));
		
		username = mPrefs.getString(DataWalk.USERNAME_KEY, "");
		password = mPrefs.getString(DataWalk.PASSWORD_KEY, "");
		
		user.setText(username);
		pass.setText(password);
		
		this.setTitle("iSENSE Login");
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter(){
			
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
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
		w = new Waffle(DataWalk.mContext);
		
		ok.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// This is what happens if the user clicks okay to enter new login info. 
				username = user.getText().toString();
				password = pass.getText().toString();
				
				api = API.getInstance(LoginIsense.this);
				new LoginTask().execute();
				
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// This is what happens if the cancel button is clicked
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
	}//Ends onCreate
	
	public class LoginTask extends AsyncTask<Void, Integer, Void> {

		boolean connect = false;
		boolean success = false;
		
		@Override
		protected Void doInBackground(Void... arg0) {
			if(api.hasConnectivity()){
				connect = true;
				success = api.createSession(username, password);
			}else {
				connect = false;
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		
			if (connect) {
				if (success){
					w.make("Login as  " + username + "  Successful.",Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
					
					final SharedPreferences mPrefs = new ObscuredSharedPreferences(
							   DataWalk.mContext, DataWalk.mContext
							   .getSharedPreferences("USER_INFO", Context.MODE_PRIVATE));
					
					SharedPreferences.Editor mEditor = mPrefs.edit();
					mEditor.putString(DataWalk.USERNAME_KEY, username);
					mEditor.putString(DataWalk.PASSWORD_KEY, password);
					mEditor.commit();
										
					setResult(RESULT_OK);
					finish();
				} else {
					w.make("Incorrect login credentials. Please try again.",Waffle.LENGTH_SHORT,Waffle.IMAGE_X);
				}
			
			} else {
				w.make("Cannot login due to lack of inernet connection. Please try again later.", Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
				setResult(RESULT_CANCELED);
				finish();
			}
			
		}

	}
	
}
