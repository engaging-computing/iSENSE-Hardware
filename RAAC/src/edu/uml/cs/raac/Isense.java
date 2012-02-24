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

package edu.uml.cs.raac;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.raac.pincushion.BluetoothService;
import edu.uml.cs.raac.pincushion.pinpointInterface;

public class Isense extends Activity implements OnClickListener {
	Button testBtn, sensorBtn, rcrdBtn; 
	ImageButton pinpointBtn;
	RelativeLayout launchLayout;
	TextView testResult, minField, maxField, aveField, medField;
	TextView mConnected;
	static pinpointInterface ppi;
	private String mConnectedDeviceName = null;
	private BluetoothService mChatService = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	private ArrayList<String[]> data;
	Animation mSlideInTop, mSlideOutTop;
	
	ArrayList<Double> bta1Data = new ArrayList<Double>();

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int SENSOR_CHANGE = 2;
	private static final int REQUEST_ENABLE_BT = 4;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSlideInTop = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        mSlideOutTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		testBtn = (Button) findViewById(R.id.btn_selectppt);
		sensorBtn = (Button) findViewById(R.id.btn_sensors);
		rcrdBtn = (Button) findViewById(R.id.btn_getRcrd);
		pinpointBtn = (ImageButton) findViewById(R.id.pinpoint_select_btn);
		launchLayout = (RelativeLayout) findViewById(R.id.launchlayout);
		testResult = (TextView) findViewById(R.id.resultText);
		mConnected = (TextView) findViewById(R.id.title_connected_to);
		minField = (TextView) findViewById(R.id.et_min);
		maxField = (TextView) findViewById(R.id.et_max);
		aveField = (TextView) findViewById(R.id.et_ave);
		medField = (TextView) findViewById(R.id.et_medi);
		
		if (mBluetoothAdapter == null) {
			testResult.setText("Bluetooth is not available on this device");
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		pinpointBtn.setOnClickListener(this);
		testBtn.setOnClickListener(this);
		sensorBtn.setOnClickListener(this);
		rcrdBtn.setOnClickListener(this);
		rcrdBtn.setEnabled(false);
		
		launchLayout.setVisibility(View.VISIBLE);

		mChatService = new BluetoothService(this, mHandler);
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't started already
			if (mChatService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(ppi != null)
			ppi.disconnect();
	}

	@Override
	public void onClick(View v) {
		if( v == pinpointBtn ) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		}
		if( v == testBtn ) {
			launchLayout.setVisibility(View.VISIBLE);
			launchLayout.startAnimation(mSlideInTop);
		}
		if( v == sensorBtn ) {
			Intent i = new Intent(this, SensorSelector.class);
			startActivityForResult(i, SENSOR_CHANGE);
		}
		if( v == rcrdBtn ) {
			int x=0;
			try {
				data = ppi.getData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				for (String[] strray : data) {
					for (String str : strray) {
						x++;
						testResult.append("\n"+x+": "+str);
						if(x == 14) {
							System.out.println(str);
							bta1Data.add(Double.parseDouble(str));
						}
					}
					x = 0;
					testResult.append("\n");
				}
				findStatistics();
			} catch (NullPointerException e) {
				testResult.append("\nThere was an error reading the data, please try again.");
				e.printStackTrace();
			}
		}
	}

	private void findStatistics() {
		double min, max, ave, med;
		double temp = 0;
		
		min = bta1Data.get(0);
		max = bta1Data.get(0);
		
		for (double i : bta1Data) {
			if(i < min) {
				min = i;
			}
			if(i > max) {
				max = i;
			}
			temp += i;
		}
		ave = temp/bta1Data.size();
		
		minField.setText(""+min);
		maxField.setText(""+max);
		aveField.setText(""+ave);
		
		if(bta1Data.size()%2 == 0) {
			med = bta1Data.get((bta1Data.size()+1)/2);
		} else {
			med = (bta1Data.get((bta1Data.size()/2)) + bta1Data.get((bta1Data.size()+1)/2))/2;
		}
		medField.setText(""+med);
	}
	
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					mConnected.setText("");
					mConnected.append(mConnectedDeviceName);
					pinpointBtn.setImageResource(R.drawable.pptbtn);
					ppi = new pinpointInterface(mChatService, getApplicationContext());
					rcrdBtn.setEnabled(true);
					pinpointBtn.setEnabled(false);
					launchLayout.startAnimation(mSlideOutTop);
					launchLayout.setVisibility(View.GONE);
					break;
				case BluetoothService.STATE_CONNECTING:
					pinpointBtn.setImageResource(R.drawable.pptbtntry);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					pinpointBtn.setImageResource(R.drawable.nopptbtn);
					break;
				}
				break;
			case BluetoothService.MESSAGE_WRITE:
			case BluetoothService.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				String readMessage = new String(readBuf, 0, msg.arg1);
				testResult.setText(readMessage);
				break;
			case BluetoothService.MESSAGE_DEVICE_NAME:
				mConnectedDeviceName = msg.getData().getString("device_name");
				break;
			case BluetoothService.MESSAGE_TOAST:
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras()
						.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		case SENSOR_CHANGE:
			// When the dialog for selecting sensors is closed
			if (resultCode == Activity.RESULT_OK) {
				// Save the selected sensors into their appropriate preferences
				SharedPreferences prefs = getSharedPreferences("SENSORS",0);
				SharedPreferences.Editor editor = prefs.edit();
				
				editor.putString("sensor_bta1", data.getExtras().getString("bta1"));
				editor.putString("sensor_bta2", data.getExtras().getString("bta2"));
				editor.putString("sensor_mini1", data.getExtras().getString("mini1"));
				editor.putString("sensor_mini2", data.getExtras().getString("mini2"));
				
				editor.commit();
			}
			break;
		}
	}
}