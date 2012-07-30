package edu.uml.cs.isense.amusement;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.Experiment;

public class Experiments extends ListActivity {
	private ExperimentAdapter m_adapter; 
	@SuppressWarnings("unused")
		private RestAPI rapi; 
	@SuppressWarnings("unused")
		private Context mContext; 
	@SuppressWarnings("unused")
		private boolean finish = false;
	private ArrayList<Experiment> m_experiments;
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final ArrayList<Experiment> list = m_adapter.items;
	    final int loaded = m_adapter.itemsLoaded;
	    final boolean allLoaded = m_adapter.allItemsLoaded;
	    final int page = m_adapter.page;
	    Object[] objs = new Object[4];
	    objs[0] = list;
	    objs[1] = loaded;
	    objs[2] = allLoaded;
	    objs[3] = page;
	    return objs;
	}
	

	/** Called when the activity is first created. */
    @SuppressWarnings({ "unchecked", "deprecation" })
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.experiments);
        rapi = RestAPI.getInstance();
        mContext = this;
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	finish = true;
        }
        
        setResult(Activity.RESULT_CANCELED);
                
        final Object data = getLastNonConfigurationInstance();
        final Object[] dataList = (Object[]) data;
        // The activity is starting for the first time, load the data from the site
        if (data != null) {
            // The activity was destroyed/created automatically
        	m_experiments = (ArrayList<Experiment>) dataList[0];
        } else {
        	m_experiments = new ArrayList<Experiment>();
        }
            
        this.m_adapter = new ExperimentAdapter(getBaseContext(), R.layout.experimentrow, R.layout.loadrow, m_experiments);
        
        if (data != null) {
        	m_adapter.itemsLoaded = (Integer) dataList[1];
        	m_adapter.allItemsLoaded = (Boolean) dataList[2];
        	m_adapter.page = (Integer) dataList[3];
        }

        setListAdapter(this.m_adapter);
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Experiment e = m_experiments.get(position);

		Intent intent = new Intent();
        intent.putExtra("edu.uml.cs.isense.experiments.exp_id", e.experiment_id);
        intent.putExtra("edu.uml.cs.isense.experiments.srate", e.srate);

        setResult(Activity.RESULT_OK, intent);
        finish();
	}
    
    
}

