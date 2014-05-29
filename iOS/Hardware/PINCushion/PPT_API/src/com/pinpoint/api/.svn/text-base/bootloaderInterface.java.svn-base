/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pinpoint.api;


import com.pinpoint.exceptions.IncorrectDeviceException;
import com.pinpoint.exceptions.InvalidHexException;
import com.pinpoint.exceptions.NoConnectionException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jdalphon
 */
public class bootloaderInterface {

    public BootComm bootpoint;

    public bootloaderInterface() throws NoConnectionException {
        try {

            // Create a vector of PinComm devices based on the results of the auto detect.
            Vector<BootComm> deviceInstances = AutoDetectBootloader.detect();

            // Temporary handle to a PinComm
            BootComm temp;

            // If no devices were found that match the PinComm handshake throw
            // no connection exception to let the UI know that there was a problem.
            if (deviceInstances.isEmpty()) {
                throw new NoConnectionException();
            }

            // Atleast one device was found so iterate through the list
            Iterator<BootComm> portIterator = deviceInstances.iterator();

            // Test each device to make sure it was a pinpoint. (Not completely necessary)
            while (portIterator.hasNext()) {
                temp = portIterator.next();
                if (temp.getDescription().compareTo("bootpoint") == 0) {
                    bootpoint = temp;

                    break;
                }
                portIterator.next();
            }

        } catch (IncorrectDeviceException ex) {
            System.err.println("IncorrectDeviceException thrown from inside pinpointInterface.java");
        } catch (IOException ex) {
            System.err.println("IOException thrown from inside pinpointInterface.java");
        }
    }

    public void disconnect() {
        try {
            bootpoint.close();
        } catch (IOException ex) {
            System.err.println("Error disconnecting pinpoint called from whithin pinpointInterface.java");
        }
    }

    public boolean sendP() {
        try {
            return bootpoint.sendP();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public String getPort() {
        return bootpoint.getPort();
    }

    public void FlashBootloader(String file) throws InvalidHexException{
        try {
            int count = 0;
            

            BufferedReader reader = new BufferedReader(new FileReader(file));

            String curString;

            ArrayList<Byte> bytes;
            ArrayList<String> lines = new ArrayList<String>();

            //Read in the file 
            while ((curString = reader.readLine()) != null) {
                lines.add(curString);
            }

            //Pepare lines for individual transmission.
            for (int i = 0; i < lines.size(); i++) {
                bytes = new ArrayList<Byte>();
                String tmp = lines.get(i);
                for (int j = 0; j < tmp.length(); j++) {
                    bytes.add((byte) tmp.charAt(j));
                }
                bytes.add((byte) '\r');
                bytes.add((byte) '\n');

                
                boolean x = bootpoint.SendLine(bytes);

                if (!x){
                    System.out.println("Finished");
                    return;
                } else {
                    count++;
                }      
            }
    
        } catch (FileNotFoundException ex) {
            System.err.println("FileNotFoundException thrown while trying to "
                    + "flash the bootloader");
        } catch (IOException ex) {
            System.err.println("IOException thrown while trying to "
                    + "flash the bootloader");
        }

    }

    public void Quit() {
        bootpoint.QuitFlashing();
    }
}
