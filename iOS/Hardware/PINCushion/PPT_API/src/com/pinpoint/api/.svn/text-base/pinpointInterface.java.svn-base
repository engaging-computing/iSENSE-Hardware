package com.pinpoint.api;

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
import com.pinpoint.exceptions.IncompatibleConversionException;

import com.pinpoint.exceptions.IncorrectDeviceException;
import com.pinpoint.exceptions.InvalidHexException;
import com.pinpoint.exceptions.MissingLogFileException;
import com.pinpoint.exceptions.NoConnectionException;
import com.pinpoint.exceptions.NoDataException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * The pinpointInterface class gives an interface to the pinpoint API to keep
 * the API separate from the rest of the code. 
 * 
 * @author James Dalphond <jdalphon@cs.uml.edu>
 */
public class pinpointInterface {

    private Preferences myPrefs;
    // Comunications protocol for the Pinpoint.
    public PinComm pinpoint;
    // The number of entries in a page of flash memory.
    private static final int PAGE_SIZE = 32;
    // The number of pages of flash memory.
    private static final int FLASH_PAGES = 4096;
    private boolean attempted = true;

    /**
     * The constructor for the interface will try to initiate the
     * connection over usb to the pinpoint. This will use the pinpoint 
     * communications protocol and report back to the UI if there was a 
     * connection problem.
     * 
     * @throws NoConnectionException
     */
    public pinpointInterface(boolean shouldConnect) throws NoConnectionException {

        if (shouldConnect) {
            try {

                // Create a vector of PinComm devices based on the results of the auto detect.
                Vector<PinComm> deviceInstances = AutoDetectDevices.detect();

                // Temporary handle to a PinComm
                PinComm temp;

                // If no devices were found that match the PinComm handshake throw
                // no connection exception to let the UI know that there was a problem.
                if (deviceInstances.isEmpty()) {
                    throw new NoConnectionException();
                }

                // Atleast one device was found so iterate through the list
                Iterator<PinComm> portIterator = deviceInstances.iterator();

                // Test each device to make sure it was a pinpoint. (Not completely necessary)
                while (portIterator.hasNext()) {
                    temp = portIterator.next();
                    if (temp.getDescription().compareTo("pinpoint") == 0) {
                        pinpoint = temp;
                        pinpoint.setRealTimeClock();
                        break; //For now we only care about one pinpoint connected to the computer.
                    }
                    portIterator.next();
                }

                myPrefs = Preferences.userNodeForPackage(this.getClass());

            } catch (IncorrectDeviceException ex) {
                System.err.println("IncorrectDeviceException thrown from inside pinpointInterface.java");
            } catch (IOException ex) {
                System.err.println("IOException thrown from inside pinpointInterface.java");
            }
        } else {
            myPrefs = Preferences.userNodeForPackage(this.getClass());
        }
    }

    /**
     * Get all of the data from the connected PINPoint. Runs all of the data
     * through known conversions to provide human readable output.
     *
     * The returned ArrayList contains all data as an array of strings.
     *
     * @return ArrayList<String[]>
     * @throws NoDataException
     * @throws FileNotFoundException
     */
    public ArrayList<String[]> getData() throws NoDataException, IncompatibleConversionException, BackingStoreException {

        HashMap<Integer, Integer> settings = null;
        System.out.println("Getting records");
        ArrayList<String[]> conversions = null;

        ArrayList<String[]> records = new ArrayList<String[]>();

        try {
            //Get the settings from the pinpoint

            settings = pinpoint.GetSettings();

            //Get the conversions from the conversions file.
            System.out.println("Getting conversions");
            conversions = this.GetConversions();

            //Create an instance of the Pinpoint converter
            PinpointConverter pc = new PinpointConverter(settings, conversions);

            //Get the data header from the pinpoint.
            byte[] dh = pinpoint.getDataHeader();

            //Figure out how many records are stored on the pinpoint.
            int numRecords = (((dh[0]) << 16) + ((dh[1] & 255) << 8) + (dh[2] & 255)) / 32;

            //Request all data from the pinpoint.
            System.out.println("Getting settings");
            ArrayList<byte[]> rawData = pinpoint.requestData(dh, numRecords);

            //Convert all data recieved from the pinpoint.
            for (int i = 0; i < numRecords; i++) {
                records.add(pc.convertAll(rawData.get(i)));
            }

            if (settings.get(PinComm.SAMPLE_RATE) < 1000) {
                pc.fixTime(records, settings.get(PinComm.SAMPLE_RATE));
            }

            return records;
        } catch (NoConnectionException ex) {
            System.err.println("NoConnectionException thrown while getting data");
        } catch (IOException ex) {
            System.err.println("IOException thrown while getting data");
        }

        return null;

    }

