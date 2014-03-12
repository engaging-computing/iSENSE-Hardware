package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import edu.uml.cs.isense.proj.BrowseProjects;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CredentialManagerEnterKey extends Activity  {

	final int PROJECT_REQUESTED = 101;
	private final String PROJECT_ID = "project_id";
	private int projID = -1;
	private String key = "";
	EditText newKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.credential_manager_add_key);
        
        newKey = (EditText) findViewById(R.id.edittext_key);

		Button bOK = (Button) findViewById(R.id.button_ok);
		Button bCancel = (Button) findViewById(R.id.button_cancel);
		Button bProject = (Button) findViewById(R.id.button_project);
		
	

		bOK.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (newKey.getText().length() != 0 && projID != -1) {
					key = newKey.getText().toString();
				}
			}
			
		});
		
		bCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
			
		});
		
		bProject.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//TODO implement browse project button
				Intent iProject = new Intent(getApplicationContext(),
						BrowseProjects.class);
				startActivityForResult(iProject, PROJECT_REQUESTED);
			}
			
		});
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PROJECT_REQUESTED) { 
															
			if (resultCode == Activity.RESULT_OK) {
				if (resultCode == Activity.RESULT_OK) {
					projID = data.getExtras().getInt(PROJECT_ID);
				}
			}
		
		}
	}
	
}
