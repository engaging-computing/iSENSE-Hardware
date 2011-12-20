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


import com.pinpoint.eval.Expression;
import com.pinpoint.exceptions.IncompatibleConversionException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains all of the conversions needed to upload data from a PINPoint
 *
 * @author jdalphon
 */
public class PinpointConverter {

    private byte[] raw = null;
    private Expression b1Conv, b2Conv, m1Conv, m2Conv;
    private HashMap<Integer, Integer> settings;
    private ArrayList<String[]> conversions;
    public static int VALUE = 0;
    public static int JACK = 1;
    public static int HUMAN_READABLE = 2;
    public static int ISENSE_READABLE = 3;
    public static int EQUATION = 4;
    public static int BTA1  = 13;
    public static int BTA2  = 14;
    public static int MINI1 = 15;
    public static int MINI2 = 16;
    private DecimalFormat threeDForm = new DecimalFormat("#.###");

    //Fred wants them to be different for some reason.
    public static String[] tableHeaders = new String[]{"Time (GMT)", "Latitude", "Longitude", "Altitude GPS (m)", "Altitude (m)", "Pressure (atm)", "Temperature (c)", "Humidity (%rh)", "Light (lux)", "X-Accel", "Y-Accel", "Z-Accel", "Acceleration", "BTA1", "BTA2", "Mini1", "Mini2"};
    public static String[] fileHeaders  = new String[]{"Time","Latitude","Longitude","Altitude GPS","Altitude","Pressure","Temperature","Humidity","Light","x","y","z","Acceleration","BTA1","BTA2","Mini1","Mini2"};

    public PinpointConverter(HashMap<Integer, Integer> settings, ArrayList<String[]> conversions) throws IncompatibleConversionException {


        this.settings = settings;
        this.conversions = conversions;
        boolean b1, b2, m1, m2;
        b1 = m1 = b2 = m2 = false;

        for (String[] i : conversions) {

            if (settings.get(PinComm.BTA1) == Integer.parseInt(i[0])) {
                b1Conv = new Expression(i[EQUATION]);
                tableHeaders[BTA1] = i[HUMAN_READABLE];
                fileHeaders[BTA1] = i[ISENSE_READABLE] + "(b1)";
                b1 = true;
            }
            if (settings.get(PinComm.BTA2) == Integer.parseInt(i[0])) {
                b2Conv = new Expression(i[EQUATION]);
                tableHeaders[BTA2] = i[HUMAN_READABLE];
                fileHeaders[BTA2] = i[ISENSE_READABLE] + "(b2)";
                b2 = true;
            }
            if (settings.get(PinComm.MINI1) == Integer.parseInt(i[0])) {
                m1Conv = new Expression(i[EQUATION]);
                tableHeaders[MINI1] = i[HUMAN_READABLE];
                fileHeaders[MINI1] = i[ISENSE_READABLE] + "(m1)";
                m1 = true;
            }
            if (settings.get(PinComm.MINI2) == Integer.parseInt(i[0])) {
                m2Conv = new Expression(i[EQUATION]);
                tableHeaders[MINI2] = i[HUMAN_READABLE];
                fileHeaders[MINI2] = i[ISENSE_READABLE] + "(m2)";
                m2 = true;
            }
        }
        
        if (m1 == false || m2 == false || b1 == false || b2 == false) {    
            throw new IncompatibleConversionException();
        }
    }

    private String latitudeConversion() {
        int lat = raw[1];
        if (lat >= 0) {
            lat = lat & 255;
        }
        lat = (lat << 8) + (raw[0] & 255);
        float flat = Float.parseFloat(("" + lat + "." + (((raw[3] & 255) << 8) + (raw[2] & 255))));
        int degs = (int) flat / 100;
        float min = flat - degs * 100;
        String retVal = "" + (degs + min / 60);

        if (retVal.compareTo("200.0") == 0) {
            return "";
        } else {
            return retVal;
        }
    }

