package edu.uml.cs.isense.amusement;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SyncTime extends Activity {
	
	private Context mContext;
	
	private static final int DIALOG_SENT         = 1;
	private static final int DIALOG_RECEIVE      = 2;
	private static final int DIALOG_FAIL_RECEIVE = 3;
	
	private long timeOffset   = 0;
	private long myTime       = 0;
	private long timeReceived = 0;
	
	int mPort = 44444;
    String tag = "UDP Socket";
    DatagramSocket mSocket;
    DatagramPacket mPack, newPack;
    String currentTime;
    String receivedTime;
    byte[] byteMessage;
    byte[] receivedMessage;
    
    boolean socketRdy = false;
    boolean success   = false;
    
    ProgressDialog dia;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.synctime);
		
		mContext = this;
		
		String dummyTime = "" + System.currentTimeMillis();
		byteMessage     = dummyTime.getBytes();
		receivedMessage = dummyTime.getBytes();
		
		socketRdy = (initSocket() == 0);
        Log.d(tag, "Socket Initialized");
	
		Button send    = (Button) findViewById(R.id.sendButton   );
		Button receive = (Button) findViewById(R.id.receiveButton);
		Button cancel  = (Button) findViewById(R.id.buttonGoBack );
		
		send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (socketRdy) {
                    Log.d(tag, "Socket Port: " + mSocket.getPort());                   
                    Log.d(tag, "Broadcast prepared: " + prepForBroadcast());
                    Log.d(tag, "Sent pack successfully: " + sendPack());
                    Log.d(tag, "Send pack: " + mPack);
                        
                    Log.d(tag, "Recieved message: " + receivePack());
                    
                    showDialog(DIALOG_SENT);
                } else {
                	Toast.makeText(mContext, "Fatal error.  Try again.", Toast.LENGTH_SHORT).show();
                }
						
			}	
		});
		
		receive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (socketRdy) {     
					new ReceiveTask().execute();
                } else {
                	Toast.makeText(mContext, "Fatal error.  Try again.", Toast.LENGTH_SHORT).show();
                }
			}	
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) mContext).finish();
				setResult(RESULT_CANCELED);
			}	
		});
	}
	
	//@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(final int id) {
	    
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	Dialog dialog;
    	
    	
    	switch(id) {
		case DIALOG_SENT:
			
			String timeSent = convertTimeStamp(timeReceived);
    	
			builder.setTitle("Time Sent")
			.setMessage("You have sent the time " + timeSent + " to other devices, which is an offset of " +
					timeOffset + " milliseconds from your local clock.")
    		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialoginterface,int i) {
    				Intent retIntent = new Intent();
    				retIntent.putExtra("offset", timeOffset);
    				setResult(RESULT_OK, retIntent);
    				
    				dialoginterface.dismiss();
    				((Activity) mContext).finish();
    			}
    		})
    		.setCancelable(false);
    	
			dialog = builder.create();
    
			break;
		
		case DIALOG_RECEIVE:
			
			String timeRec = convertTimeStamp(timeReceived);
			
			builder.setTitle("Time Received")
			.setMessage("You have received the time " + timeRec + " from the host device, which is an offset of " +
					timeOffset + "milliseconds from your local clock.")
    		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialoginterface,int i) {
    				
    				
    				Intent retIntent = new Intent();
    				retIntent.putExtra("offset", timeOffset);
    				setResult(RESULT_OK, retIntent);
    				
    				dialoginterface.dismiss();
    				((Activity) mContext).finish();
    			}
    		})
    		.setCancelable(false);
    	
			dialog = builder.create();
    
			break;
		
		case DIALOG_FAIL_RECEIVE:
			
	    	
			builder.setTitle("Connection Timed Out")
			.setMessage("Failed to synchronize time.  Please try again.")
    		.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialoginterface,int i) {
    				new ReceiveTask().execute();
    				dialoginterface.dismiss();
    			}
    		})
    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialoginterface,int i) {
    				dialoginterface.dismiss();
    			}
    		})
    		.setCancelable(true);
    	
			dialog = builder.create();
    
			break;
		
		default:
	    	dialog = null;
	    	break;
	    }
	    
	    return dialog;
	}
	
	// attempts to initialize socket
    // returns:
    // -1 for UnknownHost exception
    // -2 for Socket exception for socket creation
    // -3 for Socket exception for connecting socket
	// -4 for Socket can't set SO Timeout
    int initSocket() {

        InetAddress mAddress;
        try {
            mAddress = InetAddress.getLocalHost();
            Log.d(tag, "Address is: " + InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return -1;
        }

        try {
            mSocket = new DatagramSocket(mPort, mAddress);
        } catch (SocketException e) {
            e.printStackTrace();
            return -2;
        }
       
        try {
            mSocket.connect(mSocket.getLocalSocketAddress());
        } catch (SocketException e) {
            e.printStackTrace();
            return -3;
        }
        
        try {
			mSocket.setSoTimeout(5000);
		} catch (SocketException e) {
			e.printStackTrace();
			return -4;
		}
       
        return 0;
    }

    // set socket for sending (fails if closed)
    boolean prepForBroadcast() {
        try {
            mSocket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // attempts to send packet
    boolean sendPack() {
        try {
        	currentTime = "" + System.currentTimeMillis();
        	byteMessage = currentTime.getBytes();
        	mPack = new DatagramPacket(byteMessage, byteMessage.length, mSocket.getInetAddress(), mPort);
            mSocket.send(mPack);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
   
    // attempts to receive packet
    boolean receivePack() {
        DatagramPacket newPack = new DatagramPacket(receivedMessage, byteMessage.length, mSocket.getInetAddress(), mPort);
        try {
            mSocket.receive(newPack);
            success = true;
        } catch (IOException e) {
        	success = false;
            e.printStackTrace();
            return false;   
        } 
        
        receivedMessage = newPack.getData();
        receivedTime = new String(receivedMessage);
        
        myTime = System.currentTimeMillis();
        timeReceived = Long.parseLong(receivedTime);
        timeOffset = timeReceived - myTime;
        
        return true;       
    }
    
    private String convertTimeStamp(long timeStamp) {
    	
    	Calendar date = Calendar.getInstance();
    	date.setTimeInMillis(timeStamp);
    	
    	return date.get(Calendar.MONTH) + "/" + date.get(Calendar.DAY_OF_MONTH) + 
    			"/" + date.get(Calendar.YEAR) + ", " + date.get(Calendar.HOUR_OF_DAY) +
    			":" + date.get(Calendar.MINUTE) + ":" + date.get(Calendar.SECOND) + 
    			"." + date.get(Calendar.MILLISECOND);
    }
    
    private class ReceiveTask extends AsyncTask <Void, Integer, Void> {
	    
	    @Override protected void onPreExecute()
	    {	
	        dia = new ProgressDialog(mContext);
	        dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        dia.setMessage("Attempting to synchronize time...");
	        dia.setCancelable(false);
	        dia.show(); 
	    }

	    @Override protected Void doInBackground(Void... voids)
	    {
	    	Log.d(tag, "Recieved message: " + receivePack());     
	        publishProgress(100);
	        return null;  
	    }

	    @SuppressWarnings("deprecation")
		@Override  protected void onPostExecute(Void voids)
	    {
			dia.cancel();
			if (success)
				showDialog(DIALOG_RECEIVE);
			else
				showDialog(DIALOG_FAIL_RECEIVE);
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
		mSocket.close();
		super.onStop();
	}
	

}
