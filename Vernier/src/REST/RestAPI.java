package REST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestAPI {

    //Make Singleton
    private static RestAPI instance = null;
    private String username = null;
    private static String session_key = null;
    private static int uid;

    //Encoding and url for RestAPI
    private final String base_url = "http://localhost/ws/api.php?";
    private final String charEncoding = "iso-8859-1";

    //Standard out switch
    private boolean printOn = false;

    protected RestAPI() {
    }

    public static RestAPI getInstance() {
        if (instance == null) {
            instance = new RestAPI();
        }

        return instance;
    }

    public String getSessionKey() {
        return session_key;
    }

    public boolean isLoggedIn() {
        return (session_key != null && session_key != "null" && session_key != "");
    }

    public String getLoggedInUsername() {
        return username;
    }

    public void logout() {
        session_key = null;
        username = null;
    }

    public void login(String username, String password) {
        String url = "method=login&username=" + URLEncoder.encode(username) + "&password=" + URLEncoder.encode(password);

        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            session_key = o.getJSONObject("data").getString("session");
            uid = o.getJSONObject("data").getInt("uid");

            if (isLoggedIn()) {
                this.username = username;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public Experiment getExperiment(int id) {
        String url = "method=getExperiment&experiment=" + id;
        Experiment e = new Experiment();


        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
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
            e.firstname = obj.getString("firstname");
            e.lastname = obj.getString("lastname");



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


        return e;

    }

    public String getMyIp() {
        String url = "method=whatsMyIp";
        try {
            String data = makeRequest(url);
            JSONObject o = new JSONObject(data);
            JSONObject obj = o.getJSONObject("data");
            return obj.getString("msg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<SessionData> sessiondata(String sessions) {
        String url = "method=sessiondata&sessions=" + sessions;
        String dataString;
        ArrayList<SessionData> ses = new ArrayList<SessionData>();

        try {
            dataString = makeRequest(url);

            JSONObject o = new JSONObject(dataString);
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
                    for (int z = 1; z < dataLength; z++) {
                        tempList.add(temp.DataJSON.getJSONArray(z).getString(j));
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

        return ses;
    }

    public ArrayList<Person> getPeople(int page, int count, String action, String query) {
        String url = "method=getPeople&page=" + page + "&count=" + count + "&action=" + action + "&query=" + query;
        ArrayList<Person> pList = new ArrayList<Person>();

        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            JSONArray a = o.getJSONArray("data");

            int length = a.length();

            for (int i = 0; i < length; i++) {
                JSONObject obj = a.getJSONObject(i);
                Person p = new Person();

                p.user_id = obj.getInt("user_id");
                p.firstname = obj.getString("firstname");
                p.lastname = obj.getString("lastname");
                p.confirmed = obj.getInt("confirmed");
                p.email = obj.getString("email");
                p.icq = obj.getString("icq");
                p.skype = obj.getString("skype");
                p.yahoo = obj.getString("yahoo");
                p.aim = obj.getString("aim");
                p.msn = obj.getString("msn");
                p.institution = obj.getString("institution");
                p.department = obj.getString("department");
                p.street = obj.getString("street");
                p.city = obj.getString("city");
                p.country = obj.getString("country");
                p.longitude = obj.getDouble("longitude");
                p.latititude = obj.getDouble("latitude");
                p.langauge = obj.getString("language");
                p.firstaccess = obj.getString("firstaccess");
                p.lastaccess = obj.getString("lastaccess");
                p.lastlogin = obj.getString("lastlogin");
                p.picture = obj.getString("picture");
                p.url = obj.getString("url");
                p.timeobj = obj.getString("timeobj");
                p.date_diff = obj.getString("date_diff");
                p.experiment_count = obj.getDouble("experiment_count");
                p.session_count = obj.getDouble("session_count");

                pList.add(p);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pList;
    }

    public Item getProfile(int user_id) {
        String url = "method=getUserProfile&user=" + user_id;
        Item i = new Item();

        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            JSONObject a = o.getJSONObject("data");
            JSONArray experiments = a.getJSONArray("experiments");
            JSONArray sessions = a.getJSONArray("sessions");
            //JSONArray images = a.getJSONArray("images");

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
                e.hidden = obj.getInt("hidden");
                //e.session_count = obj.getInt("session_count");

                i.e.add(e);
            }

            length = sessions.length();

            for (int j = 0; j < length; j++) {
                JSONObject obj = sessions.getJSONObject(j);
                Session s = new Session();

                s.session_id = obj.getInt("session_id");
                System.out.println(s.name);
                s.name = obj.getString("name");
                s.description = obj.getString("description");
                s.latitude = obj.getLong("latitude");
                s.longitude = obj.getLong("longitude");
                s.timecreated = obj.getString("timeobj");
                s.timemodified = obj.getString("timemodified");

                i.s.add(s);
            }

            /*length = images.length();

            for (int j = 0; j < length; j++) {
            JSONObject obj = images.getJSONObject(j);
            Image img = new Image();

            img.title = obj.getString("title");
            img.experiment_id = obj.getInt("experiment_id");
            img.picture_id = obj.getInt("picture_id");
            img.description = obj.getString("description");
            img.provider_url = obj.getString("provider_url");
            img.provider_id = obj.getString("provider_id");
            img.timecreated = obj.getString("timecreated");
            img.name = obj.getString("name");

            i.i.add(img);
            }*/

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return i;
    }

    public ArrayList<Experiment> getExperiments(int page, int count, String action, String query) {
        String url = "method=getExperiments&page=" + page + "&count=" + count + "&action=" + action + "&query=" + query;
        ArrayList<Experiment> expList = new ArrayList<Experiment>();


        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            JSONArray a = o.getJSONArray("data");

            int length = a.length();

            for (int i = 0; i < length; i++) {
                JSONObject current = a.getJSONObject(i);
                JSONObject obj = current.getJSONObject("meta");
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
                e.firstname = obj.getString("owner_firstname");
                e.lastname = obj.getString("owner_lastname");
                e.provider_url = obj.getString("provider_url");

                expList.add(e);

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return expList;
    }

    public ArrayList<String> getExperimentImages(int id) {
        ArrayList<String> imgList = new ArrayList<String>();
        String url = "method=getExperimentImages&experiment=" + id;


        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            JSONArray a = o.getJSONArray("data");

            int length = a.length();

            for (int i = 0; i < length; i++) {
                JSONObject obj = a.getJSONObject(i);

                imgList.add(obj.getString("provider_url"));
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgList;
    }

    public ArrayList<String> getExperimentVideos(int id) {
        ArrayList<String> vidList = new ArrayList<String>();
        String url = "method=getExperimentVideos&experiment=" + id;


        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            JSONArray a = o.getJSONArray("data");

            int length = a.length();

            for (int i = 0; i < length; i++) {
                JSONObject obj = a.getJSONObject(i);

                vidList.add(obj.getString("provider_url"));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vidList;
    }

    public String getExperimentTags(int id) {
        String tags = "";
        String url = "method=getExperimentTags&experiment=" + id;


        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            JSONArray a = o.getJSONArray("data");

            int length = a.length();


            for (int i = 0; i < length; i++) {
                JSONObject obj = a.getJSONObject(i);

                tags += obj.getString("tag") + ", ";
            }

            tags = tags.substring(0, tags.lastIndexOf(","));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return tags;
    }

    public ArrayList<ExperimentField> getExperimentFields(int id) {
        String url = "method=getExperimentFields&experiment=" + id;
        ArrayList<ExperimentField> fields = new ArrayList<ExperimentField>();

        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            JSONArray a = o.getJSONArray("data");

            int length = a.length();


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


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return fields;
    }

    public ArrayList<Session> getSessions(int id) {
        ArrayList<Session> sesList = new ArrayList<Session>();
        String url = "method=getSessions&experiment=" + id;


        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            JSONArray a = o.getJSONArray("data");

            int length = a.length();

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

                sesList.add(s);

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }



        return sesList;
    }

    public int createSession(String eid, String name, String description, String street, String city, String country) {
        int sid = -1;
        String url = "method=createSession&session_key=" + session_key + "&eid=" + eid + "&name=" + name + "&description=" + description + "&street=" + street + "&city=" + city + "&country=" + country;


        try {
            String data = makeRequest(url);

            // Parse JSON Result
            JSONObject o = new JSONObject(data);
            JSONObject obj = o.getJSONObject("data");

            sid = obj.getInt("sessionId");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return sid;
    }

    public boolean putSessionData(int sid, String eid, JSONArray dataJSON) {
        String url = "method=putSessionData&session_key=" + session_key + "&sid=" + sid + "&eid=" + eid + "&data=" + dataJSON.toString();

        boolean ret = false;


        try {
            String data = makeRequest(url);
            if (data.compareTo("{}") != 0) {
                ret = true;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return ret;
    }

    public boolean updateSessionData(int sid, String eid, JSONArray dataJSON) {
        String url = "method=updateSessionData&session_key=" + session_key + "&sid=" + sid + "&eid=" + eid + "&data=" + dataJSON.toString();
        boolean ret = false;


        try {
            String data = makeRequest(url);

            if (data.compareTo("{}") != 0) {
                ret = true;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return ret;
    }

    public String makeRequest(String target) throws Exception {

        String output = "{}";

        String data = target.replace(" ", "+");

        HttpURLConnection conn = (HttpURLConnection) new URL(this.base_url).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
        conn.getOutputStream().write(data.getBytes(charEncoding));
        conn.connect();
        conn.getResponseCode();

        // Get the status code of the HTTP Request so we can figure out what to do next
        int status = conn.getResponseCode();

        switch (status) {

            case 200:
                myPrint("Successful request");

                // Build Reader and StringBuilder to output to string
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
                myPrint("Could not find URL!");
                break;

            default:
                // Catch all for all other HTTP response codes
                myPrint("Returned unhandled error code: " + status);
                break;
        }

        return output;
    }

    public int getUID() {
        return uid;
    }

    /**
     * Print based on program state
     *
     * @param x
     */
    private void myPrint(String x) {
        if (printOn) {
            System.out.println(x);
        }
    }
}