    private String longitudeConversion() {
        int lon = raw[5];
        if (lon >= 0) {
            lon = lon & 255;
        }
        lon = (lon << 8) + (raw[4] & 255);
        float flon = Float.parseFloat("" + lon + "." + (((raw[7] & 255) << 8) + (raw[6] & 255)));
        int degs = (int) flon / 100;
        float min = flon - degs * 100;
        String retVal = "" + (degs + min / 60);

        if (retVal.compareTo("200.0") == 0) {
            return "";
        } else {
            return retVal;
        }
    }

    private String altitudeConversion() {

        Double pressure = PressureConversion() * 0.01;

        Double pOverP = pressure / 1013.25;


        Double altitude = 44330 * (1 - (Math.pow(pOverP, 1 / 5.255)));

        return Double.valueOf(threeDForm.format(altitude)) + "";

    }

    private String altitudeFromGPS() {

        int reading = ((raw[9] & 255) << 8) + (raw[8] & 255);

        if (reading == 60000) {
            return " ";
        } else {
            return reading + "";
        }
    }

    private int PressureConversion() {
        int reading = ((raw[13] & 255) << 24) + ((raw[12] & 255) << 16) + ((raw[11] & 255) << 8) + (raw[10] & 255);

        return reading;
    }

    private String PressureATMConversion() {
        return Double.valueOf(threeDForm.format(PressureConversion() * 0.00000986923267)) + "";
    }

    private String TemperatureConversion() {
        int reading = ((raw[15] & 255) << 8) + (raw[14] & 255);

        //return ((float) reading / 10) + "";

        //int x[] = fixBinaryString(Integer.toBinaryString(reading));

        return ((float) unsignedToSigned(reading,16) / 10) + "";
    }

    private String AccelXConversion() {
        //16 = XXXX XXXX
        //17 = YYYY YYXX
        short reading = (short) (((raw[16] & 255)) + ((raw[17] & 0x03) << 8));

        int x[] = fixBinaryString(Integer.toBinaryString(reading));
        return myUnsignedToSigned(x) + "";


    }

    private String AccelYConversion() {
        //17 = YYYY YYXX
        //18 = ZZZZ YYYY
        int reading = (((raw[17] & 0xFC) >> 2) + ((raw[18] & 0x0F) << 6));


        int y[] = fixBinaryString(Integer.toBinaryString(reading));
        return myUnsignedToSigned(y) + "";

        //System.out.println("Y " + unsignedToSigned(reading ,10) * 0.1533);
        //return (reading * 0.1533) + "";
    }

    private String AccelZConversion() {
        //18 = ZZZZ YYYY
        //19 = EEZZ ZZZZ
        short reading = (short) (((raw[18] & 0xF0) >> 4) + ((raw[19] & 0x3f) << 4));


        int z[] = fixBinaryString(Integer.toBinaryString(reading));
        return myUnsignedToSigned(z) + "";
    }

    private int[] fixBinaryString(String bin) {
        String y[] = bin.split("");
        int x = 10 - (y.length - 1);
        //System.out.println(x);
        for (int i = 0; i < x; i++) {
            bin = "0" + bin;
        }

        y = bin.split("");
        int z[] = new int[y.length - 1];

        for (int i = 0; i < z.length; i++) {
            z[i] = Integer.parseInt(y[i + 1]);
        }

        return z;
    }

    private String AccelSoS() {
        double x = Double.parseDouble(this.AccelXConversion());
        double y = Double.parseDouble(this.AccelYConversion());
        double z = Double.parseDouble(this.AccelZConversion());

        Double sos = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

        return Double.valueOf(threeDForm.format(sos)) + "";
    }

    private String LightConversion() {
        //19 = EEZZ ZZZZ
        //20 = MMMM MMEE
        //21 = HHHH HHMM
        //E = exponent bits && M = mantissa bits

        int exponent = ((raw[19] & 0xC0) >> 6) + ((raw[20] & 0x03) << 2);
        int mantissa = ((raw[20] & 0xFC) >> 2) + ((raw[21] & 0x03) << 6);
        double value = (Math.pow(2.0, exponent)) * mantissa * 0.025;

        return Double.valueOf(threeDForm.format(value)) + "";

    }

