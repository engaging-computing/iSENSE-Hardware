package edu.uml.cs.isense.queue;

import java.io.File;
import java.io.Serializable;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.dfm.DataFieldManager;

/**
 * Class that contains all elements of an iSENSE data set and
 * the functions necessary to upload the data or media to the
 * iSENSE website.
 * 
 * @author Jeremy Poulin and Mike Stowell of the iSENSE team.
 *
 */
@SuppressLint("ParserError")
public class QDataSet implements Serializable {


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
	 * Project ID# to upload the data set to.
	 */
	private String projID;
	
	private boolean rdyForUpload = true;
	
	protected long key;
	
	private boolean hasInitialProject = true;

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

	/**
	 * Contructs an object of type DataSet
	 * @param type DataSet.PIC or DataSet.DATA
	 * @param name
	 * @param desc
	 * @param projID
	 * @param data If type is DataSet.DATA, we look here.
	 * @param picture If type is DataSet.PIC, we look here.
	 */
	public QDataSet(Type type, String name, String desc, String projID,
			String data, File picture) {
		this.type = type;
		this.name = name;
		this.desc = desc;
		this.projID = projID;
		if (data != null)
			this.data = data;
		else
			this.data = null;
		this.picture = picture;
		this.key = new Random().nextLong();
		this.hasInitialProject = projID.equals("-1") ? false : true;
	}
	
	/**
	 * Upload function specifically for when projID = -1 initially
	 * In this scenario, you'll need to provide a RestAPI instance
	 * along with a context.  Should really only be called by
	 * 
	 * @param rapi An instance of RestAPI
	 * @param c The context of the calling class
	 * 
	 * @return if the upload was successful
	 */
	public boolean upload(API api, Context c) {
		if (this.projID.equals("-1"))
			return false;
		
		if (!this.hasInitialProject)
			this.data = DataFieldManager.reOrderData(prepDataForUpload(), this.projID, api, c);
		
		return upload();
	}

	/** 
	 * Attempts to upload data with given information
	 * 
	 * @return if the upload was successful
	 */
	public boolean upload() {
	
		boolean success = true;
		if (this.rdyForUpload) {
//			switch (type) {
//			case DATA:

				// TODO - check for closed experiment
//				if (sid == -1) {
//
//					if (addr.equals("")) {
//						sid = UploadQueue.getRapi().createSession(eid, name, desc,
//								"N/A", "N/A", "United States");
//					} else {
//						sid = UploadQueue.getRapi().createSession(eid, name, desc,
//								addr, city + ", " + state, country);
//					}
//
//					// Failure to create session or not logged in
//					if (sid == -1) {
//						success = false;
//						break;
//					} else QueueLayout.lastSID = sid;
//				}
//
//				// Experiment Closed Checker
//				if (sid == -400) {
//					success = false;
//					break;
//				} else {
				JSONArray dataJSON = prepDataForUpload();
				if (!(dataJSON.isNull(0))) {
					
//					success = UploadQueue.getRapi().putSessionData(sid, eid,
//							dataJSON);
					
					JSONObject jobj = new JSONObject();
					try {
						jobj.put("data", dataJSON);
					} catch (JSONException e) {
						// uh oh
						e.printStackTrace();
					}
					jobj = UploadQueue.getAPI().rowsToCols(jobj);
					
					System.out.println("JOBJ: " + jobj.toString());
					
					// TODO - success :(?
					/*success =*/ UploadQueue.getAPI().uploadDataSet(Integer.parseInt(projID), jobj, name);
				
				}
//				}
//				break;
// TODO - pictures and stuff
//			case PIC:
//				if (sid == -1) sid = QueueLayout.lastSID;
//				if (name.equals("")) {
//					success = UploadQueue.getRapi().uploadPictureToSession(
//							picture, eid, sid, "*Session Name Not Provided*",
//							"N/A");
//				} else {
//					success = UploadQueue.getRapi().uploadPictureToSession(
//							picture, eid, sid, name, "N/A");
//				}
//				
//				break;
//				
//			case BOTH:
//				if (sid == -1) {
//
//					if (addr.equals("")) {
//						sid = UploadQueue.getRapi().createSession(eid, name, desc,
//								"N/A", "N/A", "United States");
//					} else {
//						sid = UploadQueue.getRapi().createSession(eid, name, desc,
//								addr, city + ", " + state, country);
//					}
//
//					if (sid == -1) {
//						success = false;
//						break;
//					} else QueueLayout.lastSID = sid;
//				}
//
//				// Experiment Closed Checker
//				if (sid == -400) {
//					success = false;
//					break;
//				} else {
//					JSONArray dataJSON = prepDataForUpload();
//					if (!(dataJSON.isNull(0))) {
//						
//						success = UploadQueue.getRapi().putSessionData(sid, eid,
//								dataJSON);
//						success = UploadQueue.getRapi().uploadPictureToSession(
//								picture, eid, sid, name, "N/A");
//					
//					}
//				}
//				
//				break;
//			}
		}

		return success;
	}
	
	// Creates a JSON array out of the parsed string
	private JSONArray prepDataForUpload() {
		// If the string isn't a complete JSONArray, trim off the incomplete portion
//		if (!(this.data.charAt(this.data.length() -1) == '}')) {
//			int endIndex = this.data.lastIndexOf(']');
//			if (endIndex != -1)
//				this.data = this.data.substring(0, endIndex);
//				this.data = this.data + ']';
//		}
		
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
	
	protected void setHasInitialProject(boolean hip) {
		this.hasInitialProject = hip;
	}
	
	protected boolean getHasInitialProject() {
		return this.hasInitialProject;
	}

	/**
	 * Getter for project ID.
	 * 
	 * @return The project ID associated with this data set.
	 */
	public String getProjID() {
		return this.projID;
	}
	
	/**
	 * Setter for project ID.
	 * 
	 * @param projID
	 * 		The project ID the session should be created under.
	 */
	public void setProj(String projID) {
		this.projID = projID;
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