package edu.uml.cs.isense.credentials;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;

/**
 * Login UI person fragment.
 * 
 * @author Bobby
 */
public class CredentialManagerPerson extends Fragment {
	ImageView gravatar;
	TextView email;
	Bitmap bmp;
	URL gravatar_url;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.credential_manager_person,
				container, false);

		gravatar = (ImageView) view.findViewById(R.id.imageViewPerson);
		Button logout = (Button) view.findViewById(R.id.buttonLogout);
		email = (TextView) view.findViewById(R.id.textViewPersonName);

		email.setText(CredentialManager.person.name);

		// Calls async task to set gravatar
		try {
			gravatar_url = new URL(CredentialManager.person.gravatar.toString());
			new SetGravatar().execute(gravatar_url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// Calls a method of the parent activity Credential Manager
		logout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				API.getInstance().deleteSession();
				((PersonWrapper) getActivity()).logout();
			}
		});

		return view;
	}

	/**
	 * Retrieves the gravatar image for user profile.
	 * 
	 * @author Bobby
	 */
	private class SetGravatar extends AsyncTask<URL, Integer, Bitmap> {
		protected Bitmap doInBackground(URL... url) {
			Bitmap bmp = null;
			try {
				// Log.e("gravatar_url", url[0].toString());
				bmp = BitmapFactory.decodeStream(url[0].openConnection()
						.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bmp;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				gravatar.setImageBitmap(result);
			}
		}
	}
}

/**
 * Any activity that uses this fragment must implement PersonWrapper
 * 
 * @author Bobby
 */
interface PersonWrapper {
	abstract void logout();
}
