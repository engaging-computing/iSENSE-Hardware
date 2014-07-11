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

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * The Serial Port Interface (SPI) class is used for selecting, opening and
 * closing serial ports.
 * @author Ryan Buckley <rbuckley@cs.uml.edu>
 * @author William Brendel <wbrendel@cs.uml.edu>
 */
public class SPI {
  private SerialPort serialPort;
  private CommPortIdentifier portIdentifier;
  private static final int COMM_TIMEOUT_MS = 100;
  private static final int RECEIVE_TIMEOUT_SECONDS_MS = 5;

  public SPI() {
    serialPort = null;
    portIdentifier = null;
  }

  /**
   * Retrieves a Map from serial port names to CommPortIdentifier objects.
   * @return A Map of serial port names to CommPortIdentifier objects.
   */
  public static Map<String, CommPortIdentifier> enumeratePorts() {

    HashMap<String, CommPortIdentifier> portMap     = new HashMap<String, CommPortIdentifier>();
    Enumeration<CommPortIdentifier> portEnumeration = CommPortIdentifier.getPortIdentifiers();

    while (portEnumeration.hasMoreElements()) {
      CommPortIdentifier currentPort = portEnumeration.nextElement();

      if (currentPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
        portMap.put(currentPort.getName(), currentPort);
      }
    }

    return portMap;
  }

  /**
   * Retrieves a list of valid serial port names.
   * @return A Vector of serial port names.
   */
  public static Vector<String> enumeratePortNames() {
    Map<String, CommPortIdentifier> portMap = enumeratePorts();
    Iterator<String> portMapIterator = portMap.keySet().iterator();
    Vector<String> portNames = new Vector<String>();

    while (portMapIterator.hasNext() == true) {
      portNames.add(portMapIterator.next());
    }

    return portNames;
  }

  /**
   * Opens the specified serial port using a given baud rate and flow control.
   * @param portName The port name. For example, "/dev/ttyUSB0".
   * @param baudRate The baud rate. For example, 9600.
   * @param useFlowControl Flow control flag.
   * @throws java.io.IOException If the port cannot be opened.
   * @throws gnu.io.PortInUseException If the port is already in use.
   * @throws gnu.io.UnsupportedCommOperationException If the specified port
   * cannot be opened in this way.
   */
  public void open(String portName, int baudRate, boolean useFlowControl)
      throws IOException, PortInUseException,
      UnsupportedCommOperationException {
    Map<String, CommPortIdentifier> portMap = SPI.enumeratePorts();
    portIdentifier = portMap.get(portName);

    if (portIdentifier == null) {
      throw new IOException();
    }

    serialPort = (SerialPort)portIdentifier.open(portName, SPI.COMM_TIMEOUT_MS);
    serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8,
        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    serialPort.enableReceiveTimeout(SPI.RECEIVE_TIMEOUT_SECONDS_MS);

    if (useFlowControl == true) {
      serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
          SerialPort.FLOWCONTROL_RTSCTS_OUT);
    }
  }

  /**
   * Closes the open serial port.
   */
  public void close() {
    if (serialPort != null) {
      serialPort.close();
      serialPort = null;
    }
  }

  /**
   * Attempts to read a single byte from the serial port.
   * @return A byte from the serial port.
   * @throws java.io.IOException If an error occurs while reading the byte.
   */
  public byte readByte() throws IOException {
    if (isOpen() == false) {
      throw new IOException();
    }

    InputStream inputStream = serialPort.getInputStream();
    Byte result = null;
    int attempts = 0;

    while (attempts < 10) {
      if (inputStream.available() > 0) {
        result = (byte) (inputStream.read() & 0xFF);
        System.out.println(result);
        break;
      } else {
        attempts++;
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    if (result == null) {
      throw new IOException();
    }

    return result;
  }

  /**
   * Writes a byte to the serial port.
   * @param aByte The byte to write.
   * @throws java.io.IOException If an error occurs while writing the byte.
   */
  public void writeByte(byte aByte) throws IOException {
    if (isOpen() == false) {
      throw new IOException();
    }

    OutputStream outputStream = serialPort.getOutputStream();
    outputStream.write(aByte);
  }

  /**
   * Clears the serial port input buffer.
   * @throws java.io.IOException If an error occurs while clearing the buffer.
   */
  public void clear() throws IOException {
    if (serialPort != null) {
      InputStream inputStream = serialPort.getInputStream();
      while (inputStream.available() > 0) {
        if (readByte() == -1) {
          break;
        }
      }
    } else {
      throw new IOException();
    }
  }

  /**
   * Determines if the serial port is currently open.
   * @return True if the port is open, otherwise false.
   */
  public boolean isOpen() {
    return serialPort != null;
  }

  /**
   * Retrieves the name of the serial port that this object represents.
   * @return The name of the port (e.g., "COM1", "/dev/ttyUSB0")
   */
  public String getPortName() {
    if (portIdentifier != null) {
      return portIdentifier.getName();
    } else {
      return null;
    }
  }
}