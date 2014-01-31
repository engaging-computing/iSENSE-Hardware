/**
 * Copyright (c) 2008, iSENSE Project. All rights reserved.
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

package com.pinpoint.api;

import com.pinpoint.exceptions.IncorrectDeviceException;
import com.pinpoint.exceptions.NoConnectionException;
import com.pinpoint.exceptions.NoDataException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

/**
 *
 * @author jdalphon
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
    private SerialChannel spi;
    private Double firmwareVersion = 0.0;

    private PinComm(SerialChannel spi) {
        this.spi = spi;
    }

    public static PinComm instantiate(String port) throws IOException, IncorrectDeviceException {
        //Attempt to open the port, looking for a PinPoint
        SerialChannel spi = new SerialChannel();

        try {
            //Try to open the port with the correct specs
            spi.open(port, PinComm.BAUD_RATE, PinComm.FLOW_CONTROL);
        } catch (PortInUseException e) {
            System.err.println("Port In Use");
            throw new IncorrectDeviceException();
        } catch (UnsupportedCommOperationException e) {
            System.err.println("Unsupported Comm Op");
            throw new IncorrectDeviceException();
        } catch (IOException e) {
            System.err.println("IOException");
            throw new IncorrectDeviceException();
        }

        PinComm PinPoint = new PinComm(spi);

        try {
            if (PinPoint.handshake()) {
                //Found a Pinpoint
                return PinPoint;
            } else {
                //Didnt find a pinpoint.
                spi.close();
                throw new IOException();
            }
        } catch (IOException e) {
            //Error cleanup
            spi.close();
            throw new IOException();
        }

    }

    //Handshake
    private boolean handshake() throws IOException, IncorrectDeviceException {
        if (spi.isOpen()) {
            byte reply = -1;
            try {
                Thread.sleep(50);
                spi.clear();
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
            return spi.getPortName();
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
            try {
                spi.writeByte((byte) DATA_HEADER);
                for (int i = 0; i < 4; i++) {
                    temp[i] = spi.readByte();
                }

                int numRecords = (((temp[0] & 255) << 16) + ((temp[1] & 255) << 8) + (temp[2] & 255)) / 32;

                if (numRecords == 0) {
                    throw new NoDataException();
                }

                return temp;
            } catch (IOException ex) {
                System.err.println("IOException thrown while trying to get the number of records on the PINPoint");
            }
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
    public ArrayList<byte[]> requestData(byte[] dataHeader, int numRecords) throws NoConnectionException, IOException {

        ArrayList<byte[]> data = new ArrayList<byte[]>();

        // If the serial line is open.
        if (spi.isOpen()) {

            //Clear the line
            spi.clear();

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
                        computedChecksum = (byte) (computedChecksum + (byte) records[j]);
                    }
                    data.add(records);
                }
                byte readChecksum = spi.readByte();

                long FinishTime = System.currentTimeMillis();
                System.out.println("Upload finished in " + ((FinishTime - StartTime) / 1000) + " seconds");

                if (computedChecksum != readChecksum) {
                    System.err.println("Checksum did not match");
                    return null;

                } else {
                    System.out.println("Finished uploading data");
                    return data;
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("ArrayIndexOutOfBoundsException thrown while requesting data");
            } catch (Exception e) {
                System.err.append("Exception thrown while requesting data");
            }



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
        short high, low;
        if (spi.isOpen()) {
            try {
                spi.clear();
                spi.writeByte(READ_EEPROM);

                spi.writeByte((byte) 0x00);
                spi.writeByte(hByte);
                high = (short) (spi.readByte() & 255);

                spi.clear();

                spi.writeByte(READ_EEPROM);
                spi.writeByte((byte) 0x00);
                spi.writeByte(lByte);

                low = (short) (spi.readByte() & 255);

                return ((high << 8) + low);

            } catch (IOException e) {
                System.err.println("IOException thrown while trying to request EEPROM settings!");
            }
        }
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
        short high;
        if (spi.isOpen()) {
            try {
                spi.clear();
                spi.writeByte(READ_EEPROM);
                spi.writeByte((byte) 0x00);
                spi.writeByte(sByte);
                high = (short) (spi.readByte() & 255);
                return high;
            } catch (IOException e) {
                System.err.println("IOException thrown while trying to request EEPROM settings!");
            }
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
            try {
                spi.clear();
                spi.writeByte(WRITE_EEPROM);
                spi.writeByte((byte) 0x0);
                spi.writeByte(hByte);
                spi.writeByte((byte) ((value >> 8) & 0xFF));
                spi.clear();
                spi.writeByte(WRITE_EEPROM);
                spi.writeByte((byte) 0x00);
                spi.writeByte(lByte);
                spi.writeByte((byte) (value & 0xFF));
                spi.readByte();
            } catch (IOException e) {
                System.err.println("IOException thrown while trying to set EEPROM settings!");
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
            try {
                spi.clear();
                spi.writeByte(WRITE_EEPROM);
                spi.writeByte((byte) 0x00);
                spi.writeByte(sByte);
                spi.writeByte((byte) (value & 0xFF));
                spi.clear();
                spi.readByte();
            } catch (IOException e) {
                System.err.println("IOException thrown while trying to set EEPROM settings");
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
            try {
                spi.writeByte((byte) RESET_PINPOINT);
                spi.writeByte((byte) 'C');
                spi.writeByte((byte) 'O');
                spi.writeByte((byte) 'N');
                spi.writeByte((byte) 'F');
                spi.writeByte((byte) 'I');
                spi.writeByte((byte) 'R');
                spi.writeByte((byte) 'M');

            } catch (IOException ex) {
                System.err.println("IOException thrown while trying to reset the connected PINPoint.");
            }
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
            try {

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

                byte response = spi.readByte();

                if (response == (byte) 0x06) {
                    System.out.println("Successfully set time");
                    return true;
                }


            } catch (IOException ex) {
                System.err.println("IOException thrown while trying to set the Real Time Clock");
                return false;
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
            try {
                spi.writeByte((byte) CLEAR_DATA);
                spi.writeByte((byte) 'C');
                spi.writeByte((byte) 'O');
                spi.writeByte((byte) 'N');
                spi.writeByte((byte) 'F');
                spi.writeByte((byte) 'I');
                spi.writeByte((byte) 'R');
                spi.writeByte((byte) 'M');

                if (spi.readByte() == (byte) 0x12) {
                    System.out.println("Cleared data from pinpoint!");
                }
            } catch (IOException ex) {
                System.err.println("IOError thrown while trying to clear data from pinpoint.");
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
            try {
                spi.writeByte((byte) START_RECORDING);
                spi.writeByte((byte) 'C');
                spi.writeByte((byte) 'O');
                spi.writeByte((byte) 'N');
                spi.writeByte((byte) 'F');
                spi.writeByte((byte) 'I');
                spi.writeByte((byte) 'R');
                spi.writeByte((byte) 'M');

                if (spi.readByte() == (byte) 0x14) {
                    System.out.println("Started recording data");
                }
            } catch (IOException ex) {
                System.err.println("IOException thrown while trying to start recording data");
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
        short one, two, three, four;
        int serialNumber = -1;

        if (spi.isOpen()) {
            try {

                spi.writeByte(READ_EEPROM);
                spi.writeByte((byte) 0x03);
                spi.writeByte((byte) EEPROM_SN_ONE);
                one = (short) (spi.readByte() & 255);

                spi.writeByte(READ_EEPROM);
                spi.writeByte((byte) 0x03);
                spi.writeByte((byte) EEPROM_SN_TWO);
                two = (short) (spi.readByte() & 255);

                spi.writeByte(READ_EEPROM);
                spi.writeByte((byte) 0x03);
                spi.writeByte((byte) EEPROM_SN_THREE);
                three = (short) (spi.readByte() & 255);

                spi.writeByte(READ_EEPROM);
                spi.writeByte((byte) 0x03);
                spi.writeByte((byte) EEPROM_SN_FOUR);
                four = (short) (spi.readByte() & 255);

                serialNumber = ((one << 24) + (two << 16) + (three << 8) + four);

            } catch (IOException ex) {
                System.err.println("IOException while trying to read serial number");
            }
        }
        return serialNumber;
    }

    public void initiateBootloader() throws NoConnectionException {

        // A value of 0xFF will cause the bootloader to switch into update mode,
        //a value of 0 causes the bootloader to continue on to normal operation.

        if (spi.isOpen()) {
            try {
                spi.writeByte(WRITE_EEPROM);
                spi.writeByte((byte) 0x03);
                spi.writeByte(BOOTLOADER_FLAG);
                spi.writeByte((byte) 0xFF);
                spi.readByte();


            } catch (IOException ex) {
                System.err.println("IOException while trying to set bootloader flag");
            }
        }
    }
}
