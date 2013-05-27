package edu.uml.cs.isense.queue;

import edu.uml.cs.isense.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Activity that allows the alteration of data sets in the queue.
 * Data sets can be renamed, data-altered, or deleted.
 * 
 * @author Mike Stowell and Jeremy Poulin of the iSENSE team.
 * 
 */
public class QueueAlter extends Activity {
	
	private Button rename, changeData, delete, cancel;
	
	protected final static String RETURN_CODE = "return_code";
	protected final static String IS_ALTERABLE = "is_alterable";
	
	protected final static int RENAME = 100;
	protected final static int CHANGE_DATA = 101;
	protected final static int DELETE = 102;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.queueblock_alter);
		super.onCreate(savedInstanceState);
		
		rename     = (Button) findViewById(R.id.queuealter_rename     );
		changeData = (Button) findViewById(R.id.queuealter_change_data);
		delete     = (Button) findViewById(R.id.queuealter_delete     );
		cancel     = (Button) findViewById(R.id.queuealter_cancel     );
		
		Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
			boolean isAlterable = extras.getBoolean(IS_ALTERABLE);
			if (!isAlterable) {
				changeData.setVisibility(View.GONE);
			}
		}
		
		rename.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent iRet = new Intent(QueueAlter.this, QueueLayout.class);
				iRet.putExtra(RETURN_CODE, RENAME);
				setResult(RESULT_OK, iRet);
				finish();
			}	
		});
		
		changeData.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent iRet = new Intent(QueueAlter.this, QueueLayout.class);
				iRet.putExtra(RETURN_CODE, CHANGE_DATA);
				setResult(RESULT_OK, iRet);
				finish();
			}	
		});
		
		delete.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent iRet = new Intent(QueueAlter.this, QueueLayout.class);
				iRet.putExtra(RETURN_CODE, DELETE);
				setResult(RESULT_OK, iRet);
				finish();
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