/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII               iSENSE CSV Uploader App                     SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                    By: Michael Stowell,                      SSS                **/
/**           III                        Jeremy Poulin                          SSS               **/
/**           III                                                                SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                    Group:            ECG,                              SSS      **/
/**           III                                      iSENSE                           SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell              SSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/

package edu.uml.cs.isense.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.csv.experiment.Experiment;
import edu.uml.cs.isense.csv.fails.SdCardFailure;
import edu.uml.cs.isense.csv.login.LoginActivity;
import edu.uml.cs.isense.csv.objects.DataFieldManager;
import edu.uml.cs.isense.csv.objects.Options;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.waffle.Waffle;

public class Main extends Activity {

	private enum SlideType {
		NO_SLIDE, SLIDE_LEFT, SLIDE_RIGHT,
	}

	private static String rootDirectory;
	private static String previousDirectory;
	private static String currentDirectory;

	private static String PREV_DIR = "previousDirectory";
	private static String CURR_DIR = "currentDirectory";
	public static String CHOSEN_FILE = "chosenFile";

	private static final String baseUrl = "http://isensedev.cs.uml.edu/experiment.php?id=";

	private Vibrator vibrator;
	private TextView loginInfo;
	private TextView experimentLabel;
	private Button refresh;
	private Button upload;
	private TextView noData;
	private TextView curDir;

	private static final int LOGIN_REQUESTED = 100;
	private static final int VIEW_DATA_REQUESTED = 101;
	private static final int EXPERIMENT_REQUESTED = 102;

	private RestAPI rapi;
	private Waffle w;
	private DataFieldManager dfm;

	private ProgressDialog dia;

	private static boolean clickEnabled = true;
	private static boolean usingDataView1 = false;

	private static ArrayList<Boolean> uploadSuccessArray;

	public JSONArray data;

	public static Context mContext;

	private LinearLayout dataView;
	private LinearLayout dataView2;
	private ScrollView scrollWrapper;
	private ScrollView scrollWrapper2;
	private ArrayList<File> checkedFiles;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = this;
		w = new Waffle(mContext);

		mContext = this;
		w = new Waffle(mContext);

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(true);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		checkedFiles = new ArrayList<File>();

		final SharedPreferences mUserPrefs = new ObscuredSharedPreferences(
				Main.mContext, Main.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));
		final SharedPreferences mExpPrefs = getSharedPreferences("eid", 0);

		String loginName = mUserPrefs.getString("username", "");
		if (loginName.length() >= 15)
			loginName = loginName.substring(0, 15) + "...";
		loginInfo = (TextView) findViewById(R.id.loginLabel);
		loginInfo.setText(getResources().getString(R.string.loggedInAs) + " "
				+ loginName);

		experimentLabel = (TextView) findViewById(R.id.experimentLabel);
		experimentLabel.setText(getResources().getString(
				R.string.usingExperiment)
				+ " " + mExpPrefs.getString("eid", ""));

		noData = (TextView) findViewById(R.id.noItems);

		curDir = (TextView) findViewById(R.id.cur_dir);

		dataView = (LinearLayout) findViewById(R.id.dataView);
		dataView2 = (LinearLayout) findViewById(R.id.dataView2);
		scrollWrapper = (ScrollView) findViewById(R.id.scrollWrapper);
		scrollWrapper.setVisibility(View.GONE);
		scrollWrapper2 = (ScrollView) findViewById(R.id.scrollWrapper2);
		scrollWrapper2.setVisibility(View.VISIBLE);

		rootDirectory = "/mnt";

		/* Restore current view upon rotation, or initialize the view to /mnt */
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(PREV_DIR)
					&& savedInstanceState.containsKey(CURR_DIR)) {
				previousDirectory = savedInstanceState.getString(PREV_DIR);
				currentDirectory = savedInstanceState.getString(CURR_DIR);
			} else {
				previousDirectory = rootDirectory;
				currentDirectory = rootDirectory;
			}
		} else {
			previousDirectory = rootDirectory;
			currentDirectory = rootDirectory;
		}

		refresh = (Button) findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				boolean success;
				try {
					success = getFiles(new File(currentDirectory),
							activeLayout());
				} catch (Exception e) {
					w.make(e.toString(), Waffle.IMAGE_X);
					success = false;
				}
				didGetFiles(success, true, SlideType.NO_SLIDE);
			}
		});

		upload = (Button) findViewById(R.id.upload);
		upload.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				boolean canUpload = true;

				if (checkedFiles.size() == 0) {
					w.make("No files checked to upload.", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
					canUpload = false;
				}

				final SharedPreferences mUserPrefs = new ObscuredSharedPreferences(
						Main.mContext, Main.mContext.getSharedPreferences(
								"USER_INFO", Context.MODE_PRIVATE));
				String loginName = mUserPrefs.getString("username", "");
				String loginPass = mUserPrefs.getString("password", "");

				if (!rapi.isLoggedIn()) {
					boolean success = rapi.login(loginName, loginPass);
					if (!success) {
						canUpload = false;
						w.make("Cannot upload data until logged in.",
								Waffle.LENGTH_LONG, Waffle.IMAGE_X);
					}
				}

				final SharedPreferences mExpPrefs = getSharedPreferences("eid",
						0);
				String eid = mExpPrefs.getString("eid", "");
				if (eid.equals("")) {
					canUpload = false;
					w.make("Cannot upload data until a valid experiment is selected.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_X);
				}

				if (canUpload)
					new UploadTask().execute();
			}
		});
	}

	private String getUploadTime() {
		Calendar c = Calendar.getInstance();
		long time = c.getTimeInMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss, MM/dd/yyyy",
				Locale.US);
		return sdf.format(time);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		login();

		boolean success;
		try {
			success = getFiles(new File(currentDirectory), activeLayout());
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		didGetFiles(success, true, SlideType.NO_SLIDE);

		Intent i = getIntent();
		Bundle b = i.getExtras();

		if (b != null) {
			if (b.containsKey(CHOSEN_FILE)) {
				Log.e("CSV", "found chosen_file key");
				String chosenFile = b.getString(CHOSEN_FILE);
				int nameStartPoint = chosenFile.lastIndexOf("/");
				String directoryName = chosenFile.substring(0, nameStartPoint);
				Log.e("CSV", chosenFile.substring(nameStartPoint));
				String specificFileName = chosenFile.substring(nameStartPoint);

				try {
					success = getFiles(new File(directoryName), activeLayout());
				} catch (Exception e) {
					e.printStackTrace();
					success = false;
				}
				didGetFiles(success, true, SlideType.NO_SLIDE);

				for (int j = 0; j < activeLayout().getChildCount(); j++) {
					CheckedTextView ctv = (CheckedTextView) activeLayout()
							.getChildAt(j);
					if (ctv.getText().equals(specificFileName)) {
						ctv.callOnClick();
						break;
					}
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (clickEnabled) {
			if (currentDirectory.equals(rootDirectory)) {
				super.onBackPressed();
			} else
				try {
					boolean success = getFiles(new File(previousDirectory),
							backgroundLayout());
					didGetFiles(success, false, SlideType.SLIDE_RIGHT);
					currentDirectory = previousDirectory;
					previousDirectory = getParentDir(currentDirectory);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(CURR_DIR, currentDirectory);
		outState.putString(PREV_DIR, previousDirectory);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_options:
			Intent iOptions = new Intent(mContext, Options.class);
			startActivity(iOptions);
			return true;

		case R.id.menu_item_experiment:
			Intent iExperiment = new Intent(mContext, Experiment.class);
			startActivityForResult(iExperiment, EXPERIMENT_REQUESTED);
			return true;

		case R.id.menu_item_login:
			Intent iLogin = new Intent(mContext, LoginActivity.class);
			startActivityForResult(iLogin, LOGIN_REQUESTED);
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
	}

	static int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String returnCode = data.getStringExtra("returnCode");

				if (returnCode.equals("Success")) {
					final SharedPreferences mPrefs = new ObscuredSharedPreferences(
							Main.mContext, Main.mContext.getSharedPreferences(
									"USER_INFO", Context.MODE_PRIVATE));
					String loginName = mPrefs.getString("username", "");
					if (loginName.length() >= 18)
						loginName = loginName.substring(0, 18) + "...";
					loginInfo.setText(getResources().getString(
							R.string.loggedInAs)
							+ " " + loginName);
					w.make("Login successful.", Waffle.LENGTH_SHORT,
							Waffle.IMAGE_CHECK);
				} else if (returnCode.equals("Failed")) {
					Intent i = new Intent(mContext, LoginActivity.class);
					startActivityForResult(i, LOGIN_REQUESTED);
				} else {
					// should never get here
				}

			}

		} else if (requestCode == VIEW_DATA_REQUESTED) {
			if (resultCode == RESULT_OK) {
				final SharedPreferences mPrefs = getSharedPreferences("eid", 0);
				Intent iUrl = new Intent(Intent.ACTION_VIEW);
				iUrl.setData(Uri.parse(baseUrl + mPrefs.getString("eid", "-1")));
				Intent chooser = Intent.createChooser(iUrl,
						"Select a browser to view your data on:");
				startActivity(chooser);
			}

		} else if (requestCode == EXPERIMENT_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String eid = data.getStringExtra("eid");
				final SharedPreferences mPrefs = getSharedPreferences("eid", 0);
				final SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putString("eid", eid).commit();
				experimentLabel.setText(getResources().getString(
						R.string.usingExperiment)
						+ " " + eid);
			}
		}
	}

	private Runnable uploader = new Runnable() {

		public void run() {

			// saves boolean results for which uploads succeed
			uploadSuccessArray = new ArrayList<Boolean>();

			// Do rapi uploading stuff
			for (File f : checkedFiles) {
				boolean success = uploadFile(f);
				uploadSuccessArray.add(success);
			}
		}
	};

	private class UploadTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			vibrator.vibrate(250);
			dia = new ProgressDialog(Main.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Uploading .csv files to iSENSE...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected Void doInBackground(Void... voids) {

			uploader.run();

			publishProgress(100);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {

			dia.setMessage("Done");
			dia.cancel();

			// Checks upload success
			boolean totalSuccess = true;
			int i = 0;
			String error = "Failed to upload: ";
			String errorFiles = "";
			for (boolean success : uploadSuccessArray) {
				if (success == false) {
					totalSuccess = false;
					if (errorFiles.isEmpty())
						errorFiles += checkedFiles.get(i).getName();
					else
						errorFiles += ", " + checkedFiles.get(i).getName();
				}
				i++;
			}
			error += errorFiles + ".";
			if (totalSuccess)
				w.make("Upload successful!", Waffle.LENGTH_LONG,
						Waffle.IMAGE_CHECK);
			else
				w.make(error, Waffle.LENGTH_LONG, Waffle.IMAGE_X);

			if (!(uploadSuccessArray.size() <= 1 && totalSuccess == false)) {
				Intent iView = new Intent(mContext, ViewData.class);
				startActivityForResult(iView, VIEW_DATA_REQUESTED);
			}
		}
	}

	private boolean getFiles(File dir, final LinearLayout dv) throws Exception {

		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			throw new Exception("Cannot Access External Storage.");
		}

		File[] files = dir.listFiles();
		if (files == null)
			return false;
		else {
			dv.removeAllViews();

			if (files.length == 0) {
				final TextView noData = new TextView(mContext);
				noData.setText("No files or directories.");
				noData.setPadding(10, 10, 10, 10);
				dv.addView(noData);
			}

			for (int i = 0; i < files.length; i++) {

				final CheckedTextView ctv = new CheckedTextView(mContext);
				ctv.setBackgroundResource(R.drawable.filename_background);
				ctv.setText(getFileName(dir.getName(), files[i].toString()));
				ctv.setPadding(10, 10, 10, 10);
				ctv.setChecked(false);
				ctv.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						if (clickEnabled) {
							File nextFile = new File(currentDirectory
									+ ctv.getText().toString());
							if (nextFile.isDirectory()) {
								previousDirectory = currentDirectory;
								boolean success;
								try {
									success = getFiles(nextFile,
											backgroundLayout());
									checkedFiles.removeAll(checkedFiles);
								} catch (Exception e) {
									e.printStackTrace();
									success = false;
								}
								didGetFiles(success, false,
										SlideType.SLIDE_LEFT);
							} else {
								if (isCSV(ctv.getText().toString())) {
									ctv.toggle();
									if (ctv.isChecked()) {
										ctv.setCheckMarkDrawable(R.drawable.bluecheck);
										checkedFiles.add(nextFile);
									} else {
										ctv.setCheckMarkDrawable(0);
										checkedFiles.remove(nextFile);
									}
								} else
									w.make("This file type is not supported.  Please choose a valid \".csv\".",
											Waffle.IMAGE_X);
							}
						}
					}
				});
				dv.addView(ctv);
			}
		}

		currentDirectory = dir.toString();
		return true;
	}

	private void didGetFiles(boolean yes, boolean firstGrab, SlideType st) {
		curDir.setText("Current directory: " + currentDirectory);

		if (yes) {
			noData.setVisibility(View.GONE);
			curDir.setVisibility(View.VISIBLE);

			// No transition, just swap visible view
			if (!(st == SlideType.NO_SLIDE)) {
				// new AnimateViews().execute(st);
				animateViews(st);
			}

		} else {
			// If it isn't the first time you're getting files, then the success
			// is false because you can't enter a directory (eg "secure"). Thus,
			// we don't update the UI.
			if (firstGrab) {
				noData.setVisibility(View.VISIBLE);
				curDir.setVisibility(View.GONE);
				Intent iSdFail = new Intent(mContext, SdCardFailure.class);
				startActivity(iSdFail);
			} else {
				w.make("You do not have permission to navigate into this directory.",
						Waffle.LENGTH_LONG, Waffle.IMAGE_X);
			}

		}
	}

	LinearLayout activeLayout() {
		if (usingDataView1) {
			return dataView;
		} else {
			return dataView2;
		}
	}

	LinearLayout backgroundLayout() {
		if (usingDataView1) {
			return dataView2;
		} else {
			return dataView;
		}
	}

	private boolean uploadFile(File sdFile) {
		boolean success = false;
		if (!rapi.isLoggedIn())
			login();

		if (!rapi.isLoggedIn()) {
			// NO!
			return false;
		}

		if (sdFile.isDirectory() || sdFile.isHidden() || !sdFile.canRead())
			return false;
		BufferedReader fReader = null;

		try {
			fReader = new BufferedReader(new FileReader(sdFile));
			String headerLine = fReader.readLine();
			String[] header = headerLine.split(",");

			// find out how to organize data
			String[] order = getOrder(headerLine);
			if (order == null) {
				fReader.close();
				return false;
			}

			// gets the order as an array of Array-indexes
			int[] loopOrder = new int[order.length];
			for (int i = 0; i < order.length; i++) {
				for (int j = 0; j < header.length; j++) {
					if (order[i] == null) {
						loopOrder[i] = -1;
						break;
					} else if (order[i].equals(header[j])) {
						loopOrder[i] = j;
						break;
					}
				}
			}

			JSONArray dataJSON = makeJSONArray(fReader, loopOrder);
			fReader.close();
			if (dataJSON == null) {
				return false;
			}

			SharedPreferences sp = getSharedPreferences("eid", 0);
			String eid = sp.getString("eid", "-1");
			if (eid.equals("-1"))
				return false;

			int sid = rapi.createSession(eid, sdFile.getName(),
					"Automated .csv upload from Android.  Uploaded at "
							+ getUploadTime() + ".", "Lowell", "MA", "US");
			if (sid <= 0)
				return false;

			success = rapi.putSessionData(sid, eid, dataJSON);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return success;
	}

	private JSONArray makeJSONArray(BufferedReader fReader, int[] loopOrder) {

		String dataLine;
		String[] data;

		JSONArray dataJSON = new JSONArray();
		try {
			while ((dataLine = fReader.readLine()) != null) {
				JSONArray dataLineJSON = new JSONArray();
				data = dataLine.split(",");

				for (int i = 0; i < loopOrder.length; i++) {
					if (loopOrder[i] == -1)
						dataLineJSON.put(0);
					else
						dataLineJSON.put(data[loopOrder[i]]);
				}
				dataJSON.put(dataLineJSON);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
		return dataJSON;
	}

	private String[] getOrder(String top) {

		LinkedList<String> order = new LinkedList<String>();
		String[] sdOrder = top.split(",");

		int length = sdOrder.length;
		if (length == 0)
			return null;

		final SharedPreferences mPrefs = getSharedPreferences("eid", 0);
		int eid = Integer.parseInt(mPrefs.getString("eid", "-1"));
		if (eid == -1)
			return null;

		dfm = new DataFieldManager(eid, rapi, mContext);
		dfm.getFieldOrder();

		for (String s : dfm.order) {
			for (int i = 0; i < sdOrder.length; i++) {
				boolean match = dfm.match(sdOrder[i], s);
				if (match) {
					order.add(sdOrder[i]);
					break;
				} else if (i == (sdOrder.length - 1)) {
					order.add(null);
				}
			}
		}

		String[] ret = new String[order.size()];
		for (int i = 0; i < order.size(); i++)
			ret[i] = order.get(i);

		return ret;
	}

	private String getFileName(String current, String absolutePath) {
		return absolutePath.split(current)[1];
	}

	private String getParentDir(String absolutePath) {
		String dir = "";
		String[] sections = absolutePath.split("/");
		for (int i = 1; i < (sections.length - 1); i++) {
			dir += "/" + sections[i];
		}

		return dir;
	}

	private boolean isCSV(String fileName) {
		String[] splitName = fileName.split("\\.");
		String fileType = splitName[splitName.length - 1]
				.toLowerCase(Locale.US);
		return fileType.equals("csv");
	}

	private boolean login() {
		final SharedPreferences loginPrefs = new ObscuredSharedPreferences(
				Main.mContext, Main.mContext.getSharedPreferences("USER_INFO",
						Context.MODE_PRIVATE));

		boolean success = false;
		if (!(loginPrefs.getString("username", "").equals("")))
			success = rapi.login(loginPrefs.getString("username", ""),
					loginPrefs.getString("password", ""));

		return success;

	}

	void toggleVisibleScrollView() {
		if (usingDataView1) {
			scrollWrapper.setVisibility(View.GONE);
			scrollWrapper2.setVisibility(View.VISIBLE);
		} else {
			scrollWrapper2.setVisibility(View.GONE);
			scrollWrapper.setVisibility(View.VISIBLE);
		}

		scrollWrapper.setScrollY(0);
		scrollWrapper2.setScrollY(0);

		usingDataView1 = !usingDataView1;
	}

	public void slideLeft() {

		Animation slideInFromRight = AnimationUtils.loadAnimation(mContext,
				R.anim.slide_in_from_right);
		backgroundLayout().startAnimation(slideInFromRight);
	}

	public void slideRight() {

		Animation slideInFromLeft = AnimationUtils.loadAnimation(mContext,
				R.anim.slide_in_from_left);
		backgroundLayout().startAnimation(slideInFromLeft);

	}

	public void animateViews(SlideType st) {
		clickEnabled = false;

		if (st == SlideType.SLIDE_LEFT)
			slideLeft();
		else if (st == SlideType.SLIDE_RIGHT)
			slideRight();

		toggleVisibleScrollView();
		clickEnabled = true;

	}

}