package edu.uml.cs.isense.uppt;

import java.util.ArrayList;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;

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
	Fields f;

	public DataFieldManager(int eid, RestAPI rapi, Context mContext, Fields f) {
		this.eid = eid;
		this.rapi = rapi;
		this.order = new LinkedList<String>();
		this.mContext = mContext;
		this.dataSet = new JSONArray();
		this.f = f;
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
		if (a.contains("time") && b.contains("time")) {
			if (a.contains("stamp") && (b.contains("stamp") || b.contains("date")))
				return true;
			if (a.contains("elap") && (b.contains("elap") || b.contains("pass") || b.contains("relat")))
				return true;
		}
		
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
		
		return false;
	}
	

	public JSONArray putData() {

		JSONArray dataJSON = new JSONArray();

		for (String s : this.order) {
			try {
				if (s.equals(mContext.getString(R.string.accel_x))) {
					dataJSON.put(f.accel_x);
					continue;
				}
				if (s.equals(mContext.getString(R.string.accel_y))) {
					dataJSON.put(f.accel_y);
					continue;
				}
				if (s.equals(mContext.getString(R.string.accel_z))) {
					dataJSON.put(f.accel_z);
					continue;
				}
				if (s.equals(mContext.getString(R.string.accel_total))) {
					dataJSON.put(f.accel_total);
					continue;
				}
				if (s.equals(mContext.getString(R.string.temperature_c))) {
					dataJSON.put(f.temperature_c);
					continue;
				}
				if (s.equals(mContext.getString(R.string.temperature_f))) {
					dataJSON.put(f.temperature_f);
					continue;
				}
				if (s.equals(mContext.getString(R.string.temperature_k))) {
					dataJSON.put(f.temperature_k);
					continue;
				}
				if (s.equals(mContext.getString(R.string.time))) {
					dataJSON.put(f.timeMillis);
					continue;
				}
				if (s.equals(mContext.getString(R.string.luminous_flux))) {
					dataJSON.put(f.lux);
					continue;
				}
				if (s.equals(mContext.getString(R.string.heading_deg))) {
					dataJSON.put(f.angle_deg);
					continue;
				}
				if (s.equals(mContext.getString(R.string.heading_rad))) {
					dataJSON.put(f.angle_rad);
					continue;
				}
				if (s.equals(mContext.getString(R.string.latitude))) {
					dataJSON.put(f.latitude);
					continue;
				}
				if (s.equals(mContext.getString(R.string.longitude))) {
					dataJSON.put(f.longitude);
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_x))) {
					dataJSON.put(f.mag_x);
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_y))) {
					dataJSON.put(f.mag_y);
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_z))) {
					dataJSON.put(f.mag_z);
					continue;
				}
				if (s.equals(mContext.getString(R.string.magnetic_total))) {
					dataJSON.put(f.mag_total);
					continue;
				}
				if (s.equals(mContext.getString(R.string.altitude))) {
					dataJSON.put(f.altitude);
					continue;
				}
				if (s.equals(mContext.getString(R.string.pressure))) {
					dataJSON.put(f.pressure);
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
