import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import REST.RestAPI;
import REST.columnMatchException;
import REST.isenseInterface;
import REST.loginException;

public class RestTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		isenseInterface is = new isenseInterface();
		int exp = 24;
		String headers[] = {"time","ext"};
		Vector<String> data = new Vector<String>();
		data.add("4,5");
		data.add(System.currentTimeMillis()+",6");
		
		is.login("sor", "sor");
		try {
			int sid = is.joinExperiment(exp, "b", "b", "b", "b");
			System.out.println("Created Session " + sid );
			is.addToSession(exp, sid, headers , data);
		} catch (loginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (columnMatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
/*		try {
			StartRecording();
			Thread.sleep(5000);
			StopRecording();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*//*
		String get_status = GetStatus();
		try {
			JSONObject get_status_json = new JSONObject(get_status);
			int col_size = get_status_json.getJSONObject("views").length();
			String col_id[] = new String[col_size];//column id
			String type[] = new String[col_size];//type of date eg time, temp, ph
			JSONArray col_data[] = new JSONArray[col_size];//column data
			for (int i = 0;i<col_size;i++){
				String views = get_status_json.getJSONObject("views").names().get(i).toString();
				if (get_status_json.getJSONObject("views").getJSONObject(views).has("baseColID")){
					col_id[i] = get_status_json.getJSONObject("views").getJSONObject(views).getString("baseColID");
				} else if (get_status_json.getJSONObject("views").getJSONObject(views).has("colID")) {
					col_id[i] = get_status_json.getJSONObject("views").getJSONObject(views).getString("colID");
				}
				type[i] = get_status_json.getJSONObject("columns").getJSONObject(col_id[i]).getString("name");
				JSONObject get_col_json = new JSONObject(GetColumns(col_id[i]));
				col_data[i] = get_col_json.getJSONArray("values");

				System.out.println(type[i]);
				System.out.println(col_data[i]);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	public static String StartRecording(){
		String result = null;
		try {
			result = httpGet("http://labquest.local/control/start");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String StopRecording(){
		String result = null;
		try {
			result = httpGet("http://labquest.local/control/stop");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String GetInfo(){
		String result = null;
		try {
			result = httpGet("http://labquest.local/info");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String GetStatus(){
		String result = null;
		try {
			result = httpGet("http://labquest.local/status");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String GetColumns(String column){
		String result = null;
		try {
			result = httpGet("http://labquest.local/columns/" + column);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String httpGet(String urlStr) throws IOException {
		  URL url = new URL(urlStr);
		  HttpURLConnection conn =
		      (HttpURLConnection) url.openConnection();

		  if (conn.getResponseCode() != 200) {
		    throw new IOException(conn.getResponseMessage());
		  }

		  // Buffer the result into a string
		  BufferedReader rd = new BufferedReader(
		      new InputStreamReader(conn.getInputStream()));
		  StringBuilder sb = new StringBuilder();
		  String line;
		  while ((line = rd.readLine()) != null) {
		    sb.append(line);
		  }
		  rd.close();
		  conn.disconnect();
		  return sb.toString();
	  }
}