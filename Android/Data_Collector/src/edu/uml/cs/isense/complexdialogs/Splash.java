package edu.uml.cs.isense.complexdialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.collector.objects.FontFitTextView;
import edu.uml.cs.isense.waffle.Waffle;

public class Splash extends Activity {

	private Context mContext;
	private Waffle w;
	
	private TextView eulaText;
	private CheckBox eulaCheck;
	private FontFitTextView appName;
	private TextView welcome;

	private static final int EULA_REQUESTED = 100;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		mContext = this;

		w = new Waffle(mContext);

		// Apply custom font to "iSENSE Data Collector"
		appName = (FontFitTextView) findViewById(R.id.title_text);
		final Typeface tf1 = Typeface
				.createFromAsset(getAssets(), "facets.otf");
		appName.setTypeface(tf1);

		// Begin animation of abstract_circle
		final RelativeLayout abstractCircle = (RelativeLayout) findViewById(R.id.abstract_layout);
		final Animation rotate = AnimationUtils.loadAnimation(this, R.anim.fastspinner);
		abstractCircle.startAnimation(rotate);
		
			
		// Apply custom font to "Welcome"
		welcome = (TextView) findViewById(R.id.welcome_text);
		final Typeface tf2 = Typeface.createFromAsset(getAssets(),
				"flight_sterwadess.otf");
		welcome.setTypeface(tf2);
		
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

	}

	// Used by EULA
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
	}

}