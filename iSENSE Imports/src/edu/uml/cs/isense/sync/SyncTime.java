package edu.uml.cs.isense.sync;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

/**
 * Allows the user to share his device's current time with other devices on the
 * same network.
 * 
 * @author iSENSE Android Development Team
 */
public class SyncTime extends Activity {

	private Context mContext;

	/* Constants */
	private final int TIME_RECEIVED_REQUESTED = 2;
	private final int TIME_FAILED_REQUESTED = 3;
	private final int TIME_RESET_REQUESTED = 4;
	private final String HOST = "255.255.255.255";

	/* Time Variables */
	private long timeOffset = 0;
	private long myTime = 0;
	private long timeReceived = 0;
	String currentTime;
	String receivedTime;

	/* Socket Variables */
	int mPort = 45623;
	DatagramSocket mSocket;
	DatagramPacket mPack, newPack;
	InetAddress sendAddress;
	byte[] byteMessage;
	byte[] receivedMessage;

	/* Work Flow Variables */
	boolean preInit = false;
	boolean success = false;

	/* Managers */
	private ProgressDialog dia;
	private API api;
	private Waffle w;

	public enum SyncTimeExceptionType {
		NONE, SOCKET_EXCEPTION, UNKNOWN_HOST_EXCEPTION, TIMEOUT_EXCEPTION
	}

	/**
	 * Creates the 4 button layout that can send and receive time. It also
	 * allows the user to reset his iSENSE uploading time stamp back to its
	 * default.
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.synctime);

		// Initialize important variables
		mContext = this;
		api = API.getInstance(mContext);
		w = new Waffle(this);

		// Action bar customization for API >= 11
		if (android.os.Build.VERSION.SDK_INT >= 11) {
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

		// Prepare messages
		String currentTime = "" + System.currentTimeMillis();
		byteMessage = currentTime.getBytes();
		receivedMessage = currentTime.getBytes();

		// Prepare UI buttons
		Button send = (Button) findViewById(R.id.sendButton);
		Button receive = (Button) findViewById(R.id.receiveButton);
		Button reset = (Button) findViewById(R.id.resetButton);
		Button cancel = (Button) findViewById(R.id.buttonGoBack);

		send.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (api.hasConnectivity()) {

					// Send the message on a new thread
					new SendMessage().start();

					// Listen for a message on its own thread (async task)
					new ReceiveTask().execute();

				} else {
					w.make("No internet connection found.", Waffle.IMAGE_X);
				}

			}
		});

		receive.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (api.hasConnectivity()) {

					// Listen for a message on its own thread (async task)
					new ReceiveTask().execute();

				} else {
					w.make("No internet connection found.", Waffle.IMAGE_X);
				}
			}
		});

		reset.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				// Launches the reset time activity
				Intent iReset = new Intent(SyncTime.this, TimeReset.class);
				startActivityForResult(iReset, TIME_RESET_REQUESTED);

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				((Activity) mContext).finish();
			}
		});
	}

	/**
	 * Attempts to initialize socket.
	 * 
	 * @return SyncTimeExceptionType which signifies which exception was caught
	 *         if any.
	 */
	private SyncTimeExceptionType initSocket() {

		try {
			sendAddress = InetAddress.getByName(HOST);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return SyncTimeExceptionType.UNKNOWN_HOST_EXCEPTION;
		}

		try {
			mSocket = new DatagramSocket(mPort);

		} catch (SocketException e) {
			e.printStackTrace();
			return SyncTimeExceptionType.SOCKET_EXCEPTION;
		}

		try {
			mSocket.setSoTimeout(10000);

		} catch (SocketException e) {
			e.printStackTrace();
			return SyncTimeExceptionType.TIMEOUT_EXCEPTION;
		}

		return SyncTimeExceptionType.NONE;
	}

