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

import com.pinpoint.api.PinComm;
import com.pinpoint.api.pinpointInterface;
import com.pinpoint.exceptions.MissingLogFileException;
import com.pinpoint.exceptions.MultipleCountTypesSelectedException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The Configuration window is a simple UI that allows the user to set the various
 * settings on the PINPoint. 
 * 
 * James Dalphond <jdalphon@cs.uml.edu>
 */
public class ConfigurationWindow extends javax.swing.JDialog {

    private pinpointInterface ppt = null;
    private int curGPS = 0;
    private boolean checkedOnce = false;
    private boolean countTypeSelected = false;
    private HashMap<Integer, Integer> pptSettings, tempSettings;
    private ArrayList<String[]> conversions;

    /** Creates new form ConfigurationWindow */
    public ConfigurationWindow(JFrame parent, pinpointInterface pinpoint) throws MissingLogFileException, IOException, BackingStoreException {

        initComponents();

        this.ppt = pinpoint;
      
        initValues();
    
        this.setSize(650, 620);
        this.setResizable(false);

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                ppt.disconnect();
                ((ConfigurationWindow) e.getSource()).dispose();
            }
        });
    }

    /**
     * Read in the current settings from the PINPoint
     * and set all of the fields of the configuration
     * windows to those settings.
     *
     * If values are changed the PINPoint should reset
     * and the data should be cleared.
     */
    private void initValues() throws IOException, FileNotFoundException, MissingLogFileException, BackingStoreException {

        int x = ppt.getSerialNumber();
        this.setTitle("Pinpoint Configuration: SN# " + x);

        //Get all settings from the PINPoint.
        pptSettings = ppt.GetSettings();

        //Main sample rate
        double sRate = (double) pptSettings.get(PinComm.SAMPLE_RATE);
        sampleRate.setText(sRate / 1000 + "");

        //How many satellites should we connect to before we trust GPS


        int gps = pptSettings.get(PinComm.GPS);

        if (gps > 0) {
            gpsCount.setSelectedIndex(gps - 3);
        } else {
            gpsCheckBox.setSelected(true);
            gpsCount.setEnabled(false);
        }


        conversions = ppt.GetConversions();

        //Add the correct conversions to the correct boxes
        for (String[] temp : conversions) {

            if (temp[1].compareToIgnoreCase("mini") == 0) {
                mini1Type.addItem(temp[2]);
                mini2Type.addItem(temp[2]);
            } else if (temp[1].compareToIgnoreCase("bta") == 0) {
                bta1Type.addItem(temp[2]);
                bta2Type.addItem(temp[2]);
            } else if (temp[1].compareToIgnoreCase("any") == 0) {
                bta1Type.addItem(temp[2]);
                bta2Type.addItem(temp[2]);
                mini1Type.addItem(temp[2]);
                mini2Type.addItem(temp[2]);
            }
        }


        //Set the correct index for each box.

        for (int i = 0; i < conversions.size(); i++) {

            if (Integer.parseInt(conversions.get(i)[0]) == pptSettings.get(PinComm.BTA1)) {
                bta1Type.setSelectedItem((String) conversions.get(i)[2]);
            }
            if (Integer.parseInt(conversions.get(i)[0]) == pptSettings.get(PinComm.BTA2)) {
                bta2Type.setSelectedItem((String) conversions.get(i)[2]);
            }
            if (Integer.parseInt(conversions.get(i)[0]) == pptSettings.get(PinComm.MINI1)) {
                mini1Type.setSelectedItem((String) conversions.get(i)[2]);
            }
            if (Integer.parseInt(conversions.get(i)[0]) == pptSettings.get(PinComm.MINI2)) {
                mini2Type.setSelectedItem((String) conversions.get(i)[2]);
            }
        }



    }

    /**
     * Checks to see if any changes were made to settings boxes.
     *
     * This is done so that the pinpoint can be reset and data
     * cleared if settings are changed.
     *
     * @return
     */
    private HashMap<Integer, Integer> checkForChanges() throws MultipleCountTypesSelectedException {

        tempSettings = new HashMap<Integer, Integer>();

        //Create a temporary HashMap of items Selected in GUI
        if (!gpsCheckBox.isSelected()) {           
            tempSettings.put(PinComm.GPS, gpsCount.getSelectedIndex() + 3);
        } else {
            tempSettings.put(PinComm.GPS, 0);
        }

        tempSettings.put(PinComm.SAMPLE_RATE, (int) ((Double.parseDouble(sampleRate.getText())) * 1000));

        //External Sensors are a pain in the ass
        for (int i = 0; i < conversions.size(); i++) {
            if (((String) bta1Type.getSelectedItem()).compareToIgnoreCase(conversions.get(i)[2]) == 0) {
                tempSettings.put(PinComm.BTA1, Integer.parseInt(conversions.get(i)[0]));
            }
            if (((String) bta2Type.getSelectedItem()).compareToIgnoreCase(conversions.get(i)[2]) == 0) {
                tempSettings.put(PinComm.BTA2, Integer.parseInt(conversions.get(i)[0]));
            }
            if (((String) mini1Type.getSelectedItem()).compareToIgnoreCase(conversions.get(i)[2]) == 0) {
                tempSettings.put(PinComm.MINI1, Integer.parseInt(conversions.get(i)[0]));
            }
            if (((String) mini2Type.getSelectedItem()).compareToIgnoreCase(conversions.get(i)[2]) == 0) {
                tempSettings.put(PinComm.MINI2, Integer.parseInt(conversions.get(i)[0]));
            }
        }

        //Especially since we can only have one "Counter Type" setting
        if (tempSettings.containsValue(0)) {
            Iterator<Integer> iter = tempSettings.values().iterator();
            int counter = 0;
            while (iter.hasNext()) {
                if (iter.next() == 0) {
                    counter++;
                }
            }
            if (counter > 1) {
                throw new MultipleCountTypesSelectedException();
            }
        }


        //Save any changes to a hash map of <Setting,Value>
        HashMap<Integer, Integer> changes = new HashMap<Integer, Integer>();

        for (int i = 0; i < 14; i++) {
            if ((int) tempSettings.get(i) != (int) pptSettings.get(i)) {
                //System.out.println(tempSettings.get(i) + ":" + pptSettings.get(i));
                changes.put(i, tempSettings.get(i));
            }
        }

        return changes;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        writeChangesButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        sampleRate = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        gpsCount = new javax.swing.JComboBox();
        gpsCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        pptImage = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        bta1Type = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        bta2Type = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        mini1Type = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        mini2Type = new javax.swing.JComboBox();

        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 478, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 552, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(goldeneye_v1.GoldenEye_v1App.class).getContext().getResourceMap(ConfigurationWindow.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setAlwaysOnTop(true);
        setBackground(resourceMap.getColor("Form.background")); // NOI18N
        setName("Form"); // NOI18N

        jPanel4.setBackground(resourceMap.getColor("jPanel4.background")); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        jPanel2.setBackground(resourceMap.getColor("jPanel2.background")); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        writeChangesButton.setText(resourceMap.getString("writeChangesButton.text")); // NOI18N
        writeChangesButton.setName("writeChangesButton"); // NOI18N
        writeChangesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeChangesButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(writeChangesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 117, Short.MAX_VALUE)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(writeChangesButton)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(resourceMap.getColor("jPanel3.background")); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        sampleRate.setText(resourceMap.getString("sampleRate.text")); // NOI18N
        sampleRate.setName("sampleRate"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel10.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        gpsCount.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "3-Sloppy", "4-Better", "5-Best(Longer Lock Time)"}));
        gpsCount.setName("gpsCount"); // NOI18N

        gpsCheckBox.setText(resourceMap.getString("gpsCheckBox.text")); // NOI18N
        gpsCheckBox.setName("gpsCheckBox"); // NOI18N
        gpsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gpsCheckBoxActionPerformed(evt);
            }
        });

        jSeparator1.setName("jSeparator1"); // NOI18N

        pptImage.setBackground(resourceMap.getColor("pptImage.background")); // NOI18N
        pptImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pptImage.setIcon(resourceMap.getIcon("pptImage.icon")); // NOI18N
        pptImage.setText(resourceMap.getString("pptImage.text")); // NOI18N
        pptImage.setName("pptImage"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        bta1Type.setModel(new javax.swing.DefaultComboBoxModel(new String[] { }));
        bta1Type.setName("bta1Type"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        bta2Type.setModel(new javax.swing.DefaultComboBoxModel(new String[] {  }));
        bta2Type.setName("bta2Type"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        mini1Type.setModel(new javax.swing.DefaultComboBoxModel(new String[] {  }));
        mini1Type.setName("mini1Type"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        mini2Type.setModel(new javax.swing.DefaultComboBoxModel(new String[] {  }));
        mini2Type.setName("mini2Type"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(gpsCount, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sampleRate, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gpsCheckBox)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bta1Type, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bta2Type, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(mini1Type, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(mini2Type, 0, 308, Short.MAX_VALUE)))
                    .addComponent(pptImage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(sampleRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gpsCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gpsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pptImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(bta1Type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bta2Type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mini1Type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mini2Type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap(84, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void writeChangesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeChangesButtonActionPerformed
        try {

            HashMap changes = checkForChanges();

            if (!changes.isEmpty()) {
                Object[] options = {"Continue",
                    "Cancel"};

                int n = JOptionPane.showOptionDialog(this,
                        "Changes to settings have been made.\n"
                        + "By clicking YES, data will be erased.\n"
                        + "from the PINPoint and it will reset.",
                        "Write changes",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, //do not use a custom Icon
                        options, //the titles of buttons
                        options[0]); //default button title

                if (n == 0){
                    ppt.SetMultipleSettings(changes);
                    //ppt.clearDataFromPinpoint();
                    ppt.resetPinpoint();
                    ppt.disconnect();
                    this.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "No changes were found.");
                ppt.disconnect();
                this.dispose();
            }
        } catch (MultipleCountTypesSelectedException ex) {
            JOptionPane.showMessageDialog(this, "You can only have one \"Count Type\" sensor", "Selection Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_writeChangesButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        ppt.disconnect();
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void gpsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gpsCheckBoxActionPerformed
        if (gpsCheckBox.isSelected()) {
            gpsCount.setEnabled(false);
        } else {
            gpsCount.setEnabled(true);
            gpsCount.setSelectedIndex(0);
        }
}//GEN-LAST:event_gpsCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox bta1Type;
    private javax.swing.JComboBox bta2Type;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox gpsCheckBox;
    private javax.swing.JComboBox gpsCount;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox mini1Type;
    private javax.swing.JComboBox mini2Type;
    private javax.swing.JLabel pptImage;
    private javax.swing.JTextField sampleRate;
    private javax.swing.JButton writeChangesButton;
    // End of variables declaration//GEN-END:variables
}
