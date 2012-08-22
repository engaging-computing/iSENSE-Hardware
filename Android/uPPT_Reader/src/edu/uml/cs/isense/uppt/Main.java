/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII               iSENSE uPPT Reader App                      SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                    By: Michael Stowell,                      SSS                **/
/**           III                        Jeremy Poulin,                         SSS               **/
/**           III                        Nick Ver Voort                          SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                    Group:            ECG,                              SSS      **/
/**           III                                      iSENSE                           SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell              SSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/

package edu.uml.cs.isense.uppt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.waffle.Waffle;

public class Main extends Activity implements OnClickListener {

	private static String username = "";
	private static String password = "";
	private static String experimentId = "";
	private static String sessionName = "";
	private static String sessionId = "";

	private static final String baseUrl = "http://isensedev.cs.uml.edu/newvis.php?sessions=";

	private Vibrator vibrator;
	private TextView loginInfo;

	private static final int MENU_ITEM_LOGIN = 0;

	private static final int LOGIN_REQUESTED = 100;

	private RestAPI rapi;
	private Waffle w;

	private ProgressDialog dia;

	private static boolean useMenu = true;
	private static boolean successLogin = false;

	private long uploadTime;
	public JSONArray data;

	public static Context mContext;
	
	private ListView dataView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;
		w = new Waffle(mContext);

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(true);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		loginInfo = (TextView) findViewById(R.id.loginEdit);
		
		try {
			getFiles(Environment.getExternalStorageDirectory(), dataView);
		} catch (Exception e) {
			w.make(e.toString(), Waffle.IMAGE_X);
		}

	}

	long getUploadTime() {
		Calendar c = Calendar.getInstance();
		return (long) (c.getTimeInMillis());
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
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// menu.getItem(0).setEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_login:
			Intent iLogin = new Intent(mContext, LoginActivity.class);
			startActivityForResult(iLogin, LOGIN_REQUESTED);
			return true;

		}
		return false;
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
					loginInfo.setText("" + loginName);
					successLogin = true;
					Toast.makeText(mContext, "Login successful",
							Toast.LENGTH_SHORT).show();
				} else if (returnCode.equals("Failed")) {
					successLogin = false;
					Intent i = new Intent(mContext, LoginActivity.class);
					startActivityForResult(i, LOGIN_REQUESTED);
				} else {
					// should never get here
				}

			}

		}
	}

	private Runnable uploader = new Runnable() {

		public void run() {

			// Do rapi uploading stuff

		}
	};

	private class UploadTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			vibrator.vibrate(250);
			dia = new ProgressDialog(Main.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Uploading uPPT data set to iSENSE...");
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

			// do post execute stuff

		}
	}

	private boolean getFiles(File dir, ListView lv) throws Exception {

		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			throw new Exception("Cannot Access External Storage.");
		}

		File[] files = dir.listFiles();
		if (files.equals(null))
			return false;
		else {
			for (int i = 0; i < files.length; i++) {
				final CheckedTextView ctv = new CheckedTextView(mContext);
				ctv.setChecked(false);
				ctv.setCheckMarkDrawable(R.drawable.checkmark);
				ctv.setText(files[0].toString());
				ctv.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						ctv.toggle();
					}

				});
				lv.addView(ctv);
			}
		}
		return true;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		default:
			break;

		}

	}
}