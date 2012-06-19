package edu.uml.cs.pincomm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WifiDisabled extends Activity implements OnClickListener {
	
	Button yes, no;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_page);
		
		yes = (Button) findViewById(R.id.yes);
		no =  (Button) findViewById(R.id.no);
		
		yes.setOnClickListener(this);
		no.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == yes) {
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		}
		
		finish();
	}	
}
