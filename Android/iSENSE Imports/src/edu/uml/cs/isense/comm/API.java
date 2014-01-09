package edu.uml.cs.isense.comm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
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
import edu.uml.cs.isense.objects.RNews;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProject;
import edu.uml.cs.isense.objects.RProjectField;
import edu.uml.cs.isense.objects.RTutorial;

/**
 * A class which allows Android applications to interface with
 * the iSENSE website. Given a singleton instance of this class,
 * functions can be called through an AsyncTask.
 * 
 * @author Nick Ver Voort, Jeremy Poulin, and Mike Stowell
 * of the iSENSE Android-Development Team
 * 
 */

public class API {
	private String version_major = "3";
	private String version_minor = "1c";
	private String version;
	
	private static API instance = null;
	private String baseURL = "";
	private final String publicURL = "http://129.63.16.128";
	private final String devURL = "http://129.63.16.30";
	String authToken = "";
	RPerson currentUser;
	
	public static final int CREATED_AT = 0;
	public static final int UPDATED_AT = 1;

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
	public static API getInstance() {
		if(instance == null) {
			instance = new API();
		}
		return instance;
	}

	/**
	 * Log in to iSENSE. After calling this function, authenticated API functions will work properly.
	 * 
	 * @param username The username of the user to log in as
	 * @param password The password of the user to log in as
	 * @return True if login succeeds, false if it doesn't
	 */
	public boolean createSession(String username, String password) {
		try {
			String result = makeRequest(baseURL, "login", "email="+URLEncoder.encode(username, "UTF-8")
					+"&password="+URLEncoder.encode(password, "UTF-8"), "POST", null);
			System.out.println(result);
			JSONObject j =  new JSONObject(result);
			
			authToken = j.getString("authenticity_token");
			currentUser = getUser(j.getJSONObject("user").getInt("id"));
	    return true;
		} catch (Exception e) {
			// Didn't get an authenticity token.
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
	 * Retrieves multiple projects off of iSENSE.
	 * 
	 * @param page Which page of results to start from. 1-indexed
	 * @param perPage How many results to display per page
	 * @param descending Whether to display the results in descending order (true) or ascending order (false) 
	 * @param search A string to search all projects for
	 * @return An ArrayList of Project objects
	 */
	public ArrayList<RProject> getProjects(int page, int perPage, boolean descending, int sortOn, String search) {
		ArrayList<RProject> result = new ArrayList<RProject>();
		try {
			String order = descending ? "DESC" : "ASC";
			String sortMode = "";
			if(sortOn == CREATED_AT) {
				sortMode = "created_at";
			} else {
				sortMode = "updated_at";
			}
			String reqResult = makeRequest(baseURL, "projects", "page="+page+"&per_page="+perPage+"&sort="+sortMode
					+"&order="+order+"&search="+URLEncoder.encode(search, "UTF-8"), "GET", null);
			JSONArray j = new JSONArray(reqResult);
			for(int i = 0; i < j.length(); i++) {
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
	 * @param projectId The ID of the project to retrieve
	 * @return A Project object
	 */
	public RProject getProject(int projectId) {
		RProject proj = new RProject();
		try {
			String reqResult = makeRequest(baseURL, "projects/"+projectId, "", "GET", null);
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
	 * Creates a new project on iSENSE. The Field objects in the second parameter must have
	 * at a type and a name, and can optionally have a unit. This is an authenticated function.
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
	 * Deletes a project on iSENSE. Logged in user must have permission on the site to do this
	 * 
	 * @param projectId The ID of the project on iSENSE to be deleted
	 * @return 1 if the deletion succeeds.
	 */
	public int deleteProject(int projectId) {
		try {
			makeRequest(baseURL, "projects/"+projectId, "authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "DELETE", null);
			return 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/** 
	 * Gets all of the fields associated with a project.
	 * 
	 * @param projectId The unique ID of the project whose fields you want to see
	 * @return An ArrayList of ProjectField objects
	 */
	public ArrayList<RProjectField> getProjectFields(int projectId) {
		ArrayList<RProjectField> rpfs = new ArrayList<RProjectField>();

		try {
			String reqResult = makeRequest(baseURL, "projects/"+projectId, "", "GET", null);
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
			String order = descending ? "DESC" : "ASC";
			String reqResult = makeRequest(baseURL, "tutorials", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8")
					+"&page="+page+"&per_page="+perPage+"&sort=created_at"+"&order="+order
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
	 * This is an admin only function and requires that the current user be an admin
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
			String reqResult = makeRequest(baseURL, "users", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8")+"&page="+page+"&per_page="+perPage+"&sort="+URLEncoder.encode(sortMode, "UTF-8")
					+"&search="+URLEncoder.encode(search, "UTF-8"), "GET", null);
			System.out.println(reqResult);
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
	 * @param id The id of the user to retrieve
	 * @return A Person object
	 */
	public RPerson getUser(int id) {
		RPerson person = new RPerson();
		try {
			String reqResult = makeRequest(baseURL, "users/"+id, "", "GET", null);
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
	 * Retrieves a list of news articles on iSENSE
	 * 
	 * @param page Which page of news to start the request from
	 * @param perPage How many entries per page to perform the search with
	 * @param descending Whether the list of articles should be in descending order or not
	 * @param search A string to search all articles for
	 * @return A list of News objects
	 */
	public ArrayList<RNews> getNewsEntries(int page, int perPage, boolean descending, String search) {
		ArrayList<RNews> blogs = new ArrayList<RNews>();
		try {
			String sortMode = descending ? "DESC" : "ASC";
			// TODO use the auth token in the request! Otherwise the comment is a lie and the function won't work.
			String reqResult = makeRequest(baseURL, "news", "page="+page+"&per_page="+perPage+"&sort="+URLEncoder.encode(sortMode, "UTF-8")
					+"&search="+URLEncoder.encode(search, "UTF-8"), "GET", null);
			JSONArray j = new JSONArray(reqResult);
			for(int i = 0; i < j.length(); i++) {
				JSONObject inner = j.getJSONObject(i);
				RNews blog = new RNews();

				blog.news_id = inner.getInt("id");
				//blog.featured_media_id = inner.getInt("featuredMediaId");
				blog.name = inner.getString("name");
				blog.url = inner.getString("url");
				blog.timecreated = inner.getString("createdAt");
				blog.hidden = inner.getBoolean("hidden");

				blogs.add(blog);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return blogs;
	}
	
	/**
	 * Gets a news article off iSENSE
	 * 
	 * @param newsId The id of the news entry to retrieve
	 * @return A News object
	 */
	public RNews getNewsEntry(int newsId) {
		RNews blog = new RNews();
		try {
			String reqResult = makeRequest(baseURL, "news/"+newsId, "recur=true", "GET", null);
			JSONObject j = new JSONObject(reqResult);

			blog.news_id = j.getInt("id");
			//blog.featured_media_id = j.getInt("featuredMediaId");
			blog.name = j.getString("name");
			blog.url = j.getString("url");
			blog.timecreated = j.getString("createdAt");
			blog.hidden = j.getBoolean("hidden");
			blog.content = j.getString("content");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return blog;
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
			result.data = rowsToCols(new JSONObject().put("data", j.getJSONArray("data")));
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
	 * @deprecated - Will go away, to be replaced by jsonDataUpload
	 * 
	 * Uploads a new data set to a project on iSENSE
	 * 
	 * @param projectId The ID of the project to upload data to
	 * @param data The data to be uploaded. Must be in column-major format to upload correctly
	 * @param datasetName The name of the dataset
	 * @return The integer ID of the newly uploaded dataset, or -1 if upload fails
	 */
	public int uploadDataSet(int projectId, JSONObject data, String datasetName) {
		// append timestamp to the data set name to ensure uniqueness
		datasetName += appendedTimeStamp();
		
		ArrayList<RProjectField> fields = getProjectFields(projectId);
		JSONObject requestData = new JSONObject();
		ArrayList<String> headers = new ArrayList<String>();
		for(RProjectField rpf : fields) {
			headers.add(""+rpf.field_id);
		}
		try {
			requestData.put("headers", new JSONArray(headers));
			requestData.put("data", data);
			requestData.put("id", ""+projectId);
			if(!datasetName.equals("")) requestData.put("name", datasetName);
			String reqResult = makeRequest(baseURL, "projects/"+projectId+"/manualUpload", "authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "POST", requestData);
			System.out.println("Are I blank? = " + reqResult);
			JSONObject jobj = new JSONObject(reqResult);
			System.out.println("Returning: " + jobj.toString());
			return jobj.getInt("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/** TODO - change name from manualUpload to whatever it'll be.  eventually this will go away and become new uploadDataSet
	 * Uploads a new data set to a project on iSENSE
	 * 
	 * @param projectId The ID of the project to upload data to
	 * @param data The data to be uploaded. Must be in column-major format to upload correctly
	 * @param datasetName The name of the dataset
	 * @return The integer ID of the newly uploaded dataset, or -1 if upload fails
	 */
	public int jsonDataUpload(int projectId, JSONObject data, String datasetName) {
		// append timestamp to the data set name to ensure uniqueness
		datasetName += appendedTimeStamp();
		
		JSONObject requestData = new JSONObject();

		try {
			requestData.put("data", data);
			requestData.put("id", ""+projectId);
			if(!datasetName.equals("")) 
				requestData.put("title", datasetName);
			
			String reqResult = makeRequest(baseURL, "projects/"+projectId+"/jsonDataUpload", 
					"authenticity_token="+URLEncoder.encode(authToken, "UTF-8"), "POST", requestData);
			
			JSONObject jobj = new JSONObject(reqResult);
			System.out.println("Returning: " + jobj.toString());
			
			return jobj.getInt("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Append new rows of data to the end of an existing data set
	 * ** This currently works for horrible reasons regarding how the website handles
	 * edit data sets ** Will fix hopefully --J TODO
	 * 
	 * @param dataSetId The ID of the data set to append to
	 * @param newData The new data to append
	 */
	public void appendDataSetData(int dataSetId, JSONObject newData) {
		JSONObject requestData = new JSONObject();
		RDataSet existingDs = getDataSet(dataSetId);
		JSONObject existing = existingDs.data;
		JSONObject newJobj = new JSONObject();
		Iterator<?> keys = newData.keys();
		try {
			int curIndex = 0;
			while(keys.hasNext()) {
				String currKey = (String) keys.next();
				JSONArray newDataPoints = newData.getJSONArray(currKey);
				for(int i = 0; i < newDataPoints.length(); i++) {
					existing.getJSONArray(currKey).put(newDataPoints.get(i));
				}
				newJobj.put(curIndex + "", existing.getJSONArray(currKey)); curIndex++;			
			}
			ArrayList<RProjectField> fields = getProjectFields(existingDs.project_id);
			ArrayList<String> headers = new ArrayList<String>();
			for(RProjectField rpf : fields) {
				headers.add(rpf.field_id + "");
			}
			requestData.put("headers", new JSONArray(headers));
			requestData.put("data", newJobj);
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
		// append timestamp to the data set name to ensure uniqueness
		datasetName += appendedTimeStamp();
		
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
	 * @return The media object ID for the media uploaded or -1 if upload fails
	 */
	public int uploadProjectMedia(int projectId, File mediaToUpload) {
		try {
			URL url = new URL(baseURL+"/media_objects/saveMedia/project/"+projectId+"?authenticity_token="+URLEncoder.encode(authToken, "UTF-8")+"&non_wys=true");
			System.out.println("Connect to: " + url);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			MultipartEntity entity = new MultipartEntity();
			entity.addPart("upload", new FileBody(mediaToUpload, URLConnection.guessContentTypeFromName(mediaToUpload.getName())));
			
			connection.setRequestProperty("Content-Type", entity.getContentType().getValue());
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
				while(i != -1) {
					bo.write(i);
					i = in.read();
				}
				String output = bo.toString();
				System.out.println("Returning from uploadDataSetMedia: " + output);
				try {
					JSONObject jobj = new JSONObject(output);
					int mediaObjID = jobj.getInt("id");
					return mediaObjID;
				} catch (JSONException e) {
					System.err.println("UploadProjectMedia: exception formatting JSON:");
					e.printStackTrace();
					return -1;
				} catch (Exception e) {
					System.err.println("UploadProjectMedia: generic exception:");
					e.printStackTrace();
					return -1;
				}
			} catch (IOException e) {
				return -1;
			}  catch (NumberFormatException e) {
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
	 * Uploads a file to the media section of a data set
	 * 
	 * @param dataSetId The data set ID to upload to
	 * @param mediaToUpload The file to upload
	 * @return The media object ID for the media uploaded or -1 if upload fails
	 */
	public int uploadDataSetMedia(int dataSetId, File mediaToUpload) {
		try {
			URL url = new URL(baseURL+"/media_objects/saveMedia/data_set/"+dataSetId+"?authenticity_token="+URLEncoder.encode(authToken, "UTF-8")+"&non_wys=true");
			System.out.println("Connect to: " + url);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			MultipartEntity entity = new MultipartEntity();
			entity.addPart("upload", new FileBody(mediaToUpload, URLConnection.guessContentTypeFromName(mediaToUpload.getName())));
			
			connection.setRequestProperty("Content-Type", entity.getContentType().getValue());
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
				while(i != -1) {
					bo.write(i);
					i = in.read();
				}
				String output = bo.toString();
				System.out.println("Returning from uploadDataSetMedia: " + output);
				try {
					JSONObject jobj = new JSONObject(output);
					int mediaObjID = jobj.getInt("id");
					return mediaObjID;
				} catch (JSONException e) {
					System.err.println("UploadDataSetMedia: exception formatting JSON:");
					e.printStackTrace();
					return -1;
				} catch (Exception e) {
					System.err.println("UploadDataSetMedia: generic exception:");
					e.printStackTrace();
					return -1;
				}
			} catch (IOException e) {
				System.out.println("Returning -1 from IOException in uploadDataSetMedia");
				return -1;
			} catch (NumberFormatException e) {
				System.out.println("Returning -1 from NumberFormatException in uploadDataSetMedia");
				return -1;
			} finally {
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Returning -1 from who knows why in uploadDataSetMedia");
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
				System.out.println("Post data: " + postData);
				mPostData = postData.toString().getBytes();
				urlConnection.setRequestProperty("Content-Length",Integer.toString(mPostData.length));
				urlConnection.setRequestProperty("Content-Type", "application/json");
				OutputStream out = urlConnection.getOutputStream();
				out.write(mPostData);
				out.close();
			}

			mstat = urlConnection.getResponseCode();
			InputStream in;
			System.out.println("Status: "+mstat);
			if(mstat>=200 && mstat < 300) {
				in = new BufferedInputStream(urlConnection.getInputStream());
			} else {
				in = new BufferedInputStream(urlConnection.getErrorStream());
			}
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
		} catch (ConnectException ce) {
			System.err.println("Connection failed: ENETUNREACH (network not reachable)");
			ce.printStackTrace();
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
	
	/**
	 * Creates a unique date and timestamp used to append to data sets uploaded to the iSENSE
	 * website to ensure every data set has a unique identifier.
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
	    if (rMicroseconds < 10) microString = "00" + rMicroseconds;
	    else if (rMicroseconds < 100) microString = "0" + rMicroseconds;
	    else microString = "" + rMicroseconds;
		
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
