/**MIK1 - Entire Class **/

package edu.uml.cs.isense.pictures;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class Descriptor extends Activity {
	
	static String desString = "";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checklist);
        
        final CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
        final CheckBox checkBox2 = (CheckBox) findViewById(R.id.checkBox2);
        final CheckBox checkBox3 = (CheckBox) findViewById(R.id.checkBox3);
        final CheckBox checkBox4 = (CheckBox) findViewById(R.id.checkBox4);
        final CheckBox checkBox5 = (CheckBox) findViewById(R.id.checkBox5);
        final CheckBox checkBox6 = (CheckBox) findViewById(R.id.checkBox6);
        final CheckBox checkBox7 = (CheckBox) findViewById(R.id.checkBox7);
        final CheckBox checkBox8 = (CheckBox) findViewById(R.id.checkBox8);
        final CheckBox checkBox9 = (CheckBox) findViewById(R.id.checkBox9);
        final CheckBox checkBox10 = (CheckBox) findViewById(R.id.checkBox10);
        final CheckBox checkBox11 = (CheckBox) findViewById(R.id.checkBox11);
         
       	if(pictures.c1) checkBox1.setChecked(true);
       	else checkBox1.setChecked(false);
       	
       	if(pictures.c2) checkBox2.setChecked(true);
       	else checkBox2.setChecked(false);
       	
        if(pictures.c3) checkBox3.setChecked(true);
        else checkBox3.setChecked(false);
        
       	if(pictures.c4) checkBox4.setChecked(true);
       	else checkBox4.setChecked(false);
       	
       	if(pictures.c5) checkBox5.setChecked(true);
       	else checkBox5.setChecked(false);
       	
       	if(pictures.c6) checkBox6.setChecked(true);
       	else checkBox6.setChecked(false);
       	
       	if(pictures.c7) checkBox7.setChecked(true);
       	else checkBox7.setChecked(false);
       	
       	if(pictures.c8) checkBox8.setChecked(true);
       	else checkBox8.setChecked(false);
       	
       	if(pictures.c9) checkBox9.setChecked(true);
       	else checkBox9.setChecked(false);
       	
        if(pictures.c10) checkBox10.setChecked(true);
        else checkBox10.setChecked(false);
        
        if(pictures.c11) checkBox11.setChecked(true);
        else checkBox11.setChecked(false);
        
        Button selCan = (Button) findViewById(R.id.selectCancel);
        selCan.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		finish();
        	}
        });
       
        Button selOK = (Button) findViewById(R.id.selectOK);
        selOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				desString = "";
				
				if (checkBox1.isChecked()) { 
					pictures.c1 = true;
					if (desString.equals("")) {
						desString += "Merrimack River";
					} else {
						desString += ", Merrimack River";
					}
				} else {
					pictures.c1 = false;
				}
				 
				if (checkBox2.isChecked()) {
					pictures.c2 = true;
					if (desString.equals("")) {
						desString += "Wildlife";
					} else {
						desString += ", Wildlife";
					}
				} else {
					pictures.c2 = false;
				}
				
				if (checkBox3.isChecked()) {
					pictures.c3 = true;
					if (desString.equals("")) {
						desString += "Litter";
					} else {
						desString += ", Litter";
					}
				} else {
					pictures.c3 = false;
				}
				
				if (checkBox4.isChecked()) {
					pictures.c4 = true;
					if (desString.equals("")) {
						desString += "Plants";
					} else {
						desString += ", Plants";
					}
				} else {
					pictures.c4 = false;
				}
				
				if (checkBox5.isChecked()) {
					pictures.c5 = true;
					if (desString.equals("")) {
						desString += "People";
					} else {
						desString += ", People";
					}
				} else {
					pictures.c5 = false;
				}
				
				if (checkBox6.isChecked()) {
					pictures.c6 = true;
					if (desString.equals("")) {
						desString += "Buildings";
					} else {
						desString += ", Buildings";
					}
				} else {
					pictures.c6 = false;
				}
				
				if (checkBox7.isChecked()) {
					pictures.c7 = true;
					if (desString.equals("")) {
						desString += "Things in the Sky";
					} else {
						desString += ", Things in the Sky";
					}
				} else {
					pictures.c7 = false;
				}
				
				if (checkBox8.isChecked()) {
					pictures.c8 = true;
					if (desString.equals("")) {
						desString += "Boat";
					} else {
						desString += ", Boat";
					}
				} else {
					pictures.c8 = false;
				}
				
				if (checkBox9.isChecked()) {
					pictures.c9 = true;
					if (desString.equals("")) {
						desString += "Streets/Sidewalks";
					} else {
						desString += ", Streets/Sidewalks";
					}
				} else {
					pictures.c9 = false;
				}
				
				if (checkBox10.isChecked()) {
					pictures.c10 = true;
					if (desString.equals("")) {
						desString += "Horizon";
					} else {
						desString += ", Horizon";
					}
				} else {
					pictures.c10 = false;
				}
				
				if (checkBox11.isChecked()) {
					pictures.c11 = true;
					if (desString.equals("")) {
						desString += "Bridge";
					} else {
						desString += ", Bridge";
					}
				} else {
					pictures.c11 = false;
				}
				
				if (!(desString.equals(""))) { 
					pictures.takePhoto.setEnabled(true); 
				} else { 
					pictures.takePhoto.setEnabled(false); 
				}
			
				
				finish();
			}
        });

        // still in onCreate
	}
    // still in the class
}