package edu.uml.cs.isense.manualentry;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExperimentField;
import edu.uml.cs.isense.waffle.Waffle;

public class ManualEntry extends Activity implements OnClickListener {

	private static final int REQUEST_EXP_ID = 1;

	private static final int TYPE_DEFAULT = 1;
	private static final int TYPE_LATITUDE = 2;
	private static final int TYPE_LONGITUDE = 3;
	private static final int TYPE_TIME = 4;

	private Waffle w;
	private RestAPI rapi;

	private Button uploadData;
	private Button saveData;
	private Button clearData;

	private LinearLayout dataFieldEntryList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manual_entry);

		uploadData = (Button) findViewById(R.id.manual_upload);
		saveData = (Button) findViewById(R.id.manual_save);
		clearData = (Button) findViewById(R.id.manual_clear);

		w = new Waffle(this);

		dataFieldEntryList = (LinearLayout) findViewById(R.id.field_view);

		Intent iGetExpId = new Intent(this, ManualEntrySetup.class);
		startActivityForResult(iGetExpId, REQUEST_EXP_ID);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.manual_clear:
			clearFields();
			break;
		case R.id.manual_save:
			saveFields();
			break;
		case R.id.manual_upload:
			uploadFields();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_EXP_ID) {
			if (resultCode == RESULT_OK) {
				int eid = data.getIntExtra("eid", -1);
				if (eid != -1) {
					fillDataFieldEntryList(eid);
					rapi.getExperimentFields(eid);
				} else
					w.make("Ballz x2");
			} else {
				w.make("Ballz");
			}
		}
	}

	private void fillDataFieldEntryList(int eid) {
		ArrayList<ExperimentField> fieldOrder = rapi.getExperimentFields(eid);
		for (ExperimentField expField : fieldOrder) {
			if (expField.type_id == expField.GEOSPACIAL) {
				if (expField.unit_id == expField.UNIT_LATITUDE) {
					addDataField(expField, TYPE_LATITUDE);
				} else {
					addDataField(expField, TYPE_LONGITUDE);
				}
			} else if (expField.type_id == expField.TIME) {
				addDataField(expField, TYPE_TIME);
			} else {
				addDataField(expField, TYPE_DEFAULT);
			}
		}
	}

	private void addDataField(ExperimentField expField, int type) {
		View dataField = View.inflate(this, R.id.manual_dataField, null);
		TextView fieldName = (TextView) dataField
				.findViewById(R.id.manual_dataFieldName);
		fieldName.setText(expField.field_name);
		if (type != TYPE_DEFAULT) {
			EditText fieldContents = (EditText) dataField
					.findViewById(R.id.manual_dataFieldContents);
			fieldContents.setText("Auto");
			fieldContents.setEnabled(false);
		}
		dataFieldEntryList.addView(dataField);
	}

	private void clearFields() {
		for (int i = dataFieldEntryList.getChildCount(); i > 0; i--) {
			EditText dataFieldContents = (EditText) dataFieldEntryList
					.getChildAt(i).findViewById(R.id.manual_dataFieldContents);
			if (dataFieldContents.isEnabled())
				dataFieldContents.setText("");
		}
	}

	private void saveFields() {
		// TODO
	}

	private void uploadFields() {
		// TODO
	}

}
