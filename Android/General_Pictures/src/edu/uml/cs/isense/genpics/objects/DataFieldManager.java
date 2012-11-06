package edu.uml.cs.isense.genpics.objects;

import java.util.ArrayList;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Application;
import android.content.Context;
import edu.uml.cs.isense.genpics.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;

public class DataFieldManager extends Application {

	int eid;
	RestAPI rapi;
	Context mContext;

	ArrayList<ExperimentField> expFields;
	public LinkedList<String> order;
	JSONArray dataSet;
	Fields f;

	public DataFieldManager(int eid, RestAPI rapi, Context mContext, Fields f) {
		this.eid = eid;
		this.rapi = rapi;
		this.order = new LinkedList<String>();
		this.mContext = mContext;
		this.dataSet = new JSONArray();
		this.f = f;
	}

	public void getOrder() {
		expFields = rapi.getExperimentFields(eid);

		for (ExperimentField field : expFields) {
			switch (field.type_id) {

			// Time
			case 7:
				order.add(mContext.getString(R.string.time));
				break;

			// Geospacial
			case 19:
				if (field.field_name.toLowerCase().contains("lat")) {
					order.add(mContext.getString(R.string.latitude));
				} else if (field.field_name.toLowerCase().contains("lon")) {
					order.add(mContext.getString(R.string.longitude));
				} else {
					order.add(mContext.getString(R.string.null_string));
				}
				break;


			// No match (Just about every other category)
			default:
				order.add(mContext.getString(R.string.null_string));
				break;

			}

		}

	}

	public JSONArray putData() {

		JSONArray dataJSON = new JSONArray();

		for (String s : this.order) {
			try {
				if (s.equals(mContext.getString(R.string.time))) {
					dataJSON.put(f.time);
					continue;
				}
				if (s.equals(mContext.getString(R.string.latitude))) {
					dataJSON.put(f.lat);
					continue;
				}
				if (s.equals(mContext.getString(R.string.longitude))) {
					dataJSON.put(f.lon);
					continue;
				}
				dataJSON.put(null);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return dataJSON;

	}
}