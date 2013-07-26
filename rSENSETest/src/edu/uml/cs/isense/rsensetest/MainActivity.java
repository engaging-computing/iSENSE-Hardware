package edu.uml.cs.isense.rsensetest;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProject;

public class MainActivity extends Activity implements OnClickListener {

	Button login, getusers, getprojects;
	TextView status;
	API api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		login = (Button) findViewById(R.id.btn_login);
		getusers = (Button) findViewById(R.id.btn_getusers);
		getprojects = (Button) findViewById(R.id.btn_getprojects);
		status = (TextView) findViewById(R.id.txt_results);

		login.setOnClickListener(this);
		getusers.setOnClickListener(this);
		getprojects.setOnClickListener(this);
		
		api = API.getInstance();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if ( v == login ) {
			status.setText("clicked login");
			new LoginTask().execute("testguy", "1");
		} else if ( v == getusers ) {
			status.setText("clicked get users");
			new UsersTask().execute();
		} else if ( v == getprojects ) {
			status.setText("clicked get projects");
			new ProjectsTask().execute();
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
			} else {
				status.setText("Login Failed");
			}
		}
	}
	
	private class UsersTask extends AsyncTask<Void, Void, ArrayList<RPerson>> {
		@Override
		protected ArrayList<RPerson> doInBackground(Void... params) {
			return api.getUsers();
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
		@Override
		protected ArrayList<RProject> doInBackground(Void... params) {
			return api.getProjects();
		}
		
		@Override
		protected void onPostExecute(ArrayList<RProject> projects) {
			status.setText("Projects:\n");
			for(RProject p : projects) {
				status.append(p.name + "\n");
			}
		}
	}
	
}
