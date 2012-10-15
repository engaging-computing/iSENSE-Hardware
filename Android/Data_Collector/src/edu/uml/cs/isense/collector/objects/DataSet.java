package edu.uml.cs.isense.collector.objects;

import java.io.File;
import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import edu.uml.cs.isense.collector.shared.QueueUploader;
import edu.uml.cs.isense.collector.splash.Splash;

@SuppressLint("ParserError")
public class DataSet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3776465868309657210L;
	
	public static final int NO_SESSION_DEFINED = -1;

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
	private String data;

	// Picture Only
	private File picture;

	// Optional
	private int sid = -1;
	private String city = "";
	private String state = "";
	private String country = "";
	private String addr = "";

	/**
	 * Contructs an object of type DataSet
	 * @param type DataSet.PIC or DataSet.DATA
	 * @param name
	 * @param desc
	 * @param eid
	 * @param data If type is DataSet.DATA, we look here.
	 * @param picture If type is DataSet.PIC, we look here.
	 * @param sid Give me DataSet.NO_SESSION_DEFINED if you haven't called create session.
	 * @param city
	 * @param state
	 * @param country
	 * @param addr
	 */
	public DataSet(Type type, String name, String desc, String eid,
			String data, File picture, int sid, String city, String state,
			String country, String addr) {
		this.type = type;
		this.name = name;
		this.desc = desc;
		this.eid = eid;
		if (!(data == null))
			this.data = data;
		else
			this.data = null;
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
						sid = Splash.rapi.createSession(eid, name, desc,
								"N/A", "N/A", "United States");
					} else {
						sid = Splash.rapi.createSession(eid, name, desc,
								addr, city + ", " + state, country);
					}

					if (sid == -1) {
						success = false;
						QueueUploader.qpa.uploadQueue.add(this);
						break;
					} else QueueUploader.lastSID = sid;
				}

				// Experiment Closed Checker
				if (sid == -400) {
					success = false;
					QueueUploader.qpa.uploadQueue.add(this);
					break;
				} else {
					JSONArray dataJSON = prepDataForUpload();
					if (!(dataJSON.isNull(0))) {
						success = Splash.rapi.putSessionData(sid, eid,
								dataJSON);
						if (!success)
							QueueUploader.qpa.uploadQueue.add(this);
					}
				}
				break;

			case PIC:
				if (sid == -1) sid = QueueUploader.lastSID;
				if (name.equals("")) {
					success = Splash.rapi.uploadPictureToSession(
							picture, eid, sid, "*Session Name Not Provided*",
							"N/A");
				} else {
					success = Splash.rapi.uploadPictureToSession(
							picture, eid, sid, name, "N/A");
				}
				if (!success)
					QueueUploader.qpa.uploadQueue.add(this);
				break;

			}
		}

		return success;
	}

	// Creates a JSON array out of the parsed string
	private JSONArray prepDataForUpload() {
		JSONArray dataJSON = null;
		try {
			dataJSON = new JSONArray(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dataJSON;
	}

	public void setUploadable(boolean uploadable) {
		this.rdyForUpload = uploadable;
	}

	public boolean isUploadable() {
		return this.rdyForUpload;
	}

	//Getters and Setters
	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}
	
	public String getEID() {
		return this.eid;
	}

	public String getDesc() {
		return this.desc;
	}

	public CharSequence getType() {
		if (this.type == Type.PIC)
			return "Picture";
		else if (this.type == Type.DATA)
			return "Data";
		else
			return "Unsupported Type";
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public CharSequence getName() {
		return this.name;
	}
	
}