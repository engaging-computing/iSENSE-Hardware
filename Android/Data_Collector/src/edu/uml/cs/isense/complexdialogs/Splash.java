package edu.uml.cs.isense.complexdialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.waffle.Waffle;

public class Splash extends Activity {

	private Context mContext;
	private Waffle w;
	private TextView eulaText;
	private CheckBox eulaCheck;
	
	private static final int EULA_REQUESTED = 100;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		mContext = this;

		w = new Waffle(mContext);
		
		final TextView welcome = (TextView) findViewById(R.id.welcome_text);
		final Typeface tf = Typeface.createFromAsset(getAssets(), "flight_sterwadess.otf");
		welcome.setTypeface(tf);
		
		eulaCheck = (CheckBox) findViewById(R.id.eula_check);
		eulaCheck.setEnabled(false);
		
		eulaText = (TextView) findViewById(R.id.eula_text);
		eulaText.setTextColor(Color.WHITE);
		eulaText.setText("I have read and agree to the " + Html.fromHtml("<font color=\"blue\"><u>EULA</u></font>") + ".");
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
		spans.setSpan(clickSpan, spans.length() - 5, spans.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		final Button cont = (Button) findViewById(R.id.splash_continue);
		cont.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (eulaCheck.isChecked()) {
					setResult(RESULT_OK);
					finish();
				} else
					w.make("You must accept the EULA to continue!", Toast.LENGTH_LONG, "x");
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
	
}