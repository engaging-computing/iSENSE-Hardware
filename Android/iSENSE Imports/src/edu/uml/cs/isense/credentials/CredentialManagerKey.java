package edu.uml.cs.isense.credentials;

import edu.uml.cs.isense.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

public class CredentialManagerKey extends Activity  {

	final int PROJECT_REQUESTED = 101;
	private static String key = "";
	EditText newKey;
	Button bCancel;
	Button bOK;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.credential_manager_add_key);
        
        newKey = (EditText) findViewById(R.id.edittext_key);

		bOK = (Button) findViewById(R.id.button_ok);
		bCancel = (Button) findViewById(R.id.button_cancel);
		
	    newKey.setOnTouchListener(new OnTouchListener() {

	    	
			public boolean onTouch(View arg0, MotionEvent arg1) {
				newKey.setError(null);
				return false;
			}
	    	
	    });
	

		bOK.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (newKey.getText().length() != 0) {
					key = newKey.getText().toString();
					setResult(Activity.RESULT_OK);
					finish();
				} else if (newKey.getText().length() == 0){
					newKey.setError("Key can not be empty.");
				}
			}
		});
		
		bCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
			
		});
			
	}
	
	public static String getKey() {
		return key;
	}
	
}
