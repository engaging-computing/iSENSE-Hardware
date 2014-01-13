package edu.uml.cs.isense.collector.splash;

import java.io.File;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.collector.DataCollector;
import edu.uml.cs.isense.collector.ManualEntry;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.credentials.Login;
import edu.uml.cs.isense.supplements.FileBrowser;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class SelectMode extends Activity {

	private static Context mContext;
	private Waffle w;
	private API api;

	private String tempFilepath = "";

	public static final String ENABLE_MANUAL_AND_CSV = "enable_manual_and_csv";

	private static final int UPLOAD_CSV_REQUESTED = 100;
	private static final int LOGIN_REQUESTED = 101;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_mode);

		mContext = this;
		w = new Waffle(mContext);
		api = API.getInstance();

		// Action bar customization for API >= 14
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			ActionBar bar = getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#111133")));
			bar.setIcon(getResources()
					.getDrawable(R.drawable.rsense_logo_right));
			bar.setDisplayShowTitleEnabled(false);
			int actionBarTitleId = Resources.getSystem().getIdentifier(
					"action_bar_title", "id", "android");
			if (actionBarTitleId > 0) {
				TextView title = (TextView) findViewById(actionBarTitleId);
				if (title != null) {
					title.setTextColor(Color.WHITE);
					title.setTextSize(24.0f);
				}
			}
			
			// make the actionbar clickable
			bar.setDisplayHomeAsUpEnabled(true);
		}

		// Set listeners for the buttons
		final Button dataCollector = (Button) findViewById(R.id.select_mode_data_collector);
		dataCollector.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iDC = new Intent(mContext, DataCollector.class);
				startActivity(iDC);
			}
		});

		final Button manualEntry = (Button) findViewById(R.id.select_mode_manual_entry);
		manualEntry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iME = new Intent(mContext, ManualEntry.class);
				startActivity(iME);
			}
		});
		String manualEntryText = "<font COLOR=\"#0066FF\">"
				+ "Manually Enter Data" + "</font>" + "<br/>"
				+ "<font COLOR=\"#D9A414\">" + "(requires project)" + "</font>";
		manualEntry.setText(Html.fromHtml(manualEntryText));

		final Button csvUploader = (Button) findViewById(R.id.select_mode_csv_uploader);
		csvUploader.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!Connection.hasConnectivity(mContext))
					w.make(getResources().getString(R.string.need_connectivity_to_do),
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
				else {
					Intent iFileBrowse = new Intent(mContext, FileBrowser.class);
					startActivityForResult(iFileBrowse, UPLOAD_CSV_REQUESTED);
				}
				
			}
		});
		String csvUploaderText = "<font COLOR=\"#0066FF\">"
				+ "Upload a .csv File From My Device" + "</font>" + "<br/>"
				+ "<font COLOR=\"#D9A414\">"
				+ "(requires project)" + "</font>";
		csvUploader.setText(Html.fromHtml(csvUploaderText));

		// Determine if we should disable manual entry and .csv uploader
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			boolean en = extras.getBoolean(ENABLE_MANUAL_AND_CSV);
			if (!en) {
				manualEntry.setEnabled(false);
				String m = "<font COLOR=\"#0066FF\">" + "Manually Enter Data"
						+ "</font>" + "<br/>" + "<font COLOR=\"#B88804\">"
						+ "(requires project)" + "</font>";
				manualEntry.setText(Html.fromHtml(m));

				csvUploader.setEnabled(false);
				String c = "<font COLOR=\"#0066FF\">"
						+ "Upload a .csv File From My Device" + "</font>"
						+ "<br/>" + "<font COLOR=\"#B88804\">"
						+ "(requires project)" + "</font>";
				csvUploader.setText(Html.fromHtml(c));
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == UPLOAD_CSV_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String filepath = data.getStringExtra("filepath");
				if (filepath.length() == 0) {
					w.make(getResources().getString(R.string.could_not_find_csv),
							Waffle.LENGTH_SHORT,
							Waffle.IMAGE_X);
					return;
				}

				if (!Connection.hasConnectivity(mContext)) {
					w.make(getResources().getString(R.string.no_csv_no_internet),
							Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);
					return;
				}

				String[] comp = filepath.split("\\.");

				if (comp.length == 0
						|| !(comp[comp.length - 1].toLowerCase(Locale.US)
								.equals("csv"))) {
					w.make(getResources().getString(R.string.only_csv_files),
							Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);
					return;
				}

				new LoginTask().execute(filepath);
			}
		} else if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {

				w.make(getResources().getString(R.string.login_success), Waffle.LENGTH_LONG,
						Waffle.IMAGE_CHECK);

				new UploadCSVTask().execute(tempFilepath);
				
			} else if (resultCode == Login.RESULT_ERROR) {
				
				Intent i = new Intent(mContext, Login.class);
				startActivityForResult(i, LOGIN_REQUESTED);
				
			}
		}
	}

	private class LoginTask extends AsyncTask<String, Integer, Void> {

		private ProgressDialog dia;
		private String filepath;

		@Override
		protected void onPreExecute() {

			OrientationManager.disableRotation(SelectMode.this);

			dia = new ProgressDialog(SelectMode.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Logging in...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(String... args) {

			filepath = args[0];

			final SharedPreferences mPrefs = new ObscuredSharedPreferences(
					SelectMode.mContext,
					SelectMode.mContext.getSharedPreferences(
							Login.PREFERENCES_KEY_OBSCURRED_USER_INFO,
							Context.MODE_PRIVATE));

			api.createSession(
					mPrefs.getString(
							Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME,
							Login.DEFAULT_USERNAME),
					mPrefs.getString(
							Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD,
							Login.DEFAULT_PASSWORD));

			publishProgress(100);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {
			dia.setMessage("Done");
			dia.cancel();
			OrientationManager.enableRotation(SelectMode.this);

			if (api.getCurrentUser() == null) {
				tempFilepath = filepath;
				w.make(getResources().getString(R.string.please_log_in), 
						Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);

				Intent i = new Intent(mContext, Login.class);
				startActivityForResult(i, LOGIN_REQUESTED);
			} else {
				new UploadCSVTask().execute(filepath);
			}

		}

	}

	private class UploadCSVTask extends AsyncTask<String, Integer, Void> {

		private ProgressDialog dia;
		private int dsid = -1;

		@Override
		protected void onPreExecute() {

			OrientationManager.disableRotation(SelectMode.this);

			dia = new ProgressDialog(SelectMode.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while the .csv file is uploaded...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(String... args) {

			SharedPreferences globalProjPrefs = getSharedPreferences(
					"GLOBAL_PROJ", 0);
			int projectId = Integer.parseInt(globalProjPrefs.getString(
					"project_id_csv", "-1"));

			File csvToUpload = new File(args[0]);

			String[] components = args[0].split("/");
			String datasetName = components[components.length - 1];

			dsid = api.uploadCSV(projectId, csvToUpload, datasetName);

			publishProgress(100);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {
			dia.setMessage("Done");
			dia.cancel();
			OrientationManager.enableRotation(SelectMode.this);

			if (dsid <= 0) {
				w.make(getResources().getString(R.string.csv_failed_upload), 
						Waffle.LENGTH_SHORT,
						Waffle.IMAGE_X);
			} else {
				w.make(getResources().getString(R.string.csv_success_upload), 
						Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
			}

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	onBackPressed();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}