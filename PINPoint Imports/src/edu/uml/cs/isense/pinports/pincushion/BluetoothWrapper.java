package edu.uml.cs.isense.pinports.pincushion;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class BluetoothWrapper {
	private static BluetoothAdapter mAdapter;
	
	static {
		try {
			Class.forName("android.bluetooth.BluetoothAdapter");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void checkAvailable() {}
	
	public static BluetoothDevice getRemoteDevice(String address) {
		return mAdapter.getRemoteDevice(address);
	}
	
	public static boolean isEnabled() {
		return mAdapter.isEnabled();
	}
	
	public static BluetoothAdapter getDefaultAdapter() {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		return mAdapter;
	}
}