    public String getPort() {
        return pinpoint.getPort();
    }

    /**
     * Disconnect the pointpoint and shut down communications cleanly.
     */
    public void disconnect() {
        try {
            pinpoint.close();
        } catch (IOException ex) {
            System.err.println("Error disconnecting pinpoint called from whithin pinpointInterface.java");
        }
    }

    /**
     * Gets the desired "Setting" from the PINPoints EEPROM.
     * Settings are defined in PinComm as PinComm.XXXXX
     *
     * @param Setting
     * @return Integer
     */
    public int getSetting(int Setting) {
        return pinpoint.getSetting(Setting);
    }

    /**
     * Sets the desired "Setting" to "value"
     * Settings are defined in PinComm as PinComm.XXXXX
     *
     * @param Setting
     * @param value
     */
    public void setSetting(int Setting, int value) {
        try {
            pinpoint.setSetting(Setting, value);
            //System.out.println("Setting:" + Setting + " to: " + value);
        } catch (NoConnectionException e) {
            System.err.println("Exception thrown while trying to set setting");
        }
    }

    /**
     * Simply prints the settings stored on the EEPROM of the PINPoint.
     */
    public void printSettings() {

        System.out.println("Sample Rate : " + getSetting(PinComm.SAMPLE_RATE));
        System.out.println("BTA1 Type   : " + getSetting(PinComm.BTA1));
        System.out.println("BTA2 Type   : " + getSetting(PinComm.BTA2));
        System.out.println("Mini1 Type  : " + getSetting(PinComm.MINI1));
        System.out.println("Mini2 Type  : " + getSetting(PinComm.MINI2));
        System.out.println("GPS         : " + getSetting(PinComm.GPS));

    }

    /**
     * Resets the connected PINPoint.
     */
    public void resetPinpoint() {
        try {
            pinpoint.resetPinpoint();
        } catch (NoConnectionException ex) {
            System.err.println("NoConnectionException thrown while trying to reset the connected PINPoint.");
        }
    }

    /**
     * Clears the data on the connected PINPoint.
     */
    public void clearDataFromPinpoint() {
        try {
            pinpoint.clearDataFromPinpoint();
        } catch (NoConnectionException ex) {
            System.err.println("NoConnectionException thrown while trying to clear data from the PINPoint");
        }
    }

    /**
     * Causes the connected PINPoint to start recording at its
     * configured sample rate.
     */
    public void startRecordingData() {
        try {
            pinpoint.startRecordingData();
        } catch (NoConnectionException ex) {
            System.err.println("NoConnectionException thrown while trying to start recording data.");
        }
    }

    /**
     * Sets the time on the PINPoint to the computers time.
     */
    public boolean setRealTimeClock() {
        try {
            return pinpoint.setRealTimeClock();
        } catch (NoConnectionException ex) {
            System.err.println("NoConnectionException while trying to set the Real Time Clock.");
        }
        return false;
    }

    /**
     * Gets an integer array containing all of the settings.
     *
     * @return int[]
     */
    public HashMap<Integer, Integer> GetSettings() {
        return pinpoint.GetSettings();
    }

    public void SetMultipleSettings(HashMap<Integer, Integer> changes) {
        try {
            pinpoint.SetMultipleSettings(changes);
        } catch (NoConnectionException ex) {
            System.err.println("NoConnectionException thrown while trying to set multiple settings");
        }
    }

