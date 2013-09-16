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

/**
 * Allows the user to choose their recording interval.
 * 
 * @author Rajia
 */
public class DataRateDialog extends ListActivity {

	private String[] dataRates = null;
	private String[] dataRateValues = null;
	private final String DEFAULT_INTERVAL = "10000";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get strings arrays for labels and corresponding values
		dataRates = getResources().getStringArray(R.array.dataRate);
		dataRateValues = getResources().getStringArray(R.array.dataRateVals);

		// Get the current
		String currentDataRate = getSharedPreferences(
				DataWalk.INTERVAL_PREFS_KEY, Context.MODE_PRIVATE).getString(
				DataWalk.INTERVAL_VALUE_KEY, DEFAULT_INTERVAL);

		// This loop makes sure the last value selected is the one set as the
		// currentIndex
		int currentIndex = 0;
		for (String s : dataRateValues) {
			if (s.equals(currentDataRate))
				break;
			currentIndex++;
		}

		// Creates our radio button ListView
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, dataRates));

		// Gets the current list view and sets it to use radio buttons
		final ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		// Selects our currentIndex (the last one clicked)
		listView.setItemChecked(currentIndex, true);

		// Prepares the onClick method for each option
		listView.setClickable(true);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {

				// Saves selection in preferences
				SharedPreferences sp = getSharedPreferences(
						DataWalk.INTERVAL_PREFS_KEY, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(DataWalk.INTERVAL_VALUE_KEY, dataRateValues[index] )
						.commit();

				// Dismisses the picker
				finish();
			}

		});

	}
}