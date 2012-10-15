package edu.uml.cs.isense.collector.shared;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.uml.cs.isense.collector.R;

public class QueueAlter extends Activity {
	
	private Button rename, changeData, delete, cancel;
	
	public final static String RETURN_CODE = "return_code";
	public final static int RENAME = 100;
	public final static int CHANGE_DATA = 101;
	public final static int DELETE = 102;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.queueblock_alter);
		super.onCreate(savedInstanceState);
		
		rename     = (Button) findViewById(R.id.queuealter_rename     );
		changeData = (Button) findViewById(R.id.queuealter_change_data);
		delete     = (Button) findViewById(R.id.queuealter_delete     );
		cancel     = (Button) findViewById(R.id.queuealter_cancel     );
		
		rename.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iRet = new Intent(QueueAlter.this, QueueUploader.class);
				iRet.putExtra(RETURN_CODE, RENAME);
				setResult(RESULT_OK, iRet);
				finish();
			}	
		});
		
		changeData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iRet = new Intent(QueueAlter.this, QueueUploader.class);
				iRet.putExtra(RETURN_CODE, CHANGE_DATA);
				setResult(RESULT_OK, iRet);
				finish();
			}	
		});
		
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iRet = new Intent(QueueAlter.this, QueueUploader.class);
				iRet.putExtra(RETURN_CODE, DELETE);
				setResult(RESULT_OK, iRet);
				finish();
			}	
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}	
		});
		
	}
	
}