    public int getSerialNumber() {
        int serialNumber = -1;

        try {
            serialNumber = pinpoint.getSerialNumber();
        } catch (NoConnectionException ex) {
            System.err.println("NoConnectionException thrown while trying to get the serial number");
        }

        return serialNumber;
    }

    public void initiateBootloader() {
        try {
            System.out.println("Made it into initiateBootloader");
            pinpoint.initiateBootloader();
        } catch (NoConnectionException ex) {
            System.err.println("NoConnectionException while trying to set bootloader flag");
        }

    }

    public ArrayList<String[]> GetConversions() throws BackingStoreException {

        String[] tmp = myPrefs.keys();
        ArrayList<String[]> conversions = new ArrayList<String[]>();



        for (String s : tmp) {
            conversions.add(myPrefs.get(s, null).split(","));
        }

        return conversions;

    }

    public void DownloadLatestConversions() throws IOException, BackingStoreException {
        Updater u = new Updater();

        System.out.println("Here");

        ArrayList<String[]> conversions = u.getDataFromGoogleDoc();

        myPrefs.clear();

        for (int i = 0; i < conversions.size(); i++) {
            String[] tmp = conversions.get(i);
            String values = "";
            String key;

            //Convert to key <--> values
            key = tmp[0];
            for (int j = 0; j < tmp.length; j++) {
                if (j < tmp.length - 1) {
                    values += tmp[j] + ",";
                } else {
                    values += tmp[j];
                }

            }

            System.out.println("Adding <" + key + "," + values + ">" );
            myPrefs.put(key, values);
        }
    }

    public void verifyHex(String file) throws FileNotFoundException, IOException, InvalidHexException {

        int COLON = 1;
        int NUM_BYTES_H = 2;
        int NUM_BYTES_L = 3;

        BufferedReader reader = new BufferedReader(new FileReader(file));

        ArrayList<String> lines = new ArrayList<String>();
        String curline;
        while ((curline = reader.readLine()) != null) {
            lines.add(curline);
        }

        System.out.println("Length " + lines.size());

        for (String s : lines) {
            String[] hexline = s.split("");

            //Check for :
            if (hexline[COLON].compareTo(":") != 0) {
                throw new InvalidHexException(InvalidHexException.NO_COLON);
            }

            //Check num bytes
            int numbytes = (Integer.parseInt(hexline[NUM_BYTES_H], 16) << 4) + Integer.parseInt(hexline[NUM_BYTES_L], 16);

            if ((numbytes * 2) + 11 != hexline.length - 1) {
                throw new InvalidHexException(InvalidHexException.BYTE_COUNT_ERROR);
            }

            //Validate checksum
            byte b = (byte) 0x00;
            for (int i = 2; i < hexline.length - 2; i += 2) {
                String x = hexline[i] + "" + hexline[i + 1];
                b += Integer.parseInt(x, 16);
            }
            String ComputedChecksum = Integer.toHexString((byte) 0x100 - b);


            if (ComputedChecksum.length() > 2) {
                ComputedChecksum = ComputedChecksum.substring(ComputedChecksum.length() - 2, ComputedChecksum.length());
            }
            if (ComputedChecksum.length() == 1) {
                ComputedChecksum = "0" + ComputedChecksum;
            }

            String checksum = s.substring(s.length() - 2, s.length());

            System.out.println(checksum + "<->" + ComputedChecksum);
            if (ComputedChecksum.compareToIgnoreCase(checksum) != 0) {
                throw new InvalidHexException(InvalidHexException.INVALID_CHECKSUM);
            }
        }


    }

    public int getNumReadings() throws NoConnectionException, NoDataException {
        //Get the data header from the pinpoint.
        byte[] dh = pinpoint.getDataHeader();

        //Figure out how many records are stored on the pinpoint.
        int numRecords = (((dh[0]) << 16) + ((dh[1] & 255) << 8) + (dh[2] & 255)) / 32;

        return numRecords;
    }
}
