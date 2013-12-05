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
import android.text.Html;
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

	Button btnDev, btnProd;
	TextView status;
	API api;
	
	int FILEPICK = 0;
	int MEDIAPROJPICK = 1;
	int MEDIADATASET = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		status = (TextView) findViewById(R.id.txt_results);
		btnDev = (Button) findViewById(R.id.btn_dev);
		btnProd = (Button) findViewById(R.id.btn_prod);

		btnDev.setOnClickListener(this);
		btnProd.setOnClickListener(this);

		api = API.getInstance(this);
		//api.setBaseUrl("http://129.63.17.17:3000");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if(api.hasConnectivity()) {
			if ( v == btnDev ) {
				api.useDev(true);
				status.setText("Starting test on rsense-dev...\n");
				new LoginTask().execute();
			} else if ( v == btnProd ) {
				api.useDev(false);
				status.setText("Starting test on isenseproject...\n");
				new LoginTask().execute();
			}
		} else {
			status.setText("Cannot run test, no connectivity.");
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
	
	private class LoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return api.createSession("mobile", "mobile");
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result) {
				status.append(Html.fromHtml("<font color=\"#00aa00\">Login successful.</font><br>"));
				new UsersTask().execute();
			} else {
				status.append(Html.fromHtml("<font color=\"#dd0000\">Login failed, aborting test.</font><br>"));
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

	private class UsersTask extends AsyncTask<Void, Void, RPerson> {
		@Override
		protected RPerson doInBackground(Void... params) {
			return api.getUser("NickAVV");
		}

		@Override
		protected void onPostExecute(RPerson p) {
			status.append(Html.fromHtml("<font color=\"#00aa00\">Get user "+p.name+" successful.</font><br>"));
			new ProjectsTask().execute();
		}
	}
	
	private class NewsTask extends AsyncTask<Void, Void, ArrayList<RNews>> {
		@Override
		protected ArrayList<RNews> doInBackground(Void... params) {
				return api.getNewsEntries(1, 2, true, "");
			
		}

		@Override
		protected void onPostExecute(ArrayList<RNews> blogs) {
			for(RNews p : blogs) {
				status.append(p.name + "\n");
			}
			if(blogs.size() <= 2) {
				status.append(Html.fromHtml("<font color=\"#00aa00\">Get (at most) 2 news items successful.</font><br>"));
			} else {
				status.append(Html.fromHtml("<font color=\"#dd0000\">Get (at most) 2 news items fail. Got "+blogs.size()+".</font><br>"));
			}
			//TODO- move next api call to the success case, once this succeeds on dev/live
			new CreateProjectTask().execute();
		}
	}

	private class ProjectsTask extends AsyncTask<Void, Void, ArrayList<RProject>> {
		ArrayList<ArrayList<RProjectField>> rpfs = new ArrayList<ArrayList<RProjectField>>();

		@Override
		protected ArrayList<RProject> doInBackground(Void... params) {
				ArrayList<RProject> rps = api.getProjects(1, 2, true, API.CREATED_AT, "");
				for(RProject rp : rps) {
					rpfs.add(api.getProjectFields(rp.project_id));
				}
				return rps;
		}

		@Override
		protected void onPostExecute(ArrayList<RProject> projects) {
			for(RProject p : projects) {
				status.append(p.name + "\n");
				
				if(rpfs.size() > 0) {
					for(RProjectField rp : rpfs.remove(0)) {
						status.append(" - "+rp.name+"\n");
					}
				}
			}
			if(projects.size() <= 2) {
				status.append(Html.fromHtml("<font color=\"#00aa00\">Get (at most) 2 Projects successful.</font><br>"));
				new NewsTask().execute();
			} else {
				status.append(Html.fromHtml("<font color=\"#dd0000\">Get (at most) 2 Projects fail. Got "+projects.size()+".</font><br>"));
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
	
	private class UploadTask extends AsyncTask<JSONObject, Void, Void> {
		@Override
		protected Void doInBackground(JSONObject... params) {
			api.uploadDataSet(2, params[0], "mobile upload testfuyf");
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
	
	private class CreateProjectTask extends AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
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
			
			return api.createProject("Test Project", fields);
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result == -1) {
				status.append(Html.fromHtml("<font color=\"#dd0000\">Create project fail.</font><br>"));
			} else {
				status.append(Html.fromHtml("<font color=\"#00aa00\">Create project success.</font><br>"));
				new DeleteProjectTask().execute(result);
			}
		}
	}
	private class DeleteProjectTask extends AsyncTask<Integer, Void, Integer> {
		@Override
		protected Integer doInBackground(Integer... params) {
			
			return api.deleteProject(params[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result == -1) {
				status.append(Html.fromHtml("<font color=\"#dd0000\">Delete project fail.</font><br>"));
			} else {
				status.append(Html.fromHtml("<font color=\"#00aa00\">Delete project success.</font><br>"));
			}
		}
	}

}
