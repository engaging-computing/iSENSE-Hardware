package edu.uml.cs.isense.datawalk_v2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.util.Timer;
import java.util.TimerTask;

import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.waffle.Waffle;


public class Datawalk_Service extends Service {

    private static final String TAG	= Datawalk_Service.class.getSimpleName();


    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    public MyLocationListener listener;


    private Location loc;
    private Location prevLoc;
    private Location firstLoc;

    private float accel[];
    private JSONArray dataSet;

    private int elapsedMillis = 0;

    private Timer Timer;
    private Timer recordTimer;
    private int timerTick = 0;

    private final int DEFAULT_INTERVAL = 10000;
    private int mInterval = DEFAULT_INTERVAL;

    private final int TIMER_LOOP = 1000;

    private int dataPointCount = 0;

    public static boolean running = false;

    /* Distance and Velocity */
    float distance = 0;
    float velocity = 0;
    float deltaTime = 0;
    boolean bFirstPoint = true;
    float totalDistance = 0;

    Intent intent;

    private String loginName = "";
    private String loginPass = "";
    private String projectURL = "";
    private String dataSetName = "";
    private int dataSetID = -1;


    /**
     * This is called when service is first created. The location manager is initiated but no data is
     * being recorded at this point
     */

    @Override
	public void onCreate() {
		super.onCreate();
        Log.e("oncreate", "");
        running = true;

        // GPS
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initLocationManager();

        //waitingforGPS done in Datawalk
//        waitingForGPS();

        // Sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

	}

    /**
     * This is called to start recording data
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("onStartCommand", "");

        //record data
        runRecordingTimer(intent);


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public boolean stopService(Intent name) {
        // Cancel the recording timer
        if (recordTimer != null)
            recordTimer.cancel();

        // Create the name of the session using the entered name
        dataSetName = "Testing"; //firstName + " " + lastInitial; //TODO real datasetname

        // Get user's project #, or the default if there is none
        // saved

        String projectID = "10"; //TODO real project

        // Save the newest DataSet to the Upload Queue if it has at
        // least 1 point


        QDataSet ds = new QDataSet(dataSetName,
                "Data Points: " + dataPointCount,
                QDataSet.Type.DATA,
                dataSet.toString(),
                null,
                projectID,
                null);

        ds.setRequestDataLabelInOrder(true);
        if (dataPointCount > 0) {
            uq.addDataSetToQueue(ds);         // TODO pass in queue to add data to it

        }



        return super.stopService(name);
    }


    /**
     *
     */
	@Override
	public void onDestroy() {

        Log.e("onDestroy", "");

        if (mLocationManager != null)
            mLocationManager.removeUpdates(listener);

//        if (mSensorManager != null)
//            mSensorManager.unregisterListener(listener);

        super.onDestroy();
	}

    @Override
    public IBinder onBind(Intent intent) {



        return null;
    }





