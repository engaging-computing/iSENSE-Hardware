package edu.uml.cs.isense.queue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;

import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.API.TargetType;
import edu.uml.cs.isense.comm.uploadInfo;
import edu.uml.cs.isense.credentials.CredentialManager;
import edu.uml.cs.isense.credentials.CredentialManagerKey;
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
	private boolean requestDataLabelInOrder = false;

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
	public QDataSet(String name, String desc, Type type, String data,
			File picture, String projID, LinkedList<String> fields) {
		// name and description of data set
		this.name = name;
		this.desc = desc;

		// type (data/media) and the associated data and/or media
		this.type = type;
		if (data != null)
			this.data = data;
		else {
			this.data = null;
		}
		this.picture = picture;

		// project and fields
		this.projID = projID;
		if (fields != null)
			this.fields = fields;
		else
			fields = new LinkedList<String>();
		this.hasInitialProject = projID.equals("-1") ? false : true;
		this.requestDataLabelInOrder = false;

		// randomized key
		this.key = new Random().nextLong();
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
	public uploadInfo upload(API api, Context c) {
        uploadInfo info = new uploadInfo();
		// if no project is associated with this data set yet, we can't upload
		// it
		if (this.projID.equals("-1")) {
            info.errorMessage = "No Project Selected";
            return info;
        }
        // if the data is already in a forced order and just needs to be labeled
		// with the
		// project's field IDs, we'll do so here
		if (this.requestDataLabelInOrder) {
			try {
				// see if the elements of the JSONArray are JSONArrays
				if (data != null) {
					JSONArray ja = new JSONArray(data);
					ja.getJSONArray(0);

					// if we got here, the data is a JSONArray of JSONArrays:
					// convert it
					DataFieldManager dfm = new DataFieldManager(
							Integer.parseInt(this.projID), api, c, null);
					this.data = dfm.convertInternalDataToJSONObject(ja)
							.toString();
                    requestDataLabelInOrder = false;
				}
			} catch (JSONException e) {
				// we have a JSONArray of JSONObjects: this is bad
                Log.e("QDataSet method 'upload' in iSENSE Imports: ", "JSONArray of JSONObjects - ");
                e.printStackTrace();
                info.dataSetId = -1;
                info.errorMessage = "Wrong JSON Format";
                return info;
			}
		} else {
			// if there was no initial project, we must reOrder the data with
			// the fields from FieldMatching
			if (!this.hasInitialProject) {
				Log.e("QDataSet -- fields", this.fields.toString());
				Log.e("QDataSet -- upPrepData", this.data);
				Log.e("QDataSet -- prepData", prepDataForUpload().toString());
				this.data = DataFieldManager.reOrderData(prepDataForUpload(),
						this.projID, c, this.fields, null);
				Log.e("QDataSet -- postData", this.data);
				
			}

			// otherwise, if we have a JSONArray for data, we must reOrder it as
			// well using fields
			try {
				Log.e("Upload Time", data.toString());
				
				// see if the elements of the JSONArray are JSONArrays
				if (data != null) {
					JSONArray ja = new JSONArray(data);
					ja.getJSONArray(0);

					// if we got here, the data is a JSONArray of JSONArrays:
					// reOrder it
					this.data = DataFieldManager.reOrderData(ja, this.projID,
							c, this.fields, null);
				}

			} catch (JSONException e) {
                Log.w("QDataSet in iSENSEImports: ", "we already have a JSONArray of JSONObjects, should continue to upload anyways");
				// we have a JSONArray of JSONObjects for data already -
				// continue without reOrdering
			}
		}

		return uploadDataAndMedia();
	}

	/**
	 * Attempts to upload data with the given information passed in through the
	 * QDataSet constructor
	 * 
	 * @return The ID of the data set created on iSENSE, or -1 if the upload
	 *         failed
	 */
	private uploadInfo uploadDataAndMedia() {
        uploadInfo info = new uploadInfo();
		if (this.rdyForUpload) {
			switch (type) {
			case DATA:
				info = uploadData();
				break;


			case PIC:
				if (CredentialManager.isLoggedIn()) {
                    info = UploadQueue.getAPI().uploadMedia(
							Integer.parseInt(projID), picture, TargetType.PROJECT);
				} else {
					String key = CredentialManagerKey.getKey();
                    info = UploadQueue.getAPI().uploadMedia(
							Integer.parseInt(projID), picture, TargetType.PROJECT, key, name);
				}
				break;

			case BOTH:
				info = uploadData();

                uploadInfo picInfo = new uploadInfo();
				if (CredentialManager.isLoggedIn() && info.dataSetId != -1) {
					picInfo = UploadQueue.getAPI().uploadMedia(
							info.dataSetId, picture, TargetType.DATA_SET);
				} else {
					String key = CredentialManagerKey.getKey();
					picInfo = UploadQueue.getAPI().uploadMedia(
                            info.dataSetId, picture, TargetType.DATA_SET, key, name);
				}

                info.mediaId = picInfo.mediaId;
                break;
			}
		}

		return info;
	}

	private uploadInfo uploadData() {
//		int dataSetID = -1;
        uploadInfo info = new uploadInfo();

		JSONArray dataJSON = prepDataForUpload();
		if (!(dataJSON.isNull(0))) {

			JSONObject jobj = new JSONObject();
			try {
				jobj.put("data", dataJSON);
			} catch (JSONException e) {
				e.printStackTrace();
                info.dataSetId = -1;
                info.errorMessage = "JSON Error";
				return info;
			}
			jobj = UploadQueue.getAPI().rowsToCols(jobj);

			System.out.println("JOBJ: " + jobj.toString());
			
			//If not logged in open key dialog and onActivityResult call with credential keys
			if (CredentialManager.isLoggedIn()) {

                info = UploadQueue.getAPI().uploadDataSet(
						Integer.parseInt(projID), jobj, name);
			
			}else{
				String key = CredentialManagerKey.getKey();
				String conName = CredentialManagerKey.getName();

                info = UploadQueue.getAPI().uploadDataSet(
						Integer.parseInt(projID), jobj, name, key, conName);
						
			}
			
			System.out.println("Data set ID from Upload is: " + info.dataSetId);
		}

		return info;
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) { // passes
//																					// in
//																					// a
//																					// request
//																					// code
//		super.onActivityResult(requestCode, resultCode, data);
//
//		if (requestCode == CREDENTIAL_KEY_REQUESTED) { // request to takes picture
//
//			if (resultCode == Activity.RESULT_OK) {
//
//			} else {
//				
//			}
//		}
//	}

	
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
	public Type getType() {
        return type;
//		if (this.type == Type.PIC)
//			return "Picture";
//		else if (this.type == Type.DATA)
//			return "Data";
//		else if (this.type == Type.BOTH)
//			return "Data and Picture";
//		else
//			return "Unsupported Type";
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

	/**
	 * Set this parameter for this data set if you passed in a JSONArray of
	 * JSONArrays for a data set with data, in order, of a project you want to
	 * associate with project fields, in order, at upload time. If you
	 * understood ANY of that, fantastic.
	 * 
	 * @param rdlio
	 */
	public void setRequestDataLabelInOrder(boolean rdlio) {
		this.requestDataLabelInOrder = rdlio;
	}
	
}