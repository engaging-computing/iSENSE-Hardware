package edu.uml.cs.isense.comm;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.util.Log;
import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.objects.Item;
import edu.uml.cs.isense.objects.LoadedExperiment;
import edu.uml.cs.isense.objects.Person;
import edu.uml.cs.isense.objects.Session;
import edu.uml.cs.isense.objects.SessionData;

/**
 * This class handles all the communications with the API provided by the
 * website. Most functions are blocking and supposed to be self caching.
 * 
 * Version 2.0
 * 
 * @author iSENSE Android-Development Team including Mike Stowell, Nick Ver
 *         Voort, Jeremy Poulin, and James Dalphond
 */
public class RestAPI {
	private static RestAPI instance = null;
	private static final String TAG = "RestAPI";
	private static final String serverRep = "Server Response: ";
	private static String base_url = "http://isense.cs.uml.edu/ws/api.php";
	private static String session_key = null;
	private final String charEncoding = "iso-8859-1";
	private String username = null;
	private ConnectivityManager connectivityManager;
	private RestAPIDbAdapter mDbHelper;
	private int uid;
	private JSONArray dataCache;

	/**
	 * Current connection status of RestAPI.
	 * 
	 * 200: valid connection 600: invalid connection NONE: not yet logged in
	 */
	public String connection = "NONE";

	@SuppressLint("NewApi")
	protected RestAPI() {
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
	}

	@SuppressLint("NewApi")
	protected RestAPI(ConnectivityManager cm, Context c) {
		mDbHelper = new RestAPIDbAdapter(c);
		connectivityManager = cm;
		dataCache = new JSONArray();

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
	}

	/**
	 * Gets the one instance of the RestAPI class (instead of recreating a new
	 * one every time). Functions as a constructor if the current instance is
	 * null.
	 * 
	 * @return current or new RestAPI
	 */
	public static RestAPI getInstance() {
		if (instance == null) {
			instance = new RestAPI();
		}

		return instance;
	}

	/**
	 * Get the one instance of the RestAPI class (instead of recreating a new
	 * one every time). Functions as a constructor if the current instance is
	 * null.
	 * 
	 * Will not overwrite the current instance with the new ConnectivityManager
	 * or Context.
	 * 
	 * @param cm
	 *            ContextManager to be used by RestAPI
	 * @param c
	 *            Context RestAPI will be created to
	 * @return current or new RestAPI
	 */
	public static RestAPI getInstance(ConnectivityManager cm, Context c) {
		if (instance == null) {
			instance = new RestAPI(cm, c);
		}

		return instance;
	}

	/**
	 * Gets the current session_key.
	 * 
	 * @return session_key as a String
	 */
	public String getSessionKey() {
		return session_key;
	}

	/**
	 * Gets the current user id.
	 * 
	 * @return user id as an int
	 */
	public int getUID() {
		return uid;
	}

	/**
	 * Verifies that session_key isn't null.
	 * 
	 * @return TRUE: valid session key FALSE: session key is null
	 */
	public boolean isLoggedIn() {
		return (session_key != null && session_key != "null" && session_key != "");
	}

	/**
	 * Gets current username if logged in, else returns null.
	 * 
	 * @return username as a String
	 */
	public String getLoggedInUsername() {
		return username;
	}

	/**
	 * Resets login information to null.
	 */
	public void logout() {
		session_key = null;
		username = null;
		uid = 0;
	}

