package edu.uml.cs.isense.proj;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.objects.RProject;

/**
 * An adapter used to load and query projects on the iSENSE website.
 * 
 * This class is utilized by 
 * {@link edu.uml.cs.isense.proj.BrowseProjects BrowseProjects} and
 * requires no further implementation.
 * 
 * @author iSENSE Android Development Team
 */
class ProjectAdapter extends ArrayAdapter<RProject> {
	private ArrayList<RProject> items;
	private Context mContext;
	private int resourceID;
	private int loadingRow;
	private int itemsLoaded;
	private boolean allItemsLoaded;
	private Boolean loading;
	private UIUpdateTask updateTask;
	private Handler uiHandler = new Handler();
	private API api;
	private final int PAGE_SIZE = 10;
	private int page = 0;
	protected String query = "";

	protected ProjectAdapter(Context context, int textViewResourceId,
			int loadingRow, ArrayList<RProject> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		mContext = context;
		resourceID = textViewResourceId;
		this.loadingRow = loadingRow;
		itemsLoaded = 0;
		allItemsLoaded = false;
		loading = false;
		updateTask = new UIUpdateTask();
		api = API.getInstance(context);
	}

	/**
	 * Returns the amount of projects loaded by the adapter plus one
	 * if not all projects are yet loaded from iSENSE.
	 */
	public int getCount() {
		int count = itemsLoaded;
		if (!allItemsLoaded)
			++count;
		return count;
	}

	/**
	 * Returns the project at the index of the position parameter.
	 * 
	 * @param position
	 * 		- The index of the project to return
	 */
	public RProject getItem(int position) {
		RProject result;
		synchronized (items) {
			result = items.get(position);
		}
		return result;
	}

	/**
	 * Returns the project block view at the given position.
	 * 
	 * @param position
	 * 		- The index of the view to be returned
	 * @param convertView
	 * 		- Returned back if the view requested at the position parameter
	 * 		is not a project block in the parent parameter.
	 * @param parent
	 * 		- The parent to obtain the view from.
	 */
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
			RProject p = items.get(position);
			if (p != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(p.name);
				}
				if (bt != null) {
					if (p.owner_name.equals("")) {
						bt.setVisibility(View.GONE);
					} else {
						bt.setText("Created By: " + p.owner_name);
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

	protected class LoadingThread extends Thread {
		public void run() {
			ArrayList<RProject> new_items = api.getProjects(page, PAGE_SIZE, true, query);

			if (new_items.size() == 0) {
				allItemsLoaded = true;
			} else {
				synchronized(items) {
					items.addAll(new_items);
				}
				itemsLoaded += new_items.size();
			}
			
			synchronized(loading) {
				loading = Boolean.FALSE;
			}
			uiHandler.post( updateTask );
		}
	}

	protected class UIUpdateTask implements Runnable {
		public void run() {
			notifyDataSetChanged();
		}
	}

}