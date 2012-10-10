package edu.uml.cs.isense.collector.shared;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.uml.cs.isense.collector.R;

public class DeleteDataSetFromUploadQueue extends Activity {
	
	private Button no, yes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.queueblock_delete);
		super.onCreate(savedInstanceState);
		
		no = (Button) findViewById(R.id.queue_delete_no);
		yes = (Button) findViewById(R.id.queue_delete_yes);
		
		no.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();				
			}
			
		});
		
		yes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
			
		});
		
	}
	
}