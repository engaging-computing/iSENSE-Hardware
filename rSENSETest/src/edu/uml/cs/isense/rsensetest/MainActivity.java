package edu.uml.cs.isense.rsensetest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	Button login, getusers;
	TextView status;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		login = (Button) findViewById(R.id.btn_login);
		getusers = (Button) findViewById(R.id.btn_getusers);
		status = (TextView) findViewById(R.id.txt_results);
		
		login.setOnClickListener(this);
		getusers.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if ( v == login ) {
			status.setText("clicked login");
		} else if ( v == getusers ) {
			status.setText("clicked get users");
		}
	}

}
