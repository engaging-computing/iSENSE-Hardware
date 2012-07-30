package edu.uml.cs.isense.amusement;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.uml.cs.isense.comm.RestAPI;
import edu.uml.cs.isense.objects.ExpLoaded;
import edu.uml.cs.isense.objects.Experiment;
import edu.uml.cs.isense.objects.ExperimentField;

public class ExperimentAdapter extends ArrayAdapter<Experiment> {
	@SuppressWarnings("unused")
	private final int maxDimension = 50;
	public ArrayList<Experiment> items;
	private Context mContext;
	private int resourceID;
	private int loadingRow;
	public int itemsLoaded;
	public boolean allItemsLoaded;
	private Boolean loading;
	private UIUpdateTask updateTask;
	private Handler uiHandler = new Handler();
	private RestAPI rapi;
	public static final int pageSize = 10;
	public int page = 0;
	public String action = "browse";
	public String query = "";

	public ExperimentAdapter(Context context, int textViewResourceId,
			int loadingRow, ArrayList<Experiment> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		mContext = context;
		resourceID = textViewResourceId;
		this.loadingRow = loadingRow;
		itemsLoaded = 0;
		allItemsLoaded = false;
		loading = false;
		updateTask = new UIUpdateTask();
		rapi = RestAPI.getInstance();
	}

	public int getCount() {
		int count = itemsLoaded;
		if (!allItemsLoaded)
			++count;
		return count;
	}

	public Experiment getItem(int position) {
		Experiment result;
		synchronized (items) {
			result = items.get(position);
		}
		return result;
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		boolean isLastRow = position >= itemsLoaded;
		View v = convertView;
		LayoutInflater vi = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (isLastRow) {
			v = vi.inflate(loadingRow, null);
		} else if (v == null || v.findViewById(R.id.toptext) == null) {
			v = vi.inflate(resourceID, null);
		}

		if (!isLastRow && items.size() != 0) {
			Experiment e = items.get(position);
			if (e != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(e.name);
				}
				if (bt != null) {
					if (e.firstname == "" && e.lastname == "") {
						bt.setVisibility(View.GONE);
					} else {
						bt.setText("Created By: " + e.firstname + " "
								+ e.lastname);
					}
				}
			}
		} else {
			if (!allItemsLoaded) {
				page++;
				synchronized (loading) {
					if (!loading.booleanValue()) {
						loading = Boolean.TRUE;
						Thread t = new LoadingThread();
						t.start();
					}
				}
			} else {
				uiHandler.post(updateTask);
			}
		}
		return v;
	}
	
	class LoadingThread extends Thread {
		public void run() {
			ArrayList<Experiment> new_items = rapi.getExperiments(page, pageSize, action, query);
			if (new_items.size() == 0) {
				allItemsLoaded = true;
			} else {
				synchronized(items) {
					items.addAll(new_items);
				}
				itemsLoaded += new_items.size();
			}
			
			synchronized( loading ) {
				loading = Boolean.FALSE;
			}
			uiHandler.post( updateTask );
		}
	}

	/* class LoadingThread extends Thread {
		public void run() {
			ExpLoaded expPair = rapi.getAllExperiments(page, pageSize, action,
					query);
			// ArrayList<Experiment> new_items = rapi.getExperiments(page,
			// pageSize, action, query);

			ArrayList<Experiment> selected_items = new ArrayList<Experiment>();
			for (Experiment exp : expPair.exp) {
				ArrayList<ExperimentField> exp_fields = rapi
						.getExperimentFields(exp.experiment_id);
				int fieldNum = 1;
				boolean pass = true;
				if (exp_fields.isEmpty())
					pass = false;
				for (ExperimentField field : exp_fields) {

					switch (fieldNum) {
					// Accel-x
					case 1:
						if (field.type_id != 25)
							pass = false;
						break;
					// Accel-y
					case 2:
						if (field.type_id != 25)
							pass = false;
						break;
					// Accel-z
					case 3:
						if (field.type_id != 25)
							pass = false;
						break;
					// Accel-Total
					case 4:
						if (field.type_id != 25)
							pass = false;
						break;
					// Latitude
					case 5:
						if (field.type_id != 19)
							pass = false;
						break;
					// Longitude
					case 6:
						if (field.type_id != 19)
							pass = false;
						break;
					// Heading
					case 7:
						if (field.type_id != 10)
							pass = false;
						break;
					// Mag-x
					case 8:
						if (field.type_id != 21 && field.type_id != 22)
							pass = false;
						break;
					// Mag-y
					case 9:
						if (field.type_id != 21 && field.type_id != 22)
							pass = false;
						break;
					// Mag-z
					case 10:
						if (field.type_id != 21 && field.type_id != 22)
							pass = false;
						break;
					// Time
					case 11:
						if (field.type_id != 7)
							pass = false;
						break;

					default:
						break;
					}
					fieldNum++;

					if (!pass)
						break;
				}
				if (pass)
					selected_items.add(exp);
			}

			if (expPair.getLoaded(expPair)) {
				allItemsLoaded = true;
			} else {
				allItemsLoaded = false;
				synchronized (items) {
					items.addAll(selected_items);
				}
				itemsLoaded += selected_items.size();
			}

			synchronized (loading) {
				loading = Boolean.FALSE;
			}
			uiHandler.post(updateTask);
		}
	} */

	class UIUpdateTask implements Runnable {
		public void run() {
			notifyDataSetChanged();
		}
	}

}