package edu.uml.cs.isense.rsensetest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.os.AsyncTask;

public class API {
	String baseURL = "http://129.63.17.17:3000";
	String authToken = "";
	
	public API() {
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
	}
	
	/*Call this function to log in to iSENSE*/
	/*Once you've done this you'll be able to call authenticated functions and get data back*/
	/*Returns true if login succeeds*/
	public boolean createSession(String username, String password) {
		new RequestTask().execute(baseURL, "login", "username_or_email="+username+"&password="+password, "POST");
		return true;
	}
	
	public void getProjects() {
		new RequestTask().execute(baseURL, "projects", "authenticity_token="+authToken, "GET");
	}
	
	/*Authenticated function*/
	/*Must have called createSession before calling this function*/
	public void getUsers() {
		new RequestTask().execute(baseURL, "users", "", "GET");
	}
	
	class RequestTask extends AsyncTask<String, String, String>{

		String requestPathBase = "";

		@Override
		protected String doInBackground(String... uri) {
			int mstat = 0;
			requestPathBase = uri[1];
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
