package edu.uml.cs.isense.objects;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Object containing the data contained in a session.  This includes raw data,
 * data, fields, and meta data, all in the form on JSON objects/arrays.
 * 
 * @author iSENSE Android Development Team
 */
public class SessionData {
	public JSONObject RawJSON = null;
	public JSONArray DataJSON = null;
	public JSONArray FieldsJSON = null;
	public JSONArray MetaDataJSON = null;
	public ArrayList<ArrayList<String>> fieldData = null;	
}