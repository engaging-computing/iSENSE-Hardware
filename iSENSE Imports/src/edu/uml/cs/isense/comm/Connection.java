package edu.uml.cs.isense.comm;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class is designed merely to determine if you have an Internet connection on an Android
 * device.  This method was originally abstracted from the API class in an effort to remove
 * the Android import requirements of the API class and make API entirely Java-based.
 * 
 * @author stowellm
 */
public class Connection {

	/**
	 * Returns status on whether you are connected to the Internet.
	 * 
	 * @param c
	 * 		- The context of the activity you are calling this method from.
	 * @return current connection status (true if connected to the internet, false otherwise)
	 */
	public static boolean hasConnectivity(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected());
	}
}
