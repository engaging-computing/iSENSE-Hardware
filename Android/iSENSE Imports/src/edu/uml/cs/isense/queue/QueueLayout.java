package edu.uml.cs.isense.queue;

import java.util.LinkedList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * Activity that displays the list of data sets stored in the
 * data saving queue.  From here, the user can check and uncheck data
 * sets to upload, rename them, change their data, delete them,
 * or attempt to upload them to iSENSE.
 * 
 * @author Jeremy Poulin and Mike Stowell of the iSENSE team.
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

	private static final int ALTER_DATASET_REQUESTED   = 9001;
	private static final int ALTER_DATA_NAME_REQUESTED = 9002;
	private static final int ALTER_DATA_DATA_REQUESTED = 9003;
	private static final int ALTER_DATA_EXP_REQUESTED  = 9004;

	private static int QUEUE_PARENT = -1;

	protected static int lastSID = -1;

	private static Context mContext;
	private static LinearLayout scrollQueue;
	private Runnable sdUploader;
	private static UploadQueue uq;
	private boolean uploadSuccess = true;
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

		scrollQueue = (LinearLayout) findViewById(R.id.scrollqueue);
		fillScrollQueue();
	}

	// Works through list of data to be uploaded and creates the list of blocks
	private void fillScrollQueue() {

		for (final QDataSet ds : uq.mirrorQueue)
			addViewToScrollQueue(ds);

	}

	// Adds empty space after experiment groups
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
		CheckedTextView ctv = (CheckedTextView) view.findViewById(R.id.name);
		ctv.setText(ds.getName() + " - " + ds.getType());

		TextView eid = (TextView) view.findViewById(R.id.experimentid);
		if (ds.getEID().equals("-1"))
			eid.setText("No Exp.");
		else
			eid.setText(ds.getEID());

		TextView desc = (TextView) view.findViewById(R.id.description);
		desc.setText(ds.getDesc());
	}

	/**
	 * This method was made public by Android.  Don't worry about it.
	 */
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.upload) {
				
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
			} else
				new UploadSDTask().execute();
			uq.queue = new LinkedList<QDataSet>();
		} else if (id == R.id.cancel) {
			setResultAndFinish(RESULT_CANCELED);
			finish();
		}
	}

	private void setResultAndFinish(int result_code) {
		if (result_code == RESULT_OK) {
			uq.storeAndReRetrieveQueue(true);
			setResult(RESULT_OK);
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
				w.make("\"" + uploadSet.getName() + "\" chosen not to upload", Waffle.LENGTH_SHORT);
				uq.queue.add(uploadSet);
				uq.storeAndReRetrieveQueue(false);
			} else if (uploadSuccess)
				w.make("Upload success for \"" + uploadSet.getName() + "\"", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
			else {
				w.make("Upload failed - no experiment selected or experiment is closed", Waffle.LENGTH_LONG, Waffle.IMAGE_X);
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
					uploadSuccess = ds.upload(api, mContext);
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
					
				case QueueAlter.SELECT_EXPERIMENT:
					
					Intent iExp = new Intent(mContext, Setup.class);
					iExp.putExtra("from_where", "queue");
					startActivityForResult(iExp, ALTER_DATA_EXP_REQUESTED);
					
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
		} else if (requestCode == ALTER_DATA_EXP_REQUESTED) {
			if (resultCode == RESULT_OK) {
				SharedPreferences mPrefs = getSharedPreferences("EID_QUEUE", 0);
				
				QDataSet alter = lastDataSetLongClicked;
				alter.setExp(mPrefs.getString("experiment_id", "No Exp."));
				
				uq.removeItemWithKey(lastDataSetLongClicked.key);
				scrollQueue.removeView(lastViewLongClicked);
				
				uq.addDataSetToQueue(alter);
				addViewToScrollQueue(alter);
				
			}
		}

	}

	private void addViewToScrollQueue(final QDataSet ds) {

		String previous = "";
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutParams.setMargins(1, 1, 1, 1);

		switch (ds.type) {
		case DATA:

			final View data = View.inflate(mContext, R.layout.queueblock_data,
					null);

			data.setBackgroundResource(R.drawable.listelement_bkgd_changer);

			makeBlock(data, ds);
			previous = checkPrevious(previous, scrollQueue,
					(String) ds.getName());

			scrollQueue.addView(data, layoutParams);
			ds.setUploadable(true);

			data.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					CheckedTextView ctv = (CheckedTextView) v
							.findViewById(R.id.name);
					ctv.toggle();

					if (ctv.isChecked())
						ctv.setCheckMarkDrawable(R.drawable.checkmark);
					else
						ctv.setCheckMarkDrawable(R.drawable.red_x);

					ds.setUploadable(ctv.isChecked());

				}

			});

			data.setOnLongClickListener(new OnLongClickListener() {

				public boolean onLongClick(View v) {
					lastDataSetLongClicked = ds;
					lastViewLongClicked = data;
					boolean isFromDataCollector = (parentName.equals("datacollector"));
					Intent iAlterDataSet = new Intent(mContext,
							QueueAlter.class);
					iAlterDataSet.putExtra(QueueAlter.IS_ALTERABLE, !isFromDataCollector);
					iAlterDataSet.putExtra(QueueAlter.SELECT_EXP, !lastDataSetLongClicked.getHasInitialExperiment());
					startActivityForResult(iAlterDataSet,
							ALTER_DATASET_REQUESTED);
					return false;
				}

			});

			break;

		case PIC:

			final View pic = View.inflate(mContext, R.layout.queueblock_pic,
					null);

			pic.setBackgroundResource(R.drawable.listelement_bkgd_changer);

			makeBlock(pic, ds);
			previous = checkPrevious(previous, scrollQueue,
					(String) ds.getName());

			scrollQueue.addView(pic, layoutParams);
			ds.setUploadable(true);

			pic.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					CheckedTextView ctv = (CheckedTextView) v
							.findViewById(R.id.name);
					ctv.toggle();

					if (ctv.isChecked())
						ctv.setCheckMarkDrawable(R.drawable.checkmark);
					else
						ctv.setCheckMarkDrawable(R.drawable.red_x);

					ds.setUploadable(ctv.isChecked());

				}

			});

			pic.setOnLongClickListener(new OnLongClickListener() {

				public boolean onLongClick(View v) {
					lastDataSetLongClicked = ds;
					lastViewLongClicked = pic;
					Intent iAlterDataSet = new Intent(mContext,
							QueueAlter.class);
					iAlterDataSet.putExtra(QueueAlter.IS_ALTERABLE, false);
					iAlterDataSet.putExtra(QueueAlter.SELECT_EXP, false);
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

			both.setBackgroundResource(R.drawable.listelement_bkgd_changer);

			makeBlock(both, ds);
			previous = checkPrevious(previous, scrollQueue,
					(String) ds.getName());

			scrollQueue.addView(both, layoutParams);
			ds.setUploadable(true);

			both.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					CheckedTextView ctv = (CheckedTextView) v
							.findViewById(R.id.name);
					ctv.toggle();

					if (ctv.isChecked())
						ctv.setCheckMarkDrawable(R.drawable.checkmark);
					else
						ctv.setCheckMarkDrawable(R.drawable.red_x);

					ds.setUploadable(ctv.isChecked());

				}

			});

			both.setOnLongClickListener(new OnLongClickListener() {

				public boolean onLongClick(View v) {
					lastDataSetLongClicked = ds;
					lastViewLongClicked = both;
					Intent iAlterDataSet = new Intent(mContext,
							QueueAlter.class);
					iAlterDataSet.putExtra(QueueAlter.IS_ALTERABLE, false);
					iAlterDataSet.putExtra(QueueAlter.SELECT_EXP, lastDataSetLongClicked.getEID().equals("-1"));
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