    private String HumidityConversion() {
        //21 = HHHH HHMM
        //22 = 1111 HHHH
        int reading = ((raw[21] & 0xFC) >> 2) + ((raw[22] & 0x0F) << 6);

       // double fixed = (161.29 * (0.000967742 * reading - 0.16)) / (1.0546 - 0.00216 * Double.parseDouble(TemperatureConversion()));

        double fixed = (0.204036 * reading - 25.8065) / (1.0546 - 0.00216 * Double.parseDouble(TemperatureConversion())) + 6;

        return Double.valueOf(threeDForm.format(fixed)) + "";
    }

    private String MiniOneConversion() {
        //22 = 1111 HHHH
        //23 = 2211 1111
        int reading = ((raw[22] & 0xF0) >> 4) + ((raw[23] & 0x3F) << 4);

        Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
        variables.put("x", new BigDecimal(reading));
        BigDecimal result = m1Conv.eval(variables);
        return result.setScale(3, RoundingMode.UP) + "";
    }

    private String MiniTwoConversion() {
        //23 = 2211 1111
        //24 = 2222 2222
        int reading = ((raw[23] & 0xC0) >> 6) + ((raw[24] & 0xFF) << 2);

        Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
        variables.put("x", new BigDecimal(reading));
        BigDecimal result = m2Conv.eval(variables);
        return result.setScale(3, RoundingMode.UP) + "";
    }

    private String BtaOneConversion() {
        //25 = 1111 1111
        //26 = 2222 2211
        int reading = (raw[25] & 0xFF) + ((raw[26] & 0x03) << 8);

        Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
        variables.put("x", new BigDecimal(reading));
        BigDecimal result = b1Conv.eval(variables);
        return result.setScale(3, RoundingMode.UP) + "";
    }

    private String BtaTwoConversion() {
        //26 = 2222 2211
        //27 = ssss 2222
        int reading = ((raw[26] & 0xFC) >> 2) + ((raw[27] & 0x0F) << 6);

        Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
        variables.put("x", new BigDecimal(reading));
        BigDecimal result = b2Conv.eval(variables);
        return result.setScale(3, RoundingMode.UP) + "";
    }

    private String TimeConversion() {
        //27 = SSSS 2222
        //28 = MMMM MMSS
        //29 = DDDH HHHH
        //30 = MMMT TTTT
        //31 = YYYY YYYM

        int hours, minutes, seconds, dayOfWeek, date, month, year;

        seconds = ((raw[27] & 0xF0) >> 4) + ((raw[28] & 0x03) << 4);
        minutes = ((raw[28] & 0xFC) >> 2);
        hours = (raw[29] & 0x1F);
        dayOfWeek = ((raw[29] & 0xE0) >> 5);
        date = (raw[30]) & 0x1F;
        month = ((raw[30] & 0xE0) >> 5) + ((raw[31] & 0x01) << 3);
        year = (((raw[31] & 0xFE) >> 1) & 255) + 2000;



        return hR(month) + "/" + hR(date) + "/" + year + " " + hR(hours) + ":" + hR(minutes) + ":" + hR(seconds) + ":00";
    }

    public void PrintConvertedValues(byte[] input) {
        if (input.length == 32) {
            raw = input;
            System.out.println(
                    "Latitude = " + latitudeConversion() + "\nLongitude= " + longitudeConversion() + "\nAltitude = " + altitudeConversion() + "\nPressure = "
                    + PressureConversion() + "\nTemperature = " + TemperatureConversion() + "\nAccel X = " + AccelXConversion() + "\nAccel Y= "
                    + AccelYConversion() + "\nAccel Z = " + AccelZConversion() + "\nLight = " + LightConversion() + "\nHumidity = " + HumidityConversion() + "\nMini 1 = "
                    + MiniOneConversion() + "\nMini 2 =  " + MiniTwoConversion() + "\nBta 1 = " + BtaOneConversion() + "\nBta 2 = " + BtaTwoConversion()
                    + "\n Time = " + TimeConversion());
        }
    }

