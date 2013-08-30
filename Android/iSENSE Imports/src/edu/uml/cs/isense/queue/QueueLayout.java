package edu.uml.cs.isense.queue;

import java.util.LinkedList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * Activity that displays the list of data sets stored in the
 * data saving queue.  From here, the user can check and uncheck data
 * sets to upload, rename them, change their data, delete them,
 * or attempt to upload them to iSENSE.
 * 
 * @author Mike Stowell and Jeremy Poulin of the iSENSE team.
 *
 */
public class QueueLayout extends Activity implements OnClickListener {

	/**
	 * Global string constant that the user should use to pass in the 
	 * parent name of their activity when using an intent to launch
	 * QueueLayout.  QueueLayout will not display if a parent name is
	 * not passed into it.  This parent name is used to create a 
	 * serializable file with the same name as the string passed to it.
	 */
	public static final String PARENT_NAME = "parentName";
	public static final String LOGIN_CONTEXT ="logincontext";

	private static final int ALTER_DATASET_REQUESTED   		 = 9001;
	private static final int ALTER_DATA_NAME_REQUESTED 		 = 9002;
	private static final int ALTER_DATA_DATA_REQUESTED 	 	 = 9003;
	private static final int ALTER_DATA_PROJ_REQUESTED 		 = 9004;
	private static final int QUEUE_DELETE_SELECTED_REQUESTED = 9005;
	
	private static final int QUEUE_BOX_DESELECTED = 0;
	private static final int QUEUE_BOX_SELECTED   = 1;
	
	public static final String LAST_UPLOADED_DATA_SET_ID = "lastuploadeddatasetid";

	private static int QUEUE_PARENT = -1;

	protected static int lastSID = -1;

	private static Context mContext;
	private static LinearLayout scrollQueue;
	private Runnable sdUploader;
	private static UploadQueue uq;
	private int dataSetID = -1;
	private static String parentName = "";

