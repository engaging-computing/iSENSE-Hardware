/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pinpoint.api;

import com.pinpoint.exceptions.IncorrectDeviceException;
import com.pinpoint.exceptions.InvalidHexException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author jdalphon
 */
public class BootComm {
    //Serial Constants

    public static final int BAUD_RATE = 19200;
    public static final boolean FLOW_CONTROL = false;
    private SerialChannel spi;

    private BootComm(SerialChannel spi) {
        this.spi = spi;
    }

    public static BootComm instantiate(String port) throws IOException, IncorrectDeviceException {
        //Attempt to open the port, looking for a PinPoint
        SerialChannel spi = new SerialChannel();

        try {
            //Try to open the port with the correct specs
            spi.open(port, BootComm.BAUD_RATE, BootComm.FLOW_CONTROL);
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

        BootComm BootPoint = new BootComm(spi);

        //return BootPoint;


        try {
            if (BootPoint.handshake()) {
                //Found a Pinpoint
                return BootPoint;
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
            
            try {
                Thread.sleep(500);
                spi.clear();
                spi.writeByte((byte) 0x01);

                byte one = spi.readByte();
                byte two = spi.readByte();
                
                if((one== (byte)'B') && (two == (byte) 'L')){
                    return true;
                } else {
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
        return "bootpoint";
    }

    public String getPort() {
        if (spi.isOpen()) {
            return spi.getPortName();
        } else {
            return "";
        }
    }

    public boolean sendP() throws IOException {
        if (spi.isOpen()) {
            spi.clear();
            System.out.println("About to send P");

            spi.writeByte((byte) 'P');

            if(spi.readByte() == (byte)'R'){
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void close() throws IOException {
        spi.close();


    }

    public boolean SendLine(ArrayList<Byte> line) throws InvalidHexException {

        if (spi.isOpen()) {
            
            try {

                for (int i = 0; i < line.size(); i++) {                  
                    spi.writeByte(line.get(i));
                }   

                short reply = spi.readByte();
               
                if (reply == 'R') {
                    spi.clear();
                    return true;
                } else if (reply == 'E') {
                    int error = spi.readByte() & 255;

                    throw new InvalidHexException(error);

                }
            } catch (IOException ex) {
                ex.printStackTrace();


            }
        }
        return false;


    }

    public void QuitFlashing() {
        if (spi.isOpen()) {
            try {
                spi.writeByte((byte) 'Q');


            } catch (IOException ex) {
                System.err.println("IOException thrown while trying to quit "
                        + "flashing the bootloader.");

            }
        }
    }
}
