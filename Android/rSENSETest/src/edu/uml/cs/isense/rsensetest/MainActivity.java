package edu.uml.cs.isense.rsensetest;

import java.util.ArrayList;

import android.app.Activity;
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
import edu.uml.cs.isense.objects.RDataSet;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProject;
import edu.uml.cs.isense.objects.RProjectField;

public class MainActivity extends Activity implements OnClickListener {

	Button login, logout, getusers, getprojects, randomTest;
	TextView status;
	EditText projID, userName;
	API api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		login = (Button) findViewById(R.id.btn_login);
		getusers = (Button) findViewById(R.id.btn_getusers);
		getprojects = (Button) findViewById(R.id.btn_getprojects);
		logout = (Button) findViewById(R.id.btn_logout);
		randomTest = (Button) findViewById(R.id.btn_random);
		status = (TextView) findViewById(R.id.txt_results);
		projID = (EditText) findViewById(R.id.et_projectnum);
		userName = (EditText) findViewById(R.id.et_username);

		login.setOnClickListener(this);
		logout.setOnClickListener(this);
		getusers.setOnClickListener(this);
		getprojects.setOnClickListener(this);
		randomTest.setOnClickListener(this);
		getusers.setEnabled(false);

		api = API.getInstance(this);
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
				new LoginTask().execute("testguy", "1");
			} else if ( v == getusers ) {
				status.setText("clicked get users");
				new UsersTask().execute();
			} if ( v == logout ) {
				status.setText("clicked logout");
				new LogoutTask().execute();
			} else if ( v == getprojects ) {
				status.setText("clicked get projects");
				new ProjectsTask().execute();
			} else if ( v == randomTest ) {
				status.setText("other button clicked");
				new OtherTask().execute();
			}
		} else {
			Toast.makeText(this, "no innahnet!", Toast.LENGTH_SHORT).show();
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
				return api.getUsers(1, 10, true);
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

	private class ProjectsTask extends AsyncTask<Void, Void, ArrayList<RProject>> {
		ArrayList<RProjectField> rpfs = new ArrayList<RProjectField>();

		@Override
		protected ArrayList<RProject> doInBackground(Void... params) {
			if(projID.getText().toString().equals("")) {
				return api.getProjects(1, 10, true);
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

	private class OtherTask extends AsyncTask<Void, Void, ArrayList<RDataSet>> {
		@Override
		protected ArrayList<RDataSet> doInBackground(Void... params) {
			return api.getDataSets(5);
		}

		@Override
		protected void onPostExecute(ArrayList<RDataSet> result) {
			status.setText("Datasets in project 5:\n");
			for(RDataSet rds : result) {
				status.append(rds.name+"\n");
			}
		}
	}

}
