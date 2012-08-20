package edu.uml.cs.isense.collector.splash;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.waffle.Waffle;

@SuppressWarnings("deprecation")
public class Splash extends TabActivity {

	public static Context mContext;
	public static Waffle w;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		mContext = this;

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

		// Change tab text colors
		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i)
					.findViewById(android.R.id.title);
			tv.setTextColor(Color.parseColor("#FFFFFF"));
		}

		// Add specs to host
		tabHost.addTab(mainSpec);
		tabHost.addTab(aboutSpec);
		tabHost.addTab(guideSpec);

		// Set listeners for the "Continue" and "Exit" buttons
		final Button cont = (Button) findViewById(R.id.splash_continue);
		cont.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				setResult(RESULT_OK);
				finish();

			}
		});

		final Button exit = (Button) findViewById(R.id.splash_exit);
		exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

	}

}