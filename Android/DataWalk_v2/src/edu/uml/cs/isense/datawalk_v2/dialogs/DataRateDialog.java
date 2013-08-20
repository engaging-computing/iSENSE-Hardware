package edu.uml.cs.isense.datawalk_v2.dialogs;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.uml.cs.isense.datawalk_v2.DataWalk;
import edu.uml.cs.isense.datawalk_v2.R;

public class DataRateDialog extends ListActivity {
	
	private String[] dataRates = null;
	private String[] dataRateValues = null;
	private final String DEFAULT_INTERVAL = "10000";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

	    super.onCreate(savedInstanceState);
	    
		dataRates = getResources().getStringArray(R.array.dataRate);
		dataRateValues = getResources().getStringArray(R.array.dataRateVals);
		
		String currentDataRate = getSharedPreferences(DataWalk.INTERVAL_PREFS_KEY, Context.MODE_PRIVATE).getString(DataWalk.INTERVAL_VALUE_KEY,
				DEFAULT_INTERVAL);
		
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
				SharedPreferences sp = getSharedPreferences(DataWalk.INTERVAL_PREFS_KEY, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("DataUploadRate", DEFAULT_INTERVAL).commit();
				
				finish();
			}
	    	
	    });

	}
}