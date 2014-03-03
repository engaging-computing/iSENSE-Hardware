package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CredentialManagerLogin extends Fragment {
	
	private static EditText username;
	private static EditText password;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.credential_manager_login, container, false);
		
		username = (EditText) view.findViewById(R.id.edittext_username);
		password = (EditText) view.findViewById(R.id.edittext_password);
		
		final Button ok = (Button) view.findViewById(R.id.button_ok);
		final Button cancel = (Button) view.findViewById(R.id.button_cancel);

		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.e("CredentialManager", "Login Button");
				//CredentialManager.attemptLogin();
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//setResult(RESULT_CANCELED);
				//TODO EXIT CREDENTIAL ACTIVITY
			}
		});
        // Inflate the layout for this fragment
        return view;
	}
	
	public static String getUsername() {
		return username.toString();
		
	}
	
	public static String getPassword() {
		return password.toString();
		
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	
}
	
    
	



