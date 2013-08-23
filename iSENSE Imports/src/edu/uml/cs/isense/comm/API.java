package edu.uml.cs.isense.comm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import edu.uml.cs.isense.objects.RDataSet;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProject;
import edu.uml.cs.isense.objects.RProjectField;
import edu.uml.cs.isense.objects.RTutorial;

/**
 * A class which allows Android applications to interface with
 * the iSENSE website. Given a singleton instance of this class,
 * functions can be called through an AsyncTask.
 * 
 * @author Nick Ver Voort of the iSENSE Android-Development Team
 * with input from Mike Stowell and Jeremy Poulin
 */

public class API {
	private static API instance = null;
	private String baseURL = "";
	private final String publicURL = "http://129.63.16.128";
	private final String devURL = "http://129.63.16.30";
	private Context context;
	String authToken = "";
	RPerson currentUser;

	/**
	 * Constructor not to be called by a user of the API
	 * Users should call getInstance instead, which will call
	 * this constructor if necessary
	 */
	private API() {
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		baseURL = publicURL;
	}

	/**
	 * Gets the one instance of the API class (instead of recreating a new
	 * one every time). Functions as a constructor if the current instance is
	 * null.
	 * 
	 * @return current or new API
	 */
	public static API getInstance(Context c) {
		if(instance == null) {
			instance = new API();
		}
		instance.context = c;
		return instance;
	}

