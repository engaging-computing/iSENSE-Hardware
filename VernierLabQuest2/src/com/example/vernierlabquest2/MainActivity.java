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
import android.content.Context;
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
import android.widget.TextView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;

public class MainActivity extends Activity {
	private String tag = "MainActivity";
	private RestAPI rapi;

	private TextView iSENSEStatus;
	private TextView LabQuestStatus;
	private Button LabQuestConnect;
	private Button iSENSEUpload;
	private EditText SessionName;
	private ArrayList<JSONArray> LabQuestData;
	private ArrayList<String> LabQuestType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), this);

		LabQuestStatus = (TextView) findViewById(R.id.labquest_status_text);
		iSENSEStatus = (TextView) findViewById(R.id.isense_status_text);
		LabQuestConnect = (Button) findViewById(R.id.labquest_connect);
		iSENSEUpload = (Button) findViewById(R.id.isense_upload);
		SessionName = (EditText) findViewById(R.id.session_name);

		LabQuestConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(tag, "Connect to LabQuest2");
				LabQuestConnect();
			}
		});

		iSENSEUpload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(tag, "Connect to LabQuest2");
				iSENSEUpload();
			}
		});
	}

	protected boolean iSENSEUpload() {
		SharedPreferences sp = getSharedPreferences("isense_settings", 0);
		String iSENSEUser = sp.getString("isense_user", "");
		String iSENSEPass = sp.getString("isense_pass", "");
		String iSENSEExpID = sp.getString("isense_expid", "");

		// Set iSENSEDev/iSENSE
		if (sp.getLong("isense_dev_mode", 0) == 1) {
			rapi.useDev(true);
			Log.v(tag, "Using iSENSE Dev");
		} else {
			rapi.useDev(false);
			Log.v(tag, "Using iSENSE");
		}

		// Log in to iSENSE
		if (rapi.login(iSENSEUser, iSENSEPass)) {
			Log.v(tag, "Logged in");
			iSENSEStatus.setText(getResources().getString(R.string.isense_status_logged_in));
		} else {
			Log.v(tag, "Invalid User/Pass");
			iSENSEStatus.setText(getResources().getString(R.string.isense_status_logged_in_error));
			return false;
		}

		// TODO Field Matching

		ArrayList<ExperimentField> iSENSEExpFields = rapi.getExperimentFields(Integer.parseInt(iSENSEExpID));
		String tempstr;
		tempstr = new String();
		for (ExperimentField e : iSENSEExpFields) {
			tempstr = tempstr + ", " + e.field_name;
		}
		Log.v(tag, "iSENSE Fields: " + tempstr);
		tempstr = new String();
		for (String e : LabQuestType) {
			tempstr = tempstr + ", " + e;
		}
		Log.v(tag, "LabQuest Fields: " + tempstr);

		// TODO Create JSONArray with ExperimentField and LQ2 Data
		JSONArray iSENSEExpData = new JSONArray();
		try {
			for (int i = 0; i < LabQuestData.get(0).length(); i++) {
				JSONArray temp = new JSONArray();
				for (int j = 0; j < LabQuestType.size(); j++) {
					temp.put(LabQuestData.get(j).get(i));
				}
				Log.v(tag,temp.toString());
				iSENSEExpData.put(temp);
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.v(tag,iSENSEExpData.toString());
		
		// Create Session
		int iSENSESessionID = rapi.createSession(iSENSEExpID, SessionName.getText().toString(), "Uploaded with the iSENSE LabQuest2 App!", "", "", "");

		Log.v(tag,"iSENSESessionID: "+ Integer.toString(iSENSESessionID));
		rapi.putSessionData(iSENSESessionID, iSENSEExpID, iSENSEExpData);
		return true;
	}

	protected boolean LabQuestConnect() {
		new LabQuestConnectTask().execute();
		return true;
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

		if (conn.getResponseCode() != 200) {
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

	private static String GetStatus(String IP) {
		String result = null;
		try {
			result = httpGet("http://" + IP + "/status");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String GetColumns(String IP, String column) {
		String result = null;
		try {
			result = httpGet("http://" + IP + "/columns/" + column);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private class LabQuestConnectTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPostExecute(Void result) {
			LabQuestStatus.setText(getResources().getString(R.string.labquest_status_connected));
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Void... params) {
			LabQuestData = new ArrayList<JSONArray>();
			LabQuestType = new ArrayList<String>();
			SharedPreferences sp = getSharedPreferences("labquest_settings", 0);
			String LabQuestIP = sp.getString("labquest_ip", "");
			try {
				// Gets data from LQ
				JSONObject get_status_json;
				get_status_json = new JSONObject(GetStatus(LabQuestIP));
				int col_size = get_status_json.getJSONObject("views").length();
				// start_time in unix milliseconds
				double start_time = Double.parseDouble(get_status_json.getString("columnListTimeStamp")) * 1000;
				String col_id[] = new String[col_size];// column id
				String col_type[] = new String[col_size];// type of date
				String col_data[] = new String[col_size];// column data
				JSONArray col_data2[] = new JSONArray[col_size];
				// loop through all the columns available
				for (int i = 0; i < col_size; i++) {
					String views = get_status_json.getJSONObject("views").names().getString(i);
					if (get_status_json.getJSONObject("views").getJSONObject(views).has("baseColID")) {
						col_id[i] = get_status_json.getJSONObject("views").getJSONObject(views).getString("baseColID");
					} else if (get_status_json.getJSONObject("views").getJSONObject(views).has("colID")) {
						col_id[i] = get_status_json.getJSONObject("views").getJSONObject(views).getString("colID");
					}
					col_type[i] = get_status_json.getJSONObject("columns").getJSONObject(col_id[i]).getString("name");

					JSONObject get_col_json = new JSONObject(GetColumns(LabQuestIP, col_id[i]));
					col_data2[i] = get_col_json.getJSONArray("values");
					col_data[i] = get_col_json.getString("values");
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
							for (int j = 0; j < col_data2[i].length(); j++) {
								temp.put(col_data2[i].getDouble(j) * 1000 + start_time);
							}
							// TODO add start_time
							LabQuestData.add(temp);
							LabQuestType.add(col_type[i]);
						}
					} else {
						LabQuestData.add(col_data2[i]);
						LabQuestType.add(col_type[i]);
					}
				}
				for (int i = 0; i < LabQuestData.size(); i++) {
					Log.v(tag, Integer.toString(i) + ":" + LabQuestType.get(i) + "," + LabQuestData.get(i).toString());
				}

				// TODO change this to JSON Array instead of strings
				// Long start_time: time the experiment started
				// String col_data[]: all data
				// String col_type[]: all types
				// There could be multiple time types (only use one)

				// probably dont need
				// ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
				// ArrayList<String> type = new ArrayList<String>();
				// for (int i = 0; i < col_size; i++) {
				// ArrayList<String> temp = new ArrayList<String>();
				// if ((col_type[i].compareTo("Time") == 0) && (!type.contains("Time"))) { // time
				// StringTokenizer time_tokenized = new StringTokenizer(col_data[i], "[,]");
				// while (time_tokenized.hasMoreTokens()) {
				// temp.add(Long.toString((long) Double.parseDouble(time_tokenized.nextToken()) * 1000
				// + start_time));
				// }
				// data.add(temp);
				// type.add(col_type[i]);
				// } else if (col_type[i].compareTo("Time") != 0) {
				// StringTokenizer data_tokenized = new StringTokenizer(col_data[i], "[,]");
				// while (data_tokenized.hasMoreTokens()) {
				// temp.add(data_tokenized.nextToken());
				// }
				// data.add(temp);
				// type.add(col_type[i]);
				// }
				// }
				// LabQuestData = data;
				// LabQuestType = type;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}