package edu.uml.cs.isense.carphysicsv2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;

public class LoginDialogFragment extends DialogFragment {

	public Dialog onCreateDialog(Bundle b) {

		LayoutInflater factory = LayoutInflater.from(CarRampPhysicsV2.mContext);
		final View textEntryView = factory.inflate(R.layout.login, null);
		final TextView loggedInAs = CarRampPhysicsV2.loggedInAs;

		AlertDialog.Builder alert = new AlertDialog.Builder(
				CarRampPhysicsV2.mContext);
		alert.setTitle("Login to iSENSE");
		alert.setMessage("");
		alert.setView(textEntryView);
		alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					final EditText usernameInput = (EditText) textEntryView
							.findViewById(R.id.userNameEditText);
					final EditText passwordInput = (EditText) textEntryView
							.findViewById(R.id.passwordEditText);
					RestAPI rapi = RestAPI
							.getInstance(
									(ConnectivityManager) CarRampPhysicsV2.mContext.getSystemService(Context.CONNECTIVITY_SERVICE),
									CarRampPhysicsV2.mContext);
					if (rapi.isConnectedToInternet()) {
						boolean success = rapi
								.login(usernameInput.getText().toString(),
										passwordInput.getText().toString());
						if (success) {
							Toast.makeText(
									CarRampPhysicsV2.mContext,
									"Login as "
											+ usernameInput.getText()
													.toString()
											+ " successful.",
									Toast.LENGTH_SHORT).show();
							loggedInAs.setText(getResources().getString(
									R.string.logged_in_as)
									+ " " + usernameInput.getText().toString());

						} else {
							Toast.makeText(
									CarRampPhysicsV2.mContext,
									"Incorrect login credentials. Please try again.",
									Toast.LENGTH_SHORT).show();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		return alert.create();

	}

}
