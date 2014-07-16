package edu.uml.cs.isense.queue;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.uml.cs.isense.R;

/**
 * Activity that asks for confirmation from the user if he or she
 * is sure he or she would like to delete all selected data sets. 
 * 
 * @author Mike Stowell and Jeremy Poulin of the iSENSE team.
 *
 */
public class QueueDeleteSelected extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.queue_delete_selected);
		
		final Button cancel = (Button) findViewById(R.id.queue_delete_selected_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		final Button ok = (Button) findViewById(R.id.queue_delete_selected_ok);
		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
		
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}
	
}
