package edu.uml.cs.isense.proj;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.credentials.CredentialManager;
import edu.uml.cs.isense.objects.RProjectField;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * This Activity is designed to allow users to create a project on the iSENSE
 * website.
 * 
 * @author iSENSE Android Development Team
 */
public class ProjectCreate extends Activity {

	/**
	 * Constant for an intent extra that can be passed as a boolean value as
	 * true if the implementation wants to theme the navigation bar to look
	 * rSENSE-y.
	 */
	public static final String THEME_NAV_BAR = "theme_nav_bar_constant";

	private static Context mContext;
	private static Waffle w;

	private API api;

	private Spinner fieldSpin;
	private LinearLayout fieldScroll;
	private ScrollView fieldScrollHolder;
	private EditText projectName;

	private ArrayList<RProjectField> fields;

	private int newProjID;

	/**
	 * Constant for the new project ID returned by this class.
	 */
	public static final String NEW_PROJECT_ID = "new_proj_id";

	private static final int FIELD_TYPE_TIMESTAMP = 0;
	private static final int FIELD_TYPE_NUMBER = 1;
	private static final int FIELD_TYPE_TEXT = 2;
	private static final int FIELD_TYPE_LOCATION = 3;

	private static final int LOGIN_REQUESTED = 100;
	private ProgressDialog dia;

