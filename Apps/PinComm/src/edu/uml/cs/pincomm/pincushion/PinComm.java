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
package edu.uml.cs.pincomm.pincushion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import android.app.ProgressDialog;
import edu.uml.cs.pincomm.exceptions.ChecksumException;
import edu.uml.cs.pincomm.exceptions.IncorrectDeviceException;
import edu.uml.cs.pincomm.exceptions.NoConnectionException;
import edu.uml.cs.pincomm.exceptions.NoDataException;

/**
 * The PinComm class is used to communicate with a PINPoint via a serial
 * port interface.
 * 
 * @author James Dalphond <jdalphon@cs.uml.edu>
 */
public class PinComm {

	public static final int SAMPLE_RATE = 0;
	public static final int BTA1 = 1;
	public static final int BTA2 = 2;
	public static final int MINI1 = 3;
	public static final int MINI2 = 4;
	public static final int GPS = 5;
	//Serial Constants
	//public static final int BAUD_RATE = 19200;
	public static final int BAUD_RATE = 115200;
	public static final boolean FLOW_CONTROL = false;
	//PINPoint Commands
	private static final byte HANDSHAKE = (byte) 0x01;
	private static final byte RESPONSE = (byte) 0x02;
	private static final byte READ_PAGE = (byte) 0x02;
	private static final byte WRITE_TIME = (byte) 0x03;
	private static final byte READ_EEPROM = (byte) 0x04;
	private static final byte WRITE_EEPROM = (byte) 0x05;
	private static final byte LIVE_REQUEST = (byte) 0x06;
	private static final byte DATA_HEADER = (byte) 0x07;
	private static final byte RESET_PINPOINT = (byte) 0x08;
	private static final byte CLEAR_DATA = (byte) 0x09;
	private static final byte START_RECORDING = (byte) 0x0A;
	//PINPoint EEPROM Map
	private static final byte EEPROM_SAMPLE_HIGH = (byte) 0x00;
	private static final byte EEPROM_SAMPLE_LOW = (byte) 0x01;
	private static final byte EEPROM_BTA1_TYPE = (byte) 0x0C;
	private static final byte EEPROM_BTA2_TYPE = (byte) 0x0F;
	private static final byte EEPROM_MINI1_TYPE = (byte) 0x12;
	private static final byte EEPROM_MINI2_TYPE = (byte) 0x15;
	private static final byte EEPROM_GPS = (byte) 0x16;
	private static final byte EEPROM_SN_ONE = (byte) 0xFC;
	private static final byte EEPROM_SN_TWO = (byte) 0xFD;
	private static final byte EEPROM_SN_THREE = (byte) 0xFE;
	private static final byte EEPROM_SN_FOUR = (byte) 0xFF;
	private static final byte BOOTLOADER_FLAG = (byte) 0xFB;
	//PINPoint record information
	private static final int RECORD_SIZE = 32;
	private Double firmwareVersion = 0.0;
	private BluetoothService spi;

	private PinComm(BluetoothService spi) {
		this.spi = spi;
	}

	/**
	 * The instantiate method will use the Serial channel to open up available 
	 * ports and check them to see if there is a pinpoint device listening on the other 
	 * side.
	 */
	public static PinComm instantiate(BluetoothService bts) {
		PinComm pinPoint = new PinComm(bts);
		return pinPoint;
	}

	//Handshake
	@SuppressWarnings("unused")
	private boolean handshake() throws IOException, IncorrectDeviceException {
		if (spi.isOpen()) {
			byte reply = -1;
			try {
				Thread.sleep(50);
				spi.clearBuff();
				spi.writeByte(HANDSHAKE);
				reply = spi.readByte();
				if (reply == RESPONSE) {
					short temp0 = (short) (spi.readByte() & 255);
					short temp1 = (short) (spi.readByte() & 255);
					System.out.println("Firmware version: " + temp0 + "." + Integer.toHexString((int) temp1));
					return true;
				} else if (reply != RESPONSE) {
					System.out.println("Response recieved: " + reply + " , not a PINPoint");
					return false;
				} else {
					System.out.println("No response");
					return false;
				}
			} catch (InterruptedException e) {
				System.err.println("InterruptedException while instantiating PINPoint!");
				throw new IncorrectDeviceException();
			}
		}

		System.err.println("SPI is not open!");
		return false;
	}

	public String getDescription() {
		return "pinpoint";
	}

	public String getPort() {
		if (spi.isOpen()) {
			return "port";
		} else {
			return "";
		}
	}

