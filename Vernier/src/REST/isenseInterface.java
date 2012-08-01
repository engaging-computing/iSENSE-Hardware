package REST;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;


/**
 *
 * @author jdalphon
 */
public class isenseInterface {

    //Instance of the Rest API
    private RestAPI rapi;
    private String user = "james.dalphond@gmail.com";
    private String pass = "password";
    private boolean printOn = true;
    private boolean sessionVerified = false;
    private int sessionID = -1;
    private boolean columnsMatched = false;
    private int expFields = 0;
    private int fm[] = null;

    public isenseInterface() {
        rapi = RestAPI.getInstance();
    }

    /**
     * Log into the website using the Rest API.
     * Returns true if success.
     * Returns false if fail.
     *
     * @param username
     * @param password
     * @return
     */
    public boolean login(String username, String password) {
        //Store username and password
        user = username;
        pass = password;

        //Try to log in
        rapi.login(username, password);

        //Test Login
        if (rapi.isLoggedIn()) {
            myPrint("Log in successful!");
            return true;
        } else {
            myPrint("Log in failed!");
            return false;
        }
    }

    /**
     * Create a new session in the given experiment
     *
     * @param expID
     * @param sessionName
     * @param procedure
     * @param streetAddress
     * @param cityState
     * @return
     * @throws loginException
     */
    public int joinExperiment(int expID, String sessionName, String procedure,
            String streetAddress, String cityState) throws loginException {
        if (rapi.isLoggedIn()) { //Request sessionID from website with user inputed values

            int sessionId = rapi.createSession(
                    expID + "",
                    sessionName,
                    procedure,
                    streetAddress,
                    cityState,
                    "");

            return sessionId;
        } else {
            myPrint("Not logged in. Attempting to login...");
            if (login(user, pass)) {
                return joinExperiment(expID, sessionName, procedure, streetAddress, cityState);
            } else {
                throw new loginException();
            }
        }
    }

    /**
     * Add a batch of records to a session
     *
     * @param expID
     * @param sesID
     * @param headers
     * @param records
     * @return
     * @throws loginException
     */
    public boolean addToSession(int expID, int sesID, String[] headers,
            Vector<String> records) throws loginException, columnMatchException {


        if (rapi.isLoggedIn()) {
            if (isSessionOwner(sesID)) {
                JSONArray data = createDataJSON(expID, headers, records);
                //If the JSON object was built attempt to create session
                if (data != null) {
                    Boolean result = rapi.putSessionData(sesID, expID + "", data);

                    //Let the user know if the upload was successful or not.
                    if (result) {
                        myPrint("Successfully added data to session: " + sesID + " in experiment: " + expID);
                        return true;
                    } else {
                        return false;
                    }

                } else {
                    return false;
                }
            } else {
                myPrint("Sorry you are not the session owner");
                return false;
            }
        } else {
            myPrint("Not logged in. Attempting to login...");
            if (login(user, pass)) {
                return addToSession(expID, sesID, headers, records);
            } else {
                throw new loginException();
            }
        }
    }

    /**
     * Add a single record to a session (stream)
     *
     * @param expID
     * @param sesID
     * @param headers
     * @param record
     * @return
     * @throws loginException
     */
    public boolean addToSession(int expID, int sesID, String[] headers, String record) throws loginException, columnMatchException {

        if (rapi.isLoggedIn()) {
            if (isSessionOwner(sesID)) {
                JSONArray data = createDataJSON(expID, headers, record);

                //If the JSON object was built attempt to create session
                if (data != null) {
                    Boolean result = rapi.putSessionData(sesID, expID + "", data);

                    //Let the user know if the upload was successful or not.
                    if (result) {
                        myPrint("Successfully added data to session: " + sesID + " in experiment: " + expID);
                        return true;
                    } else {
                        return false;
                    }

                } else {
                    return false;
                }
            } else {
                myPrint("Sorry you are not the owner");
                return false;
            }
        } else {
            myPrint("Not logged in. Attempting to login...");
            if (login(user, pass)) {
                return addToSession(expID, sesID, headers, record);
            } else {
                throw new loginException();
            }
        }
    }