    /**
     * Runs the main timer that records data and updates the main UI every
     * second.
     */
    void runRecordingTimer(Intent intent) {

//        // Start the sensor manager so we can get accelerometer data
//        mSensorManager.registerListener(intent.this,
//                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_FASTEST);

        // Prepare new containers where our recorded values will be stored
        dataSet = new JSONArray();
        accel = new float[4];

        // Reset timer variables
        final long startTime = System.currentTimeMillis();

        elapsedMillis = 0;
        timerTick = 0;

        // Rajia Set First Point to false hopefully this will fix the big
        // velocity issue
        bFirstPoint = true;

        // Initialize Total Distance
        totalDistance = 0;

        // Creates a new timer that runs every second
        recordTimer = new Timer();
        recordTimer.scheduleAtFixedRate(new TimerTask() {

            public void run() {

                // Increase the timerTick count
                timerTick++;

                // Rajia: Begin Distance and Velocity Calculation
                // : Only if GPS is working

                // Convert Interval to Seconds
                int nSeconds = mInterval / 1000;


                if (timerTick % nSeconds == 0) {

                    // For first point we do not have a previous location
                    // yet
                    // This will happen only once
                    if (bFirstPoint) {
                        prevLoc.set(loc);
                        bFirstPoint = false;
                        // Also Try this for total distance
                        firstLoc.set(loc);
                    }
                    distance = loc.distanceTo(prevLoc);

                    // Calculate Velocity
                    velocity = distance / nSeconds;

                    // Rajia: Now this location will be the previous one the
                    // next time we get here
                    prevLoc.set(loc);

                    // Rajia Accumlate total distance
                    totalDistance += distance;
                }




                //TODO create handler to update ui while recording
//                // Update the main UI with the correct number of seconds
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (timerTick == 1) {
//                            elapsedTimeTV.setText("Time Elapsed: " + timerTick
//                                    + " second");
//                        } else {
//                            elapsedTimeTV.setText("Time Elapsed: " + timerTick
//                                    + " seconds");
//                        }
//
//                        // Update distance and velocity text boxes
//                        distanceTV.setText("Distance: "
//                                + roundTwoDecimals(totalDistance * 0.000621371)
//                                + " Miles " + roundTwoDecimals(totalDistance)
//                                + " Meters");
//
//                        velocityTV.setText("Velocity: "
//                                + roundTwoDecimals(velocity * 2.23694)
//                                + " MPH " + roundTwoDecimals(velocity)
//                                + " M/Sec    ");
//
//                    }
//                });

                // Every n seconds which is determined by interval
                // (not including time 0)
                if ((timerTick % (mInterval / 1000)) == 0 && timerTick != 0) {

                    // Prepare a new row of data
                    JSONArray dataJSON = new JSONArray();

                    // Determine how long you've been recording for
                    elapsedMillis += mInterval;
                    long time = startTime + elapsedMillis;

                    try {

                        // Store new values into JSON Object
                        dataJSON.put("u " + time);
                        dataJSON.put(accel[3]);
                        dataJSON.put(velocity);
                        dataJSON.put(totalDistance);
                        dataJSON.put(loc.getLatitude());
                        dataJSON.put(loc.getLongitude());

                        // Save this data point if GPS says it has a lock


                            dataSet.put(dataJSON);

                            // Updated the number of points recorded here and on
                            // the main UI
                            dataPointCount++;


                            //TODO update pointcount on ui
//                            runOnUiThread(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    pointsUploadedTV
//                                            .setText("Points Recorded: "
//                                                    + dataPointCount);
//                                }
//
//                            });


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }, 0, TIMER_LOOP);

    }

    /**
     * Sets up the locations manager so that it request GPS permission if
     * necessary and gets only the most accurate points.
     */
    private void initLocationManager() {

        // Set the criteria to points with fine accuracy
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        listener = new MyLocationListener();


        mLocationManager.requestLocationUpdates(
                mLocationManager.getBestProvider(criteria, true), 0, 0,
                listener);


        // Save new GPS points in our loc variable
        loc = new Location(mLocationManager.getBestProvider(criteria, true));

        prevLoc = loc;
        firstLoc = loc;
    }

//    /**
//     * Starts a timer that displays gps points when they are found and the
//     * waiting for gps loop when they are not.
//     */
//    private void waitingForGPS() {
//
//        // Creates the new timer to update the main UI every second
//        gpsTimer = new Timer();
////        gpsTimer.scheduleAtFixedRate(new TimerTask() {
////
////            @Override
////            public void run() {
////
////                runOnUiThread(new Runnable() {
////
////                    @Override
////                    public void run() {
////
////                        // Show the GPS coordinate on the main UI, else continue
////                        // with our loop.
////                        if (gpsWorking) {
////                            latitudeTV.setText(getResources().getString(
////                                    R.string.latitude)
////                                    + " " + loc.getLatitude());
////                            longitudeTV.setText(getResources().getString(
////                                    R.string.longitude)
////                                    + " " + loc.getLongitude());
////                        } else {
////                            switch (gpsWaitingCounter % 5) {
////                                case (0):
////                                    latitudeTV.setText(R.string.latitude);
////                                    longitudeTV.setText(R.string.longitude);
////                                    break;
////                                default:
////                                    String latitude = (String) latitudeTV.getText();
////                                    String longitude = (String) longitudeTV
////                                            .getText();
////                                    latitudeTV.setText(latitude + " .");
////                                    longitudeTV.setText(longitude + " .");
////                                    break;
////                            }
////                            gpsWaitingCounter++;
////                        }
////                    }
////                });
////            }
////        }, 0, TIMER_LOOP);
//    }


    public class MyLocationListener implements LocationListener
    {

        public void onLocationChanged(final Location loc) {

            Log.e("location data here" , Double.toString(loc.getLatitude()) );






            //loc.getLongitude();

//            intent.putExtra("Latitude", loc.getLatitude());
//            intent.putExtra("Longitude", loc.getLongitude());
//            intent.putExtra("Provider", loc.getProvider());

//            sendBroadcast(intent);
        }

        public void onProviderDisabled(String provider) {

        }


        public void onProviderEnabled(String provider) {
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }


}


