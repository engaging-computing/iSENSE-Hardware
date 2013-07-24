package edu.uml.cs.isense.collector.sync;

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
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.supplements.OrientationManager;
import edu.uml.cs.isense.waffle.Waffle;

public class SyncTime extends Activity {

	private static Context mContext;

	private static final int TIME_SENT_REQUESTED = 100;
	private static final int TIME_RECEIVED_REQUESTED = 101;
	private static final int TIME_FAILED_REQUESTED = 102;
	private static final int TIME_RESET_REQUESTED = 103;

	private static long timeOffset = 0;
	private static long myTime = 0;
	private static long timeReceived = 0;

	static String host = "255.255.255.255";
	static int mPort = 45623;
	static String tag = "UDP Socket";
	static DatagramSocket mSocket;
	static DatagramPacket mPack, newPack;
	static String currentTime;
	static String receivedTime;
	static byte[] byteMessage;
	static byte[] receivedMessage;
	static InetAddress sendAddress;

	boolean preInit = false;
	boolean success = false;

	private ProgressDialog dia;
	private RestAPI rapi;
	private Waffle w;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.synctime);

		mContext = this;

		rapi = RestAPI
				.getInstance(
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE),
						getApplicationContext());
		w = new Waffle(this);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		
		// Action bar customization for API >= 11
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			ActionBar bar = getActionBar();
			bar.setBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#66AAFF")));
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
		
		String dummyTime = "" + System.currentTimeMillis();
		byteMessage = dummyTime.getBytes();
		receivedMessage = dummyTime.getBytes();

		Button send = (Button) findViewById(R.id.sendButton);
		Button receive = (Button) findViewById(R.id.receiveButton);
		Button reset = (Button) findViewById(R.id.resetButton);
		Button cancel = (Button) findViewById(R.id.buttonGoBack);

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (rapi.isConnectedToInternet()) {

					sendPack();

					receivePack();

					String timeSent = convertTimeStamp(timeReceived);

					Intent iSent = new Intent(SyncTime.this, TimeSent.class);
					iSent.putExtra("timeSent", timeSent);
					iSent.putExtra("timeOffset", timeOffset);
					startActivityForResult(iSent, TIME_SENT_REQUESTED);

				} else {
					w.make("No internet connection found.", Waffle.IMAGE_X);
				}

			}
		});

		receive.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (rapi.isConnectedToInternet()) {
					new ReceiveTask().execute();
				} else {
					w.make("No internet connection found.", Waffle.IMAGE_X);
				}
			}
		});
		
		reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent iReset = new Intent(SyncTime.this, TimeReset.class);
				startActivityForResult(iReset, TIME_RESET_REQUESTED);
			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				((Activity) mContext).finish();
			}
		});
	}

	// attempts to initialize socket
	// returns:
	// -1 for UnknownHost exception
	// -2 for Socket exception for socket creation
	// -3 for Timeout exception
	private int initSocket() {

		try {
			sendAddress = InetAddress.getByName(host);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return -1;
		}

		try {
			mSocket = new DatagramSocket(mPort);

		} catch (SocketException e) {
			e.printStackTrace();
			return -2;
		}

		try {
			mSocket.setSoTimeout(10000);

		} catch (SocketException e) {
			e.printStackTrace();
			return -3;
		}

		return 0;
	}

	// set socket for sending (fails if closed)
	private void prepForBroadcast() {
		try {
			mSocket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	// attempts to send packet
	private void sendPack() {

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

	// attempts to receive packet
	private void receivePack() {

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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		if (mSocket != null)
			if (mSocket.isConnected())
				mSocket.close();
		super.onStop();
	}

	// used to convert wifi ip address
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == TIME_SENT_REQUESTED) {
			Intent retIntent = new Intent();
			retIntent.putExtra("offset", timeOffset);
			setResult(RESULT_OK, retIntent);
			finish();
		} else if (requestCode == TIME_RECEIVED_REQUESTED) {
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