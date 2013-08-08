package edu.uml.cs.isense.collector.splash;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.collector.DataCollector;
import edu.uml.cs.isense.collector.ManualEntry;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.waffle.Waffle;

public class Splash extends FragmentActivity {

	public static Context mContext;
	public static Waffle w;
	public static RestAPI rapi;

	@SuppressLint("NewApi")
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
		
		// Action bar customization for API >= 14
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			ActionBar bar = getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#111133")));
			bar.setIcon(getResources()
					.getDrawable(R.drawable.rsense_logo_right));
			bar.setDisplayShowTitleEnabled(false);
			int actionBarTitleId = Resources.getSystem().getIdentifier(
					"action_bar_title", "id", "android");
			if (actionBarTitleId > 0) {
				TextView title = (TextView) findViewById(actionBarTitleId);
				if (title != null) {
					title.setTextColor(Color.WHITE);
					title.setTextSize(24.0f);
				}
			}
		}

		// Set up the fragment pager
		ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
		PagerTabStrip mTitles = (PagerTabStrip) findViewById(R.id.pager_title_strip);
		mTitles.setDrawFullUnderline(true);
		mTitles.setTabIndicatorColor(Color.parseColor("#2288FF"));
		mTitles.setBackgroundColor(Color.parseColor("#DEDEDE"));
		PagerAdapter mAdapter = new PagerAdapter(getSupportFragmentManager());

		// Add all the fragments to our adapter
		Fragment fragMain = new SplashMain();
		mAdapter.addFragment(fragMain, "Welcome");

		Fragment fragAbout = new SplashAbout();
		mAdapter.addFragment(fragAbout, "About");

		Fragment fragGuide = new SplashGuide();
		mAdapter.addFragment(fragGuide, "Guide");

		mViewPager.setAdapter(mAdapter);

		// Set listeners for the "Automatic" and "Manual" buttons
		final Button automatic = (Button) findViewById(R.id.splash_auto);
		automatic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent iDataCollector = new Intent(Splash.this,
						DataCollector.class);
				startActivity(iDataCollector);

			}
		});

		final Button manual = (Button) findViewById(R.id.splash_manual);
		manual.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent iManual = new Intent(Splash.this, ManualEntry.class);
				startActivity(iManual);

			}
		});

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}