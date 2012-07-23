package edu.uml.cs.isense.collector.objects;

import java.io.File;
import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import edu.uml.cs.isense.collector.DataCollector;

@SuppressLint("ParserError")
public class DataSet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3776465868309657210L;

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
	private byte[] data;

	// Picture Only
	private File picture;

	// Optional
	private int sid = -1;
	private String city = "";
	private String state = "";
	private String country = "";
	private String addr = "";
	
	public DataSet(Type type, String name, String desc, String eid,
			String data, File picture, int sid, String city, String state, String country, String addr) {
		this.type = type;
		this.name = name;
		this.desc = desc;
		this.eid = eid;
		if (!(data == null))
			this.data = data.getBytes();
		else this.data = null;
		this.picture = picture;
		this.sid = sid;
		this.city = city;
		this.state = state;
		this.country = country;
		this.addr = addr;
	}

	// Attempts to upload data with given information
	public boolean upload() {
		boolean success = true;
		if (this.rdyForUpload) {
			switch (type) {
			case DATA:

				if (sid == -1) {

					if (addr.equals("")) {
						sid = DataCollector.rapi.createSession(eid, name, desc,
								"N/A", "N/A", "United States");
					} else {
						sid = DataCollector.rapi.createSession(eid, name, desc,
								addr, city + ", " + state, country);
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
				} else {
					JSONArray dataJSON = prepDataForUpload();
					if (!(dataJSON.isNull(0)))
					 success = DataCollector.rapi.putSessionData(sid, eid, dataJSON);
				}
				break;

			case PIC:
				if (name.equals("")) {
					success = DataCollector.rapi.uploadPictureToSession(
							picture, eid, sid, "*Session Name Not Provided*",
							"N/A");
				} else {
					success = DataCollector.rapi.uploadPictureToSession(
							picture, eid, sid, name, "N/A");
				}
				break;

			}
		}

		return success;
	}

	private JSONArray prepDataForUpload() {
		String dataString = data.toString();
		JSONArray dataJSON = null;
		try {
			dataJSON = new JSONArray(dataString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dataJSON;
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