package edu.uml.cs.isense.queue;

import java.io.File;
import java.io.Serializable;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;

/**
 * Class that contains all elements of an iSENSE data set and
 * the functions necessary to upload the data or media to the
 * iSENSE website.
 * 
 * @author Jeremy Poulin and Mike Stowell of the iSENSE team.
 *
 */
@SuppressLint("ParserError")
public class DataSet implements Serializable {


	// DO NOT MODIFY -- AUTO-GENERATED SERIAL ID
	private static final long serialVersionUID = 3776465868309657210L;
	
	/**
	 * Constant that indicates that you have not yet created a session.
	 * This can be passed in the constructor of the DataSet object to
	 * indicate this.
	 * 
	 */
	public static final int NO_SESSION_DEFINED = -1;

	/**
	 * Enum that indicates whether a data set is of type data
	 * or media.
	 * 
	 * @author Jeremy Poulin and Mike Stowell of the iSENSE team.
	 *
	 */
	public enum Type {
		/**
		 * Indicates the data set is data.
		 */
		DATA, 
		/**
		 * Indicates the data set is media.
		 */
		PIC,
		/**
		 * Indicates the data is a session with both data and a single media object
		 */
		BOTH
	};

	// Both
	/**
	 * Type of data (DATA, PIC, or BOTH)
	 */
	public Type type;
	/**
	 * Name of the session associated with the data.
	 */
	private String name;
	/**
	 * Description of the data set.
	 */
	private String desc;
	/**
	 * Experiment ID# to upload the data set to.
	 */
	private String eid;
	
	private boolean rdyForUpload = true;
	
	protected long key;
	
	private boolean hasInitialExperiment = true;

	// Data Only
	/**
	 * String in JSONArray.toString() format containing all the
	 * data to upload to iSENSE.
	 */
	private String data;

	// Picture Only
	/**
	 * File containing the media in the data set.
	 */
	private File picture;

	// Optional
	/**
	 * Optional: session ID to associate the data set with.  
	 * One will be created if none is specified.
	 */
	private int sid = -1;
	/**
	 * Optional: city of where the data were recorded.
	 */
	private String city = "";
	/**
	 * Optional: state of where the data were recorded.
	 */
	private String state = "";
	/**
	 * Optional: country of where the data were recorded.
	 */
	private String country = "";
	/**
	 * Optional: address of where the data were recorded.
	 */
	private String addr = "";

	/**
	 * Contructs an object of type DataSet
	 * @param type DataSet.PIC or DataSet.DATA
	 * @param name
	 * @param desc
	 * @param eid
	 * @param data If type is DataSet.DATA, we look here.
	 * @param picture If type is DataSet.PIC, we look here.
	 * @param sid Pass DataSet.NO_SESSION_DEFINED if you have not created a session.
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
		if (data != null)
			this.data = data;
		else
			this.data = null;
		this.picture = picture;
		this.sid = sid;
		this.city = city;
		this.state = state;
		this.country = country;
		this.addr = addr;
		this.key = new Random().nextLong();
		this.hasInitialExperiment = eid.equals("-1") ? false : true;
	}

	/** 
	 * Attempts to upload data with given information
	 * 
	 * @return if the upload was successful
	 */
	public boolean upload() {

		if (!this.hasInitialExperiment)
			reOrderData();
			
		boolean success = true;
		if (this.rdyForUpload) {
			switch (type) {
			case DATA:

				if (sid == -1) {

					if (addr.equals("")) {
						sid = UploadQueue.getRapi().createSession(eid, name, desc,
								"N/A", "N/A", "United States");
					} else {
						sid = UploadQueue.getRapi().createSession(eid, name, desc,
								addr, city + ", " + state, country);
					}

					// Failure to create session or not logged in
					if (sid == -1) {
						success = false;
						break;
					} else QueueLayout.lastSID = sid;
				}

				// Experiment Closed Checker
				if (sid == -400) {
					success = false;
					break;
				} else {
					JSONArray dataJSON = prepDataForUpload();
					if (!(dataJSON.isNull(0))) {
						
						success = UploadQueue.getRapi().putSessionData(sid, eid,
								dataJSON);
					
					}
				}
				break;

			case PIC:
				if (sid == -1) sid = QueueLayout.lastSID;
				if (name.equals("")) {
					success = UploadQueue.getRapi().uploadPictureToSession(
							picture, eid, sid, "*Session Name Not Provided*",
							"N/A");
				} else {
					success = UploadQueue.getRapi().uploadPictureToSession(
							picture, eid, sid, name, "N/A");
				}
				
				break;
				
			case BOTH:
				if (sid == -1) {

					if (addr.equals("")) {
						sid = UploadQueue.getRapi().createSession(eid, name, desc,
								"N/A", "N/A", "United States");
					} else {
						sid = UploadQueue.getRapi().createSession(eid, name, desc,
								addr, city + ", " + state, country);
					}

					if (sid == -1) {
						success = false;
						break;
					} else QueueLayout.lastSID = sid;
				}

				// Experiment Closed Checker
				if (sid == -400) {
					success = false;
					break;
				} else {
					JSONArray dataJSON = prepDataForUpload();
					if (!(dataJSON.isNull(0))) {
						
						success = UploadQueue.getRapi().putSessionData(sid, eid,
								dataJSON);
						success = UploadQueue.getRapi().uploadPictureToSession(
								picture, eid, sid, name, "N/A");
					
					}
				}
				
				break;
			}
		}

		return success;
	}

