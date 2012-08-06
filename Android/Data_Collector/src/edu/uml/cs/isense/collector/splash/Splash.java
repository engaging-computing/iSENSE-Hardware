package edu.uml.cs.isense.collector.splash;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.waffle.Waffle;

@SuppressWarnings("deprecation")
public class Splash extends TabActivity {

	private Context mContext;
	private Waffle w;
	
	//private TextView eulaText;
	//private CheckBox eulaCheck;
	//private FontFitTextView appName;

	//private static final int EULA_REQUESTED = 100;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		mContext = this;

		w = new Waffle(mContext);
		
		TabHost tabHost = getTabHost();
		
		// Main Tab
		TabSpec mainSpec = tabHost.newTabSpec("Main");
		mainSpec.setIndicator("Main");
		Intent iMain = new Intent(mContext, SplashMain.class);
		mainSpec.setContent(iMain);
		
		// Guide Tab
		TabSpec guideSpec = tabHost.newTabSpec("Guide");
		guideSpec.setIndicator("Guide");
		Intent iGuide = new Intent(mContext, SplashGuide.class);
		guideSpec.setContent(iGuide);
		
		// Eula Tab
		TabSpec eulaSpec = tabHost.newTabSpec("Eula");
		eulaSpec.setIndicator("Eula");
		Intent iEula = new Intent(mContext, SplashEula.class);
		eulaSpec.setContent(iEula);
		
		// Add specs to host
		tabHost.addTab(mainSpec);
		tabHost.addTab(guideSpec);
		tabHost.addTab(eulaSpec);
		

		/*// Apply custom font to "iSENSE Data Collector"
		appName = (FontFitTextView) findViewById(R.id.title_text);
		final Typeface tf1 = Typeface
				.createFromAsset(getAssets(), "facets.otf");
		appName.setTypeface(tf1);

		// Begin animation of abstract_circle
		final RelativeLayout abstractCircle = (RelativeLayout) findViewById(R.id.abstract_layout);
		final Animation rotate = AnimationUtils.loadAnimation(this, R.anim.fastspinner);
		abstractCircle.startAnimation(rotate);
		
		// Set "EULA" in the CheckBox's string to become clickable
		eulaCheck = (CheckBox) findViewById(R.id.eula_check);

		eulaText = (TextView) findViewById(R.id.eula_text);
		eulaText.setTextColor(Color.WHITE);
		eulaText.setText("I have read and agree to the "
				+ Html.fromHtml("<font color=\"blue\"><u>EULA</u></font>")
				+ ".");
		eulaText.setMovementMethod(LinkMovementMethod.getInstance());
		Spannable spans = (Spannable) eulaText.getText();
		ClickableSpan clickSpan = new ClickableSpan() {
			@Override
			public void onClick(View v) {

				PackageInfo versionInfo = getPackageInfo();
				Intent iEula = new Intent(mContext, Eula.class);
				iEula.putExtra("versionName", versionInfo.versionName);
				startActivityForResult(iEula, EULA_REQUESTED);
			}
		};
		spans.setSpan(clickSpan, spans.length() - 5, spans.length() - 1,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Set listeners for the "Continue" and "Exit" buttons
		final Button cont = (Button) findViewById(R.id.splash_continue);
		cont.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (eulaCheck.isChecked()) {
					setResult(RESULT_OK);
					finish();
				} else
					w.make("You must accept the EULA to continue!",
							Toast.LENGTH_LONG, "x");
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
		
		final Button guide = (Button) findViewById(R.id.splash_how_to);
		guide.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iAppGuide = new Intent(Splash.this, ApplicationGuide.class);
				startActivity(iAppGuide);
			}
		}); */

	}

	/*// Used by EULA
	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	// Method to check the EULA CheckBox off if the user has accepted the EULA
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == EULA_REQUESTED) {
			if (resultCode == RESULT_OK) {
				eulaCheck.setChecked(true);
			} else if (resultCode == RESULT_CANCELED) {
				eulaCheck.setChecked(false);
			}
		}
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {	
		super.onConfigurationChanged(newConfig);	
		onCreate(null);
	} */

}