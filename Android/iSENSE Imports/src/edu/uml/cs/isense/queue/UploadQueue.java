package edu.uml.cs.isense.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import edu.uml.cs.isense.comm.RestAPI;

public class UploadQueue implements Serializable {
	/**
	 * Serializable key for UploadQueue class - DO NOT CHANGE
	 */
	private static final long serialVersionUID = -3036173866992721309L;
	public static final String SERIAL_ID = "upload_queue_object";
	
	protected Queue<DataSet> queue;
	protected Queue<DataSet> mirrorQueue;
	private static String parentName;
	private static Context mContext;
	private static RestAPI rapi;

	public UploadQueue(String parentName, Context context,
			RestAPI rapi) {
		this.queue = new LinkedList<DataSet>();
		this.mirrorQueue = new LinkedList<DataSet>();
		//mirrorQueue.addAll(queue);
		
		UploadQueue.parentName = parentName;
		UploadQueue.mContext = context;

		UploadQueue.rapi = rapi;
	}
	
	public String getParentName() {
		return parentName;
	}
	
	public Context getContext() {
		return mContext;
	}

	public void addDataSetToQueue(DataSet ds) {
		queue.add(ds);
		mirrorQueue.add(ds);
		storeAndReRetrieveQueue(true);
	}

	// Saves Q_COUNT and uploadQueue into memory for later use
	protected void storeAndReRetrieveQueue(boolean rebuildMirrorQueue) {

		Queue<DataSet> backupQueue = new LinkedList<DataSet>();
		backupQueue.addAll(queue);
		
		// save Q_COUNT in SharedPrefs
		final SharedPreferences mPrefs = mContext.getSharedPreferences(
				parentName, Context.MODE_PRIVATE);
		final SharedPreferences.Editor mPrefsEditor = mPrefs.edit();
		int Q_COUNT = backupQueue.size();
		int Q_COUNT_BACKUP = Q_COUNT;
		mPrefsEditor.putInt(parentName + "Q_COUNT", Q_COUNT);
		mPrefsEditor.commit();
		
		// obtain storage directory and file for the uploadqueue
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");
		
		if (!folder.exists()) {
			folder.mkdir();
		}

		File uploadQueueFile = new File(folder.getAbsolutePath() + "/"
				+ parentName + ".ser");

		// writes the queue to a serializable file
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(uploadQueueFile));
			
			// serializes DataSets
			while (Q_COUNT > 0) {
				DataSet ds = backupQueue.remove();
				out.writeObject(ds);
				Q_COUNT--;
			}

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// re-retrieve the queue
		
		queue = new LinkedList<DataSet>();
		Q_COUNT = Q_COUNT_BACKUP;
		
		try {
			// Deserialize the file as a whole
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					uploadQueueFile));
			
			// Deserialize the objects one by one
			for (int i = 0; i < Q_COUNT; i++) {
				DataSet dataSet = (DataSet) in.readObject();
				queue.add(dataSet);
			}
			in.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (rebuildMirrorQueue) {
			mirrorQueue.clear();
			mirrorQueue.addAll(queue);
		}
	}
	
	public void storeQueueOnPause() {
		Queue<DataSet> backupQueue = new LinkedList<DataSet>();
		backupQueue.addAll(queue);
		
		// save Q_COUNT in SharedPrefs
		final SharedPreferences mPrefs = mContext.getSharedPreferences(
				parentName, Context.MODE_PRIVATE);
		final SharedPreferences.Editor mPrefsEditor = mPrefs.edit();
		int Q_COUNT = backupQueue.size();
		mPrefsEditor.putInt(parentName + "Q_COUNT", Q_COUNT);
		mPrefsEditor.commit();
		
		// obtain storage directory and file for the uploadqueue
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");
		
		if (!folder.exists()) {
			folder.mkdir();
		}

		File uploadQueueFile = new File(folder.getAbsolutePath() + "/"
				+ parentName + ".ser");

		// writes the queue to a serializable file
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(uploadQueueFile));
			
			// serializes DataSets
			while (Q_COUNT > 0) {
				DataSet ds = backupQueue.remove();
				out.writeObject(ds);
				Q_COUNT--;
			}

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean buildQueueFromFile() {
		
		// reset the queues but save a backup
		Queue<DataSet> backupQueue = new LinkedList<DataSet>();
		backupQueue.addAll(queue);
		queue       = new LinkedList<DataSet>();
		mirrorQueue = new LinkedList<DataSet>();
		
		// get Q_COUNT from the SharedPrefs
		final SharedPreferences mPrefs = mContext.getSharedPreferences(
				parentName, Context.MODE_PRIVATE);
		int Q_COUNT = mPrefs.getInt(parentName + "Q_COUNT", -1);
		if (Q_COUNT == -1) {
			queue.addAll(backupQueue);
			mirrorQueue.addAll(backupQueue);
			return false;
		}

		// obtain storage directory and file for the uploadqueue
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");
		if (!folder.exists()) {
			queue.addAll(backupQueue);
			mirrorQueue.addAll(backupQueue);
			return false;
		}
		
		File uploadQueueFile = new File(folder.getAbsolutePath() + "/"
				+ parentName + ".ser");
		
		try {
			// Deserialize the file as a whole
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					uploadQueueFile));
			
			// Deserialize the objects one by one
			for (int i = 0; i < Q_COUNT; i++) {
				DataSet dataSet = (DataSet) in.readObject();
				queue.add(dataSet);
			}
			in.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			queue.addAll(backupQueue);
			mirrorQueue.addAll(backupQueue);
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			queue.addAll(backupQueue);
			mirrorQueue.addAll(backupQueue);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			queue.addAll(backupQueue);
			mirrorQueue.addAll(backupQueue);
			return false;
		}
		
		mirrorQueue.addAll(queue);
		return true;
	}

	public static RestAPI getRapi() {
		return rapi;
	}
	
	public boolean emptyQueue() {
		return (queue.size() == 0);
	}

	public int queueSize() {
		return (queue.size());
	}
	
}