	// TODO
	private void reOrderData() {
		JSONArray dataJSON = prepDataForUpload();
		
		JSONArray row;
		int len = dataJSON.length();
		for (int i = 0; i < len; i++) {
			try {
				row = dataJSON.getJSONArray(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Creates a JSON array out of the parsed string
	private JSONArray prepDataForUpload() {
		// If the string isn't a complete JSONArray, trim off the incomplete portion
		if (!(this.data.charAt(this.data.length() -1) == ']')) {
			int endIndex = this.data.lastIndexOf(']');
			if (endIndex != -1)
				this.data = this.data.substring(0, endIndex);
				this.data = this.data + ']';
		}
		
		JSONArray dataJSON = null;
		try {
			dataJSON = new JSONArray(data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dataJSON;
	}

	
	protected void setUploadable(boolean uploadable) {
		this.rdyForUpload = uploadable;
	}

	protected boolean isUploadable() {
		return this.rdyForUpload;
	}
	
	protected void setHasInitialExperiment(boolean hie) {
		this.hasInitialExperiment = hie;
	}
	
	protected boolean getHasInitialExperiment() {
		return this.hasInitialExperiment;
	}

	/**
	 * Getter for session ID.
	 * 
	 * @return The session ID associated with this data set.
	 */
	public int getSid() {
		return sid;
	}

	/**
	 * Setter for session ID.
	 * 
	 * @param sid
	 * 		The session ID the data should be uploaded to in the future.
	 */
	public void setSid(int sid) {
		this.sid = sid;
	}
	
	/**
	 * Getter for experiment ID.
	 * 
	 * @return The experiment ID associated with this data set.
	 */
	public String getEID() {
		return this.eid;
	}
	
	/**
	 * Setter for experiment ID.
	 * 
	 * @param eid
	 * 		The experiment ID the session should be created under.
	 */
	public void setExp(String eid) {
		this.eid = eid;
	}

	/**
	 * Getter for description.
	 * 
	 * @return The description associated with this data set.
	 */
	public String getDesc() {
		return this.desc;
	}

	/**
	 * Getter for the type of data set.
	 * 
	 * @return "Picture" for a media file, "Data" for a JSONArray formatted 
	 * String, or "Unsupported Type" otherwise.
	 */
	public CharSequence getType() {
		if (this.type == Type.PIC)
			return "Picture";
		else if (this.type == Type.DATA)
			return "Data";
		else if (this.type == Type.BOTH)
			return "Data and Picture";
		else
			return "Unsupported Type";
	}
	
	/**
	 * Setter for session name.
	 * 
	 * @param name
	 * 			The session name the data set should be associated with.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Getter for session name.
	 * 
	 * @return The name of the session associated with the data set.
	 */
	public CharSequence getName() {
		return this.name;
	}
	
	/**
	 * Setter for the data.
	 * 
	 * @param data
	 * 			The data (or media) to upload in the future.
	 */
	public void setData(String data) {
		this.data = data;
	}
	
	/**
	 * Getter for the data
	 * 
	 * @return The data associated with this data set.
	 */
	public String getData() {
		return this.data;
	}
	
}