package edu.uml.cs.isense.collector.splash;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.collector.DataCollector;
import edu.uml.cs.isense.collector.ManualEntry;
import edu.uml.cs.isense.collector.R;


public class SelectMode extends Activity {

	private static Context mContext;



	public static final String ENABLE_MANUAL = "enable_manual";

	private static final int LOGIN_REQUESTED = 101;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_mode);

		mContext = this;


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
			
			// make the actionbar clickable
			bar.setDisplayHomeAsUpEnabled(true);
		}

		// Set listeners for the buttons
		final Button dataCollector = (Button) findViewById(R.id.select_mode_data_collector);
		dataCollector.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iDC = new Intent(mContext, DataCollector.class);
				startActivity(iDC);
			}
		});

		final Button manualEntry = (Button) findViewById(R.id.select_mode_manual_entry);
		manualEntry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iME = new Intent(mContext, ManualEntry.class);
				startActivity(iME);
			}
		});
		String manualEntryText = "<font COLOR=\"#0066FF\">"
				+ "Manually Enter Data" + "</font>" + "<br/>"
				+ "<font COLOR=\"#D9A414\">" + "(requires project)" + "</font>";
		manualEntry.setText(Html.fromHtml(manualEntryText));

		
		

		// Determine if we should disable manual entry 
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			boolean en = extras.getBoolean(ENABLE_MANUAL);
			if (!en) {
				manualEntry.setEnabled(false);
				String m = "<font COLOR=\"#0066FF\">" + "Manually Enter Data"
						+ "</font>" + "<br/>" + "<font COLOR=\"#B88804\">"
						+ "(requires project)" + "</font>";
				manualEntry.setText(Html.fromHtml(m));
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == RESULT_OK) {
			} 
		}
	}

	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	onBackPressed();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}