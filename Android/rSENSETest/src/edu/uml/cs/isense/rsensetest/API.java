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

import org.json.JSONException;
import org.json.JSONObject;

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
		String result = makeRequest(baseURL, "login", "username_or_email="+username+"&password="+password, "POST");
		try {
			JSONObject j =  new JSONObject(result);
			authToken = j.getString("authenticity_token");
			if( j.getString("status").equals("success")) {
				return true;
			} else {
				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getProjects() {
		return makeRequest(baseURL, "projects", "authenticity_token="+authToken, "GET");
	}

	/*Authenticated function*/
	/*Must have called createSession before calling this function*/
	public String getUsers() {
		return makeRequest(baseURL, "users", "", "GET");
	}

	public String makeRequest(String baseURL, String path, String parameters, String reqType) {

		int mstat = 0;
		try {
			URL url = new URL(baseURL+"/"+path+"?"+parameters);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod(reqType);
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

		return "Error: status " + mstat;
	}
}
