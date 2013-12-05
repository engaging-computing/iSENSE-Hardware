package edu.uml.cs.isense.proj;

import edu.uml.cs.isense.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

/**
 * This dialog themed activity displays when the user attempts
 * to scan a QR code in the
 * {@link edu.uml.cs.isense.proj.Setup Setup} class but has not
 * yet installed the ZXing Barcode Scanner Android application.
 * 
 * No public implementation of this class is necessary.
 * 
 * @author iSENSE Android Development Team
 */
public class NoQR extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_qr);
		
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		final Button visit = (Button) findViewById(R.id.no_qr_visit);
		visit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
		
		final Button cancel = (Button) findViewById(R.id.no_qr_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
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
