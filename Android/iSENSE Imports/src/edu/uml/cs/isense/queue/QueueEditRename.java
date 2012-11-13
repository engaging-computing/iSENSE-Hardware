package edu.uml.cs.isense.queue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.R;

/**
 * Activity that allows the renaming of a data set.
 * 
 * @author Mike Stowell and Jeremy Poulin of the iSENSE team.
 *
 */
public class QueueEditRename extends Activity {

	private EditText rename;
	private Button okay, cancel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.queueedit_rename);
		super.onCreate(savedInstanceState);

		rename = (EditText) findViewById(R.id.queueedit_edit);

		okay = (Button) findViewById(R.id.queueedit_okay);
		cancel = (Button) findViewById(R.id.queueedit_cancel);

		okay.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String newName = rename.getText().toString();
				if (!newName.equals("")) {
					Intent iRet = new Intent(QueueEditRename.this,
							QueueLayout.class);
					iRet.putExtra("new_name", newName);
					setResult(RESULT_OK, iRet);
					finish();
				} else {
					setResult(RESULT_CANCELED);
					finish();
				}
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

}