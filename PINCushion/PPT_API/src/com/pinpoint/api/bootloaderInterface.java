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
import com.pinpoint.exceptions.InvalidHexException;
import com.pinpoint.exceptions.NoConnectionException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author jdalphon
 */
public class bootloaderInterface {

    public BootComm bootpoint;

    public bootloaderInterface() throws NoConnectionException {
        try {

            // Create a vector of PinComm devices based on the results of the auto detect.
            ArrayList<BootComm> deviceInstances = AutoDetectBootloader.detect();

            // Temporary handle to a PinComm
            BootComm temp;

            // If no devices were found that match the BootComm handshake throw
            // no connection exception to let the UI know that there was a problem.
            // Take the first bootpoint seen.
            if (!deviceInstances.isEmpty()) {

                for (BootComm bc : deviceInstances) {
                    temp = bc;
                    if (temp.getDescription().compareTo("bootpoint") == 0) {
                        bootpoint = temp;
                        break;
                    }
                }
            } else {
                throw new NoConnectionException();
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
           System.out.println("IOError Thrown while trying to sent \"P\"");
        }
        return false;
    }

    public String getPort() {
        return bootpoint.getPort();
    }

    public void FlashBootloader(String file) throws InvalidHexException {
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

                if (!x) {
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
