package edu.uml.cs.isense.objects;

import org.json.JSONObject;

/**
 * Class that includes information about a Data Set Field on iSENSE.
 * 
 * @author iSENSE Android Development Team
 */
public class RDataSet {
    public int ds_id;
    public boolean hidden = false;
    public String name = "";
    public String url = "";
    public String timecreated = "";
    public int fieldCount;
    public int datapointCount;
    public JSONObject data;
}
