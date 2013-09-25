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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.waffle.Waffle;

public class Welcome extends Activity {

	public static Context mContext;
	public static Waffle w;
	private SharedPreferences mPrefs;
	public API api;
	
	private static final int PROJECT_SELECTION_REQUESTED = 100;
	private static final int PROJECT_CREATE_REQUESTED    = 101;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		mContext = this;
		w = new Waffle(mContext);
		api = API.getInstance(mContext);
		api.useDev(true);
		
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
		}

		// Set listeners for the buttons
		final Button selectProject = (Button) findViewById(R.id.welcome_select_project);
		selectProject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!api.hasConnectivity())
					w.make("You need to have internet connectivity to do this",
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
				else {
					Intent iSetup = new Intent(mContext, Setup.class);
					iSetup.putExtra("from_where", "welcome");
					startActivityForResult(iSetup, PROJECT_SELECTION_REQUESTED);
				}
			}
		});

		final Button createProject = (Button) findViewById(R.id.welcome_create_project);
		createProject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iProjCreate = new Intent(mContext, ProjectCreate.class);
				startActivityForResult(iProjCreate, PROJECT_CREATE_REQUESTED);
			}
		});
		
		final Button noProject = (Button) findViewById(R.id.welcome_no_project);
		noProject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent iSelectMode = new Intent(mContext, SelectMode.class);
				iSelectMode.putExtra(SelectMode.ENABLE_MANUAL_ENTRY, false);
				startActivity(iSelectMode);
			}
		});

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
					setGlobalProjAndEnableManual(projID);
				} 
			}
		} else if (requestCode == PROJECT_CREATE_REQUESTED) {
			if (resultCode == RESULT_OK) {
				int newProjID = data.getIntExtra(ProjectCreate.NEW_PROJECT_ID, 0);
				if (newProjID != 0) {
					setGlobalProjAndEnableManual("" + newProjID);
				} else {
					// TODO - we got a bad return
				}
				
			}
		}
	}
	
	private void setGlobalProjAndEnableManual(String projID) {
		SharedPreferences globalProjPrefs = getSharedPreferences("GLOBAL_PROJ", 0);
		SharedPreferences.Editor mEdit = globalProjPrefs.edit();
		mEdit.putString("project_id", projID).commit();
		mEdit.putString("project_id_dc", projID);
		mEdit.putString("project_id_manual", projID);
		mEdit.putString("project_id_csv", projID);
		mEdit.commit();
		
		Intent iSelectMode = new Intent(mContext, SelectMode.class);
		iSelectMode.putExtra(SelectMode.ENABLE_MANUAL_ENTRY, true);
		startActivity(iSelectMode);
	}

}