package edu.uml.cs.isense.rsensetest;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.objects.RNews;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProject;
import edu.uml.cs.isense.objects.RProjectField;
import edu.uml.cs.isense.supplements.FileBrowser;

public class MainActivity extends Activity implements OnClickListener {

	Button login, logout, getusers, getprojects, getnews, appendTest, uploadTest, newProj, uploadCSV, mediaProj, mediaDataset;
	TextView status;
	EditText projID, userName, newsId;
	API api;
	
	int FILEPICK = 0;
	int MEDIAPROJPICK = 1;
	int MEDIADATASET = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		login = (Button) findViewById(R.id.btn_login);
		getusers = (Button) findViewById(R.id.btn_getusers);
		getnews = (Button) findViewById(R.id.btn_getnews);
		getprojects = (Button) findViewById(R.id.btn_getprojects);
		logout = (Button) findViewById(R.id.btn_logout);
		appendTest = (Button) findViewById(R.id.btn_append);
		uploadTest = (Button) findViewById(R.id.btn_upload);
		status = (TextView) findViewById(R.id.txt_results);
		projID = (EditText) findViewById(R.id.et_projectnum);
		newsId = (EditText) findViewById(R.id.et_newsnum);
		userName = (EditText) findViewById(R.id.et_username);
		newProj = (Button) findViewById(R.id.btn_newproj);
		uploadCSV = (Button) findViewById(R.id.btn_uploadCSV);
		mediaProj = (Button) findViewById(R.id.btn_uploadToProj);
		mediaDataset = (Button) findViewById(R.id.btn_uploadToDataSet);

		login.setOnClickListener(this);
		logout.setOnClickListener(this);
		getusers.setOnClickListener(this);
		getnews.setOnClickListener(this);
		getprojects.setOnClickListener(this);
		appendTest.setOnClickListener(this);
		uploadTest.setOnClickListener(this);
		newProj.setOnClickListener(this);
		uploadCSV.setOnClickListener(this);
		mediaProj.setOnClickListener(this);
		mediaDataset.setOnClickListener(this);

