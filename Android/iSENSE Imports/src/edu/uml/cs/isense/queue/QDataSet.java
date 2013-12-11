package edu.uml.cs.isense.queue;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.dfm.DataFieldManager;

/**
 * Class that contains all elements of an iSENSE data set and the functions
 * necessary to upload the data or media to the iSENSE website.
 * 
 * @author Jeremy Poulin and Mike Stowell of the iSENSE team.
 * 
 */
@SuppressLint("ParserError")
public class QDataSet implements Serializable {

	// DO NOT MODIFY -- AUTO-GENERATED SERIAL ID
	private static final long serialVersionUID = 3776465868309657210L;

	/**
	 * Enum that indicates whether a data set is of type data, media, or
	 * contains data and media.
	 * 
	 */
	public enum Type {
		/**
		 * Indicates the data set is data.
		 */
		DATA,
		/**
		 * Indicates the data set is a single media object.
		 */
		PIC,
		/**
		 * Indicates the data set contains data and a single media object.
		 */
		BOTH
	};

	// Both
	/**
	 * Type of data (DATA, PIC, or BOTH)
	 */
	public Type type;
	/**
	 * Name of the data set associated with the data.
	 */
	private String name;
	/**
	 * Description of the data set.
	 */
	private String desc;
	/**
	 * Project ID to upload the data set to.
	 */
	private String projID;

	private boolean rdyForUpload = true;

	protected long key;

	private boolean hasInitialProject = true;

	/**
	 * String in JSONArray.toString() format containing all the data to upload
	 * to iSENSE.
	 */
	private String data;

	/**
	 * File containing the media in the data set.
	 */
	private File picture;
	
	/**
	 * Used with FieldMatching when no initial project is set.
	 */
	private LinkedList<String> fields;

	/**
	 * Contructs an object of type QDataSet
	 * 
	 * @param type
	 *            - QDataSet.DATA QDataSet.PIC, or QDataSet.BOTH
	 * @param name
	 *            - The name of the of the data set
	 * @param desc
	 *            - A description for the data set
	 * @param projID
	 *            - The associated project ID for the data set
	 * @param data
	 *            - If type is QDataSet.DATA, we look here.
	 * @param picture
	 *            - If type is QDataSet.PIC, we look here.
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
		this.fields = new LinkedList<String>();
	}

	/**
	 * Upload function specifically for when projID = -1 initially.
	 * 
	 * In this scenario, you'll need to provide an
	 * {@link edu.uml.cs.isense.comm.API API} instance along with an activity
	 * context.
	 * 
	 * @param api
	 *            - An instance of API
	 * @param c
	 *            - The context of the calling activity
	 * 
	 * @return The ID of the data set created on iSENSE, or -1 if the upload
	 *         failed
	 */
	public int upload(API api, Context c) {
		if (this.projID.equals("-1"))
			return -1;

		if (!this.hasInitialProject) {
			System.out.println("Need to re-order some data with fields: " + this.fields.toString());
			this.data = DataFieldManager.reOrderData(prepDataForUpload(),
					this.projID, api, c, this.fields);
		}

		return upload();
	}

	/**
	 * Attempts to upload data with the given information passed in through the
	 * QDataSet constructor
	 * 
	 * @return The ID of the data set created on iSENSE, or -1 if the upload
	 *         failed
	 */
	public int upload() {

		int dataSetID = -1;
		if (this.rdyForUpload) {
			switch (type) {
			case DATA:
				dataSetID = uploadData();
				break;

			case PIC:
				dataSetID = UploadQueue.getAPI().uploadProjectMedia(Integer.parseInt(projID), picture);				
				break;

			case BOTH:
				System.out.println("Calling Upload Dataset");
				dataSetID = uploadData();
				System.out.println("Calling Upload Dataset Media");
				dataSetID = UploadQueue.getAPI().uploadProjectMedia(Integer.parseInt(projID), picture); //.uploadDataSetMedia(dataSetID, picture);
				System.out.println("New id = " + dataSetID);
				break;
				
			}
		}

		return dataSetID;
	}
	
	private int uploadData() {
		int dataSetID = - 1;
		
		JSONArray dataJSON = prepDataForUpload();
		if (!(dataJSON.isNull(0))) {

			JSONObject jobj = new JSONObject();
			try {
				jobj.put("data", dataJSON);
			} catch (JSONException e) {
				// uh oh
				e.printStackTrace();
			}
			jobj = UploadQueue.getAPI().rowsToCols(jobj);

			System.out.println("JOBJ: " + jobj.toString());

			dataSetID = UploadQueue.getAPI().uploadDataSet(
					Integer.parseInt(projID), jobj, name);
			System.out.println("Data set ID from Upload is: "
					+ dataSetID);

		}
		
		return dataSetID;
	}

	// Creates a JSON array out of the parsed string
	private JSONArray prepDataForUpload() {
		// If the string isn't a complete JSONArray, trim off the incomplete
		// portion
		// if (!(this.data.charAt(this.data.length() -1) == '}')) {
		// int endIndex = this.data.lastIndexOf(']');
		// if (endIndex != -1)
		// this.data = this.data.substring(0, endIndex);
		// this.data = this.data + ']';
		// }

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
	
	protected void setFields(LinkedList<String> fieldList) {
		this.fields = fieldList;
	}
	
	protected LinkedList<String> getFields() {
		return this.fields;
	}

	/**
	 * Getter for the project ID.
	 * 
	 * @return The project ID associated with this data set.
	 */
	public String getProjID() {
		return this.projID;
	}

	/**
	 * Setter for the project ID.
	 * 
	 * @param projID
	 *            - The project ID the session should be created under.
	 */
	public void setProj(String projID) {
		this.projID = projID;
	}

	/**
	 * Getter for the description.
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
	 *         String, "Data and Picture" for a JSONArray formatted String with
	 *         a single associated media file, or "Unsupported Type" otherwise.
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
	 * Setter for the data set name.
	 * 
	 * @param name
	 *            - The data set name the data set should be associated with.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for data set name.
	 * 
	 * @return The name of the data set associated with the data.
	 */
	public CharSequence getName() {
		return this.name;
	}

	/**
	 * Setter for the data.
	 * 
	 * @param data
	 *            - The data to upload in the future.
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

	/**
	 * Setter for the picture
	 * 
	 * @param pic
	 *            - The media to associate with this data set.
	 */
	public void setPicture(File pic) {
		this.picture = pic;
	}

	/**
	 * Getter for the picture
	 * 
	 * @return The media associated with this data set.
	 */
	public File getPicture() {
		return this.picture;
	}

}