	private int locationCount = 0;
	private int timestampCount = 0;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_create);

		mContext = this;
		w = new Waffle(mContext);

		api = API.getInstance();

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.getBoolean(THEME_NAV_BAR)) {
			// Action bar customization for API >= 14
			if (android.os.Build.VERSION.SDK_INT >= 14) {
				ActionBar bar = getActionBar();
				bar.setBackgroundDrawable(new ColorDrawable(Color
						.parseColor("#111133")));
				bar.setIcon(getResources().getDrawable(
						R.drawable.rsense_logo_right));
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
		}

		projectName = (EditText) findViewById(R.id.project_create_name);

		// Set listeners for the buttons
		final Button cancel = (Button) findViewById(R.id.project_create_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		final Button ok = (Button) findViewById(R.id.project_create_ok);
		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// check to see if the project has a name
				if (projectName.getText().toString().length() == 0) {
					w.make(getResources().getString(
							R.string.please_enter_proj_name),
							Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);
					projectName.setError("Enter a new project name");
					return;
				} else
					projectName.setError(null);

				// check to see if the project has fields
				if (fieldScroll.getChildCount() == 0) {
					w.make(getResources()
							.getString(R.string.please_enter_field),
							Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);
					return;
				} else {
					for (int i = 0; i < fieldScroll.getChildCount(); i++) {
						View cell = fieldScroll.getChildAt(i);
						EditText fieldName = (EditText) cell
								.findViewById(R.id.project_field_name);
						if (fieldName != null
								&& fieldName.getText().toString().length() == 0) {
							int fieldNum = i + 1;
							w.make(getResources().getString(
									R.string.please_enter_name_field)
									+ fieldNum, Waffle.LENGTH_SHORT,
									Waffle.IMAGE_WARN);
							fieldName.setError("Enter a field name");
							return;
						} else if (fieldName != null)
							fieldName.setError(null);

					}
				}

				// project and all fields have names - create the project after
				// checking login status
				createProjectAfterCheckingLogin();
			}
		});

		final Button addField = (Button) findViewById(R.id.project_create_add_field_button);
		addField.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				int position = fieldSpin.getSelectedItemPosition();

				if (position == FIELD_TYPE_LOCATION)
					if (locationCount == 0)
						locationCount++;
					else {
						w.make(getResources().getString(
								R.string.one_location_only),
								Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);
						return;
					}
				else if (position == FIELD_TYPE_TIMESTAMP)
					if (timestampCount == 0)
						timestampCount++;
					else {
						w.make(getResources().getString(
								R.string.one_timestamp_only),
								Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);
						return;
					}

				addFieldType(position);
			}

		});

		fieldScrollHolder = (ScrollView) findViewById(R.id.project_create_fields_scroll);
		fieldSpin = (Spinner) findViewById(R.id.project_create_fields_spinner);
		fieldScroll = (LinearLayout) findViewById(R.id.project_create_fields_view);

	}

	private void addFieldType(int tag) {

		final View v = (tag == FIELD_TYPE_LOCATION) ? View.inflate(mContext,
				R.layout.project_field_location, null) : View.inflate(mContext,
				R.layout.project_field, null);

		v.setTag(tag);

		EditText fieldName = (EditText) v.findViewById(R.id.project_field_name);
		EditText units = (EditText) v.findViewById(R.id.project_field_units);

		if (tag != FIELD_TYPE_LOCATION) {
			fieldName.setImeOptions(EditorInfo.IME_ACTION_DONE);
			fieldName.setSingleLine(true);
			units.setImeOptions(EditorInfo.IME_ACTION_DONE);
			units.setSingleLine(true);
		}

		switch (tag) {
		case FIELD_TYPE_TIMESTAMP:
			fieldName.setText("Timestamp");
			units.setVisibility(View.INVISIBLE);
			break;

		case FIELD_TYPE_NUMBER:
			fieldName.setHint("number");
			break;

		case FIELD_TYPE_TEXT:
			fieldName.setHint("text");
			units.setVisibility(View.INVISIBLE);
			break;

		case FIELD_TYPE_LOCATION:
			break;

		default:
			break;

		}

		ImageView x = (ImageView) v.findViewById(R.id.project_field_x);
		x.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				fieldScroll.removeView(v);
				if ((Integer) v.getTag() == FIELD_TYPE_LOCATION) {
					locationCount--;
				} else if ((Integer) v.getTag() == FIELD_TYPE_TIMESTAMP) {
					timestampCount--;
				}
			}
		});

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutParams.setMargins(1, 1, 1, 1);

		fieldScroll.addView(v, layoutParams);
		scrollDown();
	}

	private void scrollDown() {
		Thread scrollThread = new Thread() {
			public void run() {
				try {
					sleep(200);
					ProjectCreate.this.runOnUiThread(new Runnable() {
						public void run() {
							fieldScrollHolder.fullScroll(View.FOCUS_DOWN);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		scrollThread.start();
	}

	private void createProjectAfterCheckingLogin() {

		if (api.getCurrentUser() == null) {
			new CheckLoginTask().execute();
		} else {
			createProject();
		}

	}

	private void needLogin() {
		w.make(getResources().getString(R.string.login_isense_first),
				Waffle.LENGTH_SHORT, Waffle.IMAGE_WARN);

		Intent iLogin = new Intent(mContext, CredentialManager.class);
		startActivityForResult(iLogin, LOGIN_REQUESTED);
	}

	private void createProject() {
		fields = new ArrayList<RProjectField>();

		for (int i = 0; i < fieldScroll.getChildCount(); i++) {
			View child = fieldScroll.getChildAt(i);
			RProjectField field = new RProjectField();

			EditText fieldName = (EditText) child
					.findViewById(R.id.project_field_name);
			EditText units = (EditText) child
					.findViewById(R.id.project_field_units);

			switch ((Integer) child.getTag()) {
			case FIELD_TYPE_TIMESTAMP:
				field.name = fieldName.getText().toString();
				field.type = RProjectField.TYPE_TIMESTAMP;
				fields.add(field);

				break;

			case FIELD_TYPE_NUMBER:
				field.name = fieldName.getText().toString();
				field.type = RProjectField.TYPE_NUMBER;
				field.unit = units.getText().toString();
				fields.add(field);

				break;

			case FIELD_TYPE_TEXT:
				field.name = fieldName.getText().toString();
				field.type = RProjectField.TYPE_TEXT;
				fields.add(field);

				break;

			case FIELD_TYPE_LOCATION:
				field.name = "Latitude";
				field.type = RProjectField.TYPE_LAT;
				field.unit = "deg";
				fields.add(field);

				field = new RProjectField();
				field.name = "Longitude";
				field.type = RProjectField.TYPE_LON;
				field.unit = "deg";
				fields.add(field);

				break;

			default:
				break;
			}
		}

		new CreateProjectTask().execute();
	}

	private class CheckLoginTask extends AsyncTask<Void, Integer, Void> {

		boolean success = false;

		@Override
		protected void onPreExecute() {

			OrientationManager.disableRotation(ProjectCreate.this);

			dia = new ProgressDialog(ProjectCreate.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Trying to log in...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			api.createSession(
					CredentialManager.getUsername(ProjectCreate.mContext),
					CredentialManager.getPassword(ProjectCreate.mContext));

			publishProgress(100);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (dia != null)
				dia.cancel();
			OrientationManager.enableRotation(ProjectCreate.this);

			if (success) {
				createProject();
			} else {
				needLogin();
			}

		}
	}

	private class CreateProjectTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			OrientationManager.disableRotation(ProjectCreate.this);

			dia = new ProgressDialog(ProjectCreate.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Creating your new project...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			newProjID = api.createProject(projectName.getText().toString(),
					fields);

			publishProgress(100);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dia != null)
				dia.cancel();
			OrientationManager.enableRotation(ProjectCreate.this);

			if (newProjID != 0) {

				Intent iRet = new Intent();
				iRet.putExtra(NEW_PROJECT_ID, "" + newProjID);
				setResult(RESULT_OK, iRet);
				finish();

			} else
				w.make(getResources().getString(R.string.project_failed_create),
						Waffle.LENGTH_SHORT, Waffle.IMAGE_X);

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {

				w.make(getResources().getString(R.string.login_success),
						Waffle.LENGTH_LONG, Waffle.IMAGE_CHECK);

				createProject();

			} else if (resultCode == CredentialManager.RESULT_ERROR) {

				Intent i = new Intent(mContext, CredentialManager.class);
				startActivityForResult(i, LOGIN_REQUESTED);

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