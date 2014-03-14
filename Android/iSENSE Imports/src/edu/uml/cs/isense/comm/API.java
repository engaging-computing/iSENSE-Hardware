package edu.uml.cs.isense.comm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uml.cs.isense.objects.RDataSet;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProject;
import edu.uml.cs.isense.objects.RProjectField;

/**
 * A class which allows Android applications to interface with the iSENSE
 * website. Given a singleton instance of this class, functions can be called
 * through an AsyncTask.
 * 
 * @author Nick Ver Voort, Jeremy Poulin, and Mike Stowell of the iSENSE
 *         Android-Development Team
 * 
 */

public class API {
	private String version_major = "4";
	private String version_minor = "1";
	
	private String api_version = "1";
	private String version;

	private static API instance = null;

	private String baseURL = "";
	private final String publicURL = "http://isenseproject.org/api/v1";
	private final String devURL = "http://rsense-dev.cs.uml.edu/api/v1";

	String authToken = "";
	RPerson currentUser;

	private boolean usingDev = false;

	public static final int CREATED_AT = 0;
	public static final int UPDATED_AT = 1;

	private String email, password;

	public enum TargetType {PROJECT, DATA_SET};

	/**
	 * Constructor not to be called by a user of the API Users should call
	 * getInstance instead, which will call this constructor if necessary
	 */
	private API() {
		baseURL = publicURL;
	}

	/**
	 * Gets the one instance of the API class (instead of recreating a new one
	 * every time). Functions as a constructor if the current instance is null.
	 * 
	 * @return current or new API
	 */
	public static API getInstance() {
		if (instance == null) {
			instance = new API();
		}
		return instance;
	}

