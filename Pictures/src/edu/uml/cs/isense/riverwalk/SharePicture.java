package edu.uml.cs.isense.riverwalk;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;

import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.comm.API.TargetType;
import edu.uml.cs.isense.credentials.CredentialManager;
import edu.uml.cs.isense.credentials.CredentialManagerKey;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SharePicture extends Activity {
	
	final int PROJECT_REQUESTED = 101;
	final int LOGIN_REQUESTED = 102;
	final int KEY_REQUESTED = 103;
	final int CREDENTIAL_KEY_REQUESTED = 104;
	
	
	int project = -1;
	private String key = "";
		
	boolean loggedIn;
	
	Button bCredentials;
	Button bKey;
	Button bProject;
	
	ArrayList<Uri> imageUris = new ArrayList<Uri>();
	ArrayList<File> imageFiles = new ArrayList<File>();
	Uri imageUri;
	
	API api = API.getInstance();
	
	Context mContext;
	

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
		
	    mContext = this.getApplicationContext();
	    
	    
	    if (Intent.ACTION_SEND.equals(action) && type != null) {
	        if (type.startsWith("image/")) {
	            handleSendImage(intent); // Handle single image being sent
	        }
	    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
	        if (type.startsWith("image/")) {
	            handleSendMultipleImages(intent); // Handle multiple images being sent
	        }
	    } 
	
	   

	
	
		/*step1 select a project*/
		bProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent iproject = new Intent(getApplicationContext(),
						Setup.class);
				iproject.putExtra("constrictFields", true);
				iproject.putExtra("app_name", "Pictures");
				iproject.putExtra("showOKCancel", true);
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
				CredentialManager.logout(mContext, api);
				Intent key_intent = new Intent().setClass(mContext, CredentialManagerKey.class);
				startActivityForResult(key_intent, CREDENTIAL_KEY_REQUESTED);			
				
			}
			
		});
		
		
	}

	 void handleSendImage(Intent intent) {
		imageUris.add((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));	}

	void handleSendMultipleImages(Intent intent) {
	    imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
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
				new uploadLoggedIn().execute();
				
			}
		} else if (requestCode == KEY_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				key = CredentialManagerKey.getKey();
				new uploadWithKey().execute();
			}
		}
	}
	
		private class uploadLoggedIn extends AsyncTask<Void, Void, Boolean> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				for(int i = 0; i < imageUris.size(); i++) {
					imageFiles.add(Main.convertImageUriToFile(imageUris.get(i)));
				}
			}
			@Override
			protected Boolean doInBackground(Void... params) {
				Log.e("test", "Here");
				for(int i = 0; i < imageUris.size(); i++) {
					api.uploadMedia(project,imageFiles.get(i), TargetType.PROJECT);
				}
				return null;
			}
			@Override
			protected void onPostExecute(Boolean result) {// this method will be
															// running on UI thread
				super.onPostExecute(result);
		}
	}
		
		
		private class uploadWithKey extends AsyncTask<Void, Void, Boolean> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				for(int i = 0; i < imageUris.size(); i++) {
					imageFiles.add(Main.convertImageUriToFile(imageUris.get(i)));
				}
			}
			@Override
			protected Boolean doInBackground(Void... params) {
				for(int i = 0; i < imageUris.size(); i++) {
					File picture = imageFiles.get(i);
					api.uploadMedia(project, picture, TargetType.DATA_SET, key, "");
				}
				return null;
			}
			@Override
			protected void onPostExecute(Boolean result) {// this method will be
															// running on UI thread
				super.onPostExecute(result);
		}
	}
		
}


