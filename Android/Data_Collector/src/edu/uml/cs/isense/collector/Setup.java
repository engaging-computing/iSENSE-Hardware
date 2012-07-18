package edu.uml.cs.isense.collector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.waffle.Waffle;

public class Setup extends Activity implements OnClickListener {

	private EditText sessionName;
	private EditText eidInput;
	
	private Button okay;
	private Button cancel;
	private Button qrCode;
	private Button browse;

	private Context mContext;
	private Waffle w;
	private RestAPI rapi;
	
	private SharedPreferences mPrefs;

	private static final int DIALOG_NO_QR = 1;

	private static final int QR_CODE_REQUESTED = 100;
	private static final int EXPERIMENT_CODE = 101;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup);

		mContext = this;

		w = new Waffle(mContext);

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());

		sessionName = (EditText) findViewById(R.id.sessionName);
		sessionName.setText(DataCollector.partialSessionName);
		
		mPrefs = getSharedPreferences("EID", 0);
		eidInput = (EditText) findViewById(R.id.ExperimentInput);
		eidInput.setText(mPrefs.getString("experiment_id", ""));
		
		okay = (Button) findViewById(R.id.setup_ok);
		okay.setOnClickListener(this);
		
		cancel = (Button) findViewById(R.id.setup_cancel);
		cancel.setOnClickListener(this);
		
		qrCode = (Button) findViewById(R.id.qrCode);
		qrCode.setOnClickListener(this);
		
		browse = (Button) findViewById(R.id.BrowseButton);
		browse.setOnClickListener(this);

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.setup_ok:
			boolean pass = true;

			if (sessionName.getText().length() == 0) {
				sessionName.setError("Enter a Name");
				pass = false;
			}
			if (eidInput.getText().length() == 0) {
				eidInput.setError("Enter an Experiment");
				pass = false;
			}

			if (pass) {

				// new SensorCheckTask().execute(); <--- call this when
				// returning

				Intent i = new Intent(mContext, DataCollector.class);
				i.putExtra("sessionName", sessionName.getText().toString());

				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putString("experiment_id",
						eidInput.getText().toString()).commit();

				setResult(RESULT_OK, i);
				finish();
			}

			break;

		case R.id.setup_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;

		case R.id.qrCode:
			try {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");

				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, QR_CODE_REQUESTED);
			} catch (ActivityNotFoundException e) {
				showDialog(DIALOG_NO_QR);
			}

			break;

		case R.id.BrowseButton:

			if (!rapi.isConnectedToInternet()) {
				w.make("You must enable wifi or mobile connectivity to do this.",
						Toast.LENGTH_SHORT, "x");
			} else {

				Intent experimentIntent = new Intent(getApplicationContext(),
						Experiments.class);
				experimentIntent.putExtra(
						"edu.uml.cs.isense.amusement.experiments.propose",
						EXPERIMENT_CODE);

				startActivityForResult(experimentIntent, EXPERIMENT_CODE);
			}

			break;
		}

	}

	@SuppressLint("HandlerLeak")
	protected Dialog onCreateDialog(final int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Dialog dialog;

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

		switch (id) {
		case DIALOG_NO_QR:

			builder.setTitle("No Barcode Scanner Found")
					.setMessage(
							"Your device does not have the proper Barcode Scanner application installed "
									+ "to use this feature.  However, "
									+ "you may visit the Google Play store to download this application (\"Barcode Scanner\" "
									+ "by the Zxing Team).")
					.setPositiveButton("Visit URL",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									String url = "https://play.google.com/store/apps/details?id=com.google.zxing.client.android";
									Intent urlIntent = new Intent(
											Intent.ACTION_VIEW);
									urlIntent.setData(Uri.parse(url));
									startActivity(urlIntent);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									dialoginterface.dismiss();
								}
							}).setCancelable(true);

			dialog = builder.create();

			break;

		default:

			dialog = null;
			break;
		}

		return apiDialogCheckerCase(dialog, lp, id);
	}

	// Deals with Dialog creation whether api is tablet or not
	@SuppressWarnings("deprecation")
	private Dialog apiDialogCheckerCase(Dialog dialog, LayoutParams lp, final int id) {
		if (apiTabletDisplay(dialog)) {

			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					removeDialog(id);
				}
			});
			return null;

		} else {

			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					removeDialog(id);
				}
			});

			return dialog;
		}
	}

	// apiTabletDisplay for Dialog Building on Tablets
	private static boolean apiTabletDisplay(Dialog dialog) {
		int apiLevel = getApiLevel();
		if (apiLevel >= 11) {
			dialog.show();

			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width = DataCollector.mwidth;
			lp.height = WindowManager.LayoutParams.MATCH_PARENT;
			lp.gravity = Gravity.CENTER_VERTICAL;
			lp.dimAmount = 0.7f;

			dialog.getWindow().setAttributes(lp);
			dialog.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_DIM_BEHIND);

			return true;
		} else
			return false;

	}

	// Assists with differentiating between displays for dialogues
	@SuppressWarnings("deprecation")
	private static int getApiLevel() {
		return Integer.parseInt(android.os.Build.VERSION.SDK);
	}

	// Performs tasks after returning to main UI from previous activities
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == QR_CODE_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");

				String delimiter = "id=";
				String[] split = contents.split(delimiter);

				try {
					eidInput.setText(split[1]);
				} catch (ArrayIndexOutOfBoundsException e) {
					w.make("Invalid QR Code!", Toast.LENGTH_LONG, "x");
				}

				// Handle successful scan
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		} else if (requestCode == EXPERIMENT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				int eid = data.getExtras().getInt(
						"edu.uml.cs.isense.pictures.experiments.exp_id");
				eidInput.setText("" + eid);
			}
		}
	}
}