	/**
	 * Log in to iSENSE. Stores email and password variables so authenticated
	 * functions later will work
	 * 
	 * @param p_email
	 *            The email address of the user to log in as
	 * @param p_password
	 *            The password of the user to log in as
	 */
	public RPerson createSession(String p_email, String p_password) {
		try {
			String reqResult = makeRequest(baseURL, "users/myInfo", "email=" + URLEncoder.encode(p_email, "UTF-8")
					+ "&password=" + URLEncoder.encode(p_password, "UTF-8"),
					"GET", null);
			JSONObject j = new JSONObject(reqResult);
			if(j.getString("username") != null) {
				email = p_email;
				password = p_password;
				RPerson you = new RPerson();
				you.name = j.getString("username");
				you.gravatar = j.getString("gravatar");
				currentUser = you;
				return you;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Log out of iSENSE
	 */
	public void deleteSession() {
		email = "";
		password = "";
		currentUser = null;
	}

	public RPerson getCurrentUser() {
		return currentUser;
	}

	/**
	 * Verifies whether a given contributor key will work for a project
	 * 
	 * @param projectId
	 * @param conKey
	 * @return True is the key is valid for that project, false if it is not
	 */
	public boolean validateKey(int projectId, String conKey) {
		//TODO implement this when we get the API functionality to actually do it

		return true;
	}

	/**
	 * Retrieves multiple projects off of iSENSE.
	 * 
	 * @param page
	 *            Which page of results to start from. 1-indexed
	 * @param perPage
	 *            How many results to display per page
	 * @param descending
	 *            Whether to display the results in descending order (true) or
	 *            ascending order (false)
	 * @param search
	 *            A string to search all projects for
	 * @return An ArrayList of Project objects
	 */
	public ArrayList<RProject> getProjects(int page, int perPage,
			boolean descending, int sortOn, String search) {
		ArrayList<RProject> result = new ArrayList<RProject>();
		try {
			String order = descending ? "DESC" : "ASC";
			String sortMode = "";
			if (sortOn == CREATED_AT) {
				sortMode = "created_at";
			} else {
				sortMode = "updated_at";
			}
			String reqResult = makeRequest(baseURL, "projects", "page=" + page
					+ "&per_page=" + perPage + "&sort=" + sortMode + "&order="
					+ order + "&search=" + URLEncoder.encode(search, "UTF-8"),
					"GET", null);
			JSONArray j = new JSONArray(reqResult);
			for (int i = 0; i < j.length(); i++) {
				JSONObject inner = j.getJSONObject(i);
				RProject proj = new RProject();

				proj.project_id = inner.getInt("id");
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
	 * @param projectId
	 *            The ID of the project to retrieve
	 * @return A Project object
	 */
	public RProject getProject(int projectId) {
		RProject proj = new RProject();
		try {
			String reqResult = makeRequest(baseURL, "projects/" + projectId,
					"", "GET", null);
			JSONObject j = new JSONObject(reqResult);

			proj.project_id = j.getInt("id");
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
	 * Creates a new project on iSENSE. The Field objects in the second
	 * parameter must have at a type and a name, and can optionally have a unit.
	 * This is an authenticated function.
	 * 
	 * @param projectName
	 *            The name of the new project to be created
	 * @param fields
	 *            An ArrayList of field objects that will become the fields on
	 *            iSENSE.
	 * @return The ID of the created project
	 */
	public int createProject(String projectName, ArrayList<RProjectField> fields) {
		try {
			JSONObject postData = new JSONObject();
			postData.put("email", email);
			postData.put("password", password);
			postData.put("project_name", projectName);
			String reqResult = makeRequest(
					baseURL,
					"projects",
					"", "POST",
					postData);
			JSONObject jobj = new JSONObject(reqResult);
			int pid = jobj.getInt("id");

			for (RProjectField rpf : fields) {
				JSONObject mField = new JSONObject();
				mField.put("project_id", pid);
				mField.put("field_type", rpf.type);
				mField.put("field_name", rpf.name);
				mField.put("units", rpf.unit);
				JSONObject postData2 = new JSONObject();
				postData2.put("email", email);
				postData2.put("password", password);
				postData2.put("field", mField);
				makeRequest(baseURL, "fields", "", "POST",
						postData2);
			}

			return pid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Deletes a project on iSENSE. Logged in user must have permission on the
	 * site to do this
	 * 
	 * @param projectId
	 *            The ID of the project on iSENSE to be deleted
	 * @return 1 if the deletion succeeds.
	 */
	public int deleteProject(int projectId) {
		try {
			makeRequest(baseURL, "projects/" + projectId, "authenticity_token="
					+ URLEncoder.encode(authToken, "UTF-8"), "DELETE", null);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Gets all of the fields associated with a project.
	 * 
	 * @param projectId
	 *            The unique ID of the project whose fields you want to see
	 * @return An ArrayList of ProjectField objects
	 */
	public ArrayList<RProjectField> getProjectFields(int projectId) {
		ArrayList<RProjectField> rpfs = new ArrayList<RProjectField>();

		try {
			String reqResult = makeRequest(baseURL, "projects/" + projectId,
					"?recur=true", "GET", null);
			JSONObject j = new JSONObject(reqResult);
			JSONArray j2 = j.getJSONArray("fields");
			for (int i = 0; i < j2.length(); i++) {
				JSONObject inner = j2.getJSONObject(i);
				RProjectField rpf = new RProjectField();
				rpf.field_id = inner.getInt("id");
				rpf.name = inner.getString("name");
				rpf.type = inner.getInt("type");
				rpf.unit = inner.getString("unit");
				rpf.restrictions = inner.getString("restrictions");
				rpfs.add(rpf);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rpfs;
	}

	/**
	 * Retrieve a data set from iSENSE, with it's data field filled in The
	 * internal data set will be converted to column-major format, to make it
	 * compatible with the uploadDataSet function
	 * 
	 * @param dataSetId
	 *            The unique ID of the data set to retrieve from iSENSE
	 * @return A DataSet object
	 */
	public RDataSet getDataSet(int dataSetId) {
		RDataSet result = new RDataSet();
		try {
			String reqResult = makeRequest(baseURL, "data_sets/" + dataSetId,
					"recur=true", "GET", null);
			JSONObject j = new JSONObject(reqResult);

			result.ds_id = j.getInt("id");
			result.name = j.getString("name");
			result.hidden = j.getBoolean("hidden");
			result.url = j.getString("url");
			result.timecreated = j.getString("createdAt");
			result.fieldCount = j.getInt("fieldCount");
			result.datapointCount = j.getInt("datapointCount");
			result.data = rowsToCols(new JSONObject().put("data",
					j.getJSONArray("data")));
			result.project_id = j.getJSONObject("project").getInt("id");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Gets all the data sets associated with a project The data sets returned
	 * by this function do not have their data field filled.
	 * 
	 * @param projectId
	 *            The project ID whose data sets you want
	 * @return An ArrayList of Data Set objects, with their data fields left
	 *         null
	 */
	public ArrayList<RDataSet> getDataSets(int projectId) {
		ArrayList<RDataSet> result = new ArrayList<RDataSet>();
		try {
			String reqResult = makeRequest(baseURL, "projects/" + projectId,
					"recur=true", "GET", null);
			JSONObject j = new JSONObject(reqResult);
			JSONArray dataSets = j.getJSONArray("dataSets");
			for (int i = 0; i < dataSets.length(); i++) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Upload a dataset to iSENSE while logged in
	 * 
	 * @param projectId
	 *            The ID of the project to upload data to
	 * @param data
	 *            The data to be uploaded. Must be in column-major format to
	 *            upload correctly
	 * @param datasetName
	 *            The name of the dataset
	 * @return The integer ID of the newly uploaded dataset, or -1 if upload
	 *         fails
	 */
	public int uploadDataSet(int projectId, JSONObject data, String datasetName) {
		datasetName += appendedTimeStamp();

		JSONObject requestData = new JSONObject();

		try {
			requestData.put("email", email);
			requestData.put("password", password);
			requestData.put("title", datasetName);
			requestData.put("data", data);
			String reqResult = makeRequest(
					baseURL,
					"projects/" + projectId + "/jsonDataUpload", "", "POST",
					requestData);
			JSONObject jobj = new JSONObject(reqResult);
			return jobj.getInt("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Upload a dataset to iSENSE with a contributor key
	 * 
	 * @param projectId
	 *            The ID of the project to upload data to
	 * @param data
	 *            The data to be uploaded. Must be in column-major format to
	 *            upload correctly
	 * @param conKey
	 *            The Contributor Key
	 * @param conName
	 *            The Contributor name
	 * @return The integer ID of the newly uploaded dataset, or -1 if upload
	 *         fails
	 */
	public int uploadDataSet(int projectId, JSONObject data, String conKey, String conName) {
		JSONObject requestData = new JSONObject();

		try {
			requestData.put("contributor_key", conKey);
			requestData.put("contributor_name", conName);
			requestData.put("data", data);
			String reqResult = makeRequest(
					baseURL,
					"projects/" + projectId + "/jsonDataUpload", "", "POST",
					requestData);
			JSONObject jobj = new JSONObject(reqResult);
			return jobj.getInt("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Append new rows of data to the end of an existing data set ** This
	 * currently works for horrible reasons regarding how the website handles
	 * edit data sets ** Will fix hopefully --J TODO
	 * 
	 * @param dataSetId
	 *            The ID of the data set to append to
	 * @param newData
	 *            The new data to append
	 * 
	 * @return success or failure
	 */
	public boolean appendDataSetData(int dataSetId, JSONObject newData) {
		JSONObject requestData = new JSONObject();
		RDataSet existingDs = getDataSet(dataSetId);
		try {
			JSONObject combined = existingDs.data;
			// merge newdata into combined
			Iterator<?> keys = newData.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				for (int i = 0; i < newData.getJSONArray(key).length(); i++) {
					combined.accumulate(key, newData.getJSONArray(key).get(i));
				}
			}
			// fill in blank spots
			int maxDatapoints = 0;
			keys = combined.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (combined.getJSONArray(key).length() > maxDatapoints) {
					maxDatapoints = combined.getJSONArray(key).length();
				}
			}
			keys = combined.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				while (combined.getJSONArray(key).length() < maxDatapoints) {
					combined.accumulate(key, "");
				}
			}
			requestData.put("data", combined);
			requestData.put("id", "" + dataSetId);

			String result = makeRequest(
					baseURL,
					"data_sets/" + dataSetId + "/edit",
					"authenticity_token="
							+ URLEncoder.encode(authToken, "UTF-8"), "POST",
							requestData);
			new JSONObject(result); // this line will throw an exception if it
			// fails, thus returning false

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Uploads a file to the media section of a project while logged in
	 * 
	 * @param targetId
	 *            The ID of the thing you're uploading to
	 * @param mediaToUpload
	 *            The file to upload
	 * @param ttype
	 * 			The type of the target (project or dataset)
	 * @return The media object ID for the media uploaded or -1 if upload fails
	 */
	public int uploadMedia(int projectId, File mediaToUpload, TargetType ttype) {
		try {
			URL url = new URL(baseURL + "/media_objects/");

			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			MultipartEntity entity = new MultipartEntity();
			entity.addPart(
					"upload",
					new FileBody(mediaToUpload, URLConnection
							.guessContentTypeFromName(mediaToUpload.getName())));
			entity.addPart("email", new StringBody(email));
			entity.addPart("password", new StringBody(password));
			entity.addPart("type", new StringBody((ttype == TargetType.PROJECT) ? "project" : "data_set"));
			entity.addPart("id", new StringBody(""+projectId));

			connection.setRequestProperty("Content-Type", entity
					.getContentType().getValue());
			connection.setRequestProperty("Accept", "application/json");
			OutputStream out = connection.getOutputStream();
			try {
				entity.writeTo(out);
			} finally {
				out.close();
			}
			InputStream in = null;
			try {
				int response = connection.getResponseCode();
				if (response >= 200 && response < 300) {
					in = new BufferedInputStream(connection.getInputStream());
				} else {
					in = new BufferedInputStream(connection.getErrorStream());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return -1;
			}
			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				int i = in.read();
				while (i != -1) {
					bo.write(i);
					i = in.read();
				}
				String output = bo.toString();
				System.out.println("Returning from uploadProjectMedia: "
						+ output);
				try {
					JSONObject jobj = new JSONObject(output);
					int mediaObjID = jobj.getInt("id");
					return mediaObjID;
				} catch (JSONException e) {
					System.err
					.println("UploadProjectMedia: exception formatting JSON:");
					e.printStackTrace();
					return -1;
				} catch (Exception e) {
					System.err
					.println("UploadProjectMedia: generic exception:");
					e.printStackTrace();
					return -1;
				}
			} catch (IOException e) {
				return -1;
			} catch (NumberFormatException e) {
				return -1;
			} finally {
				in.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Uploads a file to the media section of a project with a contributor key
	 * 
	 * @param targetId
	 *            The ID of the thing you're uploading to
	 * @param mediaToUpload
	 *            The file to upload
	 * @param ttype
	 * 			The type of the target (project or dataset)
	 * @param conKey
	 * 			The contributor key
	 * @param conName
	 * 			The contributor name
	 * @return The media object ID for the media uploaded or -1 if upload fails
	 */
	public int uploadMedia(int projectId, File mediaToUpload, TargetType ttype, String conKey, String conName) {
		try {
			URL url = new URL(baseURL + "/media_objects/");

			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			MultipartEntity entity = new MultipartEntity();
			entity.addPart(
					"upload",
					new FileBody(mediaToUpload, URLConnection
							.guessContentTypeFromName(mediaToUpload.getName())));
			entity.addPart("contributor_key", new StringBody(conKey));
			entity.addPart("contributor_name", new StringBody(conName));
			entity.addPart("type", new StringBody((ttype == TargetType.PROJECT) ? "project" : "data_set"));
			entity.addPart("id", new StringBody(""+projectId));

			connection.setRequestProperty("Content-Type", entity
					.getContentType().getValue());
			connection.setRequestProperty("Accept", "application/json");
			OutputStream out = connection.getOutputStream();
			try {
				entity.writeTo(out);
			} finally {
				out.close();
			}
			InputStream in = null;
			try {
				int response = connection.getResponseCode();
				if (response >= 200 && response < 300) {
					in = new BufferedInputStream(connection.getInputStream());
				} else {
					in = new BufferedInputStream(connection.getErrorStream());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return -1;
			}
			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				int i = in.read();
				while (i != -1) {
					bo.write(i);
					i = in.read();
				}
				String output = bo.toString();
				System.out.println("Returning from uploadProjectMedia: "
						+ output);
				try {
					JSONObject jobj = new JSONObject(output);
					int mediaObjID = jobj.getInt("id");
					return mediaObjID;
				} catch (JSONException e) {
					System.err
					.println("UploadProjectMedia: exception formatting JSON:");
					e.printStackTrace();
					return -1;
				} catch (Exception e) {
					System.err
					.println("UploadProjectMedia: generic exception:");
					e.printStackTrace();
					return -1;
				}
			} catch (IOException e) {
				return -1;
			} catch (NumberFormatException e) {
				return -1;
			} finally {
				in.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Makes an HTTP request and for JSON-formatted data. This call is blocking,
	 * and so functions that call this function must not be run on the UI
	 * thread.
	 * 
	 * @param baseURL
	 *            The base of the URL to which the request will be made
	 * @param path
	 *            The path to append to the request URL
	 * @param parameters
	 *            Parameters separated by ampersands (&)
	 * @param reqType
	 *            The request type as a string (i.e. GET or POST)
	 * @return A String dump of a JSONObject representing the requested data
	 */
	private String makeRequest(String baseURL, String path, String parameters,
			String reqType, JSONObject postData) {

		byte[] mPostData = null;

		int mstat = 0;
		try {
			URL url = new URL(baseURL + "/" + path + "?" + parameters);
			System.out.println("Connect to: " + url);

			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();

			if (!reqType.equals("GET"))
			{
				urlConnection.setDoOutput(true);
			}

			urlConnection.setRequestMethod(reqType);
			urlConnection.setRequestProperty("Accept", "application/json");
			// urlConnection.setDoOutput(true);
			if (postData != null) {
				System.out.println("Post data: " + postData);
				mPostData = postData.toString().getBytes();
				urlConnection.setRequestProperty("Content-Length",
						Integer.toString(mPostData.length));
				urlConnection.setRequestProperty("Content-Type",
						"application/json");
				OutputStream out = urlConnection.getOutputStream();
				out.write(mPostData);
				out.close();
			}

			mstat = urlConnection.getResponseCode();
			InputStream in;
			System.out.println("Status: " + mstat);
			if (mstat >= 200 && mstat < 300) {
				in = new BufferedInputStream(urlConnection.getInputStream());
			} else {
				in = new BufferedInputStream(urlConnection.getErrorStream());
			}
			try {
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				int i = in.read();
				while (i != -1) {
					bo.write(i);
					i = in.read();
				}
				return bo.toString();
			} catch (IOException e) {
				return "";
			} finally {
				in.close();
			}
		} catch (ConnectException ce) {
			System.err
			.println("Connection failed: ENETUNREACH (network not reachable)");
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Error: status " + mstat;
	}

	/**
	 * Switched the API instance between using the public iSENSE and the
	 * developer iSENSE
	 * 
	 * @param use
	 *            Whether or not to use the developer iSENSE
	 */
	public void useDev(boolean use) {
		baseURL = use ? devURL : publicURL;
		usingDev = use;
	}

	/**
	 * Returns whether or not the API is using dev mode.
	 * 
	 * @return True if the API is using the development website, false
	 *         otherwise.
	 */
	public boolean isUsingDevMode() {
		return usingDev;
	}

	/**
	 * Directly set the base URL, rather than using the dev or production URLs
	 * 
	 * @param newUrl
	 *            The URL to use as a base
	 */
	public void setBaseUrl(String newUrl) {
		baseURL = newUrl;
	}

	/**
	 * Reformats a row-major JSONObject into a column-major one
	 * 
	 * @param original
	 *            The row-major formatted JSONObject
	 * @return A column-major reformatted version of the original JSONObject
	 */
	public JSONObject rowsToCols(JSONObject original) {
		JSONObject reformatted = new JSONObject();
		try {
			JSONArray inner = original.getJSONArray("data");
			for (int i = 0; i < inner.length(); i++) {
				JSONObject innermost = (JSONObject) inner.get(i);
				Iterator<?> keys = innermost.keys();
				while (keys.hasNext()) {
					String currKey = (String) keys.next();
					JSONArray currArray = new JSONArray();
					if (reformatted.has(currKey)) {
						currArray = reformatted.getJSONArray(currKey);
					}
					currArray.put(innermost.getString(currKey));
					reformatted.put(currKey, currArray);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reformatted;
	}

	/**
	 * Creates a unique date and timestamp used to append to data sets uploaded
	 * to the iSENSE website to ensure every data set has a unique identifier.
	 * 
	 * @return A pretty formatted date and timestamp
	 */
	private String appendedTimeStamp() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"MM/dd/yy, HH:mm:ss.SSS", Locale.US);
		Calendar cal = Calendar.getInstance();

		Random r = new Random();
		int rMicroseconds = r.nextInt(1000);
		String microString = "";
		if (rMicroseconds < 10)
			microString = "00" + rMicroseconds;
		else if (rMicroseconds < 100)
			microString = "0" + rMicroseconds;
		else
			microString = "" + rMicroseconds;

		return " - " + dateFormat.format(cal.getTime()) + microString;
	}

	/**
	 * Gets the current API version
	 * 
	 * @return API version in MAJOR.MINOR format
	 */
	public String getVersion() {
		version = version_major + "." + version_minor;
		return version;
	}

}
