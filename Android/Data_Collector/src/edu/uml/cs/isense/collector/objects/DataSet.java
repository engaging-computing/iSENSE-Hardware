package edu.uml.cs.isense.collector.objects;

import java.io.File;
import java.util.List;

import org.json.JSONArray;

import android.location.Address;
import edu.uml.cs.isense.collector.DataCollector;

public class DataSet {

	public enum Type {
		DATA, PIC
	};

	// Both
	public Type type;
	private String name;
	private String desc;
	private String eid;
	private boolean rdyForUpload = true;

	// Data Only
	private JSONArray data;

	// Picture Only
	private File picture;

	// Optional
	private int sid = -1;
	private String city = "";
	private String state = "";
	private String country = "";
	private String addr = "";

	public DataSet(Type type, String name, String desc,
			String eid, JSONArray data, File picture, int sid,
			List<Address> address) {
		this.type = type;
		this.name = name;
		this.desc = desc;
		this.eid = eid;
		this.data = data;
		this.picture = picture;
		this.sid = sid;
		this.city  = address.get(0).getLocality();
		this.state = address.get(0).getAdminArea();
		this.country = address.get(0).getCountryName();
		this.addr = address.get(0).getAddressLine(0);
	}

	// Attempts to upload data with given information
	public boolean upload() {
		boolean success = true;
		if (this.rdyForUpload) {
			switch (type) {
			case DATA:
				
				if (sid == -1) {

					if (addr.equals("")) {
						sid = DataCollector.rapi.createSession(eid, name, desc, "N/A", "N/A",
								"United States");
					} else {
						sid = DataCollector.rapi.createSession(eid, name, desc, addr, city
								+ ", " + state, country);
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
					success = DataCollector.rapi.putSessionData(sid, eid, data);

				break;

			case PIC:
				if (name.equals("")) {
					success = DataCollector.rapi.uploadPictureToSession(picture, eid, sid,
							"*Session Name Not Provided*", "N/A");
				} else {
					success = DataCollector.rapi.uploadPictureToSession(picture, eid, sid,
							name, "N/A");
				}
				break;

			}
		}
		
		return success;
	}

	public String getEID() {
		return this.eid;
	}

	public String getDesc() {
		return this.desc;
	}

	public CharSequence getName() {
		return this.name;
	}

	public CharSequence getType() {
		if (this.type == Type.PIC)
			return "Picture";
		else if (this.type == Type.DATA)
			return "Data";
		else
			return "Unsupported Type";
	}

	public void setUploadable(boolean uploadable) {
		this.rdyForUpload = uploadable;
	}
	
	public boolean isUploadable() {
		return this.rdyForUpload;
	}

}
