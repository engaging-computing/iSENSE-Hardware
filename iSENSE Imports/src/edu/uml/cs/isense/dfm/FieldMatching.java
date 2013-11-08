package edu.uml.cs.isense.dfm;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.supplements.OrientationManager;

/**
 * The FieldMatching class lists off fields from a project, which the implementor must pass in
 * as an intent extra to DFM_ORDER_LIST, and makes a best attempt of matching them with known
 * field names in the system.  It provides drop-down menus for users to reconfigure fields they
 * would like to record for this project.  When this activity finishes, fields accepted by the
 * user are stored in the acceptedFields LinkedList, which is public for any implementation to grab.
 * 
 * @author iSENSE Android Development Team
 */
public class FieldMatching extends Activity {
	
	/**
	 * The hard-coded String key that the implementor should pair with a String[] of their
	 * DataFieldManager instance's order array.  Please note this must be a String[], but 
	 * DFM's order field is a LinkedList.  See 
	 * {@link edu.uml.cs.isense.dfm.DataFieldManager#convertOrderToStringArray() convertOrderToStringArray()}
	 * to easily convert order into a String[].
	 */
	public static final String DFM_ORDER_LIST = "dfm_order_list";

	/**
	 * The list of fields the user accepts with.  In other words, it will be a
	 * list of all the fields selected in the drop-down menus.
	 */
	public static LinkedList<String> acceptedFields;
	
	/**
	 * A public boolean that can be checked upon return of this activity to see if the project
	 * was compatible with the app or not.  A typical implementation would ask the user to select
	 * a new project and launch the FieldMatching activity again with that new project.
	 */
	public static boolean compatible;
	
	/**
	 * This hardcoded String is used to identify whether or not the FieldMatching class should build
	 * a String containing accepted fields in SharedPreferences.
	 */
	public static String SHOULD_BUILD_PREFS_STRING = "should_build_prefs_string";
	
	private static boolean shouldBuildPrefsString = true;
	
