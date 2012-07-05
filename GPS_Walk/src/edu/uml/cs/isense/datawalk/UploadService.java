//package edu.uml.cs.isense.datawalk;
/*package edu.cs.uml.isense.datawalk;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class UploadService extends Service {
	
	public static boolean isUploading = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		if (DataWalk.running == true) {
			elapsedMillis = 0;
			len  = 0; 
			len2 = 0;
			i    = 0;
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Toast.makeText( getBaseContext() , "Data recording interrupted! Time values may be inconsistent." , Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

			if (mSensorManager != null) {
				mSensorManager.registerListener(DataWalk.this, 
						mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  
						SensorManager.SENSOR_DELAY_FASTEST);
				mSensorManager.registerListener(DataWalk.this, 
						mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 
						SensorManager.SENSOR_DELAY_FASTEST);
			}
			
			timeTimer = new Timer();
			timeTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
				
					elapsedMillis += INTERVAL;
								
					if(i >= 360) {
					
						timeTimer.cancel();
				
					} else {	
			
						i++; len++; len2++;	
			
						JSONArray dataJSON = new JSONArray();
						JSONArray dataSetNew = new JSONArray();
		    
						try {
				
							/* Accel-x    */ //dataJSON.put(toThou.format(accel[0]));
							/* Accel-y    */ //dataJSON.put(toThou.format(accel[1]));
							/* Accel-z    */ //dataJSON.put(toThou.format(accel[2]));
							/* Accel-Total*/ //dataJSON.put(toThou.format(accel[3]));
							/* Latitude   */ //dataJSON.put(loc.getLatitude());
							/* Longitude  */ //dataJSON.put(loc.getLongitude());
							/* Heading    */ //dataJSON.put(toThou.format(orientation[0]));
							/* Magnetic-x */ //dataJSON.put(rawMag[0]);
							/* Magnetic-y */ //dataJSON.put(rawMag[1]);
							/* Magnetic-z */ //dataJSON.put(rawMag[2]);
							/* Time       */ //dataJSON.put(elapsedMillis); 
	/*			
							dataSetNew.put(dataJSON);
							dataSet = dataSetNew;
							Log.d("DataSet", "DataSet" + dataSet.toString());
				
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
		});		
		
		

		super.onCreate();
	}
	@Override
	public void onDestroy() {
			isUploading = false;
	}
	


}
*/