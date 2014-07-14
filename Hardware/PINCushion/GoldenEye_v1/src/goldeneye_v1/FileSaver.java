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

package goldeneye_v1;


import com.pinpoint.api.PinpointConverter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author jdalphon
 */
public class FileSaver {

    private JFrame parent;

    public FileSaver(JFrame frame) {
        parent = frame;
    }

    public boolean saveFile(ArrayList<String[]> data) {
        
        Calendar cal = Calendar.getInstance();
        
        String fheaders = "";
        String[] headers = PinpointConverter.fileHeaders;
        for(int i = 0; i < headers.length; i++){
            if(i < headers.length - 1){
                fheaders += headers[i] + ",";
            } else {
                fheaders += headers[i] + "\n";
            }        
        }

        String notes = "#DOU:" + cal.getTime() + "\n";
        int yesOrNo = 1;
        File selectedFile = new File("tmp.txt");
        try {
            JFileChooser fc = new JFileChooser();
            fc.addChoosableFileFilter(new TextFilter());
            File f = new File("data.txt");
            fc.setSelectedFile(f);
            int ret = fc.showSaveDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                if (selectedFile.exists()) {
                    yesOrNo = JOptionPane.showConfirmDialog(
                            parent,
                            "The file already exists would you like to replace it?",
                            "File Exists",
                            JOptionPane.YES_NO_OPTION);
                    if (yesOrNo == 1) {
                        saveFile(data);
                    } else {
                        BufferedWriter out = new BufferedWriter(new FileWriter(selectedFile, false));

                        //Write the notes and headers to the file first.
                        out.write(notes);
                        out.write(fheaders);

                        //Write the data to the file.
                        for (int i = 0; i < data.size(); i++) {
                            out.write(fmtData(data.get(i)));
                        }

                        //Cleanup
                        out.close();
                        return true;
                    }

                } else {
                    BufferedWriter out = new BufferedWriter(new FileWriter(selectedFile, false));
                    //Write the notes and headers to the file first.
                    out.write(notes);
                    out.write(fheaders);

                    //Write the data to the file.
                    for (int i = 0; i < data.size(); i++) {
                        out.write(fmtData(data.get(i)));
                    }
                    //Clean up
                    out.close();
                    return true;
                }
            } else if (ret == JFileChooser.CANCEL_OPTION) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private String fmtData(String temp[]) {
        String dataString = "";

        long unixts = 0;
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");
        Date parsed;

        try {
            parsed = format.parse(temp[0]);
            unixts = parsed.getTime();

            dataString += unixts + ",";

            for (int i = 1; i < temp.length; i++) {
                if (i == temp.length - 1) {
                    dataString += temp[i] + "\n";
                } else {
                    dataString += temp[i] + ",";
                }

            }

            return dataString;
        } catch (ParseException e) {
            System.err.println("Error while parsing date.");
        }

        return "";
    }

    /**
     * The textFilter class is used to filter out only text files for saving.
     *
     * @extends javax.swing.filechooser.FileFilter
     */
    class TextFilter extends javax.swing.filechooser.FileFilter {

        /**
         * The accept function checks a file to see if it is the correct type.
         *
         * @param f File to check
         * @return boolean True is file is correct type, false otherwize.
         */
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 && i < s.length() - 1) {
                if (s.substring(i + 1).toLowerCase().equals("txt")) {
                    return true;
                }
            }

            return false;
        }

        /**
         * The getDescription function returns the type of accepted files.
         *
         * @return String describing what types of files are shown.
         */
        public String getDescription() {
            return "txt files.";
        }
    }
}
