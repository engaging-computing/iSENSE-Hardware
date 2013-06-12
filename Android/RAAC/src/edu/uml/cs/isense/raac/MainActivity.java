/*
 * Copyright (c) 2009, iSENSE Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution. Neither the name of the University of
 * Massachusetts Lowell nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package edu.uml.cs.isense.raac;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.raac.exceptions.NoConnectionException;
import edu.uml.cs.isense.raac.exceptions.NoDataException;
import edu.uml.cs.isense.raac.pincushion.BluetoothService;
import edu.uml.cs.isense.raac.pincushion.PinComm;
import edu.uml.cs.isense.raac.pincushion.pinpointInterface;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener {
	boolean showConnectOption = false, showTimeOption = false, showSensorOption = false, connectFromSplash = true, dataRdy = false;
	Button rcrdBtn, pushToISENSE, btnSetName;
	ScrollView dataScroller;
	ImageButton pinpointBtn, pinpointBtnOther;
	ImageView spinner, spinner2;
	RelativeLayout launchLayout, lastPptLayout, otherPptLayout;
	ViewFlipper flipper;
	TextView minField, maxField, aveField, medField, sensorHead, launchStatusA, lastPptName;
	LinearLayout dataLayout;
	TextView tenPoints;
	EditText nameField, splashNameField;
	static pinpointInterface ppi;
	private BluetoothService mChatService = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	ArrayList<String[]> data;
	Animation mSlideInTop, mSlideOutTop, rotateInPlace;
	int flipView = 0; //Currently displayed child of the viewFlipper
	int btStatNum = 0; //The current status of the bluetooth connection
	int sessionId = -1;
	String experimentId = "421";
	String username = "sor";
	String password = "sor";
	boolean loggedIn = false;
	static String sessionUrl;
	String baseSessionUrl = "http://isense.cs.uml.edu/highvis.php?sessions=";
	String datMed, datAve, datMax, datMin;
	private RestAPI rapi;
	private ProgressDialog dia;
	JSONArray dataSet;
	public static Context mContext;
	boolean autoRun = false;
	boolean autoConn = true;
	String defaultMac = "";
	String groupName = "";

	boolean pressedRecent = true;

	NfcAdapter mAdapter;
	PendingIntent pendingIntent;

	int lastKnownSensor = 0; //0 for temp, 24 pH

	ArrayList<Double> bta1Data = new ArrayList<Double>();
	ArrayList<String> timeData = new ArrayList<String>();

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_CONNECT_DEVICE_MAIN = 3;
	private static final int SENSOR_CHANGE = 2;
	private static final int REQUEST_ENABLE_BT = 4;
	private static final int REQUEST_VIEW_DATA = 5;
	private static final int CHANGE_EXPERIMENT = 6;
	private static final int GROUP_NAME_BOX = 7;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mContext = this;

		mSlideInTop = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
		mSlideOutTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
		rotateInPlace = AnimationUtils.loadAnimation(this, R.anim.superspinner);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), getApplicationContext());
		rapi.useDev(false);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//Get the MAC address of the default PINPoint
		defaultMac = prefs.getString("defaultPpt", "");

		initializeLayout();
		pinpointBtn.setImageResource(R.drawable.nopptbtn);

		flipper.setInAnimation(mSlideInTop);
		flipper.setOutAnimation(mSlideOutTop);

		flipView = flipper.getDisplayedChild();

		if (mBluetoothAdapter == null) {
			//
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		rcrdBtn.setEnabled(false);

		launchLayout.setVisibility(View.VISIBLE);

		pendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		mChatService = new BluetoothService(this, mHandler);
	}

	// Set up all views from the XML layout
	public void initializeLayout() {		
		dataScroller = (ScrollView) findViewById(R.id.scrollView1);
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		rcrdBtn = (Button) findViewById(R.id.btn_getRcrd);
		pushToISENSE = (Button) findViewById(R.id.btn_pushToISENSE);
		pinpointBtn = (ImageButton) findViewById(R.id.pinpoint_select_btn);
		pinpointBtnOther = (ImageButton) findViewById(R.id.otherpinpoint_select_btn);
		launchLayout = (RelativeLayout) findViewById(R.id.launchlayout);
		minField = (TextView) findViewById(R.id.et_min);
		maxField = (TextView) findViewById(R.id.et_max);
		aveField = (TextView) findViewById(R.id.et_ave);
		medField = (TextView) findViewById(R.id.et_medi);
		spinner = (ImageView) findViewById(R.id.mySpin);
		spinner2 = (ImageView) findViewById(R.id.mySpinOther);
		dataLayout = (LinearLayout) findViewById(R.id.linearLayout1);
		tenPoints = (TextView) findViewById(R.id.onlyTenPoints);
		sensorHead = (TextView) findViewById(R.id.sensorNameHeader);
		nameField = (EditText) findViewById(R.id.nameField);	
		splashNameField = (EditText) findViewById(R.id.et_groupName);
		btnSetName = (Button) findViewById(R.id.btn_setName);

		nameField.setText(groupName);
		splashNameField.setText(groupName);
		splashNameField.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE) {
					setNameThing();
					return true;
				} else {
					return false;
				}
			}
		});
		btnSetName.setOnClickListener(this);

		launchStatusA = (TextView) findViewById(R.id.launchStatusTxt);
		lastPptName = (TextView) findViewById(R.id.lastPptName);
		lastPptLayout = (RelativeLayout) findViewById(R.id.lastPptLayout);

		otherPptLayout = (RelativeLayout) findViewById(R.id.otherPptLayout);

		lastPptLayout.setOnClickListener(this);
		otherPptLayout.setOnClickListener(this);
		rcrdBtn.setOnClickListener(this);
		pushToISENSE.setOnClickListener(this);

		SharedPreferences defPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String name = defPrefs.getString("defaultPptName","");
		lastPptName.setText(name);

		minField.setText(datMin);
		maxField.setText(datMax);
		medField.setText(datMed);
		aveField.setText(datAve);

		setBtStatus();
	}

	public void LostPinPoint() {
		Toast.makeText(this, "Couldn't find the PINPoint! Press the Connect button and reconnect please.", Toast.LENGTH_LONG).show();
		rcrdBtn.setEnabled(false);
		showSensorOption = false;
		showTimeOption = false;
		invalidateOptionsMenu();
		btStatNum = 0;
		setBtStatus();
		return;
	}
	public void FoundPinPoint() {
		Toast.makeText(this, "Connected to PINPoint!", Toast.LENGTH_SHORT).show();
		rcrdBtn.setEnabled(true);
		showSensorOption = true;
		showTimeOption = true;
		invalidateOptionsMenu();
		btStatNum = 1;
		setBtStatus();
		return;
	}

	//Override to make sure that the correct layout file is used when the screen orientation changes
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.main);
		initializeLayout();

		flipper.setDisplayedChild(flipView);

		if (data != null) {
			try {
				prepDataForUpload();
				writeDataToScreen();
			} catch (NoConnectionException e) {
				LostPinPoint();
				return;
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public synchronized void onResume() {
		super.onResume();

		//Set up foreground dispatch so that this app knows to intercept NFC discoveries while it's open
		if(Build.VERSION.SDK_INT >= 10) {
			mAdapter = NfcAdapter.getDefaultAdapter(this);
			if(mAdapter != null) {
				mAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
			}
		}

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			handleNFCIntent(getIntent());
		}

		//Update preferences set in PreferenceActivity
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		autoRun = prefs.getBoolean("auto_upload", false);
		experimentId = prefs.getString("experiment_number", "421");
		autoConn = prefs.getBoolean("auto_connect", true);
		defaultMac = prefs.getString("defaultPpt", "");
	}

	@Override
	public void onPause() {
		super.onPause();
		if(Build.VERSION.SDK_INT >= 10) {
			if(mAdapter != null) {
				mAdapter.disableForegroundDispatch(this);
			}
		}
	}

	@SuppressLint("NewApi")
	void handleNFCIntent(Intent intent) {
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		if(Build.VERSION.SDK_INT >= 9) {
			if (rawMsgs != null) {
				NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
				byte[] payload = msgs[0].getRecords()[0].getPayload();
				String text = "";
				//Get the Text Encoding
				String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
				//Get the Language Code
				int languageCodeLength = payload[0] & 0077;
				try {
					//Get the Text
					text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				connectToBluetooth(text, false);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		//Check to see if the activity is being started due to reading an NFC tag
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			handleNFCIntent(intent);
		}
	}

	@Override
	public void onStart() {
		if (!loggedIn && rapi.isConnectedToInternet()) new PerformLogin().execute();
		super.onStart();
	}

	private double applyFormula(int sensor, double x) {
		BigDecimal bd, rounded;
		double temp;
		if(sensor == 24) {
			temp = (-0.0185*x)+13.769;
			bd = new BigDecimal(temp);
			rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
			return rounded.doubleValue();
		} else if(sensor == 1) {
			temp = (-33.47 * Math.log(x)) + 213.85;
			bd = new BigDecimal(temp);
			rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
			return rounded.doubleValue();
		} else {
			return x;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.menu_connect) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_MAIN);
		} else if (item.getItemId() == R.id.menu_setTime) {
			if(ppi.setRealTimeClock())
				Toast.makeText(MainActivity.this, "Successfully synced time.", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(MainActivity.this, "Could not sync time.", Toast.LENGTH_SHORT).show();
		} else if (item.getItemId() == R.id.menu_setSensors) {
			Intent i = new Intent(this, SensorSelector.class);
			startActivityForResult(i, SENSOR_CHANGE);
		} else if (item.getItemId() == R.id.menu_login) {
			if (loggedIn)
				Toast.makeText(MainActivity.this,  "Already logged in!", Toast.LENGTH_SHORT).show();
			else
				if (rapi.isConnectedToInternet()) new PerformLogin().execute();
		} else if (item.getItemId() == R.id.menu_settings) {
			Intent i = new Intent(this, Preferences.class);
			startActivity(i);
		} else if (item.getItemId() == android.R.id.home) {
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
		return true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (ppi != null)
			ppi.disconnect();
	}

	public void connectToBluetooth(String macAddr, boolean fromMain) {
		System.out.println("conneting to "+macAddr);
		try {
			BluetoothDevice device = mBluetoothAdapter
					.getRemoteDevice(macAddr);
			mChatService.connect(device);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Toast.makeText(this, "Sorry, the MAC address was invalid. Connection failed!",  Toast.LENGTH_LONG).show();
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			if(!fromMain){
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			} else {
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_MAIN);
			}
		}
	}

	@SuppressLint("NewApi")
	public void getRecords() {
		ppi.setContext(this);
		//Check PINPoint's BTA1 sensor setting
		//and if it's not pH or temperature, set it to temperature
		try {
			if(ppi.getSetting(PinComm.BTA1)!=24 && ppi.getSetting(PinComm.BTA1)!=1) {
				ppi.setSetting(PinComm.BTA1, 1);
			}
			if(ppi.getSetting(PinComm.BTA1)==24) {
				sensorHead.setText("BTA1: Vernier pH Sensor");
			} else {
				sensorHead.setText("BTA1: Vernier Temperature Probe");
			}
		} catch (NoConnectionException e) {
			LostPinPoint();
			return;
		}

		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Please wait, reading data from PINPoint");
		progressDialog.setCancelable(false);
		if(Build.VERSION.SDK_INT >= 11) {
			progressDialog.setProgressNumberFormat(null);
		}
		progressDialog.show();

		final Runnable toastRun = new Runnable() { 
			public void run() { 
				Toast.makeText(getApplicationContext(), "Error retrieving data from PINPoint! Please try again", Toast.LENGTH_SHORT).show();
				rcrdBtn.setEnabled(true);
				dataLayout.removeAllViews();
				bta1Data.clear();
				timeData.clear();
			}
		};

		dataLayout.removeAllViews();
		if( bta1Data != null || timeData != null ) {
			bta1Data.clear();
			timeData.clear();
		}

		Thread thread=new Thread(
				new Runnable(){

					public void run(){

						try {
							data = ppi.getData(progressDialog);

							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									if(progressDialog.isShowing()) {
										progressDialog.dismiss();
									}
									if (data != null && data.size() == progressDialog.getMax()) {
										try {
											prepDataForUpload();
											writeDataToScreen();
											rcrdBtn.setEnabled(true);
										} catch (NoConnectionException e) {
											LostPinPoint();
											return;
										}
									} else {
										runOnUiThread(toastRun);
									}
								}

							});
						} catch (Exception e) {
							e.printStackTrace();
							runOnUiThread(toastRun);
							Thread.currentThread().interrupt();
						}
					}

				});
		thread.start();
		pushToISENSE.setEnabled(true);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		if (v == lastPptLayout) {
			if(!groupName.equals("")) {
				pressedRecent = true;
				splashNameField.setError(null);
				if(!defaultMac.equals("")) {
					connectToBluetooth(defaultMac, false);
				} else {
					Intent serverIntent = new Intent(this, DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				}
			} else {
				splashNameField.setError("A group name is required");
			}
		}
		if (v == otherPptLayout) {
			if(!groupName.equals("")) {
				pressedRecent = false;
				splashNameField.setError(null);
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			} else {
				splashNameField.setError("A group name is required");
			}
		}
		if (v == rcrdBtn) {
			rcrdBtn.setEnabled(false);
			getRecords();
		}
		if (v == pushToISENSE) {
			pushToISENSE.setEnabled(false);
			try {
				uploadData();
			} catch (NoConnectionException e) {
				LostPinPoint();
			}
		}
		if (v == btnSetName ) {
			setNameThing();
		}

	}

	public void setNameThing() {
		String name = splashNameField.getText().toString();
		if(!name.equals("")) {
			splashNameField.setError(null);
			groupName = name;
			nameField.setText(groupName);
			Toast.makeText(this, "Group name has been set!", Toast.LENGTH_SHORT).show();
			InputMethodManager imm = (InputMethodManager)getSystemService(
					Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(splashNameField.getWindowToken(), 0);
		} else {
			splashNameField.setError("A group name is required");
		}
	}

	public void prepDataForUpload() throws NoConnectionException {
		int x = 0;
		int sensorsetting = ppi.getSetting(PinComm.BTA1);
		for (int i = 0; i < data.size(); i++) {
			String[] strray = data.get(i);

			for (String str : strray) {
				x++;
				switch(x) {
				case 1:  timeData.add(formatTime(str)); 
				break;
				case 14: bta1Data.add(applyFormula(sensorsetting, Double.parseDouble(str)));
				data.get(i)[14] = ""+applyFormula(sensorsetting, Double.parseDouble(str)); 
				break;
				default: break;
				}
			}
			x = 0;
		}
		dataRdy = true;
		findStatistics();
		if(autoRun) {
			uploadData();
		}
	}

	public void writeDataToScreen() throws NoConnectionException {
		dataLayout.removeAllViews();

		int i = 0;
		int y = 1;
		if (data.size() > 10) {
			dataLayout.addView(tenPoints);
			tenPoints.setVisibility(View.VISIBLE);
			i = data.size()-10;
			y = data.size()-9;
		}

		int x = 0;
		String label = "";
		Resources res = getResources();
		int currSensor = ppi.getSetting(PinComm.BTA1);

		try {
			for (; i<data.size(); i++) {
				String[] strray = data.get(i);
				DecimalFormat df = new DecimalFormat("#0.00");
				LinearLayout newRow = new LinearLayout(getBaseContext());
				newRow.setOrientation(LinearLayout.HORIZONTAL);
				if(i%2 != 0) {
					newRow.setBackgroundColor(res.getColor(R.color.rowcols));
				}
				TextView tvLeft1 = new TextView(getBaseContext());
				tvLeft1.setText(Html.fromHtml("<b>" + "Datapoint " + y + "</b>"));
				tvLeft1.setTextColor(Color.BLACK);
				TextView tvRight1 = new TextView(getBaseContext());
				newRow.addView(tvLeft1);
				newRow.addView(tvRight1);
				dataLayout.addView(newRow);
				for (String str : strray) {
					x++;
					switch(x) {
					case 1: label = "Time (GMT)"; break;
					case 14: if(currSensor==24) {
						label = "BTA1: Vernier pH Sensor";
					} else {
						label = "BTA1: Vernier Temperature Probe";
					} 
					break;
					default:
						continue;
					}
					LinearLayout newRow2 = new LinearLayout(getBaseContext());
					newRow2.setOrientation(LinearLayout.HORIZONTAL);
					if(i%2 != 0) {
						newRow2.setBackgroundColor(res.getColor(R.color.rowcols));
					}
					TextView tvLeft2 = new TextView(getBaseContext());
					tvLeft2.setText(label);
					tvLeft2.setTextColor(Color.BLACK);
					tvLeft2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
					TextView tvRight2 = new TextView(getBaseContext());
					if(x==14) {
						tvRight2.setText(df.format(applyFormula(currSensor, Double.parseDouble(str))));
					} else {
						tvRight2.setText(str);
					}
					tvRight2.setTextColor(Color.BLACK);
					newRow2.addView(tvLeft2);
					newRow2.addView(tvRight2);
					dataLayout.addView(newRow2);
				}
				LinearLayout newRow3 = new LinearLayout(getBaseContext());
				newRow3.setPadding(5, 5, 5, 5);
				newRow3.setGravity(Gravity.CENTER_VERTICAL);
				newRow3.setOrientation(LinearLayout.HORIZONTAL);
				if(i%2 != 0) {
					newRow3.setBackgroundColor(res.getColor(R.color.rowcols));
				}
				TextView tvLeft3 = new TextView(getBaseContext());
				tvLeft3.setText("");
				TextView tvRight3 = new TextView(getBaseContext());
				tvRight3.setText("");
				newRow3.addView(tvLeft3);
				newRow3.addView(tvRight3);
				dataLayout.addView(newRow3);
				dataScroller.post(new Runnable() {
					@Override
					public void run() {
						dataScroller.fullScroll(ScrollView.FOCUS_UP);
					}
				});
				x = 0;
				y++;
			}
			//findStatistics();
		} catch (NullPointerException e) {
			Toast.makeText(getApplicationContext(), "Error collecting data, please try again", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	private void findStatistics() {
		DecimalFormat form = new DecimalFormat("0.00");
		double min, max, ave, med;
		double temp = 0;

		if (bta1Data.size() != 0) {
			min = bta1Data.get(0);
			max = bta1Data.get(0);

			for (double i : bta1Data) {
				if (i < min) {
					min = i;
				}
				if (i > max) {
					max = i;
				}
				temp += i;
			}
			ave = temp / bta1Data.size();

			datMin = "" + form.format(min);
			datMax = "" + form.format(max);
			datAve = "" + form.format(ave);

			minField.setText(datMin);
			maxField.setText(datMax);
			aveField.setText(datAve);

			if (bta1Data.size() == 1) {
				med = bta1Data.get(0);
			} else if (bta1Data.size() % 2 == 0) {
				med = bta1Data.get((bta1Data.size() + 1) / 2);
			} else {
				med = (bta1Data.get((bta1Data.size() / 2)) + bta1Data.get((bta1Data
						.size() + 1) / 2)) / 2;
			}

			datMed = "" + form.format(med);
			medField.setText(datMed);

		}
	}

	public void setBtStatus() {
		if (btStatNum == 0) {
			rcrdBtn.setEnabled(false);
		} else if (btStatNum == 1) {
			rcrdBtn.setEnabled(true);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.getItem(0).setEnabled(showConnectOption);
		menu.getItem(1).setEnabled(showSensorOption);
		menu.getItem(2).setEnabled(showTimeOption);
		menu.getItem(3).setEnabled(!loggedIn);
		return true;
	}

	// The Handler that gets information back from the BluetoothService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					if(pressedRecent) {
						spinner.clearAnimation();
						spinner.setVisibility(View.GONE);
						pinpointBtn.setImageResource(R.drawable.pptbtn);
					} else {
						spinner2.clearAnimation();
						spinner2.setVisibility(View.GONE);
						pinpointBtnOther.setImageResource(R.drawable.pptbtn);
					}
					ppi = new pinpointInterface(mChatService);
					rcrdBtn.setEnabled(true);
					pinpointBtn.setEnabled(false);
					pinpointBtnOther.setEnabled(false);
					btStatNum = 1;
					setBtStatus();
					//Sleep thread to allow near-future communications to succeed
					try {
						Thread.sleep(600);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(connectFromSplash) {
						showConnectOption = true;
						showTimeOption = true;
						showSensorOption = true;
						if(Build.VERSION.SDK_INT >= 11) {
							invalidateOptionsMenu();
						}
						flipper.showNext();
						flipView = flipper.getDisplayedChild();
						connectFromSplash = false;
					} else {
						FoundPinPoint();
					}
					//Set the time on the PINPoint's internal clock
					int tries = 0;
					while(!ppi.setRealTimeClock() && tries < 10) {
						tries ++;
					}
					if(tries < 10) {
						Toast.makeText(MainActivity.this, "Successfully synced time.", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MainActivity.this, "Couldn't sync time.", Toast.LENGTH_SHORT).show();
					}
					ppi.setSetting(PinComm.SAMPLE_RATE, 1000);
					try {
						lastKnownSensor = ppi.getSetting(PinComm.BTA1);
					} catch (NoConnectionException e) {
						LostPinPoint();
						e.printStackTrace();
					}

					if(autoRun) {
						getRecords();
					}
					break;
				case BluetoothService.STATE_CONNECTING:
					if(pressedRecent) {
						pinpointBtn.setImageResource(R.drawable.pptbtntry);
						spinner.setVisibility(View.VISIBLE);
						spinner.startAnimation(rotateInPlace);
					} else {
						pinpointBtnOther.setImageResource(R.drawable.pptbtntry);
						spinner2.setVisibility(View.VISIBLE);
						spinner2.startAnimation(rotateInPlace);
					}
					btStatNum = 2;
					setBtStatus();
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					if(pressedRecent) {
						spinner.clearAnimation();
						spinner.setVisibility(View.GONE);
						pinpointBtn.setImageResource(R.drawable.nopptbtn);
					} else {
						spinner2.clearAnimation();
						spinner2.setVisibility(View.GONE);
						pinpointBtnOther.setImageResource(R.drawable.nopptbtn);
					}
					showSensorOption = false;
					if(Build.VERSION.SDK_INT >= 11) {
						invalidateOptionsMenu();
					}
					btStatNum = 0;
					setBtStatus();
					break;
				}
				break;
			case BluetoothService.MESSAGE_WRITE:
			case BluetoothService.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				@SuppressWarnings("unused")
				String readMessage = new String(readBuf, 0, msg.arg1);
				break;
			case BluetoothService.MESSAGE_DEVICE_NAME:
				break;
			case BluetoothService.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothService.TOAST), Toast.LENGTH_SHORT).show();
				if(msg.getData().getString(BluetoothService.TOAST).equals("Unable to connect device")) {
					Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				}
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		SharedPreferences defPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor defEditor = defPrefs.edit();
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			connectFromSplash = true;
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				String name = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);
				defEditor.putString("defaultPpt", address);
				defEditor.putString("defaultPptName", name);
				defEditor.commit();

				lastPptName.setText(name);

				connectToBluetooth(address, false);
			}
			break;
		case REQUEST_CONNECT_DEVICE_MAIN:
			connectFromSplash = false;
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				String name = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);
				defEditor.putString("defaultPpt", address);
				defEditor.putString("defaultPptName", name);
				defEditor.commit();

				lastPptName.setText(name);

				connectToBluetooth(address, true);
				Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
				rcrdBtn.setEnabled(false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		case SENSOR_CHANGE:
			// When the dialog for selecting sensors is closed
			try {
				lastKnownSensor = ppi.getSetting(PinComm.BTA1);
			} catch (NoConnectionException e) {
				LostPinPoint();
				e.printStackTrace();
			}
			break;
		case REQUEST_VIEW_DATA:
			//When the data has been uploaded
			break;
		case CHANGE_EXPERIMENT:
			break;
		case GROUP_NAME_BOX:
			if (resultCode == Activity.RESULT_OK) {
				groupName = data.getStringExtra("groupname");
				nameField.setText(groupName);
				splashNameField.setText(groupName);
			}
			break;
		}
	}

	//preps the JSONArray, and pushes time, temp and ph to iSENSE
	private void uploadData() throws NoConnectionException {
		if (nameField.length() == 0) {
			nameField.setText("SoR Group");
		}
		if (!dataRdy) {
			Toast.makeText(this, "There is no data to push.", Toast.LENGTH_LONG).show();
			return;
		}

		if (timeData.size() != bta1Data.size()) {
			Toast.makeText(this, "Error in preparing data.  Please try pressing \"Get Data\" again.",
					Toast.LENGTH_LONG).show();
			return;
		}

		dataSet = new JSONArray();

		JSONArray dataJSON;
		if (lastKnownSensor == 1 || lastKnownSensor == 24) //PINPoint is set to use Temperature Probe or pH Sensor
			for (int i = 0; i < timeData.size(); i++) {
				dataJSON = new JSONArray();
				dataJSON.put(timeData.get(i));
				dataJSON.put(bta1Data.get(i));
				dataSet.put(dataJSON);
			}
		else {
			Toast.makeText(this, "Invalid sensor type.", Toast.LENGTH_LONG).show();
			return;
		}

		new UploadTask().execute();
	}

	//the uploading thread that does all the work
	private Runnable uploader = new Runnable() {

		@Override
		public void run() {

			String nameOfSession = nameField.getText().toString();

			if (!loggedIn && rapi.isConnectedToInternet())
				new PerformLogin().execute();

			if (loggedIn) {

				if (sessionId == -1) {
					sessionId = rapi.createSession(experimentId, 
							nameOfSession, 
							"Automated Submission Through Android App", 
							"500 Pawtucket Blvd.", "Lowell, Massachusetts", "United States");
					if (sessionId != -1) {
						rapi.putSessionData(sessionId, experimentId, dataSet);
						sessionUrl = baseSessionUrl + sessionId;
					} else {
						sessionUrl = baseSessionUrl;
						return;	
					}

				}
				else {
					if(!(rapi.updateSessionData(sessionId, experimentId, dataSet))) {
						Toast.makeText(MainActivity.this, "Could not update session data.", Toast.LENGTH_SHORT).show();
					}
				}
			} else sessionUrl = baseSessionUrl;

		}

	};

	// Control task for uploading data
	private class UploadTask extends AsyncTask <Void, Integer, Void> {

		@Override protected void onPreExecute() {
			dia = new ProgressDialog(MainActivity.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while your data is uploaded to iSENSE...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override protected Void doInBackground(Void... voids) {

			uploader.run();
			publishProgress(100);

			return null;

		}

		@Override  protected void onPostExecute(Void voids)	{
			sessionId = -1;
			dia.setMessage("Done");
			dia.cancel();

			if (!(sessionUrl.equals(baseSessionUrl))) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(sessionUrl));
				rcrdBtn.setEnabled(false);
				showSensorOption = false;
				showTimeOption = false;
				invalidateOptionsMenu();
				startActivity(i);  
			} else {
				Toast.makeText(MainActivity.this, "Upload failed. Check your internet connection, and make sure the experiment is not closed.", Toast.LENGTH_LONG).show();
				dataRdy = true;
				pushToISENSE.setEnabled(true);
			}
		}
	}

	//String formatter
	public String formatTime(String temp) {
		String dataString = "";

		long unixTs = 0;
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SS z");
		Date parsed;

		try {
			parsed = format.parse(temp+" GMT");
			unixTs = parsed.getTime();

			dataString = "" + unixTs;
			return dataString;
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
	}

	private class PerformLogin extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			boolean success = rapi.login(username, password);
			if (success)
				loggedIn = true;
			else
				loggedIn = false;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			Toast.makeText(MainActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
		}

	}


}
