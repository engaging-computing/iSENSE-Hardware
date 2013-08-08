package edu.uml.cs.isense.datawalk_v2;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class CustomOnItemSelectedListener implements OnItemSelectedListener {
	public static String savedValueString = "10 seconds";
	public static int savedValueInt = 3;
	public static int mIntervalHack = 10000;
	 public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			//Toast.makeText(parent.getContext(), "You Selected : " + parent.getItemAtPosition(pos).toString(),Toast.LENGTH_SHORT).show();
				savedValueString = parent.getItemAtPosition(pos).toString();
				savedValueInt = pos;
				
				if (pos == 0){
					mIntervalHack = 1000;
				}else if (pos == 1){
					mIntervalHack = 2000;
				}else if (pos == 2){
					mIntervalHack = 5000;
				}else if (pos == 3){
					mIntervalHack = 10000;
				}else if (pos == 4){
					mIntervalHack = 30000;
				}else if (pos == 5){
					mIntervalHack = 60000;
				}
		  	
	 }

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
