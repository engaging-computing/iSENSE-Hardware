package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CredentialManagerKeys extends Fragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.credential_manager_keys, container, false);

		final Button addKey = (Button) view.findViewById(R.id.button_add);
		addKey.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//Open newKeydialog
			}
			
		});
		
		
        return view;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}



