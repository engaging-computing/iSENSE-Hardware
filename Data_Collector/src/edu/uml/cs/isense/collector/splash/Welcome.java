package edu.uml.cs.isense.collector.splash;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.comm.Connection;
import edu.uml.cs.isense.proj.ProjectCreate;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.waffle.Waffle;

public class Welcome extends Activity {

	public static Context mContext;
	public static Waffle w;
	private SharedPreferences mPrefs;
	public API api;
	
	private CountDownTimer cdt;
	private int actionBarTapCount = 0;
	public static boolean useDev = false;
	
	private static final int PROJECT_SELECTION_REQUESTED = 100;
	private static final int PROJECT_CREATE_REQUESTED    = 101;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		mContext = this;
		w = new Waffle(mContext);
		api = API.getInstance();
		api.useDev(useDev);
		
		mPrefs = getSharedPreferences("PROJID_WELCOME", 0);
		
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
		final Button selectProject = (Button) findViewById(R.id.welcome_select_project);
		selectProject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!Connection.hasConnectivity(mContext))
					w.make(getResources().getString(R.string.need_connectivity_to_do),
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
				else {
					Intent iSetup = new Intent(mContext, Setup.class);
					iSetup.putExtra("from_where", "welcome");
					iSetup.putExtra(ProjectCreate.THEME_NAV_BAR, true);
					startActivityForResult(iSetup, PROJECT_SELECTION_REQUESTED);
				}
			}
		});
		
		final Button noProject = (Button) findViewById(R.id.welcome_no_project);
		noProject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setGlobalProjAndEnableManual("", false);
			}
		});
		
		final Button registerAccount = (Button) findViewById(R.id.welcome_register_for_isense);
		registerAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Connection.hasConnectivity(mContext)) {
					if (api.isUsingDevMode()) {
						Intent iRegister = new Intent(Intent.ACTION_VIEW);
						iRegister.setData(Uri.parse("http://rsense-dev.cs.uml.edu/users/new"));
						startActivity(iRegister);
					} else {
						Intent iRegister = new Intent(Intent.ACTION_VIEW);
						iRegister.setData(Uri.parse("http://isenseproject.org/users/new"));
						startActivity(iRegister);
					}	
				} else {
					w.make(getResources().getString(R.string.need_connectivity_to_do),
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
				}
			}
		});
		
		final Button viewTutorials = (Button) findViewById(R.id.welcome_view_tutorials);
		viewTutorials.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Connection.hasConnectivity(mContext)) {
					if (api.isUsingDevMode()) {
						Intent iTutorials = new Intent(Intent.ACTION_VIEW);
						iTutorials.setData(Uri.parse("http://rsense-dev.cs.uml.edu/tutorials"));
						startActivity(iTutorials);
					} else {
						Intent iTutorials = new Intent(Intent.ACTION_VIEW);
						iTutorials.setData(Uri.parse("http://isenseproject.org/tutorials"));
						startActivity(iTutorials);
					}	
				} else {
					w.make(getResources().getString(R.string.need_connectivity_to_do),
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
				}
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		
		actionBarTapCount = 0;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	
	    	// Give user 10 seconds to switch dev/prod mode
	    	if (actionBarTapCount == 0) {
	    		cdt = new CountDownTimer(5000, 5000) {
	    		     public void onTick(long millisUntilFinished) {}
	    		     public void onFinish() {
	    		         actionBarTapCount = 0;
	    		     }
	    		  }.start();
	    	}
	    	
	    	String other = (useDev) ? "production" : "dev";
	       
	    	switch (++actionBarTapCount) {
	    	case 5:
	    		w.make(getResources().getString(R.string.two_more_taps) 
	    				+ other 
	    				+ getResources().getString(R.string.mode_type));
	    		break;
	    	case 6:
	    		w.make(getResources().getString(R.string.one_more_tap)
	    				+ other 
	    				+ getResources().getString(R.string.mode_type));
	    		break;
	    	case 7:
	    		w.make(getResources().getString(R.string.now_in_mode)
	    				+ other 
	    				+ getResources().getString(R.string.mode_type));
	    		useDev = !useDev;
	    		
	    		if (cdt != null) cdt.cancel();
	    		
	    		if (api.getCurrentUser() != null) {
	    			Runnable r = new Runnable() {
	    				public void run() {
	    					api.deleteSession();
	    					api.useDev(useDev);
	    				}
	    			};
	    			new Thread(r).start();
	    		} else
	    			api.useDev(useDev);
	    		
	    		actionBarTapCount = 0;
	    		break;
	    	}
	    	
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PROJECT_SELECTION_REQUESTED) {
			if (resultCode == RESULT_OK) {

				String projID = mPrefs.getString("project_id", "");
				if (!(projID.equals("") || projID.equals("-1"))) {
					setGlobalProjAndEnableManual(projID, true);
				} 
			}
		} else if (requestCode == PROJECT_CREATE_REQUESTED) {
			if (resultCode == RESULT_OK) {
				int newProjID = data.getIntExtra(ProjectCreate.NEW_PROJECT_ID, 0);
				if (newProjID != 0) {
					setGlobalProjAndEnableManual("" + newProjID, true);
				}
			}
		}
	}
	
	private void setGlobalProjAndEnableManual(String projID, boolean enable) {
		SharedPreferences globalProjPrefs = getSharedPreferences("GLOBAL_PROJ", 0);
		SharedPreferences.Editor mEdit = globalProjPrefs.edit();
		mEdit.putString("project_id", projID).commit();
		mEdit.putString("project_id_dc", projID);
		mEdit.putString("project_id_manual", projID);
		mEdit.putString("project_id_csv", projID);
		mEdit.commit();
		
		Intent iSelectMode = new Intent(mContext, SelectMode.class);
		iSelectMode.putExtra(SelectMode.ENABLE_MANUAL_AND_CSV, enable);
		startActivity(iSelectMode);
	}

}