    private String hR(int x) {
        if (x < 10) {
            return "0" + x;
        }

        return x + "";
    }

    public void fixTime(ArrayList<String[]> records, int SampleRate) {
        int currentSecond = Integer.parseInt(records.get(0)[0].split(":")[2]);
        int currentCounter = 0;
        int SamplesPerSecond = 1000 / SampleRate;
        int i, j, k;
        String[] fixed = new String[12];
        int testSecond = 0;

        for (i = 0; i < records.size(); i++) {
            for (j = i; j < records.size(); j++) {
                testSecond = Integer.parseInt(records.get(j)[0].split(":")[2]);
                if (testSecond == currentSecond) {
                    currentCounter++;
                } else {
                    break;
                }
            }

            int tmp = SamplesPerSecond;
            int millis;

            for (k = j - 1; k >= j - currentCounter; k--) {
                millis = 100 / SamplesPerSecond * --tmp;
                String x = records.get(k)[0].substring(0, 19);

                if (millis < 10) {
                    x = x + ":0" + millis + "0";
                } else {
                    x = x + ":" + millis + "0";
                }
                fixed = records.get(k);
                fixed[0] = x;
                records.set(k, fixed);
            }



            i = j;
            currentCounter = 1;
            currentSecond = testSecond;
        }

    }

    public String[] convertAll(byte[] input) {
        raw = input;

        String[] values = new String[17];
        values[0] = this.TimeConversion();
        values[1] = this.latitudeConversion();
        values[2] = this.longitudeConversion();
        values[3] = this.altitudeFromGPS();
        values[4] = this.altitudeConversion();
        values[5] = this.PressureATMConversion();
        values[6] = this.TemperatureConversion();
        values[7] = this.HumidityConversion();
        values[8] = this.LightConversion();
        values[9] = this.AccelXConversion();
        values[10] = this.AccelYConversion();
        values[11] = this.AccelZConversion();
        values[12] = this.AccelSoS();
        values[13] = this.BtaOneConversion();
        values[14] = this.BtaTwoConversion();
        values[15] = this.MiniOneConversion();
        values[16] = this.MiniTwoConversion();

        return values;
    }

    double roundThreeDecimals(double d) {
        return Double.valueOf(threeDForm.format(d));
    }

    public double myUnsignedToSigned(int[] bits) {
        int ret = 1;
        int x = 0;
        String tmp = "";
        if (bits[0] == 1) {
            bits[0] = 0;

            for (int i = 1; i < bits.length; i++) {
                if (bits[i] == 0) {
                    bits[i] = 1;
                } else {
                    bits[i] = 0;
                }
            }

            int carry = 1;
            for (int i = (bits.length - 1); i > -1; i--) {
                if (bits[i] + carry == 2) {
                    bits[i] = 0;
                } else {
                    bits[i] = 1;
                    break;
                }
            }

            for (int i = 0; i < bits.length; i++) {
                //System.out.print(bits[i]);
                if (bits[i] == 0) {
                } else {
                    x += Math.pow(2, 9 - i);
                }
            }
            // System.out.println();
            return (x * -0.1533);

            //System.out.println("-----");
        } else {
            for (int i = 0; i < bits.length; i++) {
                //System.out.print(bits[i]);
                if (bits[i] == 0) {
                } else {
                    x += Math.pow(2, 9 - i);
                }
            }

            return x * 0.1533;
        }
    }

    public int unsignedToSigned(int num, int bits) {

        int ret = 1;
        int x;
        if ((num & (1 << (bits - 1))) > 0) {

            for (x = 0; x < bits - 1; x++) {
                if ((num & (1 << x)) == 0) {
                    ret += 1 << x;
                    System.out.println(x + " : " + ret);
                }
                ret = -ret;
            }

        } else {
            return num;
        }
        return ret;
    }
}
