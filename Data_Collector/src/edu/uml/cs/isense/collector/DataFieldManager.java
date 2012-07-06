package edu.uml.cs.isense.collector;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Application;
import android.content.Context;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;

public class DataFieldManager extends Application {
	
	int eid;
	RestAPI rapi;
	Context mContext;
	
	ArrayList<ExperimentField> expFields;
	LinkedList<String> order;
	
	public DataFieldManager(int eid, RestAPI rapi, Context mContext) {
		this.eid  = eid;
		this.rapi = rapi;
		this.order = new LinkedList<String>();
		this.mContext = mContext;
	}
	
	public void sort() {
		expFields = rapi.getExperimentFields(eid);
		
		for (ExperimentField field : expFields) {
			switch (field.type_id) {
			
			//Temperature
			case 1:
				order.add(mContext.getString(R.string.temperature));
				break;
			
			// Time
			case 7:
				order.add(mContext.getString(R.string.time));
				break;
				
			// Light
			case 8:
			case 9:
				order.add(mContext.getString(R.string.luminous_flux));
				break;
				
			// Angle
			case 10:
				if (field.unit_name.toLowerCase().contains("deg")) {
					order.add(mContext.getString(R.string.heading_deg));
				} else if (field.unit_name.toLowerCase().contains("rad")) {
					order.add(mContext.getString(R.string.heading_rad));
				} else {
					order.add(mContext.getString(R.string.null_string));
				}
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
				
			// Numeric/Custom
			case 21:
			case 22:
				if (field.field_name.toLowerCase().contains("magnetic")) {
					if (field.field_name.toLowerCase().contains("x")) {
						order.add(mContext.getString(R.string.magnetic_x));
					} else if (field.field_name.toLowerCase().contains("y")) {
						order.add(mContext.getString(R.string.magnetic_y));
					} else if (field.field_name.toLowerCase().contains("z")) {
						order.add(mContext.getString(R.string.magnetic_z));
					} else if ((field.field_name.toLowerCase().contains("total")) || 
							(field.field_name.toLowerCase().contains("average")) ||
							(field.field_name.toLowerCase().contains("mean"))) {
						order.add(mContext.getString(R.string.magnetic_total));
					}
				} else if (field.field_name.toLowerCase().contains("altitude")) {
					order.add(mContext.getString(R.string.altitude));
				} else order.add(mContext.getString(R.string.null_string));
				break;
				
			// Acceleration
			case 25:
				if (field.field_name.toLowerCase().contains("x")) {
					order.add(mContext.getString(R.string.accel_x));
				} else if (field.field_name.toLowerCase().contains("y")) {
					order.add(mContext.getString(R.string.accel_y));
				} else if (field.field_name.toLowerCase().contains("z")) {
					order.add(mContext.getString(R.string.accel_z));
				} else if ((field.field_name.toLowerCase().contains("total")) || 
						(field.field_name.toLowerCase().contains("average")) ||
						(field.field_name.toLowerCase().contains("mean"))) {
					order.add(mContext.getString(R.string.accel_total));
				} else {
					order.add(mContext.getString(R.string.null_string));
				}
				break;
			
			// Pressure
			case 27:
				order.add(mContext.getString(R.string.pressure));
				break;
				
			// No match (Just about every other category)
			default:
				order.add(mContext.getString(R.string.null_string));
				break;
			
			}
			
		}
		
	}

}
