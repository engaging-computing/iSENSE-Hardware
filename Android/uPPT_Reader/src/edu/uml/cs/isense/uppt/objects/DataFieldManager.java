package edu.uml.cs.isense.uppt.objects;

import java.util.ArrayList;
import java.util.LinkedList;

import org.json.JSONArray;

import android.app.Application;
import android.content.Context;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;

public class DataFieldManager extends Application {

	int eid;
	RestAPI rapi;
	Context mContext;

	ArrayList<ExperimentField> expFields;
	public LinkedList<String> order;
	JSONArray dataSet;

	public DataFieldManager(int eid, RestAPI rapi, Context mContext) {
		this.eid = eid;
		this.rapi = rapi;
		this.order = new LinkedList<String>();
		this.mContext = mContext;
		this.dataSet = new JSONArray();
	}

	public void getFieldOrder() {
		expFields = rapi.getExperimentFields(eid);

		for (ExperimentField field : expFields) {
			order.add(field.field_name);
		}
	}
	
	/*
	 * Fields for uPPT (in order):
	 * 
	 * Timestamp, Elapsed Time, Temperature, Pressure, Altitude, Acceleration X, 
	 * Acceleration Y, Acceleration Z, Acceleration Magnitude, Light 
	 */
	public boolean match(String a, String b) {
		
		a = a.toLowerCase();
		b = b.toLowerCase();
		
		// Timestamp and Elapsed Time
		/*if (a.contains("time") && b.contains("time")) {
			if (a.contains("stamp") && (b.contains("stamp") || b.contains("date")))
				return true;
			if ((a.contains("elap") || a.contains("relat")) 
					&& (b.contains("elap") || b.contains("pass") || b.contains("relat")))
				return true;
			if (a.equals(b))
				return true;
		}*/
		if (a.contains("time") && b.contains("time"))
			return true;
		
		// Temperature
		if (a.contains("temp") && b.contains("temp"))
			return true;
		
		// Pressure
		if (a.contains("pressure") || a.contains("presure"))
			if (b.contains("pressure") || b.contains("presure"))
				return true;
		
		// Altitude
		if (a.contains("altit") && b.contains("altit"))
			return true;
		
		// Accel-X, -Y, -Z, and -Magnitude
		if (a.contains("accel") && b.contains("accel")) {
			if (a.contains("x") && b.contains("x"))
				return true;
			if (a.contains("y") && b.contains("y"))
				return true;
			if (a.contains("z") && b.contains("z"))
				return true;
			if ((a.contains("total") || a.contains("mag")) && 
					(b.contains("total") || b.contains("mag")))
				return true;
		}
		
		// Light
		if (a.contains("light") && (b.contains("light") || b.contains("lumin")))
			return true;
		
		// ---- Non-uPPT specific fields:
		
		// Temperature
		
		// Length
		
		// Distance
		
		// Force
		
		// Volume
		
		// Mass
		
		// Angle
		
		// E. Potential
		
		// E. Current
		
		// Power
		
		// E. Charge
		
		// Speed
		
		// Latitude
		if (a.contains("lat") && b.contains("lat"))
			return true;
		
		// Longitude
		if (a.contains("long") && b.contains("long"))
			return true;
		
		// Humidity
		
		// pH
		
		// Dissolved Oxygen
		
		// Turbidity
		
		// Flow Rate
		
		// Conductivity
		
		// Concentration
		
		return false;
	}

}
