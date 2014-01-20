package edu.uml.cs.isense.datawalk_v2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class Datawalk_Service extends Service {
private static final String TAG	= Datawalk_Service.class.getSimpleName();
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(TAG, "onCreate'd");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "onDestroy'd");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind'd");
		return null;
	}

}
