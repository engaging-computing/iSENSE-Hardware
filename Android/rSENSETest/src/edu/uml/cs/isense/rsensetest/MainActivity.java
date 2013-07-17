package edu.uml.cs.isense.rsensetest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

import java.net.CookiePolicy;
import org.json.JSONObject;

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
	String authToken = "";

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
		
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
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
			new RequestTask().execute(baseURL, "login", "username_or_email=testguy&password=1", "POST");
		} else if ( v == getusers ) {
			status.setText("clicked get users");
			new RequestTask().execute(baseURL, "users", "", "GET");
		} else if ( v == getprojects ) {
			status.setText("clicked get projects");
			new RequestTask().execute(baseURL, "projects", "authenticity_token="+authToken, "GET");
		}
	}

	//nabbed from StackOverflow
	//http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
	class RequestTask extends AsyncTask<String, String, String>{

		String requestPathBase = "";

		@Override
		protected String doInBackground(String... uri) {
			int mstat = 0;
			requestPathBase = uri[1];
			System.out.println(authToken);
			try {
				URL url = new URL(uri[0]+"/"+uri[1]+"?"+uri[2]);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod(uri[3]);
				urlConnection.setRequestProperty("Accept", "application/json");
				mstat = urlConnection.getResponseCode();
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
			return "Status: "+mstat;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			System.out.println(result);
			try {
				if(requestPathBase.equals("login")) {
					JSONObject j = new JSONObject(result);
					authToken = j.getString("authenticity_token");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
