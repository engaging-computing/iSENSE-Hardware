package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
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

		/* Calls a method of the parent activity Credential Manager */
		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((LoginWrapper) getActivity()).WrapperLogin(username.getText().toString(), password.getText().toString());
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getActivity().setResult(Activity.RESULT_CANCELED);
				getActivity().finish();
			}
		});
        // Inflate the layout for this fragment
        return view;
	}
	
	public static String getUsername() {
		return username.getText().toString();
	}
	
	public static String getPassword() {
		return password.getText().toString();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}
	

/*any activity that uses this fragment must implement PersonWrapper */
interface LoginWrapper {
	abstract void WrapperLogin(String username, String password);
}
    


