package edu.uml.cs.isense.pinports.pincushion;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.uml.cs.isense.pinports.exceptions.NoConnectionException;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */

public class BluetoothService {
	private static final String TAG = "BluetoothService";
	private static final boolean D = true;

	private static final String NAME = "BluetoothService";

	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedComm mConnectedComm;
	private int mState;

	public static final int STATE_NONE = 0;     
	public static final int STATE_LISTEN = 1;     
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;

	// Bluetooth states
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * @param context  The UI Activity Context
	 * @param handler  A Handler to send messages back to the UI Activity
	 */
	public BluetoothService(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * Set the current state of the chat connection
	 * @param state  An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * Return the current connection state. 
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Start the message service. Specifically start AcceptThread to begin a
	 * session in listening mode.
	 */
	public synchronized void start() {
		if (D) Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

		// Cancel any thread currently running a connection
		if (mConnectedComm != null) {mConnectedComm.cancel(); mConnectedComm = null;}

		setState(STATE_LISTEN);
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * @param device  The BluetoothDevice to connect
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (D) Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
		}

		// Cancel any thread currently running a connection
		if (mConnectedComm != null) {mConnectedComm.cancel(); mConnectedComm = null;}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * @param socket  The BluetoothSocket on which the connection was made
	 * @param device  The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		if (D) Log.d(TAG, "connected");

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

		// Cancel any thread currently running a connection
		if (mConnectedComm != null) {mConnectedComm.cancel(); mConnectedComm = null;}

		// Cancel the accept thread because we only want to connect to one device
		if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

		// Start the thread to manage the connection and perform transmissions
		mConnectedComm = new ConnectedComm(socket);
		mConnectedComm.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessageAtFrontOfQueue(msg);

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D) Log.d(TAG, "stop");

		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
		if (mConnectedComm != null) {mConnectedComm.cancel(); mConnectedComm = null;}
		if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
		setState(STATE_NONE);
	}

	public boolean isOpen() {
		if (mState == STATE_CONNECTED) return true;
		else return false;
	}
	
	public void clearBuff() {
		if (mState != STATE_CONNECTED) return;
		mConnectedComm.clearBuffer();
	}

	public void close() {
		if (mState != STATE_CONNECTED) return;
		mConnectedComm.close();
	}

	public void writeByte(byte out) throws NoConnectionException {
		byte[] b = new byte[1];
		b[0] = out;
		writeByte(b);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * @param out The bytes to write
	 * @param handler  The handler to send the response to
	 * @throws NoConnectionException 
	 * @see ConnectedThread#write(byte[])
	 */
	public void writeByte(byte[] out) throws NoConnectionException {
		if (mState != STATE_CONNECTED) return;
		mConnectedComm.write(out);
	}

	public byte readByte() throws IOException {
		if (mState != STATE_CONNECTED) return 0x00;
		return mConnectedComm.read();
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		setState(STATE_LISTEN);

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "Unable to connect device");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	@SuppressWarnings("unused")
	private void connectionLost() {
		setState(STATE_LISTEN);

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "Finished communicating with the PINPoint");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * This thread runs while listening for incoming connections.
	 * It runs until a connection is accepted (or until cancelled).
	 */
	private class AcceptThread extends Thread {
		// The local socket
		private final BluetoothServerSocket mmServerSocket;

		@SuppressWarnings("unused")
		public AcceptThread() {
			BluetoothServerSocket tmp = null;

			// Create a new listening socket
			try {
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
			mmServerSocket = tmp;
		}

		public void run() {
			if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
			setName("AcceptThread");
			BluetoothSocket socket = null;

			// Listen to the socket if we're not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() failed", e);
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			if (D) Log.i(TAG, "END mAcceptThread");
		}

		public void cancel() {
			if (D) Log.d(TAG, "cancel " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
	}


	/**
	 * This thread runs while attempting to make an outgoing connection
	 * with a device. It runs straight through; the connection either
	 * succeeds or fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		@SuppressLint("NewApi")
		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the given BluetoothDevice
			try {
				if(Build.VERSION.SDK_INT >= 10) {
					tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
				} else {
					tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				}
			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			//discovery will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a 
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				connectionFailed();
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() socket during connection failure", e2);
				}
				// Start the service over to restart listening mode
				BluetoothService.this.start();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedComm extends Thread {
		private final BluetoothSocket mmSocket;
		private final DataInputStream mmInStream;
		private final OutputStream mmOutStream;
		public ConnectedComm(BluetoothSocket socket) {
			Log.d(TAG, "create Connected");
			mmSocket = socket;
			DataInputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = new DataInputStream(socket.getInputStream());
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public byte read() throws IOException {
			int i = 0;
			try {
				for(i = 0; i < 200; i++) {
					if(mmInStream.available() > 0) {
						return mmInStream.readByte();
					} else {
						Thread.sleep(5);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.err.println("Failed after " + i + " tries");
			throw new IOException();
		}

		/**
		 * Write to the connected OutStream.
		 * @param buffer  The bytes to write
		 */
		public synchronized void write(byte[] message) throws NoConnectionException {
			try {
				mmOutStream.write(message);
				wait(101);
			} catch (Exception e) {
				e.printStackTrace();
				throw new NoConnectionException();
			}
		}

		public void clearBuffer() {
			try {
				mmInStream.skipBytes(mmInStream.available());
				mmOutStream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void close() {
			try {
				mmInStream.close();
				mmOutStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}