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
		
		String aO = a, bO = b;
		a = a.toLowerCase(); // SD Card string
		b = b.toLowerCase(); // Experiment field string
		
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
		
		// Length
		if ((a.contains("length") && b.contains("length")) || 
				(a.contains("height") && b.contains("height")))
			return true;
		
		// Distance
		if (a.contains("distan") && b.contains("distan"))
			return true;
		
		// Force
		if (a.contains("force") && b.contains("force"))
			return true;
		
		// Volume
		if (a.contains("volume") && b.contains("volume"))
			return true;
		
		// Mass
		if (a.contains("mass") && b.contains("mass"))
			return true;
		
		// Angle
		if (a.contains("angle") && b.contains("angle"))
			return true;
		
		// E. Potential
		if (a.contains("potential") && b.contains("potential"))
			return true;
		
		// E. Current
		if (a.contains("curren") && b.contains("curren"))
			return true;
		
		// Power
		if (a.contains("power") && b.contains("power"))
			return true;
		
		// E. Charge
		if (a.contains("charg") && b.contains("charg"))
			return true;
		
		// Speed
		if ((a.contains("speed") || a.contains("veloc")) && (b.contains("speed") || b.contains("veloc")))
			return true;
		
		// Latitude
		if (a.contains("lat") && b.contains("lat"))
			return true;
		
		// Longitude
		if (a.contains("long") && b.contains("long"))
			return true;
		
		// Humidity
		if (a.contains("humid") && b.contains("humid"))
			return true;
		
		// pH
		if (aO.contains("pH") && bO.contains("pH"))
			return true;
		
		// Dissolved Oxygen
		if (((a.contains("dis") && a.contains("oxy")) && (b.contains("") && b.contains("oxy"))) || 
				(a.contains("d.o.") && b.contains("d.o.")))
			return true;
		
		// Turbidity
		if (a.contains("turbid") && b.contains("turbid"))
			return true;
		
		// Flow Rate
		if ((a.contains("flow") && a.contains("rate")) && (b.contains("flow") && b.contains("rate")))
			return true;
		
		// Conductivity
		if (a.contains("conduct") && b.contains("conduct"))
			return true;
		
		// Concentration
		if (a.contains("concent") && b.contains("concent"))
			return true;
		
		return false;
	}

}