	private LinearLayout scrollViewLayout;
	private int nullViewCount = 0;
	private boolean isEmpty = false;
	private TextView errorTV;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.field_matching);
		
		mContext = this;
		compatible = true;
		
		OrientationManager.disableRotation((Activity) mContext);
		
		LinkedList<String> fields = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String[] sa = extras.getStringArray(DFM_ORDER_LIST);
			fields = DataFieldManager.convertStringArrayToLinkedList(sa);
			
			shouldBuildPrefsString = extras.getBoolean(SHOULD_BUILD_PREFS_STRING, true);
		} else {
			throw new RuntimeException("Incorrect usage of FieldMatching: please pass in dfm's order list");
		}
	
		scrollViewLayout = (LinearLayout) findViewById(R.id.field_matching_view);

		if (fields == null || fields.isEmpty()) {
			isEmpty = true;
		} else {
			for (String field : fields) {
				View v = View.inflate(mContext, R.layout.field_match_cell, null);

				TextView name = (TextView) v.findViewById(R.id.field_match_cell_name);
				if (field.contains(getString(R.string.null_string))) {
					String subStr = field.replace(getString(R.string.null_string), "");
					name.setText(subStr);
				} else
					name.setText(field);
				
				Spinner selector = (Spinner) v.findViewById(R.id.field_match_cell_spinner);
				
				if (field.contains(getString(R.string.null_string))) {
					selector.setSelection(0);
				} else if (field.equals(getString(R.string.time))) {
					selector.setSelection(Fields.TIME + 1);
				} else if (field.equals(getString(R.string.latitude))) {
					selector.setSelection(Fields.LATITUDE + 1);
				} else if (field.equals(getString(R.string.longitude))) {
					selector.setSelection(Fields.LONGITUDE + 1);
				} else if (field.equals(getString(R.string.accel_x))) {
					selector.setSelection(Fields.ACCEL_X + 1);
				} else if (field.equals(getString(R.string.accel_y))) {
					selector.setSelection(Fields.ACCEL_Y + 1);
				} else if (field.equals(getString(R.string.accel_z))) {
					selector.setSelection(Fields.ACCEL_Z + 1);
				} else if (field.equals(getString(R.string.accel_total))) {
					selector.setSelection(Fields.ACCEL_TOTAL + 1);
				} else if (field.equals(getString(R.string.magnetic_x))) {
					selector.setSelection(Fields.MAG_X);
				} else if (field.equals(getString(R.string.magnetic_y))) {
					selector.setSelection(Fields.MAG_Y + 1);
				} else if (field.equals(getString(R.string.magnetic_z))) {
					selector.setSelection(Fields.MAG_Z + 1);
				} else if (field.equals(getString(R.string.magnetic_total))) {
					selector.setSelection(Fields.MAG_TOTAL + 1);
				} else if (field.equals(getString(R.string.heading_deg))) {
					selector.setSelection(Fields.HEADING_DEG + 1);
				} else if (field.equals(getString(R.string.heading_rad))) {
					selector.setSelection(Fields.HEADING_RAD + 1);
				} else if (field.equals(getString(R.string.temperature_c))) {
					selector.setSelection(Fields.TEMPERATURE_C + 1);
				} else if (field.equals(getString(R.string.temperature_f))) {
					selector.setSelection(Fields.TEMPERATURE_F + 1);
				} else if (field.equals(getString(R.string.temperature_k))) {
					selector.setSelection(Fields.TEMPERATURE_K + 1);
				} else if (field.equals(getString(R.string.pressure))) {
					selector.setSelection(Fields.PRESSURE + 1);
				} else if (field.equals(getString(R.string.luminous_flux))) {
					selector.setSelection(Fields.LIGHT + 1);
				} else if (field.equals(getString(R.string.altitude))) {
					selector.setSelection(Fields.ALTITUDE + 1);
				}

				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					     LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

				layoutParams.setMargins(1, 1, 1, 1);
				scrollViewLayout.addView(v, layoutParams);

				if (field.equals(getString(R.string.null_string))) {
					nullViewCount++;
				}
			}

		}

		if (nullViewCount == scrollViewLayout.getChildCount()) {
			errorTV = new TextView(mContext);
			errorTV.setBackgroundColor(Color.TRANSPARENT);
			if (isEmpty) errorTV.setText(getString(R.string.invalidProject));
			else {
				compatible = false;
				errorTV.setText(getString(R.string.noCompatibleFields));
			}
			scrollViewLayout.addView(errorTV);
			
			TextView topText = (TextView) findViewById(R.id.field_matching_text);
			topText.setText("");
			ScrollView fieldScroll = (ScrollView) findViewById(R.id.field_matching_scroll);
			fieldScroll.setBackgroundColor(Color.TRANSPARENT);
			
			isEmpty = false;
		}

		Button back = (Button) findViewById(R.id.field_matching_back);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
				OrientationManager.enableRotation((Activity) mContext);
				finish();
			}
		});
		
		Button okay = (Button) findViewById(R.id.field_matching_ok);
		okay.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setAcceptedFields();
				setResult(RESULT_OK);
				OrientationManager.enableRotation((Activity) mContext);
				finish();
			}
		});

	}

	private void setAcceptedFields() {
		acceptedFields = new LinkedList<String>();
		scrollViewLayout.removeView(errorTV);
		
		for (int i = 0; i < scrollViewLayout.getChildCount(); i++) {
			View v = scrollViewLayout.getChildAt(i);

			Spinner selector = (Spinner) v.findViewById(R.id.field_match_cell_spinner);
			if (selector.getSelectedItemPosition() != 0)
				acceptedFields.add((String) selector.getSelectedItem());
			else
				acceptedFields.add(getString(R.string.null_string));	
			
		}
		
		if (shouldBuildPrefsString)
			buildPrefsString();

	}
	
	private void buildPrefsString() {
		SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
		SharedPreferences.Editor mEdit = mPrefs.edit();
		
		StringBuilder sb = new StringBuilder();
		
		for (String s : acceptedFields) {
			sb.append(s).append(",");
		}
		
		String prefString = sb.toString();
		if(prefString.length() == 0)
			return;
		prefString = prefString.substring(0, prefString.length() - 1);
		
		mEdit.putString("accepted_fields", prefString).commit();
		mEdit.putString("accepted_proj", mPrefs.getString("project_id", "-1")).commit();
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		OrientationManager.enableRotation((Activity) mContext);
		finish();
	}

}