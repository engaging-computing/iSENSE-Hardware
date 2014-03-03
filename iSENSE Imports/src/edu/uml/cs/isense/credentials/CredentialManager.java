package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;


/**
 *
 * 
 * @author iSENSE Android Development Team
 */

public class CredentialManager extends Activity {
	FragmentManager fragmentManager;
	FragmentTransaction fragmentTransaction;
	
	
	CredentialManagerLogin fragmentLogin = new CredentialManagerLogin();
    CredentialManagerKeys fragmentKeys = new CredentialManagerKeys();
    CredentialManagerPerson fragmentPerson = new CredentialManagerPerson();
    
    int TRANSIT_FRAGMENT_OPEN = 4097;
    int TRANSIT_FRAGMENT_CLOSE = 8194;
    int TRANSIT_FRAGMENT_FADE = 4099;
	
	 /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.credential_manager);
            
            LoggedOutView();
    }
	
	
	/*this is the standard view when user is logged out*/
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void LoggedOutView() {
		fragmentManager = getFragmentManager();
	    fragmentTransaction = fragmentManager.beginTransaction();

	    fragmentTransaction.add(R.id.fragment_first, fragmentLogin);
	    fragmentTransaction.add(R.id.fragment_second, fragmentKeys);
	    fragmentTransaction.commit();

	}
	
	/*call this when user logs in*/
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void LoggedInView() {
		fragmentManager = getFragmentManager();
	    fragmentTransaction = fragmentManager.beginTransaction();
	    
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);
	    fragmentTransaction.remove(fragmentLogin);
	    fragmentTransaction.remove(fragmentKeys);
	    
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_OPEN);
	    fragmentTransaction.add(R.id.fragment_first, fragmentPerson);
	    
	    fragmentTransaction.addToBackStack(null);
	    
	    fragmentTransaction.commit();

	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	/*call this when user is logged out to make the keys fragment take up the whole screen */
	void KeysOnlyView() {
		fragmentManager = getFragmentManager();
	    fragmentTransaction = fragmentManager.beginTransaction();
	    
	    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);
	    fragmentTransaction.remove(fragmentLogin);
	    fragmentTransaction.addToBackStack(null);

	    fragmentTransaction.commit();

	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
			
		}
	
   
	
	
	
}

