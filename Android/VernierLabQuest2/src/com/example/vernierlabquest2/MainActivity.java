package com.example.vernierlabquest2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.objects.ExperimentField;

public class MainActivity extends Activity {
	private String tag = "LabQuest2 iSENSE";
	private RestAPI rapi;
	private Button Connect;
	private EditText SessionName;
	private ArrayList<JSONArray> LabQuestData;
	private ArrayList<String> LabQuestType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), this);

		Connect = (Button) findViewById(R.id.connect);
		SessionName = (EditText) findViewById(R.id.session_name);

		Connect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(tag, "Attempting to Upload...");
				new ConnectAndUpload().execute();
			}
		});
	}

	protected boolean iSENESLogin() {
		SharedPreferences sp = getSharedPreferences("isense_settings", 0);
		// Set iSENSEDev/iSENSE
		if (sp.getLong("isense_dev_mode", 0) == 1) {
			rapi.useDev(true);
			Log.v(tag, "Using iSENSE Dev");
		} else {
			rapi.useDev(false);
			Log.v(tag, "Using iSENSE Live");
		}
		if (rapi.login(sp.getString("isense_user", ""), sp.getString("isense_pass", ""))) {
			return true;
		}
		return false;
	}

	protected int iSENSEUpload() {
		SharedPreferences sp = getSharedPreferences("isense_settings", 0);
		String iSENSEExpID = sp.getString("isense_expid", "");

		ArrayList<Integer> FieldMatch = new ArrayList<Integer>();
		ArrayList<ExperimentField> iSENSEExpFields = rapi.getExperimentFields(Integer.parseInt(iSENSEExpID));

		String tempstr;

		//unordered LQ
		tempstr = new String();
		for (String e : LabQuestType) {
			tempstr = tempstr + e + ", ";
		}
		Log.v(tag, "LabQuest Fields: " + tempstr);

		//isense fields
		tempstr = new String();
		for (ExperimentField e : iSENSEExpFields) {
			tempstr = tempstr + e.field_name + ", ";
		}
		Log.v(tag, "iSENSE Fields  : " + tempstr);

		for (ExperimentField isense_field : iSENSEExpFields)
		{
			for (int i = 0; i < LabQuestType.size(); i++)
			{
				if (isense_field.field_name.equals(LabQuestType.get(i)))
				{
					FieldMatch.add(i);
					break;
				}
			}
		}

		//creating JSON Array
		JSONArray iSENSEExpData = new JSONArray();
		try {
			for (int i = 0; i < LabQuestData.get(0).length(); i++) {
				JSONArray temp = new JSONArray();
				for (int j = 0; j < LabQuestType.size(); j++) {
					//put each data point into a json array
					temp.put(LabQuestData.get(FieldMatch.get(j)).get(i));
				}
				//put the json array into a json array
				iSENSEExpData.put(temp);
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// Create Session
		int iSENSESessionID = rapi.createSession(iSENSEExpID, SessionName.getText().toString(), "Uploaded with the iSENSE LabQuest2 App!", "", "", "");
		Log.v(tag, "iSENSESessionID: " + Integer.toString(iSENSESessionID));

		// Put Session
		rapi.putSessionData(iSENSESessionID, iSENSEExpID, iSENSEExpData);
		return iSENSESessionID;
	}

	protected void LabQuestConnect() {
		LabQuestData = new ArrayList<JSONArray>();
		LabQuestType = new ArrayList<String>();
		SharedPreferences sp = getSharedPreferences("labquest_settings", 0);
		String LabQuestIP = sp.getString("labquest_ip", "");
		try {
			// Gets data from LQ
			JSONObject get_status_json;
			get_status_json = new JSONObject(LabQuestGetStatus(LabQuestIP));
			int col_size = get_status_json.getJSONObject("views").length();
			// start_time in unix milliseconds
			double start_time = Double.parseDouble(get_status_json.getString("viewListTimeStamp")) * 1000;
			String col_id[] = new String[col_size];// column id
			String col_type[] = new String[col_size];// type of date
			JSONArray col_data[] = new JSONArray[col_size];
			// loop through all the columns available
			for (int i = 0; i < col_size; i++) {
				String views = get_status_json.getJSONObject("views").names().getString(i);
				if (get_status_json.getJSONObject("views").getJSONObject(views).has("baseColID")) {
					col_id[i] = get_status_json.getJSONObject("views").getJSONObject(views).getString("baseColID");
				} else if (get_status_json.getJSONObject("views").getJSONObject(views).has("colID")) {
					col_id[i] = get_status_json.getJSONObject("views").getJSONObject(views).getString("colID");
				}
				col_type[i] = get_status_json.getJSONObject("columns").getJSONObject(col_id[i]).getString("name");

				JSONObject get_col_json = new JSONObject(LabQuestGetColumns(LabQuestIP, col_id[i]));
				col_data[i] = get_col_json.getJSONArray("values");
			}
			// removes duplicate time entries
			boolean timefix = false;
			for (int i = 0; i < col_size; i++) {
				if (col_type[i].compareTo("Time") == 0) {
					if (timefix) {
						continue;
					} else {
						timefix = true;
						JSONArray temp = new JSONArray();
						for (int j = 0; j < col_data[i].length(); j++) {
							temp.put(col_data[i].getDouble(j) * 1000 + start_time);
						}
						LabQuestData.add(temp);
						LabQuestType.add(col_type[i]);
					}
				} else {
					LabQuestData.add(col_data[i]);
					LabQuestType.add(col_type[i]);
				}
			}
			for (int i = 0; i < LabQuestData.size(); i++) {
				Log.v(tag, Integer.toString(i) + ":" + LabQuestType.get(i) + "," + LabQuestData.get(i).toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.labquest_settings:
			Log.v(tag, "Start Activity LabQuestSettings");
			i = new Intent(this, LabQuestSettings.class);
			startActivity(i);
			return true;
		case R.id.isense_settings:
			Log.v(tag, "Start Activity iSENSESettings");
			i = new Intent(this, iSENSESettings.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static String httpGet(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(1000);
		if (conn.getResponseCode() != 200) {
			conn.disconnect();
			throw new IOException(conn.getResponseMessage());
		}
		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();
		conn.disconnect();
		return sb.toString();
	}

	private String LabQuestGetInfo(String IP) {
		String result = null;
		try {
			result = httpGet("http://" + IP + "/info");
		} catch (IOException e) {
			return null;
		}
		return result;
	}

	private String LabQuestGetStatus(String IP) {
		String result = null;

		try {
			result = httpGet("http://" + IP + "/status");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	private String LabQuestGetColumns(String IP, String column) {
		String result = null;
		try {
			result = httpGet("http://" + IP + "/columns/" + column);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private class ConnectAndUpload extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected void onPostExecute(final Integer e) {
			if (e > 0) {
				// success
				AlertDialog AD = new AlertDialog.Builder(MainActivity.this).create();
				AD.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AD.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO: launch website url
						Log.v(tag, "Session ID: " + e);
						dialog.dismiss();
					}
				});
				AD.setTitle("Successfully Uploaded!");
				AD.setMessage("Would you like to visualize your data on iSENSE?");
				AD.show();
			} else {
				AlertDialog AD = new AlertDialog.Builder(MainActivity.this).create();
				AD.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				switch (e) {
				default:
					AD.dismiss();
					break;
				case -1:
					AD.setTitle("Error");
					AD.setMessage("Unable to Connect to LabQuest2");
					AD.show();
					break;
				case -2:
					AD.setTitle("Error");
					AD.setMessage("Not Connected to the Internet");
					AD.show();
					break;
				case -3:
					AD.setTitle("Error");
					AD.setMessage("Please enter a Session Name");
					AD.show();
					break;
				case -4:
					AD.setTitle("Error");
					AD.setMessage("Invalid Username/Password on iSENSE");
					AD.show();
					break;
				case -5:
					AD.setTitle("Error");
					AD.setMessage("Please enter a username for iSENSE");
					AD.show();
					break;
				case -6:
					AD.setTitle("Error");
					AD.setMessage("Please enter a password for iSENSE");
					AD.show();
					break;
				case -7:
					AD.setTitle("Error");
					AD.setMessage("Please enter an Experiment ID for iSENSE");
					AD.show();
					break;
				case -8:
					AD.setTitle("Error");
					AD.setMessage("Unable to add a Session to iSENSE");
					AD.show();
					break;
				case -9:
					AD.setTitle("Error");
					AD.setMessage("Experiment does not exist");
					AD.show();
					break;
				case -10:
					AD.setTitle("Error");
					AD.setMessage("Experiment is closed");
					AD.show();
					break;
				}
			}
		}

		@Override
		protected Integer doInBackground(Void... params) {
			Log.v(tag, "ConnectAndUpload doInBackground");
			SharedPreferences sp_lq = getSharedPreferences("labquest_settings", 0);
			SharedPreferences sp_is = getSharedPreferences("isense_settings", 0);

			// Null session name
			if (SessionName.getText().length() == 0) {
				return -3;
			}
			// Null iSENSE username
			if (sp_is.getString("isense_user", "").length() == 0) {
				return -5;
			}
			// Null iSENSE password
			if (sp_is.getString("isense_pass", "").length() == 0) {
				return -6;
			}
			// Null iSENSE Experiment ID
			if (sp_is.getString("isense_expid", "").length() == 0) {
				return -7;
			}

			// no LabQuest
			if (LabQuestGetInfo(sp_lq.getString("labquest_ip", "")) == null) {
				return -1;
			}
			// online?
			if (!rapi.isConnectedToInternet()) {
				return -2;
			}

			// iSENSE login?
			if (!iSENESLogin()) {
				return -4;
			}
			//does the experiment exist?
			Experiment iSENSEExp = rapi.getExperiment(Integer.parseInt(sp_is.getString("isense_expid", "")));
			if (iSENSEExp == null) {
				return -9;
			}
			//is the experiment closed?
			Log.v(tag,"closed" + iSENSEExp.closed);
			if (iSENSEExp.closed == 1) {
				return -10;
			}
			LabQuestConnect();

			int iSENSESessionID = iSENSEUpload();
			if (iSENSESessionID == -1) {
				return -8;
			}

			return iSENSESessionID;
		}

	}
}