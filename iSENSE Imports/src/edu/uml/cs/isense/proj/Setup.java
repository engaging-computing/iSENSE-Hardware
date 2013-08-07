package edu.uml.cs.isense.proj;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.waffle.Waffle;

public class Setup extends Activity implements OnClickListener {

	private EditText projInput;

	private Button okay;
	private Button cancel;
	private Button qrCode;
	private Button browse;

	private Context mContext;
	private Waffle w;

	private SharedPreferences mPrefs;

	private static final int QR_CODE_REQUESTED = 100;
	private static final int PROJECT_CODE = 101;
	private static final int NO_QR_REQUESTED = 102;
	
	private static String prefsString = "PROJID";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_id);

		mContext = this;

		w = new Waffle(mContext);

		okay = (Button) findViewById(R.id.project_ok);
		okay.setOnClickListener(this);

		cancel = (Button) findViewById(R.id.project_cancel);
		cancel.setOnClickListener(this);

		qrCode = (Button) findViewById(R.id.project_qr);
		qrCode.setOnClickListener(this);

		browse = (Button) findViewById(R.id.project_browse);
		browse.setOnClickListener(this);
		
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String fromWhere = extras.getString("from_where");
			if (fromWhere != null) {
				if (fromWhere.equals("manual")) {
					prefsString = "PROJID_MANUAL";
				} else if (fromWhere.equals("queue")) {
					prefsString = "PROJID_QUEUE";
				} else {
					prefsString = "PROJID";
				}
			} else {
				prefsString = "PROJID";
			}	
		}
		
		
		mPrefs = getSharedPreferences(prefsString, 0);
		String projID = mPrefs.getString("project_id", "").equals("-1") ? "" : mPrefs.getString("project_id", "");
		
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
				mEditor.putString("project_id",
						projInput.getText().toString()).commit();

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
					BrowseProjects.class);;
			startActivityForResult(iProject, PROJECT_CODE);
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
				int projID = data.getExtras().getInt("project_id");
				projInput.setText("" + projID);

			}
		} else if (requestCode == NO_QR_REQUESTED) {
			if (resultCode == RESULT_OK) {
				String url = "https://play.google.com/store/apps/details?id=com.google.zxing.client.android";
				Intent urlIntent = new Intent(Intent.ACTION_VIEW);
				urlIntent.setData(Uri.parse(url));
				startActivity(urlIntent);
			}
		}
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}
}