	protected static QDataSet lastDataSetLongClicked;
	private View lastViewLongClicked;
	private Waffle w;
	private API api;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.queueprompt);

		mContext = this;
		w = new Waffle(mContext);

		api = API.getInstance(mContext);

		Bundle extras = getIntent().getExtras();
		parentName = extras.getString(PARENT_NAME);
		if (parentName == null || parentName.equals("")) {
			w.make("Parent name not passed!", Waffle.IMAGE_X);
		}

		uq = new UploadQueue(parentName, mContext, api);
		boolean success = uq.buildQueueFromFile();

		if (uq == null || !success) {
			w.make("Queue not built properly.", Waffle.IMAGE_X);
		}

		Button upload = (Button) findViewById(R.id.upload);
		upload.setOnClickListener(this);

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
		
		Button select = (Button) findViewById(R.id.select_deselect_all);
		select.setOnClickListener(this);
		
		Button delete = (Button) findViewById(R.id.delete_selected);
		delete.setOnClickListener(this);

		scrollQueue = (LinearLayout) findViewById(R.id.scrollqueue);
		fillScrollQueue();
	}

	// Works through list of data to be uploaded and creates the list of blocks
	private void fillScrollQueue() {

		for (final QDataSet ds : uq.mirrorQueue)
			addViewToScrollQueue(ds);

	}

	// Adds empty space after project groups
	private String checkPrevious(String previous, LinearLayout scrollQueue,
			String ds) {

		LinearLayout space = new LinearLayout(mContext);
		space.setPadding(0, 10, 0, 10);

		if ((!previous.equals(ds)) && (!previous.equals("")))
			scrollQueue.addView(space);

		return ds;
	}

	// Fills the text fields in the list element blocks
	private void makeBlock(View view, QDataSet ds) {
		TextView tv = (TextView) view.findViewById(R.id.name);
		tv.setText(ds.getName());

		TextView projIDText = (TextView) view.findViewById(R.id.project_id);
		if (ds.getProjID().equals("-1"))
			projIDText.setText("No Project");
		else
			projIDText.setText("Project: " + ds.getProjID());
		
		TextView uploadType = (TextView) view.findViewById(R.id.upload_type);
		uploadType.setText("Type: " + ds.getType());

		TextView desc = (TextView) view.findViewById(R.id.description);
		desc.setText(ds.getDesc());
	}

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.upload) {
			
			if (allSelectedDataSetsHaveProjects()) {
				if (!api.hasConnectivity()) {
					w.make("No internet connection found", Waffle.IMAGE_X);
					return;
				}
				
				if (api.getCurrentUser() == null) {
					w.make("Login information not found - please login again", Waffle.IMAGE_X);
					return;
				}
				
				lastSID = -1;
				if (uq.mirrorQueue.isEmpty()) {
					uq.storeAndReRetrieveQueue(true);
					setResultAndFinish(RESULT_OK);
					return;
				} else {
					new UploadSDTask().execute();
					// clear the queue so we can re-add un-uploaded data sets from the mirrorQueue
					uq.queue = new LinkedList<QDataSet>();
				}
			} else {
				Intent iNoInitialProject = new Intent(QueueLayout.this, NoInitialProject.class);
				startActivity(iNoInitialProject);
			}
				
		} else if (id == R.id.cancel) {
			setResultAndFinish(RESULT_CANCELED);
			finish();
		} else if (id == R.id.select_deselect_all) {
			Button select = (Button) findViewById(R.id.select_deselect_all);
			if (select.getText().equals(getResources().getString(R.string.select_all))) {
				select.setText(getResources().getString(R.string.deselect_all));
				for (int i = 0; i < scrollQueue.getChildCount(); i++) {
					View view = scrollQueue.getChildAt(i);
					if (view.getTag() == Integer.valueOf(QUEUE_BOX_DESELECTED)) {
						view.performClick();
					}
				}
			} else {
				select.setText(getResources().getString(R.string.select_all));
				for (int i = 0; i < scrollQueue.getChildCount(); i++) {
					View view = scrollQueue.getChildAt(i);
					if (view.getTag() == Integer.valueOf(QUEUE_BOX_SELECTED)) {
						view.performClick();
					}
				}
			}
			
		} else if (id == R.id.delete_selected) {
			Intent iDelSel = new Intent(QueueLayout.this, QueueDeleteSelected.class);
			startActivityForResult(iDelSel, QUEUE_DELETE_SELECTED_REQUESTED);
		}
		
	}
	
	private boolean allSelectedDataSetsHaveProjects() {
		for (QDataSet qds : uq.queue) {
			if (qds.isUploadable() && qds.getProjID().equals("-1")) {
				return false;
			}
		}
		return true;
	}

	private void setResultAndFinish(int result_code) {
		if (result_code == RESULT_OK) {
			uq.storeAndReRetrieveQueue(true);
			Intent iRet = new Intent();
			iRet.putExtra(LAST_UPLOADED_DATA_SET_ID, dataSetID);
			setResult(RESULT_OK, iRet);
		} else {
			setResult(RESULT_CANCELED);
		}

		finish();
	}

	// Control task for uploading data from SD card
	private class UploadSDTask extends AsyncTask<Void, Integer, Void> {

		boolean dialogShow = true;
		ProgressDialog dia = null;
		QDataSet uploadSet;
		boolean doThings = true;

		@Override
		protected void onPreExecute() {

			uploadSet = uq.mirrorQueue.remove();
			if (!uploadSet.isUploadable())
				doThings = false;
			
			createRunnable(uploadSet);

			OrientationManager.disableRotation(QueueLayout.this);

			dia = new ProgressDialog(QueueLayout.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while \"" + uploadSet.getName() + "\" is uploaded...");
			dia.setCancelable(false);
			try {
				dia.show();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				dialogShow = false;
			}

		}

		@Override
		protected Void doInBackground(Void... voids) {
			if (doThings)
				sdUploader.run();
			
			if (dia != null)
				dia.setProgress(100);
			
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			Waffle w = new Waffle(mContext);

			if (!doThings) {
				w.make("\"" + uploadSet.getName() + "\" chosen not to upload", Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);
				uq.queue.add(uploadSet);
				uq.storeAndReRetrieveQueue(false);
			} else if (dataSetID != -1)
				w.make("Upload success for \"" + uploadSet.getName() + "\"", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
			else if (uploadSet.getProjID().equals("-1")) {
				w.make("Select a project first for \"" + uploadSet.getName() + "\"", Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				uq.queue.add(uploadSet);
				uq.storeAndReRetrieveQueue(false);
			} else {
				w.make("Upload failed - project is closed, deleted, or contains broken data sets", Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				uq.queue.add(uploadSet);
				uq.storeAndReRetrieveQueue(false);
			}

			if (dialogShow && dia != null)
				dia.dismiss();

			if (uq.mirrorQueue.isEmpty()) {
				uq.storeAndReRetrieveQueue(true);
				setResultAndFinish(RESULT_OK);
				return;
			} else {
				//if (uploadSuccess)
				continueUploading();
			}
			
			OrientationManager.enableRotation(QueueLayout.this);
		}
	}

	// Create an uploader particular to the DataSet to be uploaded in the queue
	private void createRunnable(final QDataSet ds) {
		sdUploader = new Runnable() {

			public void run() {
				if (ds.isUploadable()) {
					dataSetID = ds.upload(api, mContext);
				} else {
					uq.queue.add(ds);
					uq.storeAndReRetrieveQueue(false);
				}
			}

		};

	}

	// Calls the next upload task if there are more DataSets in the queue
	private void continueUploading() {
		new UploadSDTask().execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == ALTER_DATASET_REQUESTED) {
			if (resultCode == RESULT_OK) {

				int returnCode = data.getIntExtra(QueueAlter.RETURN_CODE, -1);
				
				switch (returnCode) {

				case QueueAlter.RENAME:
					
					Intent iRename = new Intent(mContext, QueueEditRename.class);
					startActivityForResult(iRename, ALTER_DATA_NAME_REQUESTED);

					break;

				case QueueAlter.CHANGE_DATA:

					Intent iData = new Intent(mContext, QueueEditData.class);
					startActivityForResult(iData, ALTER_DATA_DATA_REQUESTED);

					break;
					
				case QueueAlter.SELECT_PROJECT:
					
					Intent iProj = new Intent(mContext, Setup.class);
					iProj.putExtra("from_where", "queue");
					startActivityForResult(iProj, ALTER_DATA_PROJ_REQUESTED);
					
					break;

				case QueueAlter.DELETE:
					
					uq.removeItemWithKey(lastDataSetLongClicked.key);
					scrollQueue.removeView(lastViewLongClicked);

					break;

				default:
					w.make("Could not process request.", Waffle.IMAGE_X);
					break;
				}
			}
		} else if (requestCode == ALTER_DATA_NAME_REQUESTED) {
			if (resultCode == RESULT_OK) {

				String newName = data.getStringExtra("new_name");
				if (!newName.equals("")) {
					QDataSet alter = lastDataSetLongClicked;
					alter.setName(newName); 

					uq.removeItemWithKey(lastDataSetLongClicked.key);
					scrollQueue.removeView(lastViewLongClicked);

					uq.addDataSetToQueue(alter);
					addViewToScrollQueue(alter);

				}
			}
		} else if (requestCode == ALTER_DATA_DATA_REQUESTED) {
			if (resultCode == RESULT_OK) {

				QDataSet alter = QueueEditData.alter;
			
				uq.removeItemWithKey(lastDataSetLongClicked.key);
				scrollQueue.removeView(lastViewLongClicked);
				
				uq.addDataSetToQueue(alter);
				addViewToScrollQueue(alter);
				
			}
		} else if (requestCode == ALTER_DATA_PROJ_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences mPrefs = getSharedPreferences("PROJID_QUEUE", 0);
				
				QDataSet alter = lastDataSetLongClicked;
				alter.setProj(mPrefs.getString("project_id", "No Proj."));
				
				uq.removeItemWithKey(lastDataSetLongClicked.key);
				scrollQueue.removeView(lastViewLongClicked);
				
				uq.addDataSetToQueue(alter);
				addViewToScrollQueue(alter);
				
			}
		} else if (requestCode == QUEUE_DELETE_SELECTED_REQUESTED) {
			if (resultCode == RESULT_OK) {
				int count = scrollQueue.getChildCount();
				int childIndex = 0;
				for (int i = 0; i < count; i++) {
					View view = scrollQueue.getChildAt(childIndex);
					if (view.getTag() == Integer.valueOf(QUEUE_BOX_SELECTED)) {
						long dataSetKey = Long.parseLong("" + view.getContentDescription());
						uq.removeItemWithKey(dataSetKey);
						scrollQueue.removeView(view);
					} else
						++childIndex;
				}
			}
		}

	}

	// Adds ds to the scrollQueue object
	// Each block in the scrollQueue gets 2 additional (and confusing) properties:
	//		tag: the selection state of the queue block (QUEUE_BOX_SELECTED or QUEUE_BOX_DESELECTED)
	//		content description: the key of the associated data set
	private void addViewToScrollQueue(final QDataSet ds) {

		String previous = "";
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutParams.setMargins(5, 5, 5, 5);

		switch (ds.type) {
		case DATA:

			final View data = View.inflate(mContext, R.layout.queueblock_data,
					null);

			data.setBackgroundResource(R.drawable.listelement_bkgd_changer_selected);

			makeBlock(data, ds);
			previous = checkPrevious(previous, scrollQueue,
					(String) ds.getName());

			scrollQueue.addView(data, layoutParams);
			ds.setUploadable(true);
			data.setContentDescription("" + ds.key);
			data.setTag(QUEUE_BOX_SELECTED);
			
			data.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					if (ds.isUploadable()) {
						data.setBackgroundResource(R.drawable.listelement_bkgd_changer);
						ds.setUploadable(false);
						data.setTag(QUEUE_BOX_DESELECTED);
					} else {
						data.setBackgroundResource(R.drawable.listelement_bkgd_changer_selected);
						ds.setUploadable(true);
						data.setTag(QUEUE_BOX_SELECTED);
					}

				}

			});

			data.setOnLongClickListener(new OnLongClickListener() {

				public boolean onLongClick(View v) {
					lastDataSetLongClicked = ds;
					lastViewLongClicked = data;
					boolean isFromDataCollector = (parentName.equals("datacollector")) || (parentName.equals("carrampphysics"))
							|| (parentName.equals("data_walk"));
					Intent iAlterDataSet = new Intent(mContext,
							QueueAlter.class);
					iAlterDataSet.putExtra(QueueAlter.IS_ALTERABLE, !isFromDataCollector);
					iAlterDataSet.putExtra(QueueAlter.SELECT_PROJ, !lastDataSetLongClicked.getHasInitialProject());
					startActivityForResult(iAlterDataSet,
							ALTER_DATASET_REQUESTED);
					return false;
				}

			});

			break;

		case PIC:

			final View pic = View.inflate(mContext, R.layout.queueblock_pic,
					null);

			pic.setBackgroundResource(R.drawable.listelement_bkgd_changer_selected);

			makeBlock(pic, ds);
			previous = checkPrevious(previous, scrollQueue,
					(String) ds.getName());

			scrollQueue.addView(pic, layoutParams);
			ds.setUploadable(true);
			pic.setContentDescription("" + ds.key);
			pic.setTag(QUEUE_BOX_SELECTED);

			pic.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					if (ds.isUploadable()) {
						pic.setBackgroundResource(R.drawable.listelement_bkgd_changer);
						ds.setUploadable(false);
						pic.setTag(QUEUE_BOX_DESELECTED);
					} else {
						pic.setBackgroundResource(R.drawable.listelement_bkgd_changer_selected);
						ds.setUploadable(true);
						pic.setTag(QUEUE_BOX_SELECTED);
					}

				}

			});

			pic.setOnLongClickListener(new OnLongClickListener() {

				public boolean onLongClick(View v) {
					lastDataSetLongClicked = ds;
					lastViewLongClicked = pic;
					Intent iAlterDataSet = new Intent(mContext,
							QueueAlter.class);
					iAlterDataSet.putExtra(QueueAlter.IS_ALTERABLE, false);
					iAlterDataSet.putExtra(QueueAlter.SELECT_PROJ, false);
					iAlterDataSet.putExtra("parent", QUEUE_PARENT);
					startActivityForResult(iAlterDataSet,
							ALTER_DATASET_REQUESTED);
					return false;
				}

			});

			break;
			
		case BOTH:
			
			final View both = View.inflate(mContext, R.layout.queueblock_pic,
					null);

			both.setBackgroundResource(R.drawable.listelement_bkgd_changer_selected);

			makeBlock(both, ds);
			previous = checkPrevious(previous, scrollQueue,
					(String) ds.getName());

			scrollQueue.addView(both, layoutParams);
			ds.setUploadable(true);
			both.setContentDescription("" + ds.key);
			both.setTag(QUEUE_BOX_SELECTED);

			both.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					if (ds.isUploadable()) {
						both.setBackgroundResource(R.drawable.listelement_bkgd_changer);
						ds.setUploadable(false);
						both.setTag(QUEUE_BOX_DESELECTED);
					} else {
						both.setBackgroundResource(R.drawable.listelement_bkgd_changer_selected);
						ds.setUploadable(true);
						both.setTag(QUEUE_BOX_SELECTED);
					}

				}

			});

			both.setOnLongClickListener(new OnLongClickListener() {

				public boolean onLongClick(View v) {
					lastDataSetLongClicked = ds;
					lastViewLongClicked = both;
					Intent iAlterDataSet = new Intent(mContext,
							QueueAlter.class);
					iAlterDataSet.putExtra(QueueAlter.IS_ALTERABLE, false);
					iAlterDataSet.putExtra(QueueAlter.SELECT_PROJ, lastDataSetLongClicked.getProjID().equals("-1"));
					iAlterDataSet.putExtra("parent", QUEUE_PARENT);
					startActivityForResult(iAlterDataSet,
							ALTER_DATASET_REQUESTED);
					return false;
				}

			});
			
			break;
		}

	}

}