	/**
	 * Breaks file up into a byte[] for internal use.
	 * 
	 * @param file
	 *            file to be broken down
	 * @return file as a byte[]
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
			return null;
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	/**
	 * Uploads an image in conjunction to a specific session.
	 * 
	 * @param image
	 *            file to be uploaded
	 * @param eid
	 *            experiment id as a String
	 * @param sid
	 *            session id as an integer
	 * @param img_name
	 *            name of the image
	 * @param img_desc
	 *            description of the image
	 * @return whether or not uploading was successful
	 */
	public boolean uploadPictureToSession(File image, String eid, int sid,
			String img_name, String img_desc) {
		// String target = "?method=uploadImageToSession&session_key=" +
		// session_key + "&sid=" + sid + "&img_name=" + img_name + "&img_desc="
		// + img_desc;

		try {

			byte[] data = getBytesFromFile(image);

			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";

			URL connectURL = new URL(RestAPI.base_url);
			HttpURLConnection conn = (HttpURLConnection) connectURL
					.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data, boundary=" + boundary);
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"method\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes("uploadImageToSession");

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"session_key\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes(session_key);

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"eid\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes(eid + "");

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"sid\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes(sid + "");

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"img_name\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes(img_name.replace(" ", "+"));

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"img_description\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes(img_desc.replace(" ", "+"));

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();
			dos.writeBytes(twoHyphens + boundary + lineEnd);

			// write content header
			dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\""
					+ image.getName() + "\"");
			dos.writeBytes(lineEnd);
			dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
			dos.writeBytes(lineEnd);

			// create a buffer of maximum size
			dos.write(data, 0, data.length);

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			dos.flush();
			dos.close();

			// used to be used for error checking
			DataInputStream inStream = new DataInputStream(
					conn.getInputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(
					inStream));
			String str;
			try {
				while ((str = in.readLine()) != null) {
					Log.i(TAG, serverRep + str);
				}
				inStream.close();
				return true;
			} catch (IOException ioex) {
				ioex.printStackTrace();
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Uploads an image to an experiment (NOT session specific).
	 * 
	 * @param image
	 *            file to be uploaded
	 * @param eid
	 *            experiment id as a String
	 * @param img_name
	 *            name of the image
	 * @param img_desc
	 *            description of the image
	 * @return whether or not uploading was successful
	 */
	public boolean uploadPicture(File image, String eid, String img_name,
			String img_desc) {
		// String target = "?method=uploadImageToExperiment&session_key=" +
		// session_key + "&eid=" + eid + "&img_name=" + img_name + "&img_desc="
		// + img_desc;

		try {
			byte[] data = getBytesFromFile(image);

			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary = "*****";

			URL connectURL = new URL(RestAPI.base_url);
			HttpURLConnection conn = (HttpURLConnection) connectURL
					.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data, boundary=" + boundary);

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"method\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes("uploadImageToExperiment");

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"session_key\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes(session_key);

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"eid\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes(eid + "");

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"img_name\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes(img_name.replace(" ", "+"));

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();

			// submit header
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"img_description\""
					+ lineEnd);
			dos.writeBytes(lineEnd);

			// insert submit
			dos.writeBytes(img_desc.replace(" ", "+"));

			// submit closer
			dos.writeBytes(lineEnd);
			dos.flush();
			dos.writeBytes(twoHyphens + boundary + lineEnd);

			// write content header
			dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\""
					+ image.getName() + "\"");
			dos.writeBytes(lineEnd);
			dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
			dos.writeBytes(lineEnd);

			// create a buffer of maximum size
			dos.write(data, 0, data.length);

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			dos.flush();
			dos.close();

			try {
				DataInputStream inStream = new DataInputStream(
						conn.getInputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						inStream));
				String str;

				while ((str = in.readLine()) != null) {
					Log.i(TAG, serverRep + str);
				}
				inStream.close();
				return true;
			} catch (IOException ioex) {
				ioex.printStackTrace();
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Attempts to login with the given username and password.
	 * 
	 * @param username
	 *            provided username
	 * @param password
	 *            provided password
	 * @return whether or not the login was successful
	 */
	public boolean login(String username, String password) {
		String url = null;
		try {
			url = "method=login&username="
					+ URLEncoder.encode(username, "UTF-8") + "&password="
					+ URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		if (url == null)
			return false;

		if (isConnected()) {
			try {
				connection = "NONE";
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				connection = o.getString("status");
				checkStatus(connection);
				if (connection.equals("600")) {
					Log.e(TAG, "Invalid username or password.");
					return false;
				}
				session_key = o.getJSONObject("data").getString("session");
				uid = o.getJSONObject("data").getInt("uid");

				if (isLoggedIn()) {
					this.username = username;
					return true;
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
				connection = "NONE";
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				connection = "NONE";
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				connection = "NONE";
				return false;
			}

			return true;
		}
		connection = "NONE";
		return false;
	}

	/**
	 * Gets all the information associated with a given experiment.
	 * 
	 * @param id
	 *            experiment id as an int
	 * @return Experiment object filled with meta-data
	 */
	public Experiment getExperiment(int id) {
		String url = "method=getExperiment&experiment=" + id;
		Experiment e = new Experiment();

		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONObject obj = o.getJSONObject("data");

				e.experiment_id = obj.getInt("experiment_id");
				e.owner_id = obj.getInt("owner_id");
				e.name = obj.getString("name");
				e.description = obj.getString("description");
				e.timecreated = obj.getString("timecreated");
				e.timemodified = obj.getString("timemodified");
				e.default_read = obj.getInt("default_read");
				e.default_join = obj.getInt("default_join");
				e.featured = obj.getInt("featured");
				e.rating = obj.getInt("rating");
				e.rating_votes = obj.getInt("rating_votes");
				e.hidden = obj.getInt("hidden");
				e.activity = obj.getInt("activity");
				e.activity_for = obj.getInt("activity_for");
				e.req_name = obj.getInt("req_name");
				e.req_location = obj.getInt("req_location");
				e.req_procedure = obj.getInt("req_procedure");
				e.name_prefix = obj.getString("name_prefix");
				e.location = obj.getString("location");
				e.closed = obj.getInt("closed");
				e.recommended = obj.getInt("recommended");
				e.srate = obj.getInt("srate");
				e.firstname = obj.getString("firstname");
				e.lastname = obj.getString("lastname");

				mDbHelper.open();
				mDbHelper.deleteExperiment(e);
				mDbHelper.insertExperiment(e);
				mDbHelper.close();

			} catch (MalformedURLException ee) {
				ee.printStackTrace();
				return null;
			} catch (IOException ee) {
				ee.printStackTrace();
				return null;
			} catch (Exception ee) {
				ee.printStackTrace();
				return null;
			}
		} else {
			mDbHelper.open();
			Cursor c = mDbHelper.getExperiment(id);
			mDbHelper.close();

			if (c == null || c.getCount() == 0)
				return e;

			e.experiment_id = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_EXPERIMENT_ID));
			e.owner_id = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_OWNER_ID));
			e.name = c.getString(c.getColumnIndex(RestAPIDbAdapter.KEY_NAME));
			e.description = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_DESCRIPTION));
			e.timecreated = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_TIMECREATED));
			e.timemodified = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_TIMEMODIFIED));
			e.default_read = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_DEFAULT_READ));
			e.default_join = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_DEFAULT_JOIN));
			e.featured = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_FEATURED));
			e.rating = c.getInt(c.getColumnIndex(RestAPIDbAdapter.KEY_RATING));
			e.rating_votes = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_RATING_VOTES));
			e.hidden = c.getInt(c.getColumnIndex(RestAPIDbAdapter.KEY_HIDDEN));
			e.firstname = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_FIRSTNAME));
			e.lastname = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_LASTNAME));
			e.provider_url = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_PROVIDER_URL));
			e.activity = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_ACTIVITY));
			e.activity_for = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_ACTIVITY_FOR));
			e.req_name = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_REQ_NAME));
			e.req_location = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_REQ_LOCATION));
			e.req_procedure = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_REQ_PROCEDURE));
			e.name_prefix = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_NAME_PREFIX));
			e.location = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_LOCATION));
			e.closed = c.getInt(c.getColumnIndex(RestAPIDbAdapter.KEY_CLOSED));
			e.recommended = c.getInt(c
					.getColumnIndex(RestAPIDbAdapter.KEY_RECOMMENDED));
			e.srate = c.getInt(c.getColumnIndex(RestAPIDbAdapter.KEY_SRATE));
		}
		return e;
	}

	/**
	 * Gets the data tied to a specific session.
	 * 
	 * @param sessions
	 *            String corresponding to session number.
	 * @return ArrayList of session information
	 */
	public ArrayList<SessionData> sessiondata(String sessions) {
		String url = "method=sessiondata&sessions=" + sessions;
		String dataString;
		ArrayList<SessionData> ses = new ArrayList<SessionData>();

		if (isConnected()) {
			try {
				dataString = makeRequest(url);

				// parse JSON result
				JSONObject o = new JSONObject(dataString);
				checkStatus(o.getString("status"));
				JSONArray data = o.getJSONArray("data");

				int length = data.length();

				for (int i = 0; i < length; i++) {
					SessionData temp = new SessionData();
					JSONObject current = data.getJSONObject(i);
					temp.RawJSON = current;
					temp.DataJSON = current.getJSONArray("data");
					temp.MetaDataJSON = current.getJSONArray("meta");
					temp.FieldsJSON = current.getJSONArray("fields");

					int fieldCount = temp.FieldsJSON.length();

					temp.fieldData = new ArrayList<ArrayList<String>>();

					for (int j = 0; j < fieldCount; j++) {
						ArrayList<String> tempList = new ArrayList<String>();
						int dataLength = temp.DataJSON.length();
						for (int z = 0; z < dataLength; z++) {
							tempList.add(temp.DataJSON.getJSONArray(z)
									.getString(j));
						}
						temp.fieldData.add(tempList);
					}

					ses.add(temp);
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ses;
	}

	/**
	 * Gets a list of iSENSE users.
	 * 
	 * @deprecated Since version 2.0. Use getPeople(int page, int limit, String
	 *             query) instead.
	 * 
	 * @param page
	 *            page of size limit, offset into the full list of results,
	 *            default 1
	 * @param limit
	 *            number of results desired, default 10
	 * @param action
	 *            how the results are to be sorted (NO LONGER SUPPORTED)
	 * @param query
	 *            person you are searching for
	 * @return ArrayList of Person objects
	 */
	public ArrayList<Person> getPeople(int page, int limit, String action,
			String query) {
		String url = "method=getPeople&page=" + page + "&count=" + limit
				+ "&action=" + action + "&query=" + query;
		ArrayList<Person> pList = new ArrayList<Person>();

		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONArray a = o.getJSONArray("data");

				int length = a.length();

				if (action.toLowerCase(Locale.US).compareTo("browse") == 0)
					mDbHelper.open();

				for (int i = 0; i < length; i++) {
					try {
						JSONObject obj = a.getJSONObject(i);
						Person p = new Person();

						p.firstname = obj.getString("firstname");
						p.user_id = obj.getInt("user_id");
						p.picture = obj.getString("picture");
						p.session_count = obj.getInt("session_count");
						p.experiment_count = obj.getInt("experiment_count");

						pList.add(p);
						if (action.toLowerCase(Locale.US).compareTo("browse") == 0) {
							mDbHelper.deletePerson(p);
							mDbHelper.insertPerson(p);
						}
					} catch (JSONException e) {
						e.printStackTrace();

						continue;
					}
				}
				if (action.toLowerCase(Locale.US).compareTo("browse") == 0)
					mDbHelper.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (action.toLowerCase(Locale.US).compareTo("browse") == 0) {
			mDbHelper.open();
			Cursor c = mDbHelper.getPeople(page, limit);
			mDbHelper.close();

			if (c == null || c.getCount() == 0)
				return pList;

			while (!c.isAfterLast()) {

				Person p = new Person();

				p.firstname = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_FIRSTNAME));
				p.user_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_USER_ID));
				p.picture = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_PICTURE));
				p.session_count = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_SESSION_COUNT));
				p.experiment_count = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_EXPERIMENT_COUNT));

				pList.add(p);

				if (c.isLast())
					break;
				c.moveToNext();
			}
		}

		return pList;
	}

	/**
	 * Gets a list of iSENSE users.
	 * 
	 * @param page
	 *            page of size limit, offset into the full list of results,
	 *            default 1
	 * @param limit
	 *            number of results desired, default 10
	 * @param query
	 *            person you searching for
	 * 
	 * @return ArrayList of Person objects
	 */
	public ArrayList<Person> getPeople(int page, int limit, String query) {
		String url = "method=getPeople&page=" + page + "&count=" + limit
				+ "&query=" + query;
		ArrayList<Person> pList = new ArrayList<Person>();

		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONArray a = o.getJSONArray("data");

				int length = a.length();

				for (int i = 0; i < length; i++) {
					try {
						JSONObject obj = a.getJSONObject(i);
						Person p = new Person();

						p.firstname = obj.getString("firstname");
						p.user_id = obj.getInt("user_id");
						p.picture = obj.getString("picture");
						p.session_count = obj.getInt("session_count");
						p.experiment_count = obj.getInt("experiment_count");

						pList.add(p);
					} catch (JSONException e) {
						e.printStackTrace();

						continue;
					}
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			mDbHelper.open();

			Cursor c = mDbHelper.getPeople(page, limit);
			mDbHelper.close();

			if (c == null || c.getCount() == 0)
				return pList;

			while (!c.isAfterLast()) {

				Person p = new Person();

				p.firstname = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_FIRSTNAME));
				p.user_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_USER_ID));
				p.picture = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_PICTURE));
				p.session_count = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_SESSION_COUNT));
				p.experiment_count = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_EXPERIMENT_COUNT));

				pList.add(p);

				if (c.isLast())
					break;
				c.moveToNext();
			}
		}
		return pList;
	}

	/**
	 * Returns profile information the specified user or null if user_id is
	 * invalid.
	 * 
	 * @param user_id
	 *            user_id as an int
	 * @return Item object that contains user specific information
	 */
	public Item getProfile(int user_id) {
		String url = "method=getUserProfile&user=" + user_id + "&session_key="
				+ session_key;
		Item i = new Item();

		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONObject a = o.getJSONObject("data");
				JSONArray experiments;
				try {
					experiments = a.getJSONArray("experiments");
				} catch (JSONException e) {
					experiments = new JSONArray();
				}

				JSONArray sessions;
				try {
					sessions = a.getJSONArray("sessions");
				} catch (JSONException e) {
					sessions = new JSONArray();
				}

				int length = experiments.length();

				for (int j = 0; j < length; j++) {
					JSONObject obj = experiments.getJSONObject(j);
					Experiment e = new Experiment();

					e.experiment_id = obj.getInt("experiment_id");
					e.owner_id = obj.getInt("owner_id");
					e.name = obj.getString("name");
					e.description = obj.getString("description");
					e.timecreated = obj.getString("timecreated");
					e.timemodified = obj.getString("timemodified");
					e.default_read = obj.getInt("default_read");
					e.default_join = obj.getInt("default_join");
					e.featured = obj.getInt("featured");
					e.rating = obj.getInt("rating");
					e.rating_votes = obj.getInt("rating_votes");
					e.hidden = obj.getInt("hidden");
					e.firstname = obj.getString("firstname");
					e.provider_url = obj.getString("provider_url");
					e.name_prefix = obj.getString("name_prefix");
					e.location = obj.getString("location");
					e.recommended = obj.getInt("recommended");
					e.activity_for = obj.getInt("activity_for");
					e.closed = obj.getInt("closed");
					e.rating_comp = obj.getString("rating_comp");
					e.activity = obj.getInt("activity");
					e.req_name = obj.getInt("req_name");
					e.req_location = obj.getInt("req_location");
					e.req_procedure = obj.getInt("req_procedure");
					e.srate = obj.getInt("srate");
					e.exp_image = obj.getString("exp_image");
					e.timeobj = obj.getString("timeobj");

					i.e.add(e);
				}

				length = sessions.length();

				for (int j = 0; j < length; j++) {
					JSONObject obj = sessions.getJSONObject(j);
					Session s = new Session();

					s.session_id = obj.getInt("session_id");
					s.name = obj.getString("name");
					s.description = obj.getString("description");
					s.latitude = obj.getLong("latitude");
					s.longitude = obj.getLong("longitude");
					s.timecreated = obj.getString("timeobj");
					s.timemodified = obj.getString("timemodified");
					s.experiment_id = obj.getInt("experiment_id");
					s.timeobj = obj.getString("timeobj");
					s.owner_id = obj.getInt("owner_id");
					s.experiment_name = obj.getString("experiment_name");

					i.s.add(s);
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return i;
	}

	/**
	 * Returns a list of experiments.
	 * 
	 * @param page
	 *            page of size limit, offset into the full list of results,
	 *            default 1
	 * @param limit
	 *            number of results desired, default 10
	 * @param query
	 *            search terms by tags/name of experiment example "water"
	 * @param sort
	 *            what to sort results by - accepts the following: recent,
	 *            popularity, activity, rating
	 * 
	 * @return ArrayList of Experiment objects
	 */
	public ArrayList<Experiment> getExperiments(int page, int limit,
			String query, String sort) {
		String url = "method=getExperiments&page=" + page + "&limit=" + limit
				+ "&query=" + query + "&sort=" + sort;

		ArrayList<Experiment> expList = new ArrayList<Experiment>();

		if (isConnected()) {
			try {

				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONArray a = o.getJSONArray("data");

				int length = a.length();
				for (int i = 0; i < length; i++) {
					try {
						JSONObject current = a.getJSONObject(i);
						JSONObject obj = current.getJSONObject("meta");
						Experiment e = new Experiment();

						e.tags = current.getString("tags");
						e.contrib_count = current.getInt("contrib_count");
						e.session_count = current.getInt("session_count");
						e.experiment_id = obj.getInt("experiment_id");
						e.owner_id = obj.getInt("owner_id");
						e.name = obj.getString("name");
						e.description = obj.getString("description");
						e.timecreated = obj.getString("timecreated");
						e.timemodified = obj.getString("timemodified");
						e.default_read = obj.getInt("default_read");
						e.default_join = obj.getInt("default_join");
						e.featured = obj.getInt("featured");
						e.rating = obj.getInt("rating");
						e.rating_votes = obj.getInt("rating_votes");
						e.hidden = obj.getInt("hidden");
						e.firstname = obj.getString("firstname");
						e.provider_url = obj.getString("provider_url");
						e.name_prefix = obj.getString("name_prefix");
						e.location = obj.getString("location");
						e.recommended = obj.getInt("recommended");
						e.activity_for = obj.getInt("activity_for");
						e.closed = obj.getInt("closed");
						e.rating_comp = obj.getString("rating_comp");
						e.activity = obj.getInt("activity");
						e.req_name = obj.getInt("req_name");
						e.req_location = obj.getInt("req_location");
						e.req_procedure = obj.getInt("req_procedure");
						e.srate = obj.getInt("srate");
						e.exp_image = obj.getString("exp_image");

						expList.add(e);
					} catch (JSONException e) {
						e.printStackTrace();

						continue;
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			mDbHelper.open();
			Cursor c = mDbHelper.getExperiments(page, limit);
			mDbHelper.close();

			if (c == null || c.getCount() == 0)
				return expList;

			while (!c.isAfterLast()) {

				Experiment e = new Experiment();

				e.experiment_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_EXPERIMENT_ID));
				e.owner_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_OWNER_ID));
				e.name = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_NAME));
				e.description = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_DESCRIPTION));
				e.timecreated = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TIMECREATED));
				e.timemodified = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TIMEMODIFIED));
				e.default_read = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_DEFAULT_READ));
				e.default_join = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_DEFAULT_JOIN));
				e.featured = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_FEATURED));
				e.rating = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_RATING));
				e.rating_votes = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_RATING_VOTES));
				e.hidden = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_HIDDEN));
				e.firstname = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_FIRSTNAME));
				e.provider_url = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_PROVIDER_URL));
				e.tags = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TAGS));
				e.contrib_count = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_CONTRIB_COUNT));
				e.session_count = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_SESSION_COUNT));
				e.name_prefix = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_NAME_PREFIX));
				e.location = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_LOCATION));
				e.recommended = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_RECOMMENDED));
				e.activity_for = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_ACTIVITY_FOR));
				e.closed = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_CLOSED));
				e.rating_comp = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_RATING_COMP));
				e.activity = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_ACTIVITY));
				e.req_name = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_REQ_NAME));
				e.req_location = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_REQ_LOCATION));
				e.req_procedure = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_REQ_PROCEDURE));
				e.srate = c
						.getInt(c.getColumnIndex(RestAPIDbAdapter.KEY_SRATE));
				e.exp_image = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_EXP_IMAGE));

				expList.add(e);

				if (c.isLast())
					break;
				c.moveToNext();
			}

		}
		return expList;
	}

	/**
	 * Gets experiments by page as a LoadedExperiment object.
	 * 
	 * @param page
	 *            page of size limit, offset into the full list of results,
	 *            default 1
	 * @param limit
	 *            number of results desired, default 10
	 * @param query
	 *            search terms by tags/name of experiment example "water"
	 * @param sort
	 *            what to sort results by - accepts the following: recent,
	 *            popularity, activity, rating
	 * @return LoadedExperiment object containing Experiment objects
	 */
	public LoadedExperiment getAllExperiments(int page, int limit,
			String query, String sort) {
		String url = "method=getExperiments&page=" + page + "&limit=" + limit
				+ "&query=" + query + "&sort=" + sort;

		LoadedExperiment expPair = new LoadedExperiment();
		expPair.exp = new ArrayList<Experiment>();
		expPair.setLoaded(false);

		if (isConnected()) {
			try {

				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONArray a = o.getJSONArray("data");

				int length = a.length();
				if (length < 10)
					expPair.setLoaded(true);
				else
					expPair.setLoaded(false);

				for (int i = 0; i < length; i++) {
					try {
						JSONObject current = a.getJSONObject(i);
						JSONObject obj = current.getJSONObject("meta");
						Experiment e = new Experiment();

						e.tags = current.getString("tags");
						e.contrib_count = current.getInt("contrib_count");
						e.session_count = current.getInt("session_count");
						e.experiment_id = obj.getInt("experiment_id");
						e.owner_id = obj.getInt("owner_id");
						e.name = obj.getString("name");
						e.description = obj.getString("description");
						e.timecreated = obj.getString("timecreated");
						e.timemodified = obj.getString("timemodified");
						e.default_read = obj.getInt("default_read");
						e.default_join = obj.getInt("default_join");
						e.featured = obj.getInt("featured");
						e.rating = obj.getInt("rating");
						e.rating_votes = obj.getInt("rating_votes");
						e.hidden = obj.getInt("hidden");
						e.firstname = obj.getString("firstname");
						e.provider_url = obj.getString("provider_url");
						e.name_prefix = obj.getString("name_prefix");
						e.location = obj.getString("location");
						e.recommended = obj.getInt("recommended");
						e.activity_for = obj.getInt("activity_for");
						e.closed = obj.getInt("closed");
						e.rating_comp = obj.getString("rating_comp");
						e.activity = obj.getInt("activity");
						e.req_name = obj.getInt("req_name");
						e.req_location = obj.getInt("req_location");
						e.req_procedure = obj.getInt("req_procedure");
						e.srate = obj.getInt("srate");
						e.exp_image = obj.getString("exp_image");

						expPair.exp.add(e);
					} catch (JSONException e) {
						e.printStackTrace();

						continue;
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			mDbHelper.open();
			Cursor c = mDbHelper.getExperiments(page, limit);
			mDbHelper.close();

			if (c == null || c.getCount() == 0)
				return expPair;

			while (!c.isAfterLast()) {

				Experiment e = new Experiment();

				e.experiment_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_EXPERIMENT_ID));
				e.owner_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_OWNER_ID));
				e.name = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_NAME));
				e.description = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_DESCRIPTION));
				e.timecreated = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TIMECREATED));
				e.timemodified = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TIMEMODIFIED));
				e.default_read = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_DEFAULT_READ));
				e.default_join = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_DEFAULT_JOIN));
				e.featured = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_FEATURED));
				e.rating = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_RATING));
				e.rating_votes = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_RATING_VOTES));
				e.hidden = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_HIDDEN));
				e.firstname = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_FIRSTNAME));
				e.provider_url = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_PROVIDER_URL));
				e.tags = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TAGS));
				e.contrib_count = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_CONTRIB_COUNT));
				e.session_count = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_SESSION_COUNT));
				e.name_prefix = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_NAME_PREFIX));
				e.location = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_LOCATION));
				e.recommended = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_RECOMMENDED));
				e.activity_for = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_ACTIVITY_FOR));
				e.closed = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_CLOSED));
				e.rating_comp = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_RATING_COMP));
				e.activity = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_ACTIVITY));
				e.req_name = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_REQ_NAME));
				e.req_location = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_REQ_LOCATION));
				e.req_procedure = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_REQ_PROCEDURE));
				e.srate = c
						.getInt(c.getColumnIndex(RestAPIDbAdapter.KEY_SRATE));
				e.exp_image = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_EXP_IMAGE));

				expPair.exp.add(e);

				if (c.isLast()) {
					expPair.setLoaded(true);
					break;
				}
				c.moveToNext();
			}

		}
		return expPair;
	}

	/**
	 * Gets all images associated with an experiment.
	 * 
	 * @param id
	 *            experiment id as an int
	 * @return ArrayList of Strings listing images
	 */
	public ArrayList<String> getExperimentImages(int id) {
		ArrayList<String> imgList = new ArrayList<String>();
		String url = "method=getExperimentImages&experiment=" + id;

		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONArray a = o.getJSONArray("data");

				int length = a.length();
				mDbHelper.open();
				for (int i = 0; i < length; i++) {
					JSONObject obj = a.getJSONObject(i);

					imgList.add(obj.getString("provider_url"));
				}

				mDbHelper.deleteExperimentImages(id);
				mDbHelper.insertExperimentImages(id, imgList);
				mDbHelper.close();

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			mDbHelper.open();
			Cursor c = mDbHelper.getExperimentImages(id);
			mDbHelper.close();

			if (c == null || c.getCount() == 0)
				return imgList;

			String images = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_PROVIDER_URL));

			String img[] = images.split(",");

			for (int i = 0; i < img.length; i++) {
				imgList.add(img[i]);
			}

		}

		return imgList;
	}

	/**
	 * Returns all videos associated with an experiment.
	 * 
	 * @param id
	 *            experiment id as an int
	 * @return ArrayList of Strings listing videos
	 */
	public ArrayList<String> getExperimentVideos(int id) {
		ArrayList<String> vidList = new ArrayList<String>();
		String url = "method=getExperimentVideos&experiment=" + id;

		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONArray a = o.getJSONArray("data");

				int length = a.length();
				mDbHelper.open();
				for (int i = 0; i < length; i++) {
					JSONObject obj = a.getJSONObject(i);

					vidList.add(obj.getString("provider_url"));
				}
				mDbHelper.deleteExperimentVideos(id);
				mDbHelper.insertExperimentVideos(id, vidList);
				mDbHelper.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			mDbHelper.open();
			Cursor c = mDbHelper.getExperimentVideos(id);
			mDbHelper.close();

			if (c == null || c.getCount() == 0)
				return vidList;

			String videos = c.getString(c
					.getColumnIndex(RestAPIDbAdapter.KEY_PROVIDER_URL));

			String vid[] = videos.split(",");

			for (int i = 0; i < vid.length; i++) {
				vidList.add(vid[i]);
			}
		}
		return vidList;
	}

	/**
	 * Gets all tags associated with an experiment.
	 * 
	 * @param id
	 *            experiment id as an int
	 * @return list of tags as a String
	 */
	public String getExperimentTags(int id) {
		// Added StringBuilder for efficiency
		StringBuilder builder = new StringBuilder();

		String tags = "";
		String url = "method=getExperimentTags&experiment=" + id;

		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONArray a = o.getJSONArray("data");

				int length = a.length();

				mDbHelper.open();
				for (int i = 0; i < length; i++) {
					JSONObject obj = a.getJSONObject(i);
					builder.append(obj.getString("tag")).append(", ");
				}

				tags = builder.toString();
				tags = tags.substring(0, tags.lastIndexOf(","));

				mDbHelper.deleteExperimentTags(id);
				mDbHelper.insertExperimentTags(id, tags);
				mDbHelper.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			mDbHelper.open();
			Cursor c = mDbHelper.getExperimentTags(id);
			mDbHelper.close();

			if (c == null || c.getCount() == 0)
				return tags;

			tags = c.getString(c.getColumnIndex(RestAPIDbAdapter.KEY_TAGS));
		}

		return tags;
	}

	/**
	 * Returns the fields associated with a given experiment.
	 * 
	 * @param id
	 *            experiment id as an int
	 * @return ArrayList of ExperimentField objects
	 */
	public ArrayList<ExperimentField> getExperimentFields(int id) {
		String url = "method=getExperimentFields&experiment=" + id;
		ArrayList<ExperimentField> fields = new ArrayList<ExperimentField>();

		mDbHelper.open();
		Cursor c = mDbHelper.getExperimentFields(id);
		mDbHelper.close();

		if ((c == null || c.getCount() == 0) && isConnected()) {
			Log.w("tag", "interwebs");
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONArray a = o.getJSONArray("data");

				int length = a.length();

				mDbHelper.open();
				for (int i = 0; i < length; i++) {
					JSONObject obj = a.getJSONObject(i);
					ExperimentField f = new ExperimentField();

					f.field_id = obj.getInt("field_id");
					f.field_name = obj.getString("field_name");
					f.type_id = obj.getInt("type_id");
					f.type_name = obj.getString("type_name");
					f.unit_abbreviation = obj.getString("unit_abbreviation");
					f.unit_id = obj.getInt("unit_id");
					f.unit_name = obj.getString("unit_name");

					fields.add(f);
				}
				mDbHelper.deleteExperimentFields(id);
				mDbHelper.insertExperimentFields(id, fields);
				mDbHelper.close();

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			Log.w("tag", "cache");
			while (!c.isAfterLast()) {
				ExperimentField f = new ExperimentField();

				f.field_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_FIELD_ID));
				f.field_name = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_FIELD_NAME));
				f.type_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TYPE_ID));
				f.type_name = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TYPE_ID));
				f.unit_abbreviation = c
						.getString(c
								.getColumnIndex(RestAPIDbAdapter.KEY_UNIT_ABBREVIATION));
				f.unit_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_UNIT_ID));
				f.unit_name = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_UNIT_NAME));

				fields.add(f);

				c.moveToNext();
			}
		}

		return fields;
	}

	/**
	 * Returns the sessions associated with an experiment.
	 * 
	 * @param id
	 *            experiment id as an int
	 * @return ArrayList of Session objects
	 */
	public ArrayList<Session> getSessions(int id) {
		ArrayList<Session> sesList = new ArrayList<Session>();
		String url = "method=getSessions&experiment=" + id;

		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONArray a = o.getJSONArray("data");

				int length = a.length();

				mDbHelper.open();
				for (int i = 0; i < length; i++) {
					JSONObject obj = a.getJSONObject(i);
					Session s = new Session();

					s.session_id = obj.getInt("session_id");
					s.owner_id = obj.getInt("owner_id");
					s.experiment_id = id;
					s.name = obj.getString("name");
					s.description = obj.getString("description");
					s.street = obj.getString("street");
					s.city = obj.getString("city");
					s.country = obj.getString("country");
					s.latitude = obj.getDouble("latitude");
					s.longitude = obj.getDouble("longitude");
					s.timecreated = obj.getString("timecreated");
					s.timemodified = obj.getString("timemodified");
					s.debug_data = obj.getString("debug_data");
					s.firstname = obj.getString("firstname");
					s.lastname = obj.getString("lastname");
					s.priv = obj.getInt("private");

					sesList.add(s);
					mDbHelper.deleteSession(s);
					mDbHelper.insertSession(s);
				}
				mDbHelper.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			mDbHelper.open();
			Cursor c = mDbHelper.getSessions(id);
			mDbHelper.close();

			if (c == null || c.getCount() == 0)
				return sesList;

			while (!c.isAfterLast()) {

				Session s = new Session();

				s.session_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_SESSION_ID));
				s.owner_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_OWNER_ID));
				s.experiment_id = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_EXPERIMENT_ID));
				s.name = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_NAME));
				s.description = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_DESCRIPTION));
				s.street = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_STREET));
				s.city = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_CITY));
				s.country = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_COUNTRY));
				s.latitude = c.getDouble(c
						.getColumnIndex(RestAPIDbAdapter.KEY_LATITUDE));
				s.longitude = c.getDouble(c
						.getColumnIndex(RestAPIDbAdapter.KEY_LONGITUDE));
				s.timecreated = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TIMECREATED));
				s.timemodified = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_TIMEMODIFIED));
				s.debug_data = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_DEBUG_DATA));
				s.firstname = c.getString(c
						.getColumnIndex(RestAPIDbAdapter.KEY_FIRSTNAME));
				s.priv = c.getInt(c
						.getColumnIndex(RestAPIDbAdapter.KEY_PRIVATE));

				sesList.add(s);

				if (c.isLast())
					break;
				c.moveToNext();
			}
		}

		return sesList;
	}

	/**
	 * Creates a session under a given experiment.
	 * 
	 * @param eid
	 *            experiment id as a String
	 * @param name
	 *            name of the session
	 * @param description
	 *            description of the session
	 * @param street
	 *            street where the session was created
	 * @param city
	 *            city/locality where the session was created
	 * @param country
	 *            country where where the session was created
	 * @return int that equals a new session id if successful, -400 if the
	 *         experiment is closed, or -1 if failure
	 */
	public int createSession(String eid, String name, String description,
			String street, String city, String country) {
		int sid = -1;
		String url = "method=createSession&session_key=" + session_key
				+ "&eid=" + eid + "&name=" + name + "&description="
				+ description + "&street=" + street + "&city=" + city
				+ "&country=" + country;
		
		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// Parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				JSONObject obj = o.getJSONObject("data");

				String msg = obj.optString("msg");
				if (msg.compareToIgnoreCase("Experiment Closed") == 0)
					// Experiment has been closed
					sid = -400;
				else if (msg.compareToIgnoreCase("Not logged in") == 0)
					// Not logged in
					sid = -1;
				else
					sid = obj.getInt("sessionId");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return sid;
	}

	/**
	 * Puts a JSON array of data in a given experiment's specified session.
	 * 
	 * @param sid
	 *            session id as an integer
	 * @param eid
	 *            experiment id as a String
	 * @param dataJSON
	 *            JSONArray of data to be uploaded
	 * @return whether or not putSessionData was successful
	 */
	public boolean putSessionData(int sid, String eid, JSONArray dataJSON) {
		String url = "method=putSessionData&session_key=" + session_key
				+ "&sid=" + sid + "&eid=" + eid + "&data="
				+ dataJSON.toString();
		//Log.d(TAG, url);
		boolean ret = false;
		
		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				String status = o.getString("status");

				if (status.equals("200"))
					ret = true;
				else {
					JSONObject msg = o.getJSONObject("data");
					Log.e(TAG, msg.getString("msg"));
					ret = false;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return ret;
	}

	/**
	 * Puts a JSON array of data in a given experiment's specified session.
	 * 
	 * Method has been updated from original: in String url = ..., changed
	 * dataJSON.tostring(); from dataCache.toString();
	 * 
	 * @param sid
	 *            session id as an integer
	 * @param eid
	 *            experiment id as a String
	 * @param dataJSON
	 *            JSONArray of data to be uploaded
	 * @return whether or not putSessionData was successful
	 */
	public boolean updateSessionData(int sid, String eid, JSONArray dataJSON) {
		dataCache.put(dataJSON);
		String url = "method=updateSessionData&session_key=" + session_key
				+ "&sid=" + sid + "&eid=" + eid + "&data="
				+ dataJSON.toString();
		boolean ret = false;

		if (isConnected()) {
			try {
				String data = makeRequest(url);

				// parse JSON Result
				JSONObject o = new JSONObject(data);
				checkStatus(o.getString("status"));
				String status = o.getString("status");

				if (status.equals("200")) {
					dataCache = new JSONArray();
					ret = true;
				} else {
					JSONObject msg = o.getJSONObject("data");
					Log.e(TAG, msg.getString("msg"));
					ret = false;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return ret;
	}

	/**
	 * Returns IP as a String.
	 * 
	 * @deprecated Since version 2.0. Will always return status 600 - DO NOT
	 *             USE. This function was removed in iSENSE API v1.0.
	 * @return IP address as a String
	 */
	public String getMyIp() {
		String url = "method=whatsMyIp";
		try {
			String data = makeRequest(url);
			JSONObject o = new JSONObject(data);
			checkStatus(o.getString("status"));
			JSONObject obj = o.getJSONObject("data");
			return obj.getString("msg");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Internal method that handles data requests.
	 * 
	 * @param target
	 *            url of the request
	 * @return server response from the request
	 * @throws Exception
	 *             : IOException (unhandled http status)
	 */
	public String makeRequest(String target) throws Exception {

		String output = "{}";

		String data = target.replace(" ", "+");

		HttpURLConnection conn = (HttpURLConnection) new URL(RestAPI.base_url)
				.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Length",
				Integer.toString(data.length()));
		conn.getOutputStream().write(data.getBytes(charEncoding));
		conn.connect();
		conn.getResponseCode();

		// Get the status code of the HTTP Request so we can figure out what to
		// do next
		int status = conn.getResponseCode();

		switch (status) {

		case 200:

			// Build Reader and StringBuilder to output to String
			BufferedReader br = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;

			// Loop through response to build JSON String
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}

			// Set output from response
			output = sb.toString();
			break;

		case 404:
			// Handle 404 page not found
			Log.e(TAG, "Could not find URL! (404 Exception)");
			throw new IOException();

		default:
			// Catch all for all other HTTP response codes
			Log.e(TAG, "Returned unhandled error code: " + status);
			throw new IOException();
		}

		return output;
	}

	/**
	 * Returns status on whether you are connected to the Internet.
	 * 
	 * @return current connection status
	 */
	public boolean isConnectedToInternet() {

		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		return (info != null && info.isConnected());

	}

	/**
	 * Switch between iSENSEdev and iSENSE (default behavior is FALSE).
	 * 
	 * @param devSwitch
	 *            true if you want to use iSENSEdev, false for iSENSE
	 */
	public void useDev(boolean devSwitch) {
		if (devSwitch)
			base_url = "http://isensedev.cs.uml.edu/ws/api.php";
		else
			base_url = "http://isense.cs.uml.edu/ws/api.php";
	}

	/**
	 * Internal error checking. Logs for invalid statuses.
	 * 
	 * @param status
	 *            from JSONObject
	 */
	private void checkStatus(String status) {
		if (!status.equals("200")) {
			Log.e(TAG, serverRep + status);
			if (status.equals("600"))
				Log.e(TAG, "Bad request");
			else if (status.equals("551"))
				Log.e(TAG, "Missing parameter");
		}
	}

	/**
	 * Internal check that ConnectivityManager isn't null.
	 * 
	 * @return whether or not you are connected
	 */
	private boolean isConnected() {
		return (connectivityManager != null
				&& connectivityManager.getNetworkInfo(
						ConnectivityManager.TYPE_WIFI).isConnected() || (connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()));
	}
	
	
}
