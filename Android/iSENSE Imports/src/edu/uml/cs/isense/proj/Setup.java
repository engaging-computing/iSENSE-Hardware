package edu.uml.cs.isense.proj;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.objects.RProjectField;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * This Activity is designed to select a project from the iSENSE website. It
 * features an EditText that the user may manually enter a project ID into, a
 * Browse feature to pick from a list of projects, and a QR code scanning
 * feature to find the project ID from a project on iSENSE.
 * 
 * To use this Activity, launch an Intent to this class and catch it in your
 * onActivityResult() method. To obtain the project ID returned by this
 * Activity, create a SharedPreferences object using the PREFS_ID variable as
 * the "name" parameter. Then, request a String with the PROJECT_ID "key"
 * parameter. For example:
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	SharedPreferences mPrefs = getSharedPreferences(Setup.PREFS_ID, 0);
 * 	String projID = mPrefs.getString(Setup.PROJECT_ID, &quot;-1&quot;);
 * }
 * </pre>
 * 
 * @author iSENSE Android Development Team
 */
public class Setup extends Activity implements OnClickListener {

	private EditText projInput;

	private Button okay;
	private Button cancel;
	private Button qrCode;
	private Button browse;
	private Button createProject;

	private LinearLayout oklayout;

	private Context mContext;
	private Waffle w;
	private API api;

	private SharedPreferences mPrefs;

	private static final int QR_CODE_REQUESTED = 100;
	private static final int PROJECT_CODE = 101;
	private static final int NO_QR_REQUESTED = 102;
	private static final int NAME_FOR_NEW_PROJECT_REQUESTED = 103;
	private static final int NEW_PROJ_REQUESTED = 104;

	/**
	 * The constant for the "name" parameter in a SharedPreference's
	 * getSharedPreferences(name, mode) call. Use this String constant to build
	 * a SharedPreferences object in which you may obtain the project ID
	 * returned by this Activity.
	 */
	public static String PROJ_PREFS_ID = "PROJID";
	/**
	 * The constant for the "key" parameter in a SharedPreference's
	 * getString(key, defValue) call. Use this String constant to retrieve the
	 * project ID, in the form of a String, returned by this Activity.
	 */
	public static String PROJECT_ID = "project_id";

	public static String APPNAME;

