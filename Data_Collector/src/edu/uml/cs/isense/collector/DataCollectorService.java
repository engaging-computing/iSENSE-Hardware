package edu.uml.cs.isense.collector;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

// http://developer.android.com/guide/components/bound-services.html
public class DataCollectorService extends Service {
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private boolean terminate = false;

	// Hardcoded constant of Process.THREAD_PRIORITY_BACKGROUND
	private static final int PROCESS_THREAD_PRIORITY_BACKGROUND = 10;

	// Objects sent in from the DataCollector class

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
	public void handleMessage(Message msg) {
		// Update the main UI timer
		final Handler handlerTime = new Handler(Looper.getMainLooper());
		Timer timeElapsedTimer = new Timer();
		timeElapsedTimer.scheduleAtFixedRate(new TimerTask() {
			int secondsElapsed = 0;

			public void run() {
				handlerTime.post(new Runnable() {
					@Override
					public void run() {
						DataCollector.setTime(secondsElapsed++);
					}
				});
			}
		}, 0, 1000);

		long endTime = System.currentTimeMillis() + 10 * 1000;
		while (System.currentTimeMillis() < endTime) {
			synchronized (this) {
				try {
					// Terminate the service if client set the terminate
					// parameter
					if (terminate) {
						terminate = false;
						if (timeElapsedTimer != null)
							timeElapsedTimer.cancel();
						break;
					}

					// Get current execution time
					long curExecution = System.currentTimeMillis();

					// Execute main body at rate of srate
					Log.w("lol", "frognull");

					// Wait for a period of srate - execution time
					wait(1000 - (System.currentTimeMillis() - curExecution));

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
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

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
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
		terminate = true;
		// Do not call super.onDestroy(); Doing so will result in terminate
		// not being set and while the service will return, it will not stop.
	}

	// Function set by client class to stop the service manually
	public void terminateRecording() {
		terminate = true;
	}
}
