package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Login UI fragment.
 * 
 * @author Bobby
 */
public class CredentialManagerLogin extends android.support.v4.app.Fragment {

	private static EditText username;
	private static EditText password;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.credential_manager_login,
				container, false);

		username = (EditText) view.findViewById(R.id.edittext_username);
		password = (EditText) view.findViewById(R.id.edittext_password);

		final Button ok = (Button) view.findViewById(R.id.button_ok);
		final Button cancel = (Button) view.findViewById(R.id.button_cancel);

		// Calls a method of the parent activity Credential Manager
		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((LoginWrapper) getActivity()).login(username.getText()
						.toString(), password.getText().toString());
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

	/**
	 * Retrieve username from this fragment.
	 * 
	 * @return username
	 */
	public static String getUsername() {
		return username.getText().toString();
	}

	/**
	 * Retrieve password from this fragment.
	 * 
	 * @return password
	 */
	public static String getPassword() {
		return password.getText().toString();
	}
}

/**
 * Any activity that uses this fragment must implement PersonWrapper.
 * 
 * @author Bobby
 */
interface LoginWrapper {
	abstract void login(String username, String password);
}