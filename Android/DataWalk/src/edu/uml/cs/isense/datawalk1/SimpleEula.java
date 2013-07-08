package edu.uml.cs.isense.datawalk1;

import edu.uml.cs.isense.datawalk1.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class SimpleEula {

	private String aboutApp_PREFIX = "aboutApp_";
	private Activity mActivity;
	private Activity mainActivity;

	public SimpleEula(Activity context) {
		mActivity = context;
		mainActivity = (Activity) DataWalk.mContext;
	}

	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	public AlertDialog.Builder show() {
		PackageInfo versionInfo = getPackageInfo();

		// the aboutAppKey changes every time you increment the version number in the AndroidManifest.xml
		final String aboutAppKey = aboutApp_PREFIX + versionInfo.versionCode;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
		boolean hasBeenShown = prefs.getBoolean(aboutAppKey, false);
    
		if(hasBeenShown == false) {

			// Show the aboutApp
			String title = mActivity.getString(R.string.app_name) + " v" + versionInfo.versionName;
            
			//Includes the updates as well so users know what changed.
			String message = mActivity.getString(R.string.aboutApp);

			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("Accept", new Dialog.OnClickListener() {
        
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					// Mark this version as read.
					SharedPreferences.Editor editor = prefs.edit();
                	editor.putBoolean(aboutAppKey, true);
                	editor.commit();
                	dialogInterface.dismiss();
				}
			})
			.setNegativeButton("Decline", new Dialog.OnClickListener() {
        
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					mActivity.finish();
					mainActivity.finish();
				}
			})
			.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					mActivity.finish();
					mainActivity.finish();
					
				}
			});
			
			return builder;
			//builder.create().show();
		}
		return null;
	}

}
