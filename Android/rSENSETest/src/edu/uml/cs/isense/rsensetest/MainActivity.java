package edu.uml.cs.isense.rsensetest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	Button login, getusers, getprojects;
	TextView status;
	String baseURL = "http://129.63.17.17:3000";

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
		} else if ( v == getusers ) {
			status.setText("clicked get users");
		} else if ( v == getprojects ) {
			new RequestTask().execute(baseURL+"/projects");
		}
	}

	//nabbed from StackOverflow
	//http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
	class RequestTask extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... uri) {
			try {
				URL url = new URL(uri[0]);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestProperty("Accept", "application/json");
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				try {
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					int i = in.read();
					while(i != -1) {
						bo.write(i);
						i = in.read();
					}
					return bo.toString();
				} catch (IOException e) {
					return "";
				}
				finally {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "Error Reading Data!";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			System.out.println(result);
		}
	}

}
