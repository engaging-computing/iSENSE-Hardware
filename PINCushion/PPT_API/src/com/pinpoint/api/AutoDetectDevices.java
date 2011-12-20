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
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class responsible for the detection and instantiation of sensor devices.
 * @author James Dalphond  <jdalphon@cs.uml.edu>
 */
public class AutoDetectDevices {

    public static ArrayList<PinComm> detect() throws IncorrectDeviceException, IOException {

        /* Holds PinComm objects representing the detected devices */
        ArrayList<PinComm> devices = new ArrayList<PinComm>();

        /* Get all available serial ports */
        ArrayList<String> serialPorts = SPI.enumeratePortNames();
       
        /* Try to instantiate a device on each serial port */
        for(String port : serialPorts) {
            
            /* Fix for Mac to only connect to usbserial ports. This will decrease
             * connection time and reduce error with opening Bluetooth ports.
             */
            if (System.getProperty("os.name").contains("Mac") && !port.contains("usbserial")) {
                continue;
            }

            /* Fix for Linux to only connect to USB ports since we will never
             * connect to a Serial port
             */
            if (System.getProperty("os.name").contains("Linux") && port.contains("ttyS0")) {
                continue;
            }

            try {
                System.out.println("-----------------------------------");
                System.out.println("Searching for a PINPoint on: " + port);
                PinComm pinPoint = PinComm.instantiate(port);                    //Figure out if the port has a PINPoint
                System.out.println("Found a PINPoint");
                devices.add(pinPoint);
               
                break;                                                           //Fred only wants to connect to 1
            } catch (IOException e) {
                System.out.println("Not a PINPoint");
            }
        }
        return devices;
    }
}
