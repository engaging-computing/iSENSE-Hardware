/**MIK1 - Entire Class **/

package edu.uml.cs.isense.pictures;

import android.app.Activity;
import android.content.res.Resources;
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
        
        checkBox1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkBox1.setChecked(true);
				checkBox2.setChecked(false);
				checkBox3.setChecked(false);
				checkBox4.setChecked(false);
				checkBox5.setChecked(false);
			}      	
        });
      
        checkBox2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkBox1.setChecked(false);
				checkBox2.setChecked(true);
				checkBox3.setChecked(false);
				checkBox4.setChecked(false);
				checkBox5.setChecked(false);
			}      	
        });
        
        checkBox3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkBox1.setChecked(false);
				checkBox2.setChecked(false);
				checkBox3.setChecked(true);
				checkBox4.setChecked(false);
				checkBox5.setChecked(false);
			}      	
        });
        
        checkBox4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkBox1.setChecked(false);
				checkBox2.setChecked(false);
				checkBox3.setChecked(false);
				checkBox4.setChecked(true);
				checkBox5.setChecked(false);
			}      	
        });
        
        checkBox5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkBox1.setChecked(false);
				checkBox2.setChecked(false);
				checkBox3.setChecked(false);
				checkBox4.setChecked(false);
				checkBox5.setChecked(true);
			}      	
        });

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
				Resources res = getResources();
				String[] descriptors = res.getStringArray(R.array.descriptor_array);
				
				desString = "";
				
				if (checkBox1.isChecked()) { 
					pictures.c1 = true;
					if (desString.equals("")) {
						desString += descriptors[0];
					} else {
						desString += ", " + descriptors[0];
					}
				} else {
					pictures.c1 = false;
				}
				 
				if (checkBox2.isChecked()) {
					pictures.c2 = true;
					if (desString.equals("")) {
						desString += descriptors[1];
					} else {
						desString += ", " + descriptors[1];
					}
				} else {
					pictures.c2 = false;
				}
				
				if (checkBox3.isChecked()) {
					pictures.c3 = true;
					if (desString.equals("")) {
						desString += descriptors[2];
					} else {
						desString += ", " + descriptors[2];
					}
				} else {
					pictures.c3 = false;
				}
				
				if (checkBox4.isChecked()) {
					pictures.c4 = true;
					if (desString.equals("")) {
						desString += descriptors[3];
					} else {
						desString += ", " + descriptors[3];
					}
				} else {
					pictures.c4 = false;
				}
				
				if (checkBox5.isChecked()) {
					pictures.c5 = true;
					if (desString.equals("")) {
						desString += descriptors[4];
					} else {
						desString += ", " + descriptors[4];
					}
				} else {
					pictures.c5 = false;
				}
				
				if (!(desString.equals(""))) { 
					pictures.takePhoto.setEnabled(true); 
				} else { 
					pictures.takePhoto.setEnabled(false); 
				}
			
				setResult(pictures.RESULT_OK);
				finish();
			}
        });

        // still in onCreate
	}
    // still in the class
}