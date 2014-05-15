package edu.uml.cs.isense.riverwalk;

import java.util.ArrayList;

import edu.uml.cs.isense.credentials.CredentialManager;
import edu.uml.cs.isense.credentials.CredentialManagerKey;
import edu.uml.cs.isense.proj.Setup;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SharePicture extends Activity {
	
	final int PROJECT_REQUESTED = 101;
	final int LOGIN_REQUESTED = 102;
	final int KEY_REQUESTED = 103;
	
	
	int project = -1;
	private String username = "";
	private String password = "";
	private String key = "";
	
	Button bCredentials;
	Button bKey;
	Button bProject;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_picture);
		bProject = (Button) findViewById(R.id.buttonProject);
		bCredentials = (Button) findViewById(R.id.buttonCredentials);
		bKey = (Button) findViewById(R.id.buttonKey);

		bCredentials.setEnabled(false);
		bKey.setEnabled(false);
		
		Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();
		
		
		/*step1 select a project*/
		bProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent iproject = new Intent(getApplicationContext(),
						Setup.class);
				iproject.putExtra("constrictFields", true);
				iproject.putExtra("app_name", "Pictures");
				startActivityForResult(iproject, PROJECT_REQUESTED);
			}
			
		});
		
		/*Step 2*/
		/*upload with credentials*/
		bCredentials.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getApplicationContext(),
						CredentialManager.class), LOGIN_REQUESTED);				
			}
		});
		/*upload with a contributor key*/
		bKey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getApplicationContext(),
						CredentialManagerKey.class), LOGIN_REQUESTED);	
				
			}
			
		});
		
		
		
		 if (Intent.ACTION_SEND.equals(action) && type != null) {
		        if (type.startsWith("image/")) {
		            handleSendImage(intent); // Handle single image being sent
		        }
		    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
		        if (type.startsWith("image/")) {
		            handleSendMultipleImages(intent); // Handle multiple images being sent
		        }
		    } 
		}

		void handleSendImage(Intent intent) {
		    Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		    if (imageUri != null) {
		        // Update UI to reflect image being shared
		    }
		}

		void handleSendMultipleImages(Intent intent) {
		    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		    if (imageUris != null) {
		        // Update UI to reflect multiple images being shared
		    }
		
		
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PROJECT_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
				project = Integer.parseInt(mPrefs.getString("project_id", ""));
		
				bProject.setEnabled(false);
				bCredentials.setEnabled(true);
				bKey.setEnabled(true);
			
			}
		} else if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				//TODO upload
			}
		} else if (requestCode == KEY_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				//TODO upload
			}
		}
		
		 

	}
}