	/**
	 * Log in to iSENSE. After calling this function, authenticated API functions will work properly
	 * 
	 * @param username The username of the user to log in as
	 * @param password The password of the user to log in as
	 * @return True if login succeeds, false if it doesn't
	 */
	public boolean createSession(String username, String password) {
		try {
			String result = makeRequest(baseURL, "login", "username_or_email="+URLEncoder.encode(username, "UTF-8")+"&password="+URLEncoder.encode(password, "UTF-8"), "POST", null);
			System.out.println(result);
			JSONObject j =  new JSONObject(result);
			authToken = j.getString("authenticity_token");
			if( j.getString("status").equals("success")) {
				currentUser = getUser(username);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Log out of iSENSE
	 */
	public void deleteSession() {
		try {
			makeRequest(baseURL, "login", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "DELETE", null);
			currentUser = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 	Retrieves multiple projects off of iSENSE
	 * 
	 *@param page Which page of results to start from. 1-indexed
	 *@param perPage How many results to display per page
	 *@param descending Whether to display the results in descending order (true) or ascending order (false) 
	 *@param search A string to search all projects for
	 *@return An ArrayList of Project objects
	 */
	public ArrayList<RProject> getProjects(int page, int perPage, boolean descending, String search) {
		ArrayList<RProject> result = new ArrayList<RProject>();
		try {
			String sortMode = descending ? "DESC" : "ASC";
			String reqResult = makeRequest(baseURL, "projects", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8")
					+"&page="+page+"&per_page="+perPage+"&sort="+URLEncoder.encode(sortMode, "UTF-8")
					+"&search="+URLEncoder.encode(search, "UTF-8"), "GET", null);
			JSONArray j = new JSONArray(reqResult);
			for(int i = 0; i < j.length(); i++) {
				JSONObject inner = j.getJSONObject(i);
				RProject proj = new RProject();

				proj.project_id = inner.getInt("id");
				//proj.featured_media_id = inner.getInt("featuredMediaId");
				proj.name = inner.getString("name");
				proj.url = inner.getString("url");
				proj.hidden = inner.getBoolean("hidden");
				proj.featured = inner.getBoolean("featured");
				proj.like_count = inner.getInt("likeCount");
				proj.timecreated = inner.getString("createdAt");
				proj.owner_name = inner.getString("ownerName");
				proj.owner_url = inner.getString("ownerUrl");

				result.add(proj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Retrieves information about a single project on iSENSE
	 * 
	 * @param projectId The ID of the project to retrieve
	 * @return A Project object
	 */
	public RProject getProject(int projectId) {
		RProject proj = new RProject();
		try {
			String reqResult = makeRequest(baseURL, "projects/"+projectId, "authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "GET", null);
			JSONObject j = new JSONObject(reqResult);

			proj.project_id = j.getInt("id");
			//proj.featured_media_id = j.getInt("featuredMediaId");
			proj.name = j.getString("name");
			proj.url = j.getString("url");
			proj.hidden = j.getBoolean("hidden");
			proj.featured = j.getBoolean("featured");
			proj.like_count = j.getInt("likeCount");
			proj.timecreated = j.getString("createdAt");
			proj.owner_name = j.getString("ownerName");
			proj.owner_url = j.getString("ownerUrl");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return proj;
	}

	/**
	 * Creates a new project on iSENSE. The Field objects in the second parameter must have
	 * at a type and a name, and can optionally have a unit.
	 * 
	 * @param projectName The name of the new project to be created
	 * @param fields An ArrayList of field objects that will become the fields on iSENSE. 
	 * @return The ID of the created project
	 */
	public int createProject(String projectName, ArrayList<RProjectField> fields) {
		try {
			JSONObject postData = new JSONObject();
			postData.put("project_name", projectName);
			String reqResult = makeRequest(baseURL, "projects", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "POST", postData);
			JSONObject jobj = new JSONObject(reqResult);
			int pid = jobj.getInt("id");

			for(RProjectField rpf : fields) {
				JSONObject mField = new JSONObject();
				mField.put("project_id", pid);
				mField.put("field_type", rpf.type);
				mField.put("name", rpf.name);
				mField.put("unit", rpf.unit);
				JSONObject postData2 = new JSONObject();
				postData2.put("field", mField);
				postData2.put("project_id", pid);
				makeRequest(baseURL, "fields", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "POST", postData2);
			}

			return pid;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/** 
	 * Gets all of the fields associated with a project
	 * 
	 * @param projectId The unique ID of the project whose fields you want to see
	 * @return An ArrayList of ProjectField objects
	 */
	public ArrayList<RProjectField> getProjectFields(int projectId) {
		ArrayList<RProjectField> rpfs = new ArrayList<RProjectField>();

		try {
			String reqResult = makeRequest(baseURL, "projects/"+projectId, "authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "GET", null);
			JSONObject j = new JSONObject(reqResult);
			JSONArray j2 = j.getJSONArray("fields");
			for(int i = 0; i < j2.length(); i++) {
				JSONObject inner = j2.getJSONObject(i);
				RProjectField rpf = new RProjectField();
				rpf.field_id = inner.getInt("id");
				rpf.name = inner.getString("name");
				rpf.type = inner.getInt("type");
				rpf.unit = inner.getString("unit");
				rpfs.add(rpf);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rpfs;
	}

	/**
	 * 	Retrieves multiple tutorials off of iSENSE
	 * 
	 *@param page Which page of results to start from. 1-indexed
	 *@param perPage How many results to display per page
	 *@param descending Whether to display the results in descending order (true) or ascending order (false) 
	 *@param search A string to search all tutorials for
	 *@return An ArrayList of Tutorial objects
	 */
	public ArrayList<RTutorial> getTutorials(int page, int perPage, boolean descending, String search) {
		ArrayList<RTutorial> result = new ArrayList<RTutorial>();
		try {
			String sortMode = descending ? "DESC" : "ASC";
			String reqResult = makeRequest(baseURL, "tutorials", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8")+"&page="+page+"&per_page="+perPage+"&sort="+URLEncoder.encode(sortMode, "UTF-8")
					+"&search="+URLEncoder.encode(search, "UTF-8"), "GET", null);
			JSONArray j = new JSONArray(reqResult);
			for(int i = 0; i < j.length(); i++) {
				JSONObject inner = j.getJSONObject(i);
				RTutorial tut = new RTutorial();

				tut.tutorial_id = inner.getInt("id");
				tut.name = inner.getString("name");
				tut.url = inner.getString("url");
				tut.hidden = inner.getBoolean("hidden");
				tut.timecreated = inner.getString("createdAt");
				tut.owner_name = inner.getString("ownerName");
				tut.owner_url = inner.getString("ownerUrl");

				result.add(tut);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * Get a tutorial from iSENSE
	 * 
	 * @param tutorialId The ID of the tutorial to retrieve
	 * @return A Tutorial object
	 */
	public RTutorial getTutorial(int tutorialId) {
		RTutorial tut = new RTutorial();
		try {
			String reqResult = makeRequest(baseURL, "tutorials/"+tutorialId, "", "GET", null);
			JSONObject j = new JSONObject(reqResult);

			tut.tutorial_id = j.getInt("id");
			//proj.featured_media_id = j.getInt("featuredMediaId");
			tut.name = j.getString("name");
			tut.url = j.getString("url");
			tut.hidden = j.getBoolean("hidden");
			tut.timecreated = j.getString("createdAt");
			tut.owner_name = j.getString("ownerName");
			tut.owner_url = j.getString("ownerUrl");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return tut;
	}

	/**
	 * Retrieves a list of users on iSENSE
	 * This is an authenticated function and requires that the createSession function was called earlier
	 * 
	 * @param page Which page of users to start the request from
	 * @param perPage How many users per page to perform the search with
	 * @param descending Whether the list of users should be in descending order or not
	 * @param search A string to search all users for
	 * @return A list of Person objects
	 */
	public ArrayList<RPerson> getUsers(int page, int perPage, boolean descending, String search) {
		ArrayList<RPerson> people = new ArrayList<RPerson>();
		try {
			String sortMode = descending ? "DESC" : "ASC";
			String reqResult = makeRequest(baseURL, "users", "page="+page+"&per_page="+perPage+"&sort="+URLEncoder.encode(sortMode, "UTF-8")
					+"&search="+URLEncoder.encode(search, "UTF-8"), "GET", null);
			JSONArray j = new JSONArray(reqResult);
			for(int i = 0; i < j.length(); i++) {
				JSONObject inner = j.getJSONObject(i);
				RPerson person = new RPerson();

				person.person_id = inner.getInt("id");
				person.name = inner.getString("name");
				person.username = inner.getString("username");
				person.url = inner.getString("url");
				person.gravatar = inner.getString("gravatar");
				person.timecreated = inner.getString("createdAt");
				person.hidden = inner.getBoolean("hidden");

				people.add(person);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return people;
	}

	/**
	 * Gets a user off of iSENSE
	 * 
	 * @param username The username of the user to retrieve
	 * @return A Person object
	 */
	public RPerson getUser(String username) {
		RPerson person = new RPerson();
		try {
			String reqResult = makeRequest(baseURL, "users/"+username, "", "GET", null);
			JSONObject j = new JSONObject(reqResult);

			person.person_id = j.getInt("id");
			person.name = j.getString("name");
			person.username = j.getString("username");
			person.url = j.getString("url");
			person.gravatar = j.getString("gravatar");
			person.timecreated = j.getString("createdAt");
			person.hidden = j.getBoolean("hidden");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return person;
	}


	/**
	 * Retrieve a data set from iSENSE, with it's data field filled in
	 * The internal data set will be converted to column-major format, to make it compatible with 
	 * the uploadDataSet function
	 * 
	 * @param dataSetId The unique ID of the data set to retrieve from iSENSE
	 * @return A DataSet object
	 */
	public RDataSet getDataSet(int dataSetId) {
		RDataSet result = new RDataSet();
		try {
			String reqResult = makeRequest(baseURL, "data_sets/"+dataSetId, "recur=true", "GET", null);
			JSONObject j = new JSONObject(reqResult);

			result.ds_id = j.getInt("id");
			result.name = j.getString("name");
			result.hidden = j.getBoolean("hidden");
			result.url = j.getString("url");
			result.timecreated = j.getString("createdAt");
			result.fieldCount = j.getInt("fieldCount");
			result.datapointCount = j.getInt("datapointCount");
			result.data = rowsToCols(j.getJSONObject("data"));
			result.project_id = j.getJSONObject("project").getInt("id");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Gets all the data sets associated with a project
	 * The data sets returned by this function do not have their data field filled.
	 * 
	 * @param projectId The project ID whose data sets you want
	 * @return An ArrayList of Data Set objects, with their data fields left null
	 */
	public ArrayList<RDataSet> getDataSets(int projectId) {
		ArrayList<RDataSet> result = new ArrayList<RDataSet>();
		try {
			String reqResult = makeRequest(baseURL, "projects/"+projectId, "recur=true", "GET", null);
			JSONObject j = new JSONObject(reqResult);
			JSONArray dataSets = j.getJSONArray("dataSets");
			for(int i = 0; i < dataSets.length(); i++) {
				RDataSet rds = new RDataSet();
				JSONObject inner = dataSets.getJSONObject(i);
				rds.ds_id = inner.getInt("id");
				rds.name = inner.getString("name");
				rds.hidden = inner.getBoolean("hidden");
				rds.url = inner.getString("url");
				rds.timecreated = inner.getString("createdAt");
				rds.fieldCount = inner.getInt("fieldCount");
				rds.datapointCount = inner.getInt("datapointCount");
				result.add(rds);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Uploads a new data set to a project on iSENSE
	 * 
	 * @param projectId The ID of the project to upload data to
	 * @param data The data to be uploaded. Must be in column-major format to upload correctly
	 * @param datasetName The name of the dataset
	 * @return The integer ID of the newly uploaded dataset, or -1 if upload fails
	 */
	public int uploadDataSet(int projectId, JSONObject data, String datasetName) {
		ArrayList<RProjectField> fields = getProjectFields(projectId);
		JSONObject requestData = new JSONObject();
		ArrayList<String> headers = new ArrayList<String>();
		for(RProjectField rpf : fields) {
			headers.add(rpf.name);
		}
		try {
			requestData.put("headers", new JSONArray(headers));
			requestData.put("data", data);
			requestData.put("id", ""+projectId);
			if(!datasetName.equals("")) requestData.put("name", datasetName);
			String reqResult = makeRequest(baseURL, "projects/"+projectId+"/manualUpload", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "POST", requestData);
			JSONObject jobj = new JSONObject(reqResult);
			return jobj.getInt("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Append new rows of data to the end of an existing data set
	 * 
	 * @param dataSetId The ID of the data set to append to
	 * @param newData The new data to append
	 */
	public void appendDataSetData(int dataSetId, JSONObject newData) {
		JSONObject requestData = new JSONObject();
		RDataSet existingDs = getDataSet(dataSetId);
		JSONObject existing = existingDs.data;
		Iterator<?> keys = newData.keys();
		try {
			while(keys.hasNext()) {
				String currKey = (String) keys.next();
				JSONArray newDataPoints = newData.getJSONArray(currKey);
				for(int i = 0; i < newDataPoints.length(); i++) {
					existing.getJSONArray(currKey).put(newDataPoints.get(i));
				}
			}
			ArrayList<RProjectField> fields = getProjectFields(existingDs.project_id);
			ArrayList<String> headers = new ArrayList<String>();
			for(RProjectField rpf : fields) {
				headers.add(rpf.name);
			}
			requestData.put("headers", new JSONArray(headers));
			requestData.put("data", existing);
			requestData.put("id", ""+dataSetId);
			makeRequest(baseURL, "data_sets/"+dataSetId+"/edit", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "POST", requestData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Uploads a CSV file to iSENSE as a new data set
	 *
	 * @param projectId The ID of the project to upload data to
	 * @param csvToUpload The CSV as a File object
	 * @param datasetName The name of the dataset
	 * @return The ID of the data set created on iSENSE 
	 */ 
	public int uploadCSV(int projectId, File csvToUpload, String datasetName) {
		try {
			URL url = new URL(baseURL+"/projects/"+projectId+"/CSVUpload?authenticity_token="+URLEncoder.encode(authToken, "UTF-8"));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			MultipartEntity entity = new MultipartEntity();
			entity.addPart("utf8", new StringBody("\u2713", "text/plain", Charset.forName("UTF-8")));
			entity.addPart("dataset_name", new StringBody(datasetName));
			entity.addPart("csv", new FileBody(csvToUpload, "text/csv"));
			
			connection.setRequestProperty("Content-Type", entity.getContentType().getValue());
			connection.setRequestProperty("Accept", "application/json");
			OutputStream out = connection.getOutputStream();
			try {
				entity.writeTo(out);
			} finally {
				out.close();
			}
			connection.getResponseCode();
			InputStream in = new BufferedInputStream(connection.getInputStream());
			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				int i = in.read();
				while(i != -1) {
					bo.write(i);
					i = in.read();
				}
				JSONObject j = new JSONObject(bo.toString());
				return j.getInt("id");
			} catch (IOException e) {
				return -1;
			}
			finally {
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Uploads a file to the media section of a project
	 * 
	 * @param projectId The project ID to upload to
	 * @param mediaToUpload The file to upload
	 * @return ??? or -1 if upload fails
	 */
	public int uploadProjectMedia(int projectId, File mediaToUpload) {
		try {
			URL url = new URL(baseURL+"/media_objects/saveMedia/project/"+projectId+"?authenticity_token="+URLEncoder.encode(authToken, "UTF-8"));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			MultipartEntity entity = new MultipartEntity();
			entity.addPart("file", new FileBody(mediaToUpload, URLConnection.guessContentTypeFromName(mediaToUpload.getName())));
			
			connection.setRequestProperty("Content-Type", entity.getContentType().getValue());
			connection.setRequestProperty("Accept", "application/json");
			OutputStream out = connection.getOutputStream();
			try {
				entity.writeTo(out);
			} finally {
				out.close();
			}
			connection.getResponseCode();
			InputStream in = new BufferedInputStream(connection.getInputStream());
			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				int i = in.read();
				while(i != -1) {
					bo.write(i);
					i = in.read();
				}
			} catch (IOException e) {
				return -1;
			}
			finally {
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Uploads a file to the media section of a data set
	 * 
	 * @param dataSetId The data set ID to upload to
	 * @param mediaToUpload The file to upload
	 * @return ??? or -1 if upload fails
	 */
	public int uploadDataSetMedia(int dataSetId, File mediaToUpload) {
		try {
			URL url = new URL(baseURL+"/media_objects/saveMedia/data_set/"+dataSetId+"?authenticity_token="+URLEncoder.encode(authToken, "UTF-8"));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			MultipartEntity entity = new MultipartEntity();
			entity.addPart("file", new FileBody(mediaToUpload, URLConnection.guessContentTypeFromName(mediaToUpload.getName())));
			
			connection.setRequestProperty("Content-Type", entity.getContentType().getValue());
			connection.setRequestProperty("Accept", "application/json");
			OutputStream out = connection.getOutputStream();
			try {
				entity.writeTo(out);
			} finally {
				out.close();
			}
			connection.getResponseCode();
			InputStream in = new BufferedInputStream(connection.getInputStream());
			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				int i = in.read();
				while(i != -1) {
					bo.write(i);
					i = in.read();
				}
			} catch (IOException e) {
				return -1;
			}
			finally {
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public RPerson getCurrentUser() {
		return currentUser;
	}

	/**
	 * Makes an HTTP request and for JSON-formatted data. This call is blocking, and so functions that 
	 * call this function must not be run on the UI thread.
	 * 
	 * @param baseURL The base of the URL to which the request will be made
	 * @param path The path to append to the request URL
	 * @param parameters Parameters separated by ampersands (&)
	 * @param reqType The request type as a string (i.e. GET or POST)
	 * @return A String dump of a JSONObject representing the requested data
	 */
	private String makeRequest(String baseURL, String path, String parameters, String reqType, JSONObject postData) {

		byte[] mPostData = null;

		int mstat = 0;
		try {
			URL url = new URL(baseURL+"/"+path+"?"+parameters);
			System.out.println("Connect to: " + url);
			
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod(reqType);
			urlConnection.setRequestProperty("Accept", "application/json");

			if(postData != null) {
				mPostData = postData.toString().getBytes();
				urlConnection.setRequestProperty("Content-Length",Integer.toString(mPostData.length));
				urlConnection.setRequestProperty("Content-Type", "application/json");
				OutputStream out = urlConnection.getOutputStream();
				out.write(mPostData);
				out.close();
			}

			mstat = urlConnection.getResponseCode(); // TODO - add a try/catch for a java.net.ConnectException here.  We can get a connection failed here (ENETUNREACH) if the network is unreachable
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

	/**
	 * Switched the API instance between using the public iSENSE and the developer iSENSE
	 * 
	 * @param use Whether or not to use the developer iSENSE
	 */
	public void useDev(boolean use) {
		baseURL = use ? devURL : publicURL;
	}
	
	/**
	 * Directly set the base URL, rather than using the dev or production URLs
	 * 
	 * @param newUrl The URL to use as a base
	 */
	public void setBaseUrl(String newUrl) {
		baseURL = newUrl;
	}

	/**
	 * Returns status on whether you are connected to the Internet.
	 * 
	 * @return current connection status
	 */
	public boolean hasConnectivity() {

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected());

	}

	/**
	 * Reformats a row-major JSONObject into a column-major one
	 * 
	 * @param original The row-major formatted JSONObject
	 * @return A column-major reformatted version of the original JSONObject
	 */
	public JSONObject rowsToCols(JSONObject original) {
		JSONObject reformatted = new JSONObject();
		try {
			JSONArray inner = original.getJSONArray("data");
			for(int i = 0; i < inner.length(); i++) {
				JSONObject innermost = (JSONObject) inner.get(i);
				Iterator<?> keys = innermost.keys();
				while(keys.hasNext()) {
					String currKey = (String) keys.next();
					JSONArray currArray = new JSONArray();
					if(reformatted.has(currKey)) {
						currArray = reformatted.getJSONArray(currKey);
					}
					currArray.put(innermost.getString(currKey));
					reformatted.put(currKey, currArray);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return reformatted;
	}

	private static byte[] convertFileToByteArray(File f) {
		byte[] byteArray = null;
		try
		{
			InputStream inputStream = new FileInputStream(f);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024*8];
			int bytesRead =0;

			while ((bytesRead = inputStream.read(b)) != -1)
			{
				bos.write(b, 0, bytesRead);
			}

			byteArray = bos.toByteArray();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return byteArray;
	}
}
