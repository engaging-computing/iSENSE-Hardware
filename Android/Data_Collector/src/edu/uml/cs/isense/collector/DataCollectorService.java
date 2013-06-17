package edu.uml.cs.isense.collector;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;

// http://developer.android.com/guide/components/bound-services.html
public class DataCollectorService extends Service {
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private boolean terminate = false;
	private Timer timeElapsedTimer;
	private PowerManager pm;
	private PowerManager.WakeLock wl;

	// Hardcoded constant of Process.THREAD_PRIORITY_BACKGROUND
	private static final int PROCESS_THREAD_PRIORITY_BACKGROUND = 10;

	// Objects sent in from the DataCollector class
	public static final String SRATE = "srate";
	private static long srate = DataCollector.S_INTERVAL;
	public static final String REC_LENGTH = "rec_length";
	private static long  recLength = DataCollector.TEST_LENGTH;

	// Handler that receives messages from the thread
	private final static class ServiceHandler extends Handler {
		// WeakReference to outer class
		private final WeakReference<DataCollectorService> mService;

		// Constructor that builds weak reference
		ServiceHandler(DataCollectorService service, Looper looper) {
			super(looper);
			mService = new WeakReference<DataCollectorService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			// Handle the message if the reference still exists
			DataCollectorService service = mService.get();
			if (service != null) {
				service.handleMessage(msg);
			}
		}
	}

	// Main service class message handler function to prevent potential static
	// memory leak
	@SuppressWarnings("deprecation")
	public void handleMessage(Message msg) {
		// Acquire a wakelock
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "data_collector_service_wakelock");
		wl.acquire();
		
		// Update the main UI timer
		final Handler handlerTime = new Handler(Looper.getMainLooper());
		timeElapsedTimer = new Timer();
		timeElapsedTimer.scheduleAtFixedRate(new TimerTask() {
			int secondsElapsed = 0;

			public void run() {
				handlerTime.post(new Runnable() {
					@Override
					public void run() {
						DataCollector.setTime(secondsElapsed++);
						if (terminate) timeElapsedTimer.cancel();
					}
				});
			}
		}, 0, 1000);

		long endTime = System.currentTimeMillis() + recLength * 1000;
		while (System.currentTimeMillis() < endTime) {
			if (!pm.isScreenOn()) {
				DataCollector.terminateThroughPowerOff = true;
				terminate = true;
				break;
			}
			
			synchronized (this) {
				try {
					// Terminate the service if client set the terminate
					// parameter
					if (terminate)
						break;
					
					// Get current execution time
					long curExecution = System.currentTimeMillis();

					// Execute main body at rate of srate
					DataCollector.pollForData();

					// Wait for a period of srate - execution time
					wait(srate - (System.currentTimeMillis() - curExecution));

				} catch (Exception e) {
				}
			}
		}

		// Stop the service using the startId, so that we don't stop
		// the service in the middle of handling another job
		stopSelf(msg.arg1);
		if (timeElapsedTimer != null)
			timeElapsedTimer.cancel();

		// Inform client we have stopped
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				DataCollector.serviceHasStopped();
			}
		});
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				PROCESS_THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(this, mServiceLooper);
        
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
				
		// Get client's passed-in values necessary for execution
		Bundle extras = intent.getExtras();
		if (extras != null) {
			srate = extras.getLong(SRATE);
			recLength = extras.getLong(REC_LENGTH);
		}

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, do not restart the
		// service
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		terminate = true;
		if (timeElapsedTimer != null) timeElapsedTimer.cancel();
		
		// Disable the wakelock
		if (wl != null) wl.release();
		
		// Do not call super.onDestroy(); Doing so will result in terminate
		// not being set and while the service will return, it will not stop.
	}

	// Function set by client class to stop the service manually
	// Is functionally similar to onDestroy, which can only be called by the
	// service itself
	public void terminateRecording() {
		if (timeElapsedTimer != null) timeElapsedTimer.cancel();
		terminate = true;
	}
}