	public void close() throws IOException {
		spi.close();
	}

	public byte[] getDataHeader() throws NoConnectionException, NoDataException {
		byte[] temp = new byte[4];

		if (spi.isOpen()) {
			spi.clearBuff();
			spi.writeByte((byte) DATA_HEADER);
			for (int i = 0; i < 4; i++) {
				try {
					temp[i] = spi.readByte();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			int numRecords = (((temp[0] & 255) << 16) + ((temp[1] & 255) << 8) + (temp[2] & 255)) / 32;

			if (numRecords == 0) {
				throw new NoDataException();
			}

			return temp;
		}
		throw new NoConnectionException();
	}

	/**
	 * Communicates with the pinpoint to get back a raw copy of the requested
	 * page of flash memory of the pinpoint.
	 *
	 * @param dataHeader
	 * @param numRecords
	 * @return ArrayList<byte[]>
	 * @throws IOException
	 * @throws NoConnectionException
	 */
	public ArrayList<byte[]> requestData(byte[] dataHeader, int numRecords, final ProgressDialog pDiag) throws NoConnectionException, IOException, ChecksumException {

		ArrayList<byte[]> data = new ArrayList<byte[]>();

		// If the bluetooth line is open.
		if (spi.isOpen()) {

			//Clear the line
			spi.clearBuff();

			System.out.println("Requesting data...");

			//Tell the pinpoint we will be requesting a page.
			spi.writeByte(READ_PAGE);

			//Tell the pinpoint which page we are starting at.
			spi.writeByte((byte) 0);
			spi.writeByte((byte) 0);
			spi.writeByte((byte) 0);

			//Tell the pinpoint which page we are stopping at.
			spi.writeByte((byte) dataHeader[0]);
			spi.writeByte((byte) dataHeader[1]);
			spi.writeByte((byte) dataHeader[2]);

			byte computedChecksum = (byte) 0;

			//Read each byte back from the serial line and place it into the raw storage.
			try {
				System.out.println("Recieving data...");
				long StartTime = System.currentTimeMillis();
				for (int i = 0; i < numRecords; i++) {
					//Raw storage for the response.
					byte[] records = new byte[RECORD_SIZE];
					for (int j = 0; j < RECORD_SIZE; j++) {
						records[j] = spi.readByte();
						System.out.print(records[j]);
						computedChecksum = (byte) (computedChecksum + (byte) records[j]);
					}
					pDiag.setProgress(i);
					System.out.println();
					data.add(records);
				}
				System.out.println(data.size());
				byte readChecksum = spi.readByte();

				long FinishTime = System.currentTimeMillis();
				System.out.println("Upload finished in " + ((FinishTime - StartTime) / 1000) + " seconds");

				if (computedChecksum != readChecksum) {
					
					throw new ChecksumException();

				} else {
					
					System.out.println("Finished uploading data");
					return data;
					
				}

			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("ArrayIndexOutOfBoundsException thrown while requesting data");
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				System.err.append("Exception thrown while requesting data");
			}

			spi.clearBuff();

			//Return the raw values.
			return data;
		}

		//Faied return null.
		return null;
	}

	/**
	 * Returns the value stored in the PINPoints EEPROM for the
	 * requested settings
	 *
	 * @param hByte
	 * @param lByte
	 * @return int
	 * @throws NoConnectionException
	 */
	private int getSetting(byte hByte, byte lByte)  {
		short high = 0, low = 0;
		if (spi.isOpen()) {
			spi.clearBuff();
			spi.writeByte(READ_EEPROM);

			spi.writeByte((byte) 0x00);
			spi.writeByte(hByte);
			try {
				high = (short) (spi.readByte() & 255);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			spi.clearBuff();

			spi.writeByte(READ_EEPROM);
			spi.writeByte((byte) 0x00);
			spi.writeByte(lByte);

			try {
				low = (short) (spi.readByte() & 255);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			spi.clearBuff();

			return ((high << 8) + low);
		}
		spi.clearBuff();
		return -1;
	}

	/**
	 * Returns the value stored in the PINPoints EEPROM for the
	 * requested settings
	 *
	 * @param sByte
	 * @return int
	 * @throws NoConnectionException
	 */
	private int getSetting(byte sByte) {
		short high = 0;
		if (spi.isOpen()) {
			spi.clearBuff();
			spi.writeByte(READ_EEPROM);
			spi.writeByte((byte) 0x00);
			spi.writeByte(sByte);
			try {
				high = (short) (spi.readByte() & 255);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return high;
		}
		return -1;
	}

	/**
	 * Allows EEPROM settings to be requested simply
	 *
	 * @param request
	 * @return int
	 * @throws NoConnectionException
	 */
	public int getSetting(int request) {

		switch (request) {
		case SAMPLE_RATE:
			return getSetting(EEPROM_SAMPLE_HIGH, EEPROM_SAMPLE_LOW);
		case BTA1:
			return getSetting(EEPROM_BTA1_TYPE);
		case BTA2:
			return getSetting(EEPROM_BTA2_TYPE);
		case MINI1:
			return getSetting(EEPROM_MINI1_TYPE);
		case MINI2:
			return getSetting(EEPROM_MINI2_TYPE);
		case GPS:
			return getSetting(EEPROM_GPS);
		}
		return -1;
	}

	/**
	 * Sets the value of the desired setting in the PINPoints EEPROM
	 *
	 * @param hByte
	 * @param lByte
	 * @param value
	 * @throws NoConnectionException
	 */
	private void setSetting(byte hByte, byte lByte, int value) throws NoConnectionException {
		if (spi.isOpen()) {
			spi.clearBuff();
			spi.writeByte(WRITE_EEPROM);
			spi.writeByte((byte) 0x0);
			spi.writeByte(hByte);
			spi.writeByte((byte) ((value >> 8) & 0xFF));
			spi.clearBuff();
			spi.writeByte(WRITE_EEPROM);
			spi.writeByte((byte) 0x00);
			spi.writeByte(lByte);
			spi.writeByte((byte) (value & 0xFF));
			try {
				spi.readByte();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new NoConnectionException();
		}

	}

	/**
	 * Sets the value of the desired setting in the PINPoints EEPROM
	 *
	 * @param sByte
	 * @param value
	 * @throws NoConnectionException
	 */
	private void setSetting(byte sByte, int value) throws NoConnectionException {
		if (spi.isOpen()) {
			spi.clearBuff();
			spi.writeByte(WRITE_EEPROM);
			spi.writeByte((byte) 0x00);
			spi.writeByte(sByte);
			spi.writeByte((byte) (value & 0xFF));
			spi.clearBuff();
			try {
				spi.readByte();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new NoConnectionException();
		}
	}

	/**
	 * Sets the desired setting on the connected PINPoint to the
	 * desired value. 
	 *
	 * @param request
	 * @param value
	 * @throws NoConnectionException
	 */
	public void setSetting(int request, int value) throws NoConnectionException {
		switch (request) {
		case SAMPLE_RATE:
			setSetting(EEPROM_SAMPLE_HIGH, EEPROM_SAMPLE_LOW, value);
			break;
		case BTA1:
			setSetting(EEPROM_BTA1_TYPE, value);
			break;
		case BTA2:
			setSetting(EEPROM_BTA2_TYPE, value);
			break;
		case MINI1:
			setSetting(EEPROM_MINI1_TYPE, value);
			break;
		case MINI2:
			setSetting(EEPROM_MINI2_TYPE, value);
			break;
		case GPS:
			setSetting(EEPROM_GPS, value);
			break;
		}

	}

	/**
	 * Sets all of the eeprom settings represented as KEYs in the hashmap to 
	 * their corresponding VALUE in the hashmap.
	 * 
	 * @param changes
	 * @throws NoConnectionException
	 */
	public void SetMultipleSettings(HashMap<Integer, Integer> changes) throws NoConnectionException {
		if (spi.isOpen()) {
			Iterator<Integer> iter = changes.keySet().iterator();
			while (iter.hasNext()) {
				int x = iter.next();
				this.setSetting(x, changes.get(x));
			}
		}
	}

	/**
	 * Returns a hashmap of all of the settings in eeprom.
	 *
	 * @return HashMap<Integer,Integer>
	 * @throws NoConnectionException
	 */
	public HashMap<Integer, Integer> GetSettings() {

		HashMap<Integer, Integer> settings = new HashMap<Integer, Integer>();

		for (int i = 0; i < 14; i++) {
			settings.put(i, this.getSetting(i));
		}

		return settings;
	}

	/**
	 * Resets the connected PINPoint.
	 *
	 * @throws NoConnectionException
	 */
	public void resetPinpoint() throws NoConnectionException {
		if (spi.isOpen()) {
			spi.writeByte((byte) RESET_PINPOINT);
			spi.writeByte((byte) 'C');
			spi.writeByte((byte) 'O');
			spi.writeByte((byte) 'N');
			spi.writeByte((byte) 'F');
			spi.writeByte((byte) 'I');
			spi.writeByte((byte) 'R');
			spi.writeByte((byte) 'M');
		}
	}

	/**
	 * Sets the Real Time Clock chip in the connected PINPoint to the current
	 * time in GMT.
	 *
	 * @throws NoConnectionException
	 */
	public boolean setRealTimeClock() throws NoConnectionException {

		if (spi.isOpen()) {
			//Calendar cal = Calendar.getInstance(new SimpleTimeZone("GMT"));
			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

			spi.writeByte((byte) WRITE_TIME);
			spi.writeByte((byte) cal.get(Calendar.SECOND));
			spi.writeByte((byte) cal.get(Calendar.MINUTE));
			spi.writeByte((byte) cal.get(Calendar.HOUR));
			spi.writeByte((byte) cal.get(Calendar.DAY_OF_WEEK));
			spi.writeByte((byte) (cal.get(Calendar.DATE)));
			spi.writeByte((byte) (cal.get(Calendar.MONTH) + 1));
			
			int date = (cal.get(Calendar.YEAR)) % 100;
			spi.writeByte((byte) date);

			byte response = 0x00;
			try {
				response = spi.readByte();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (response == (byte) 0x06) {
				System.out.println("Successfully set time");
				return true;
			}

		}
		return false;
	}

	/**
	 * Clears the data stored on the pinpoint. 
	 * 
	 * @throws NoConnectionException
	 */
	public void clearDataFromPinpoint() throws NoConnectionException {
		if (spi.isOpen()) {
			spi.writeByte((byte) CLEAR_DATA);
			spi.writeByte((byte) 'C');
			spi.writeByte((byte) 'O');
			spi.writeByte((byte) 'N');
			spi.writeByte((byte) 'F');
			spi.writeByte((byte) 'I');
			spi.writeByte((byte) 'R');
			spi.writeByte((byte) 'M');

			try {
				if (spi.readByte() == (byte) 0x12) {
					System.out.println("Cleared data from pinpoint!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Tells the pinpoint to start recording data at its
	 * current recording interval.
	 *
	 * @throws NoConnectionException
	 */
	public void startRecordingData() throws NoConnectionException {
		if (spi.isOpen()) {
			spi.writeByte((byte) START_RECORDING);
			spi.writeByte((byte) 'C');
			spi.writeByte((byte) 'O');
			spi.writeByte((byte) 'N');
			spi.writeByte((byte) 'F');
			spi.writeByte((byte) 'I');
			spi.writeByte((byte) 'R');
			spi.writeByte((byte) 'M');

			try {
				if (spi.readByte() == (byte) 0x14) {
					System.out.println("Started recording data");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the serial number of the connected pinpoint.
	 *
	 * @return int
	 * @throws NoConnectionException
	 */
	public int getSerialNumber() throws NoConnectionException {
		short one = 0, two = 0, three = 0, four = 0;
		int serialNumber = -1;
		spi.clearBuff();
		if (spi.isOpen()) {
			spi.writeByte(READ_EEPROM);
			spi.writeByte((byte) 0x03);
			spi.writeByte((byte) EEPROM_SN_ONE);
			try {
				one = (short) (spi.readByte() & 255);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			spi.writeByte(READ_EEPROM);
			spi.writeByte((byte) 0x03);
			spi.writeByte((byte) EEPROM_SN_TWO);
			try {
				two = (short) (spi.readByte() & 255);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			spi.writeByte(READ_EEPROM);
			spi.writeByte((byte) 0x03);
			spi.writeByte((byte) EEPROM_SN_THREE);
			try {
				three = (short) (spi.readByte() & 255);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			spi.writeByte(READ_EEPROM);
			spi.writeByte((byte) 0x03);
			spi.writeByte((byte) EEPROM_SN_FOUR);
			try {
				four = (short) (spi.readByte() & 255);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			serialNumber = ((one << 24) + (two << 16) + (three << 8) + four);
		}
		spi.clearBuff();
		return serialNumber;
	}

	public void initiateBootloader() throws NoConnectionException {

		// A value of 0xFF will cause the bootloader to switch into update mode,
		//a value of 0 causes the bootloader to continue on to normal operation.

		if (spi.isOpen()) {
			spi.writeByte(WRITE_EEPROM);
			spi.writeByte((byte) 0x03);
			spi.writeByte(BOOTLOADER_FLAG);
			spi.writeByte((byte) 0xFF);
			try {
				spi.readByte();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
