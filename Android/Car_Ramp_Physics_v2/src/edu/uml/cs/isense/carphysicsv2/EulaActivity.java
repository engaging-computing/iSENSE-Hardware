package edu.uml.cs.isense.carphysicsv2;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.R;

public class EulaActivity extends Activity {

	Button accept, decline;
	TextView txt;
	public static String EULA_PREFIX = "eula_";

	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = getPackageManager().getPackageInfo(getPackageName(),
					PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.eula);
		setTitle(CarRampPhysicsV2.mContext.getString(R.string.app_name) + " v" + getPackageInfo().versionName);

		accept = (Button) findViewById(R.id.acceptButton);
		decline = (Button) findViewById(R.id.declineButton);
		txt = (TextView) findViewById(R.id.eulatext);

		String message = CarRampPhysicsV2.mContext.getString(R.string.updates)
				+ "\n\n"
				+ CarRampPhysicsV2.mContext.getString(R.string.about_app);

		txt.setText(message);

		accept.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});

		decline.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

}
