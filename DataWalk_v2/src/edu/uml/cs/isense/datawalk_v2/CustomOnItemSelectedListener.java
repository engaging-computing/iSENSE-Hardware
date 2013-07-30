package edu.uml.cs.isense.datawalk_v2;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class CustomOnItemSelectedListener implements OnItemSelectedListener {
	public static String savedValueString = "10 seconds";
	public static int savedValueInt = 3;
	 public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			//Toast.makeText(parent.getContext(), "You Selected : " + parent.getItemAtPosition(pos).toString(),Toast.LENGTH_SHORT).show();
				savedValueString = parent.getItemAtPosition(pos).toString();
				savedValueInt = pos;
				
		  	
	 }

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
