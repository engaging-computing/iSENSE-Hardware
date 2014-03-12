package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CredentialManagerKeys extends Fragment {
	private static Context appContext;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.credential_manager_keys, container, false);

		appContext = this.getActivity().getApplicationContext();
		
		final Button addKey = (Button) view.findViewById(R.id.button_add);
		addKey.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(appContext,
						CredentialManagerAddKey.class));			}

			
		});
		
		
        return view;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
//		super.onActivityResult(requestCode, resultCode, data);
//
//		if (requestCode == LOGIN_REQUESTED) { // request to takes picture
//
//		}
//	}
	
}