		api = API.getInstance(this);
		api.setBaseUrl("http://129.63.17.17:3000");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if(api.hasConnectivity()) {
			if ( v == login ) {
				status.setText("clicked login");
				new LoginTask().execute("sor", "sor");
			} else if ( v == getusers ) {
				status.setText("clicked get users");
				new UsersTask().execute();
			} if ( v == logout ) {
				status.setText("clicked logout");
				new LogoutTask().execute();
			} else if ( v == getprojects ) {
				status.setText("clicked get projects");
				new ProjectsTask().execute();
			} else if ( v == getnews ) {
				status.setText("clicked get news");
				new NewsTask().execute();
			} else if ( v == appendTest ) {
				status.setText("append button clicked");
				new AppendTask().execute();
			} else if ( v == uploadTest ) {
				status.setText("upload button clicked");
				new UploadTask().execute();
			} else if ( v == newProj ) {
				status.setText("create project button clicked");
				new CreateProjectTask().execute();
			} else if ( v == uploadCSV ) {
				Intent i = new Intent(this, FileBrowser.class);
				i.putExtra("filefilter", new String[]{"CSV"});
				startActivityForResult(i, FILEPICK);
			} else if ( v == mediaProj ) {
				Intent i = new Intent(this, FileBrowser.class);
				startActivityForResult(i, MEDIAPROJPICK);
			} else if ( v == mediaDataset ) {
				Intent i = new Intent(this, FileBrowser.class);
				startActivityForResult(i, MEDIADATASET);
			}
		} else {
			Toast.makeText(this, "no innahnet!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			if(requestCode == FILEPICK) {
				String filepath = data.getStringExtra("filepath");
				new CSVTask().execute(filepath);
			} else if (requestCode == MEDIAPROJPICK) {
				String filepath = data.getStringExtra("filepath");
				new ProjMediaTask().execute(filepath);
			} else if (requestCode == MEDIADATASET) {
				String filepath = data.getStringExtra("filepath");
				new DSMediaTask().execute(filepath);
			}
		}
	}
	
	private class LoginTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			return api.createSession(params[0], params[1]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result) {
				status.setText("Login Succeeded");
				getusers.setEnabled(true);
			} else {
				status.setText("Login Failed");
			}
		}
	}
	private class LogoutTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			api.deleteSession();
			return null;
		}
	}

	private class UsersTask extends AsyncTask<Void, Void, ArrayList<RPerson>> {
		@Override
		protected ArrayList<RPerson> doInBackground(Void... params) {
			if(userName.getText().toString().equals("")) {
				return api.getUsers(1, 10, true, "");
			} else {
				ArrayList<RPerson> rp = new ArrayList<RPerson>();
				rp.add(api.getUser(userName.getText().toString()));
				return rp;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<RPerson> people) {
			status.setText("People:\n");
			for(RPerson p : people) {
				status.append(p.name + "\n");
			}
		}
	}
	
	private class NewsTask extends AsyncTask<Void, Void, ArrayList<RNews>> {
		@Override
		protected ArrayList<RNews> doInBackground(Void... params) {
			if(newsId.getText().toString().equals("")) {
				return api.getNewsEntries(1, 10, true, "");
			} else {
				ArrayList<RNews> rp = new ArrayList<RNews>();
				rp.add(api.getNewsEntry(Integer.valueOf(newsId.getText().toString())));
				return rp;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<RNews> blogs) {
			status.setText("News:\n");
			for(RNews p : blogs) {
				status.append(p.name + "\n");
			}
		}
	}

	private class ProjectsTask extends AsyncTask<Void, Void, ArrayList<RProject>> {
		ArrayList<RProjectField> rpfs = new ArrayList<RProjectField>();

		@Override
		protected ArrayList<RProject> doInBackground(Void... params) {
			if(projID.getText().toString().equals("")) {
				return api.getProjects(1, 10, true, "");
			} else {
				ArrayList<RProject> rp = new ArrayList<RProject>();
				rp.add(api.getProject(Integer.parseInt(projID.getText().toString())));
				rpfs = api.getProjectFields(Integer.parseInt(projID.getText().toString()));
				return rp;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<RProject> projects) {
			status.setText("Projects:\n");
			for(RProject p : projects) {
				status.append(p.name + "\n");
				if(rpfs.size() > 0) {
					status.append("\nFields:\n");
					for(RProjectField rp : rpfs) {
						status.append(rp.name+"\n");
					}
				}
			}
		}
	}

	private class AppendTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			JSONObject toAppend = new JSONObject();
			try {
				toAppend.put("0", new JSONArray().put("2013/08/05 10:50:20"));
				toAppend.put("1", new JSONArray().put("119"));
				toAppend.put("2", new JSONArray().put("120"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			api.appendDataSetData(20, toAppend);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			
		}
	}
	
	private class UploadTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			JSONObject newData = new JSONObject();
			try {
				newData.put("0", new JSONArray().put("2013/08/05 10:50:20"));
				newData.put("1", new JSONArray().put("119"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			api.uploadDataSet(2, newData, "mobile upload testfuyf");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
		}
	}
	
	private class CSVTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			api.uploadCSV(7, new File(params[0]), "csv from app");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
		}
	}
	
	private class ProjMediaTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			api.uploadProjectMedia(7, new File(params[0]));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
		}
	}
	
	private class DSMediaTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			api.uploadDataSetMedia(42, new File(params[0]));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
		}
	}
	
	private class CreateProjectTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<RProjectField> fields = new ArrayList<RProjectField>();
			
			RProjectField time = new RProjectField();
			time.type = RProjectField.TYPE_TIMESTAMP;
			time.name = "Time";
			fields.add(time);
			
			RProjectField amount = new RProjectField();
			amount.type = RProjectField.TYPE_NUMBER;
			amount.name = "Amount";
			amount.unit = "units";
			fields.add(amount);
			
			api.createProject("Project from Mobile", fields);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
		}
	}

}
