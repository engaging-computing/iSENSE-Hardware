package edu.uml.cs.isense.datawalk_v2.dialogs;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.uml.cs.isense.datawalk_v2.R;

public class DataRateDialog extends ListActivity {
	
	private String[] dataRates = null;
	String[] dataRateValues = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

	    super.onCreate(savedInstanceState);
	    
		dataRates = getResources().getStringArray(R.array.dataRate);
		dataRateValues = getResources().getStringArray(R.array.dataRateVals);
		
		String currentDataRate = getSharedPreferences("RecordingPrefs", 0).getString("DataUploadRate",
				"10000");
		
		int currentIndex = 0;
		for (String s: dataRateValues) {
			if (s.equals(currentDataRate)) break;
			currentIndex++;
		}

	    setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice, dataRates));

	   final ListView listView = getListView();
	    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    listView.setItemChecked(currentIndex, true);
	    listView.setClickable(true);
	    listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				SharedPreferences sp = getSharedPreferences("RecordingPrefs", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("DataUploadRate", dataRateValues[arg2]).commit();
				
				finish();
			}
	    	
	    });

	}
}