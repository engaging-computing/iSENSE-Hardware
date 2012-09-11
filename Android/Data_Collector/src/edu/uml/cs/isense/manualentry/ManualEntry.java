package edu.uml.cs.isense.manualentry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import edu.uml.cs.isense.collector.R;

public class ManualEntry extends Activity implements OnClickListener {

	private static final int REQUEST_EXP_ID = 1;
	
	private Button uploadData;
	private Button saveData;
	private Button clearData;
	
	private LinearLayout dataFieldEntryList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manual_entry);
		
		Intent iGetExpId = new Intent(this, ManualEntrySetup.class);
		startActivityForResult(iGetExpId, REQUEST_EXP_ID);
		
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
}
