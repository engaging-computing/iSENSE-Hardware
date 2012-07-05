package edu.uml.cs.isense.collector;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Application;

import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;

public class DataFieldManager extends Application {
	
	int eid;
	RestAPI rapi;
	
	ArrayList<ExperimentField> expFields;
	LinkedList<String> order = new LinkedList<String>();
	
	public DataFieldManager(int eid, RestAPI rapi) {
		this.eid  = eid;
		this.rapi = rapi;
	}
	
	public void sort() {
		expFields = rapi.getExperimentFields(eid);
		
		for (ExperimentField field : expFields) {
			switch (field.type_id) {
			
			// Time
			case 7:
				order.add(getString(R.string.time));
				break;
			
			// Geospacial
			case 19:
				if (field.field_name.toLowerCase().contains("lat")) {
					order.add(getString(R.string.latitude));
				} else if (field.field_name.toLowerCase().contains("lon")) {
					order.add(getString(R.string.longitude));
				} else {
					order.add(getString(R.string.null_string));
				}
				break;
				
			// Numeric/Custom
			case 21:
			case 22:
				if (field.field_name.toLowerCase().contains("magnetic")) {
					if (field.field_name.toLowerCase().contains("x")) {
						order.add(getString(R.string.magnetic_x));
					} else if (field.field_name.toLowerCase().contains("y")) {
						order.add(getString(R.string.magnetic_y));
					} else if (field.field_name.toLowerCase().contains("z")) {
						order.add(getString(R.string.magnetic_z));
					} else if ((field.field_name.toLowerCase().contains("total")) || 
							(field.field_name.toLowerCase().contains("average")) ||
							(field.field_name.toLowerCase().contains("mean"))) {
						order.add(getString(R.string.magnetic_total));
					}
				}
				
			// Acceleration
			case 25:
				if (field.field_name.toLowerCase().contains("x")) {
					order.add(getString(R.string.accel_x));
				} else if (field.field_name.toLowerCase().contains("y")) {
					order.add(getString(R.string.accel_y));
				} else if (field.field_name.toLowerCase().contains("z")) {
					order.add(getString(R.string.accel_z));
				} else if ((field.field_name.toLowerCase().contains("total")) || 
						(field.field_name.toLowerCase().contains("average")) ||
						(field.field_name.toLowerCase().contains("mean"))) {
					order.add(getString(R.string.accel_total));
				} else {
					order.add(getString(R.string.null_string));
				}
				break;
			
			// No match
			default:
				order.add(getString(R.string.null_string));
				break;
			
			}
			
		}
		
	}

}
