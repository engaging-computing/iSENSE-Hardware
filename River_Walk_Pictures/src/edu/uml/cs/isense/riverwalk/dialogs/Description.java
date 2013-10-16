package edu.uml.cs.isense.riverwalk.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.uml.cs.isense.riverwalk.R;

public class Description extends Activity {
	
	public static String photo_description;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.description);
	
		if (android.os.Build.VERSION.SDK_INT >= 11)
			setFinishOnTouchOutside(false);
		
		final Button ok = (Button) findViewById(R.id.description_okay);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText descriptionObject = (EditText) findViewById(R.id.editTextDescribe);
				photo_description = descriptionObject.getText().toString();
				finish();
			}
		});	
	}
}