package edu.uml.cs.raac;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ChangeExperiment extends Activity implements OnClickListener {
	Button ok, cancel;
	EditText eid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_experiment);
		
		eid    = (EditText) findViewById(R.id.new_eid);
		eid.setText(Isense.experimentId);
		
		ok     = (Button)   findViewById(R.id.experiment_ok);
		cancel = (Button)   findViewById(R.id.experiment_cancel);
		
		ok.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == ok) {
			if (eid.getText().toString().equals(""))
				;
			else
				Isense.experimentId = eid.getText().toString();
		}
		
		finish();
	}	
}
