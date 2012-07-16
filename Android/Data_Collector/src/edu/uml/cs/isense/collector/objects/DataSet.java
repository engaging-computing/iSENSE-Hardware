package edu.uml.cs.isense.collector.objects;

import java.io.File;
import java.util.List;

import org.json.JSONArray;

import android.location.Address;
import edu.uml.cs.isense.comm.RestAPI;

public class DataSet {
	public enum Type {
		DATA, PIC
	};

	// Both
	public Type type;
	private RestAPI rapi;
	private String name;
	private String desc;
	private String eid;

	// Data Only
	private JSONArray data;

	// Picture Only
	private File picture;

	// Optional
	private int sid = -1;
	private List<Address> address;

	public DataSet(Type type, RestAPI rapi, String name, String desc, String eid,
			JSONArray data, File picture, int sid, List<Address> address) {
		this.type = type;
		this.rapi = rapi;
		this.name = name;
		this.desc = desc;
		this.eid = eid;
		this.data = data;
		this.picture = picture;
		this.sid = sid;
		this.address = address;
	}

	// Attempts to upload data with given information
	public boolean upload() {
		boolean success = true;
		switch (type) {
		case DATA:
			if (sid == -1) {
				String city = "", state = "", country = "", addr = "";

				if (address.size() > 0) {
					city = address.get(0).getLocality();
					state = address.get(0).getAdminArea();
					country = address.get(0).getCountryName();
					addr = address.get(0).getAddressLine(0);
				}

				if (address == null || address.size() <= 0) {
					sid = rapi.createSession(eid, name, desc, "N/A", "N/A",
							"United States");
				} else {
					sid = rapi.createSession(eid, name, desc, addr, city + ", "
							+ state, country);
				}

				if (sid == -1) {
					success = false;
					break;
				}
			}
			
			// Experiment Closed Checker
			if (sid == -400) {
				success = false;
				break;
			} else
				success = rapi.putSessionData(sid, eid, data);
			
			break;
	
		case PIC:
			if (name.equals("")) {
				success = rapi.uploadPictureToSession(picture, eid, sid,
						"*Session Name Not Provided*", "N/A");
			} else {
				success = rapi.uploadPictureToSession(picture, eid, sid, name,
						"N/A");
			}
			break;

		}

		return success;
	}

	public String getEID() {
		return this.eid;
	}
	
	public String getDesc() {
		return this.desc;
	}

}
