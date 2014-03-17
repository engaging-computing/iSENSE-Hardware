package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import edu.uml.cs.isense.proj.BrowseProjects;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

public class CredentialManagerKey extends Activity  {

	final int PROJECT_REQUESTED = 101;
	private final String PROJECT_ID = "project_id";
	private int projID = -1;
	private String key = "";
	EditText newKey;
	Button bProject;
	Button bCancel;
	Button bOK;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.credential_manager_add_key);
        
        newKey = (EditText) findViewById(R.id.edittext_key);

		bOK = (Button) findViewById(R.id.button_ok);
		bCancel = (Button) findViewById(R.id.button_cancel);
	    bProject = (Button) findViewById(R.id.button_project);
		
	    newKey.setOnTouchListener(new OnTouchListener() {

	    	
			public boolean onTouch(View arg0, MotionEvent arg1) {
				newKey.setError(null);
				return false;
			}
	    	
	    });
	

		bOK.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (newKey.getText().length() != 0 && projID != -1) {
					Intent results = new Intent();
					results.putExtra("key", newKey.getText().toString());
					results.putExtra("proj", projID);
					setResult(Activity.RESULT_OK, results);
				} else if (newKey.getText().length() == 0 && projID == -1 ) {
					newKey.setError("Key can not be empty.");
					bProject.setError("Project not selected.");

				} else if (newKey.getText().length() == 0){
					newKey.setError("Key can not be empty.");

				} else if (projID != -1){
					bProject.setError("Project not selected.");

				}
			}
			
		});
		
		bCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
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
					bProject.setError(null);
				}
			}
		
		}
	}
	
}
