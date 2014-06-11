package edu.uml.cs.isense.datawalk_v2;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import edu.uml.cs.isense.queue.QDataSet;


public class Datawalk_Service extends Service {

    private static final String TAG	= Datawalk_Service.class.getSimpleName();


    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private MyLocationListener locationListener;
    private MySensorListener sensorListener;


    private Location loc;
    private Location prevLoc;
    private Location firstLoc;

    private float accel[];
    private JSONArray dataSet;

    private int elapsedMillis = 0;

    private Timer Timer;
    private Timer recordTimer;
    private int timerTick = 0;

    private final int TIMER_LOOP = 1000;

    private int dataPointCount = 0;

    public static boolean running = false;
    private boolean gpsWorking = true;

    private Timer gpsTimer;


    /* Distance and Velocity */
    float distance = 0;
    float velocity = 0;
    float deltaTime = 0;
    boolean bFirstPoint = true;
    float totalDistance = 0;
    long startTime;

    Intent intent;

    private String loginName = "";
    private String loginPass = "";
    private String projectURL = "";
    private String dataSetName = "";
    private int dataSetID = -1;

    LocalBroadcastManager broadcaster;
    static final public String DATAWALK_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";


    /**
     * This is called when service is first created. The location manager is initiated but no data is
     * being recorded at this point
     */
    @SuppressLint("NewApi")
    @Override
	public void onCreate() {
		super.onCreate();
        Log.e("oncreate", "");


        // initialize GPS and Sensor managers
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        initLocationManager();

        //MySensorListener is an object of the class I made down below
        sensorListener = new MySensorListener();

        // Start the sensor manager so we can get accelerometer data
        mSensorManager.registerListener(sensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_FASTEST);


        broadcaster = LocalBroadcastManager.getInstance(this);

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            Intent intent = new Intent(DataWalk.mContext, DataWalk.class);
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(DataWalk.mContext, 01, intent, 0);

            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            builder.setContentIntent(pendingIntent);
            builder.setContentTitle("iSense DataWalk");
            builder.setContentText("Recording Data");
            builder.setTicker("Started Recording");
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setContentIntent(pendingIntent);
            builder.setOngoing(true);
            builder.setPriority(0);
            Notification notification = builder.build();
            NotificationManager notificationManger =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManger.notify(01, notification);
        }

