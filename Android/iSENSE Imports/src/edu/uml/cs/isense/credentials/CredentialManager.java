package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;


/**
 *
 * 
 * @author iSENSE Android Development Team
 */

@SuppressWarnings("deprecation") //Necessary for Pre-Android 3.0
public class CredentialManager extends TabActivity{
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.credential_manager);

            // create the TabHost that will contain the Tabs
            TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);


            TabSpec tab1 = tabHost.newTabSpec("First Tab");
            TabSpec tab2 = tabHost.newTabSpec("Second Tab");

           // Set the Tab name and Activity
           // that will be opened when particular Tab will be selected
            tab1.setIndicator("Login");
            tab1.setContent(new Intent(this,CredentialManagerLogin.class));
            
            tab2.setIndicator("Contributor Keys");
            tab2.setContent(new Intent(this,CredentialManagerKeys.class));

            /** Add the tabs  to the TabHost to display. */
            tabHost.addTab(tab1);
            tabHost.addTab(tab2);

    }
} 