	// set socket for sending (fails if closed)
	private void prepForBroadcast() {
		try {
			mSocket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Attempts to send a packet. Print an IOException to the error log if any
	 * is caught.
	 */
	synchronized private void sendPack() {

		initSocket();
		preInit = true;
		prepForBroadcast();

		try {
			currentTime = "" + System.currentTimeMillis();
			byteMessage = currentTime.getBytes();
			mPack = new DatagramPacket(byteMessage, byteMessage.length,
					sendAddress, mPort);
			mSocket.send(mPack);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * A thread class dedicated to sending our time packet into the network.
	 * 
	 * @author jpoulin
	 */
	private class SendMessage extends Thread {
		public void run() {
			sendPack();
		}
	}

	/**
	 * Attempts to receive a packet. Print an IOException to the error log if
	 * any is caught.
	 */
	synchronized private void receivePack() {

		if (!preInit)
			initSocket();

		DatagramPacket newPack = new DatagramPacket(receivedMessage,
				byteMessage.length);
		try {
			mSocket.receive(newPack);
			mSocket.close();
			success = true;
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}

		receivedMessage = newPack.getData();
		receivedTime = new String(receivedMessage);

		myTime = System.currentTimeMillis();
		timeReceived = Long.parseLong(receivedTime);
		timeOffset = timeReceived - myTime;

	}

	/**
	 * Converts a millisecond time stamp into human readable form.
	 * 
	 * @param timeStamp
	 *            Time you want to convert in milliseconds.
	 * @return A human readable date string in the form MM/DD/YYYY, HH:MM:SS.MS
	 */
	private String convertTimeStamp(long timeStamp) {

		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(timeStamp);
		int minute = date.get(Calendar.MINUTE), second = date
				.get(Calendar.SECOND), millis = date.get(Calendar.MILLISECOND);
		String minuteSt, secondSt, millisSt;

		minuteSt = (minute < 10) ? "0" + minute : "" + minute;
		secondSt = (second < 10) ? "0" + second : "" + second;
		millisSt = (millis < 100) ? "0" + millis : "" + millis;
		millisSt = (millis < 10) ? "0" + millisSt : millisSt;

		return (date.get(Calendar.MONTH) + 1) + "/"
				+ date.get(Calendar.DAY_OF_MONTH) + "/"
				+ date.get(Calendar.YEAR) + ", "
				+ date.get(Calendar.HOUR_OF_DAY) + ":" + minuteSt + ":"
				+ secondSt + "." + millisSt;
	}

	/**
	 * An AsyncTask class dedicated to that launches a progress dialog, and
	 * attempts to receive a packet from the network. Launches
	 * {@link edu.uml.cs.isense.sync.TimeReceived TimeReceived} or
	 * {@link edu.uml.cs.isense.sync.TimeFailed TimeFailed} in onPostExecute,
	 * depending on whether or not the packet was received successfully.
	 * 
	 * @author jpoulin
	 */
	private class ReceiveTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			OrientationManager.disableRotation(SyncTime.this);
			dia = new ProgressDialog(mContext);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Attempting to synchronize time (automatic timeout in 10 seconds)...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {
			receivePack();
			publishProgress(100);
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			dia.cancel();
			OrientationManager.enableRotation(SyncTime.this);
			if (success) {
				String timeRec = convertTimeStamp(timeReceived);
				Intent iReceived = new Intent(SyncTime.this, TimeReceived.class);
				iReceived.putExtra("timeReceived", timeRec);
				iReceived.putExtra("timeOffset", timeOffset);
				startActivityForResult(iReceived, TIME_RECEIVED_REQUESTED);
			} else {
				Intent iFail = new Intent(SyncTime.this, TimeFailed.class);
				startActivityForResult(iFail, TIME_FAILED_REQUESTED);
			}
		}
	}

	/**
	 * Makes sure the socket is closed.
	 */
	@Override
	protected void onStop() {
		if (mSocket != null)
			if (mSocket.isConnected())
				mSocket.close();
		super.onStop();
	}

	/**
	 * Used to convert WiFi IP address into usable string form.
	 * 
	 * @param ipAddress
	 *            IP address in integer form.
	 * @return
	 */
	public String formatIp(int ipAddress) {
		int intMyIp3 = ipAddress / 0x1000000;
		int intMyIp3mod = ipAddress % 0x1000000;

		int intMyIp2 = intMyIp3mod / 0x10000;
		int intMyIp2mod = intMyIp3mod % 0x10000;

		int intMyIp1 = intMyIp2mod / 0x100;
		int intMyIp0 = intMyIp2mod % 0x100;

		String ipString = String.valueOf(intMyIp3) + "."
				+ String.valueOf(intMyIp2) + "." + String.valueOf(intMyIp1)
				+ "." + String.valueOf(intMyIp0);

		return ipString;
	}

	/**
	 * Catches the returns of {@link edu.uml.cs.isense.sync.TimeReceived
	 * TimeReceived}, {@link edu.uml.cs.isense.sync.TimeFailed TimeFailed}, and
	 * {@link edu.uml.cs.isense.sync.TimeReset TimeReset}.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == TIME_RECEIVED_REQUESTED) {
			Intent retIntent = new Intent();
			retIntent.putExtra("offset", timeOffset);
			setResult(RESULT_OK, retIntent);
			finish();
		} else if (requestCode == TIME_FAILED_REQUESTED) {
			if (resultCode == RESULT_OK) {
				new ReceiveTask().execute();
			}
		} else if (requestCode == TIME_RESET_REQUESTED) {
			timeOffset = 0;
			Intent retIntent = new Intent();
			retIntent.putExtra("offset", timeOffset);
			setResult(RESULT_OK, retIntent);
			finish();
		}
	}

}