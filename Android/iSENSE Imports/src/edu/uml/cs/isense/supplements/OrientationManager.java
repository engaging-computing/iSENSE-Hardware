package edu.uml.cs.isense.supplements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.view.Surface;

/**
 * Class that enables/disables the ability to rotate the Android device's orientation.
 * 
 * @author User "roboshed" of stackoverflow.com
 *
 */
public class OrientationManager {
	
	/**
	 * Locks the user's orientation in its current state.
	 * 
	 * @param activity Activity whose orientation will be locked.
	 */
	@SuppressLint("NewApi")
	public static void disableRotation(Activity activity) {       
	    final int orientation = activity.getResources().getConfiguration().orientation;
	    final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	    final int build = Build.VERSION.SDK_INT;

	    // Copied from Android docs, since we don't have these values in Froyo 2.2
	    int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
	    int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;

	    if (build <= Build.VERSION_CODES.FROYO) {
	        SCREEN_ORIENTATION_REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	        SCREEN_ORIENTATION_REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	    }

	    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
	    	
	        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
	        	
	        	if (build < Build.VERSION_CODES.JELLY_BEAN && build >= Build.VERSION_CODES.HONEYCOMB)
	        		activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
	        	else
	        		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        	
	        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        }
	        
	    } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
	    	
	        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
	        	
	        	if (build < Build.VERSION_CODES.JELLY_BEAN && build >= Build.VERSION_CODES.HONEYCOMB)
	        		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        	else
	        		activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
	        	
	        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE)  {
	        	activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
	        }
	        
	    }
	}

	/**
	 * Allows the rotation of the user's activity.
	 * 
	 * @param activity Activity whose orientation will re-enabled to rotate.
	 */
	public static void enableRotation(Activity activity) {
	    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
}
