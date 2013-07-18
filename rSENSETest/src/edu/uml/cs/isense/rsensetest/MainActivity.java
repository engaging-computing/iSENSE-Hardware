package edu.uml.cs.isense.rsensetest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

import java.net.CookiePolicy;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	Button login, getusers, getprojects;
	TextView status;
	API api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		login = (Button) findViewById(R.id.btn_login);
		getusers = (Button) findViewById(R.id.btn_getusers);
		getprojects = (Button) findViewById(R.id.btn_getprojects);
		status = (TextView) findViewById(R.id.txt_results);

		login.setOnClickListener(this);
		getusers.setOnClickListener(this);
		getprojects.setOnClickListener(this);
		
		api = new API();
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
			api.createSession("testguy", "1");
		} else if ( v == getusers ) {
			status.setText("clicked get users");
			api.getUsers();
		} else if ( v == getprojects ) {
			status.setText("clicked get projects");
			api.getProjects();
		}
	}

}
