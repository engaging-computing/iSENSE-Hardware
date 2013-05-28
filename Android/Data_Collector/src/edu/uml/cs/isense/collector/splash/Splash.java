package edu.uml.cs.isense.collector.splash;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import edu.uml.cs.isense.collector.DataCollector;
import edu.uml.cs.isense.collector.ManualEntry;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.collector.dialogs.FirstTimeUse;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.waffle.Waffle;

@SuppressWarnings("deprecation")
public class Splash extends TabActivity {

	public static Context mContext;
	public static Waffle w;
	public static RestAPI rapi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		mContext = this;

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		rapi.useDev(false);

		w = new Waffle(mContext);

		TabHost tabHost = getTabHost();

		// Main Tab
		TabSpec mainSpec = tabHost.newTabSpec("Welcome");
		mainSpec.setIndicator("Welcome",
				getResources().getDrawable(R.drawable.icon_splash_main));
		Intent iMain = new Intent(mContext, SplashMain.class);
		mainSpec.setContent(iMain);

		// About Tab
		TabSpec aboutSpec = tabHost.newTabSpec("About");
		aboutSpec.setIndicator("About",
				getResources().getDrawable(R.drawable.icon_splash_about));
		Intent iAbout = new Intent(mContext, SplashAbout.class);
		aboutSpec.setContent(iAbout);

		// Guide Tab
		TabSpec guideSpec = tabHost.newTabSpec("Guide");
		guideSpec.setIndicator("Guide",
				getResources().getDrawable(R.drawable.icon_splash_guide));
		Intent iGuide = new Intent(mContext, SplashGuide.class);
		guideSpec.setContent(iGuide);

		// Add specs to host
		tabHost.addTab(mainSpec);
		tabHost.addTab(aboutSpec);
		tabHost.addTab(guideSpec);

		// Change tab text colors
		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i)
					.findViewById(android.R.id.title);
			tv.setTextColor(Color.parseColor("#666666"));
		}

		// Set listeners for the "Continue" and "Exit" buttons
		final Button cont = (Button) findViewById(R.id.splash_auto);
		cont.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent iDataCollector = new Intent(Splash.this,
						DataCollector.class);
				startActivity(iDataCollector);

			}
		});

		final Button exit = (Button) findViewById(R.id.splash_manual);
		exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent iManual = new Intent(Splash.this, ManualEntry.class);
				startActivity(iManual);

			}
		});
		
		SharedPreferences mPrefs = getSharedPreferences("first_time_use", 0);
		if (mPrefs.getBoolean("first_use", false) == false) {
			SharedPreferences.Editor mEdit = mPrefs.edit();
			mEdit.putBoolean("first_use", true);
			mEdit.commit();
			Intent iFirstTimeUse = new Intent(Splash.this, FirstTimeUse.class);
			startActivity(iFirstTimeUse);
		}

	}

	// Overridden to prevent user from exiting app unless back button is pressed
	// twice
	@Override
	public void onBackPressed() {

		if (!w.isDisplaying)
			w.make("Double press \"Back\" to exit");
		else if (w.canPerformTask)
			super.onBackPressed();

	}

}