	private boolean showOKCancel = true;
	private boolean constrictFields = false;
	private boolean themeNavBar = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_id);

		mContext = this;

		api = API.getInstance();

		w = new Waffle(mContext);

		okay = (Button) findViewById(R.id.project_ok);
		okay.setOnClickListener(this);

		cancel = (Button) findViewById(R.id.project_cancel);
		cancel.setOnClickListener(this);

		qrCode = (Button) findViewById(R.id.project_qr);
		qrCode.setOnClickListener(this);

		browse = (Button) findViewById(R.id.project_browse);
		browse.setOnClickListener(this);

		createProject = (Button) findViewById(R.id.createProjectBtn);
		createProject.setOnClickListener(this);

		oklayout = (LinearLayout) findViewById(R.id.OKCancelLayout);
		oklayout.setVisibility(View.VISIBLE);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			showOKCancel = extras.getBoolean("showOKCancel", true);
			constrictFields = extras.getBoolean("constrictFields", false);
			themeNavBar = extras.getBoolean(ProjectCreate.THEME_NAV_BAR, false);

			if (!showOKCancel)
				oklayout.setVisibility(View.GONE);

			String fromWhere = extras.getString("from_where");
			if (fromWhere != null) {
				if (fromWhere.equals("manual")) {
					PROJ_PREFS_ID = "PROJID_MANUAL";
				} else if (fromWhere.equals("queue")) {
					PROJ_PREFS_ID = "PROJID_QUEUE";
				} else if (fromWhere.equals("welcome")) {
					PROJ_PREFS_ID = "PROJID_WELCOME";
				} else {
					PROJ_PREFS_ID = "PROJID";
				}

			} else {
				PROJ_PREFS_ID = "PROJID";
			}

			APPNAME = extras.getString("app_name");

		} else {
			PROJ_PREFS_ID = "PROJID";
		}

		mPrefs = getSharedPreferences(PROJ_PREFS_ID, 0);
		String projID = mPrefs.getString(PROJECT_ID, "").equals("-1") ? ""
				: mPrefs.getString(PROJECT_ID, "");

		projInput = (EditText) findViewById(R.id.projectInput);
		projInput.setText(projID);

	}

	public void onClick(View v) {

		int id = v.getId();
		if (id == R.id.project_ok) {
			boolean pass = true;
			if (projInput.getText().length() == 0) {
				projInput.setError("Enter a project ID");
				pass = false;
			}
			if (pass) {

				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putString(PROJECT_ID, projInput.getText().toString())
						.commit();

				setResult(RESULT_OK);
				finish();
			}
		} else if (id == R.id.project_cancel) {
			setResult(RESULT_CANCELED);
			finish();
		} else if (id == R.id.project_qr) {
			try {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");

				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, QR_CODE_REQUESTED);
			} catch (ActivityNotFoundException e) {
				Intent iNoQR = new Intent(Setup.this, NoQR.class);
				startActivityForResult(iNoQR, NO_QR_REQUESTED);
			}
		} else if (id == R.id.project_browse) {
			Intent iProject = new Intent(getApplicationContext(),
					BrowseProjects.class);
			;
			startActivityForResult(iProject, PROJECT_CODE);
		} else if (id == R.id.createProjectBtn) {
			if (!Connection.hasConnectivity(mContext))
				w.make("Internet connection required to create project",
						Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
			else {
				if (!constrictFields) {
					Intent iProjCreate = new Intent(getApplicationContext(),
							ProjectCreate.class);
					iProjCreate.putExtra(ProjectCreate.THEME_NAV_BAR,
							themeNavBar);
					startActivityForResult(iProjCreate, NEW_PROJ_REQUESTED);
				} else {
					Intent iNewProjName = new Intent(getApplicationContext(),
							ProjectNameDialog.class);
					startActivityForResult(iNewProjName,
							NAME_FOR_NEW_PROJECT_REQUESTED);
				}
			}
		}

	}

	// Performs tasks after returning to main UI from previous activities
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == QR_CODE_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");

				String delimiter = "projects/";
				String[] split = contents.split(delimiter);

				try {
					projInput.setText(split[1]);
					Integer.parseInt(split[1]);
				} catch (ArrayIndexOutOfBoundsException e) {
					w.make("Invalid QR code scanned", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
				} catch (NumberFormatException nfe) {
					w.make("Invalid QR code scanned", Waffle.LENGTH_LONG,
							Waffle.IMAGE_X);
				}

			}
		} else if (requestCode == PROJECT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				int projID = data.getExtras().getInt(PROJECT_ID);
				projInput.setText("" + projID);

			}
		} else if (requestCode == NO_QR_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String url = "https://play.google.com/store/apps/details?id=com.google.zxing.client.android";
				Intent urlIntent = new Intent(Intent.ACTION_VIEW);
				urlIntent.setData(Uri.parse(url));
				startActivity(urlIntent);
			}
		} else if (requestCode == NAME_FOR_NEW_PROJECT_REQUESTED) {
			if (resultCode == RESULT_OK) {
				ArrayList<RProjectField> fields = getArrayOfFields();
				if (data.hasExtra("new_proj_name")) {
					new CreateProjectTask().execute(
							data.getStringExtra("new_proj_name"), fields);
				}
			}
		} else if (requestCode == NEW_PROJ_REQUESTED) {
			if (resultCode == RESULT_OK) {
				if (data.hasExtra(ProjectCreate.NEW_PROJECT_ID)) {
					String pid = data
							.getStringExtra(ProjectCreate.NEW_PROJECT_ID);
					SharedPreferences.Editor mEditor = mPrefs.edit();
					mEditor.putString(PROJECT_ID, pid).commit();
					setResult(RESULT_OK);
					finish();
				}
			} else {
				setResult(RESULT_CANCELED);
				finish();
			}
		}
	}

	class CreateProjectTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected void onPostExecute(Integer projNum) {
			super.onPostExecute(projNum);

			SharedPreferences.Editor mEditor = mPrefs.edit();
			mEditor.putString(PROJECT_ID, projNum + "").commit();
			setResult(RESULT_OK);
			finish();
		}

		@Override
		protected Integer doInBackground(Object... params) {
			String projName = (String) params[0];

			// Make sure there are RProjectFields
			if (params[1] instanceof ArrayList<?>) {
				@SuppressWarnings("unchecked")
				ArrayList<RProjectField> fields = (ArrayList<RProjectField>) params[1];
				return api.createProject(projName, fields);
			} else {
				return -1;
			}
		}
	}

	private ArrayList<RProjectField> getArrayOfFields() {
		ArrayList<RProjectField> fields = new ArrayList<RProjectField>();

		if (APPNAME.equals("CRP")) {
			RProjectField time = new RProjectField();
			time.name = "Time";
			time.type = RProjectField.TYPE_TIMESTAMP;
			fields.add(time);

			RProjectField aX, aY, aZ, aT;
			aX = new RProjectField();
			aY = new RProjectField();
			aZ = new RProjectField();
			aT = new RProjectField();

			String b = "Accel-";
			aX.name = b + "X";
			aY.name = b + "Y";
			aZ.name = b + "Z";
			aT.name = b + "Total";

			aX.type = aY.type = aZ.type = aT.type = RProjectField.TYPE_NUMBER;
			aX.unit = aY.unit = aZ.unit = aT.unit = "m/s^2";

			fields.add(aX);
			fields.add(aY);
			fields.add(aZ);
			fields.add(aT);

		} else if (APPNAME.equals("DataWalk")) {
			RProjectField time = new RProjectField();
			time.name = "Time";
			time.type = RProjectField.TYPE_TIMESTAMP;
			fields.add(time);

			RProjectField aT, Vel, TD, Lat, Lon;

			aT = new RProjectField();
			aT.name = "Accel-Magnitude";
			aT.type = RProjectField.TYPE_NUMBER;
			aT.unit = "m/s^2";

			Vel = new RProjectField();
			Vel.name = "Velocity";
			Vel.type = RProjectField.TYPE_NUMBER;
			Vel.unit = "m/s";

			TD = new RProjectField();
			TD.name = "Total Distance";
			TD.type = RProjectField.TYPE_NUMBER;
			TD.unit = "m";

			Lat = new RProjectField();
			Lat.name = "Latitude";
			Lat.type = RProjectField.TYPE_LAT;
			Lat.unit = "deg";

			Lon = new RProjectField();
			Lon.name = "Longitude";
			Lon.type = RProjectField.TYPE_LON;
			Lon.unit = "deg";

			fields.add(aT);
			fields.add(Vel);
			fields.add(TD);
			fields.add(Lat);
			fields.add(Lon);

		} else if (APPNAME.equals("Canobie")) {

			RProjectField time = new RProjectField();
			time.name = "Time";
			time.type = RProjectField.TYPE_TIMESTAMP;
			fields.add(time);

			RProjectField aX, aY, aZ, aT;
			aX = new RProjectField();
			aY = new RProjectField();
			aZ = new RProjectField();
			aT = new RProjectField();

			String b = "Accel-";
			aX.name = b + "X";
			aY.name = b + "Y";
			aZ.name = b + "Z";
			aT.name = b + "Total";

			aX.type = aY.type = aZ.type = aT.type = RProjectField.TYPE_NUMBER;
			aX.unit = aY.unit = aZ.unit = aT.unit = "m/s^2";

			RProjectField Lat, Lon;

			Lat = new RProjectField();
			Lat.name = "Latitude";
			Lat.type = RProjectField.TYPE_LAT;
			Lat.unit = "deg";

			Lon = new RProjectField();
			Lon.name = "Longitude";
			Lon.type = RProjectField.TYPE_LON;
			Lon.unit = "deg";

			fields.add(aX);
			fields.add(aY);
			fields.add(aZ);
			fields.add(aT);
			fields.add(Lat);
			fields.add(Lon);

		} else if (APPNAME.equals("Pictures")) {

			RProjectField time, Lat, Lon;

			time = new RProjectField();
			time.name = "Time";
			time.type = RProjectField.TYPE_TIMESTAMP;
			fields.add(time);

			Lat = new RProjectField();
			Lat.name = "Latitude";
			Lat.type = RProjectField.TYPE_LAT;
			Lat.unit = "deg";

			Lon = new RProjectField();
			Lon.name = "Longitude";
			Lon.type = RProjectField.TYPE_LON;
			Lon.unit = "deg";

			fields.add(time);
			fields.add(Lat);
			fields.add(Lon);
		}

		return fields;
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}
}