        //TODO callback to update ui with location data instead of having two location managers (one here and one in DataWalk.java)
//        waitingForGPS();

	}

    public void updateDataPoints(String message) {
        Intent intent = new Intent(DATAWALK_RESULT);
        if(message != null)
            intent.putExtra("VELOCITY", message);
        broadcaster.sendBroadcast(intent);
    }

    public void updateDistance(String message) {
        Intent intent = new Intent(DATAWALK_RESULT);
        if(message != null)
            intent.putExtra("TIME", message);
        broadcaster.sendBroadcast(intent);
    }

    public void updateVelocity(String message) {
        Intent intent = new Intent(DATAWALK_RESULT);
        if(message != null)
            intent.putExtra("DISTANCE", message);
        broadcaster.sendBroadcast(intent);
    }

    public void updateTime(String message) {
        Intent intent = new Intent(DATAWALK_RESULT);
        if(message != null)
            intent.putExtra("POINTS", message);
        broadcaster.sendBroadcast(intent);
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

        running = true;

        dataPointCount = 0;

        //record data
        runRecordingTimer(intent);

        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy() {
        Log.e("onDestroy", "");


        if (running) {

            running = false;

            // Cancel the recording timer
            if (recordTimer != null)
                recordTimer.cancel();

            // Create the name of the session using the entered name
            dataSetName = DataWalk.firstName + " " + DataWalk.lastInitial;


            // Save the newest DataSet to the Upload Queue if it has at
            // least 1 point

            Date date = new Date();


            QDataSet ds = new QDataSet(dataSetName,
                    "Time: " + getNiceDateString(date)  + " Data Points: " + dataPointCount,
                    QDataSet.Type.DATA,
                    dataSet.toString(),
                    null,
                    DataWalk.projectID,
                    null);

            ds.setRequestDataLabelInOrder(true);

            Log.e("dataset: ", ds.getData());

                    if (dataPointCount > 0) {
                        DataWalk.uq.addDataSetToQueue(ds);
                    }


        }

        if (mLocationManager != null)
            mLocationManager.removeUpdates(locationListener);

        if (mSensorManager != null)
            mSensorManager.unregisterListener(sensorListener);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(01);

        super.onDestroy();
    }

    /**
     * Returns a nicely formatted date.
     *
     * @param date
     *            Date you wish to convert
     * @return The date in string form: MM/dd/yyyy, HH:mm:ss
     */
    String getNiceDateString(Date date) {

        SimpleDateFormat niceFormat = new SimpleDateFormat(
                "MM/dd/yyyy, HH:mm:ss", Locale.US);

        return niceFormat.format(date);
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


        // Prepare new containers where our recorded values will be stored
        dataSet = new JSONArray();
        accel = new float[4];

        // Reset timer variables
        startTime = System.currentTimeMillis();

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
                recordData();
            }

        }, 3000, TIMER_LOOP); //3000 is 3 second delay before starting to give gps time to boot up

    }

    void recordData() {

        // Increase the timerTick count
        timerTick++;

        // Rajia: Begin Distance and Velocity Calculation
        // : Only if GPS is working

        // Convert Interval to Seconds
        int nSeconds = DataWalk.mInterval / 1000;

        if (gpsWorking) {
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
        }



        // Update the main UI with the correct number of seconds


        if (timerTick == 1) {
            updateTime("Time Elapsed: " + timerTick
                    + " second");
        } else {
            updateTime("Time Elapsed: " + timerTick
                    + " seconds");
        }

        // Update distance and velocity text boxes
        updateDistance("Distance: "
                + roundTwoDecimals(totalDistance * 0.000621371)
                + " Miles " + roundTwoDecimals(totalDistance)
                + " Meters");

        updateVelocity("Velocity: "
                + roundTwoDecimals(velocity * 2.23694)
                + " MPH " + roundTwoDecimals(velocity)
                + " M/Sec    ");


        // Every n seconds which is determined by interval
        // (not including time 0)
        if ((timerTick % (DataWalk.mInterval / 1000)) == 0 && timerTick != 0) {

            // Prepare a new row of data
            JSONArray dataJSON = new JSONArray();

            // Determine how long you've been recording for
            elapsedMillis += DataWalk.mInterval;
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



                updateDataPoints("Points Recorded: "
                        + dataPointCount);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }






    /**
     * Sets up the locations manager so that it request GPS permission if
     * necessary and gets only the most accurate points.
     */
    private void initLocationManager() {

        // Set the criteria to points with fine accuracy
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        locationListener = new MyLocationListener();


        mLocationManager.requestLocationUpdates(
                mLocationManager.getBestProvider(criteria, true), 0, 0,
                locationListener);


        // Save new GPS points in our loc variable
        loc = new Location(mLocationManager.getBestProvider(criteria, true));

        prevLoc = loc;
        firstLoc = loc;
    }

    // formats numbers to 2 decimal points
    double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    /**
     * You can not implement a LocationListener to a service so that is why
     * There is a separate class here that implements a LocationListener
     */
    public class MyLocationListener implements LocationListener
    {

        public void onLocationChanged(final Location location) {

            Log.e("location data here" , Double.toString(loc.getLatitude()) );


            if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                loc = location;
                gpsWorking = true;
            } else {
                // Rajia will that fix the random velocity problem
                prevLoc.set(loc);
                gpsWorking = false;
            }



        }

        public void onProviderDisabled(String provider) {
            gpsWorking = false;
        }


        public void onProviderEnabled(String provider) {
            gpsWorking = true;
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }

    /**
     * You can not implement a SensorEventListener to a service so that is why
     * There is a separate class here that implements a SensorEventListener
     */
    public class MySensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.e("onSensorChanged ", "here");

            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                accel[0] = event.values[0];
                accel[1] = event.values[1];
                accel[2] = event.values[2];
                accel[3] = (float) Math.sqrt((float) (Math.pow(accel[0], 2)
                        + Math.pow(accel[1], 2) + Math.pow(accel[2], 2)));
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


}