    /**
     * Add a single record to a session.
     *
     * @param expID
     * @param fields
     * @param record
     * @return
     */
    private JSONArray createDataJSON(int expID, String[] fields, String record) throws columnMatchException {
        //JSON Array returned from
        JSONArray ret = new JSONArray();

        JSONArray obj;

        obj = new JSONArray();

        fieldMatch(expID, fields);

        String[] values = record.split(",");
        obj = new JSONArray();
        int t = 0;
     
        while (t < expFields) {
            obj.put(values[fm[t]]);
            t++;
        }

        ret.put(obj);

        return ret;

    }

    /**
     * Creates a JSON object for upload to the website from data recorded
     * with a pinpoint.
     *
     * @param expID
     * @param fields
     * @param records
     * @return
     */
    private JSONArray createDataJSON(int expID, String[] fields, Vector<String> records) throws columnMatchException {

        //JSON Array returned from
        JSONArray ret = new JSONArray();

        JSONArray obj;

        //Create iterator for records
        Iterator<String> recordIterator = records.iterator();



        obj = new JSONArray();

        fieldMatch(expID, fields);

        while (recordIterator.hasNext()) {
            String[] values = recordIterator.next().split(",");
            obj = new JSONArray();
            int t = 0;
            while (t < expFields) {
                obj.put(values[fm[t]]);
                t++;
            }

            ret.put(obj);
        }

        return ret;


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

    public void fieldMatch(int expID, String[] fields) throws columnMatchException {
        
        
        if (!columnsMatched) {
            //Pull the experiment ID from the form, and return the field names
            //(sensors) required by the experiment
            ArrayList<ExperimentField> experimentFields = rapi.getExperimentFields(expID);

            //Create iterators for experiment fields
            Iterator<ExperimentField> eFieldIterator = experimentFields.iterator();

            String[] eFields = new String[experimentFields.size()];
            int i = 0;

            //Create array of field names from website.
            while (eFieldIterator.hasNext()) {
                ExperimentField x = eFieldIterator.next();
                eFields[i] = x.field_name;
                i++;
            }

            //Hash table matching fields to record columns
            Hashtable fieldMatches = new Hashtable();
            fm = new int[eFields.length];

            //Fill the has table with field to column matches
            myPrint("Experiment Fields  <---->  Data Fields\n"
                    + "---------------------------");
            for (int j = 0; j < fields.length; j++) {
                for (int k = 0; k < eFields.length; k++) {
                    if (fields[j].replaceAll("\\s","").compareToIgnoreCase(eFields[k].replaceAll("\\s","")) == 0) {
                        fieldMatches.put(eFields[k], j);
                        fm[k] = j;
                        myPrint(eFields[k] + "(" + k + ")" + "  <---->  " + fields[j] + "(" + j + ")");
                    }
                }
            }
            myPrint("\n");



            if (fieldMatches.size() == eFields.length) {
                myPrint("All Fields Matched!");
                columnsMatched = true;
                expFields = fieldMatches.size();
               
            } else {
                throw new columnMatchException();
            }
        }
        
    }

    public boolean isSessionOwner(int sessID) {
        if (!sessionVerified || (sessID != sessionID)) {
            ArrayList<SessionData> sessionData = rapi.sessiondata(sessID + "");

            SessionData sd = new SessionData();

            Iterator i = sessionData.iterator();

            int owner = 0;

            while (i.hasNext()) {
                sd = (SessionData) i.next();
                try {
                    owner = sd.MetaDataJSON.getJSONObject(0).getInt("owner_id");
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }

            if (owner == rapi.getUID()) {
                sessionID = sessID;
                sessionVerified = true;
                return true;
            } else {
                sessionID = -1;
                sessionVerified = false;
                return false;
            }
        }
        return true;
    }
}
