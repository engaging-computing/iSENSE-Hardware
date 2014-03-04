package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CredentialManagerPerson extends Fragment {
	ImageView gravatar;
	TextView email;
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.credential_manager_person, container, false);
        
        gravatar = (ImageView) view.findViewById(R.id.imageViewPerson);
        Button logout = (Button) view.findViewById(R.id.buttonLogout);
        email = (TextView) view.findViewById(R.id.textViewPersonName);
        
        email.setText(CredentialManager.person.person_id);
        
        return view;
    }

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}
