package com.example.vernierlabquest2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
import android.widget.TextView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.waffle.Waffle;

public class MainActivity extends Activity {
	private RestAPI rapi;
	private Waffle w;
	// private TextView iSENSEStatus;
	private TextView LabQuestStatus;
	private Button LabQuestConnect;
	private Button iSENSEUpload;

	private ArrayList<ArrayList<String>> LabQuestData;
	private ArrayList<String> LabQuestType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						this);
		w = new Waffle(this);
		LabQuestStatus = (TextView) findViewById(R.id.labquest_status_text);
		// iSENSEStatus = (TextView) findViewById(R.id.isense_status_text);
		LabQuestConnect = (Button) findViewById(R.id.labquest_connect);
		iSENSEUpload = (Button) findViewById(R.id.isense_upload);
		LabQuestData = new ArrayList<ArrayList<String>>();
		LabQuestType = new ArrayList<String>();

		LabQuestConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ConnectToLabQuest();
			}
		});

		iSENSEUpload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("isense_settings",
						0);
				String iSENSEUser = sp.getString("isense_user", "");
				String iSENSEPass = sp.getString("isense_pass", "");
				boolean iSENSELoggedIn = rapi.login(iSENSEUser, iSENSEPass);
				if (iSENSELoggedIn) {
					Log.v("Tag", "Logged in");
				} else {
					Log.v("Tag", "Invalid User/Pass");
				}

				// w.make("Upload to iSENSE!",
				// Waffle.LENGTH_LONG,Waffle.IMAGE_CHECK);

			}
		});
	}

	protected boolean ConnectToLabQuest() {
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
			i = new Intent(this, LabQuestSettings.class);
			startActivity(i);
			return true;
		case R.id.isense_settings:
			// iSENSEStatus.setText(getResources().getString(R.string.isense_status_logged_in));
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
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private static String GetColumns(String IP, String column) {
		String result = null;
		try {
			result = httpGet("http://" + IP + "/columns/" + column);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private class LabQuestConnectTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPostExecute(Void result) {
			for (ArrayList<String> i : LabQuestData) {
				for (String j : i) {
					Log.v("Type", j);
				}
			}
			LabQuestStatus.setText(getResources().getString(
					R.string.labquest_status_connected));
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Void... params) {

			SharedPreferences sp = getSharedPreferences("labquest_settings", 0);
			String LabQuestIP = sp.getString("labquest_ip", "");

			try {
				// Gets data from LQ
				JSONObject get_status_json;
				get_status_json = new JSONObject(GetStatus(LabQuestIP));
				int col_size = get_status_json.getJSONObject("views").length();
				long start_time = Long.parseLong(get_status_json
						.getString("columnListTimeStamp")) * 1000;// time
																	// started,
																	// in unix
																	// milliseconds
				String col_id[] = new String[col_size];// column id
				String col_type[] = new String[col_size];// type of date eg
															// time, temp, ph
				String col_data[] = new String[col_size];// column data
				// gets data from vernier labquest 2, in JSON format, puts into
				// strings
				for (int i = 0; i < col_size; i++) {// loop through all the
													// columns available
					String views = get_status_json.getJSONObject("views")
							.names().getString(i);
					if (get_status_json.getJSONObject("views")
							.getJSONObject(views).has("baseColID")) {
						col_id[i] = get_status_json.getJSONObject("views")
								.getJSONObject(views).getString("baseColID");
					} else if (get_status_json.getJSONObject("views")
							.getJSONObject(views).has("colID")) {
						col_id[i] = get_status_json.getJSONObject("views")
								.getJSONObject(views).getString("colID");
					}
					col_type[i] = get_status_json.getJSONObject("columns")
							.getJSONObject(col_id[i]).getString("name");
					JSONObject get_col_json = new JSONObject(GetColumns(
							LabQuestIP, col_id[i]));
					col_data[i] = get_col_json.getString("values");
				}

				// Rearranges data
				// gets data from strings and puts time data in time, and each
				// data set in data, and type in type
				ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
				ArrayList<String> type = new ArrayList<String>();
				for (int i = 0; i < col_size; i++) {
					ArrayList<String> temp = new ArrayList<String>();
					if ((col_type[i].compareTo("Time") == 0)
							&& (!type.contains("Time"))) { // time
						StringTokenizer time_tokenized = new StringTokenizer(
								col_data[i], "[,]");
						while (time_tokenized.hasMoreTokens()) {
							temp.add(Long.toString((long) Double
									.parseDouble(time_tokenized.nextToken())
									* 1000 + start_time));
						}
						data.add(temp);
						type.add(col_type[i]);
					} else if (col_type[i].compareTo("Time") != 0) {
						StringTokenizer data_tokenized = new StringTokenizer(
								col_data[i], "[,]");
						while (data_tokenized.hasMoreTokens()) {
							temp.add(data_tokenized.nextToken());
						}
						data.add(temp);
						type.add(col_type[i]);
					}
				}
				LabQuestData = data;
				LabQuestType = type;
			} catch (JSONException e) {
				e.printStackTrace();
			}

			// Log.v("JSON", JSONTest);
			return null;
		}
	}
}