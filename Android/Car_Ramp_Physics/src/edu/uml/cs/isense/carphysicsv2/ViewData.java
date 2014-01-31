package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ViewData extends Activity {

	Button view, cancel;

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.view_data);

		view = (Button) findViewById(R.id.view);
		cancel = (Button) findViewById(R.id.noview);

		setTitle("View Your Data On iSENSE?");

		view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(CarRampPhysicsV2.dataSetUrl));
				startActivity(i);
				finish();

			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});

	}

}
