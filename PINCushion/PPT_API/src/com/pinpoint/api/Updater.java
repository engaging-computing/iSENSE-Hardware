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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 *
 * @author jdalphon
 */
public class Updater {

    public Updater() {
    }

    public void getDataFromIsense() {
        boolean result = false;

        try {
            // Construct data
            String data = URLEncoder.encode("key1", "UTF-8") + "=" + URLEncoder.encode("value1", "UTF-8");
            data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" + URLEncoder.encode("value2", "UTF-8");

            // Send data
            URL url = new URL("http://isense.cs.uml.edu/duck/pptV4Conversions.log");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";


            String file = System.getProperty("java.io.tmpdir") + "/pptV4Conversions.log";

            // Create a buffered writter to write the response from the server to the conversions file.
            BufferedWriter br = new BufferedWriter(new FileWriter(file, false));



            // Write the conversions file based on the response from the server.
            while ((line = rd.readLine()) != null) {
                br.write(line + "\n");
                // System.out.println("> " + line.split(",")[1]);
            }

            // Cleanup
            wr.close();
            rd.close();
            br.close();

            System.out.println("File has been downloaded to " + file);

        } catch (UnsupportedEncodingException ex) {
            System.err.println("UnsupportedEncodingException ex");
        } catch (MalformedURLException ex) {
            System.err.println("MalformedURLException ex");
        } catch (IOException ex) {
            System.err.println("IOException");
        }

    }

    public ArrayList<String[]> getDataFromGoogleDoc() throws IOException{

        String fileURL = "https://spreadsheets.google.com/spreadsheet/pub?key=0Aos8U59XvkPkdDFPUFkzUlYzSkt5N2ZMRXpVZ01nUEE&single=true&gid=0&output=csv";

        URL url;

        HttpURLConnection conn;

        BufferedReader rd;

        String line;

        String result = "";

        url = new URL(fileURL);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String file;

        // Create a buffered writter to write the response from the server to the conversions file.

        ArrayList<String[]> conversions = new ArrayList<String[]>();
        // Write the conversions file based on the response from the server.

        line = rd.readLine();

        while ((line = rd.readLine()) != null) {
            conversions.add(line.split(","));

        }
        return conversions;
 
    }

    public boolean makeTempFilePermenant() {
        File tmpFile, permFile;

        if (System.getProperty("os.name").contains("Win")) {
            permFile = new File("lib\\pptV4Conversions.log");
            tmpFile = new File(System.getProperty("java.io.tmpdir") + "pptV4Conversions.log");
        } else {
            tmpFile = new File(System.getProperty("java.io.tmpdir") + "/pptV4Conversions.log");
            permFile = new File("lib/pptV4Conversions.log");
        }



        FileChannel source = null;
        FileChannel destination = null;

        try {
            System.out.println("Attempting to make conversions permenant");
            source = new FileInputStream(tmpFile).getChannel();
            destination = new FileOutputStream(permFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (FileNotFoundException ex) {
            System.err.println("Could not make conversions file permenant");
            return false;
        } catch (IOException ex) {
            System.err.println("Could not make conversions file permenant");
            return false;
        }

        System.out.println("Successfuly made conversions file permenant");
        return true;
    }
}
