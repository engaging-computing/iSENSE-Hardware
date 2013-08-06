package edu.uml.cs.isense.proj;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.objects.RProject;

public class BrowseProjects extends ListActivity {
	private ProjectAdapter m_adapter;
	@SuppressWarnings("unused")
	private Context mContext;
	@SuppressWarnings("unused")
	private boolean finish = false;
	private ArrayList<RProject> m_projects;

	@Override
	public Object onRetainNonConfigurationInstance() {
		final ArrayList<RProject> list = m_adapter.items;
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
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.projects);
		mContext = this;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			finish = true;
		}

		setResult(Activity.RESULT_CANCELED);

		@SuppressWarnings("deprecation")
		final Object data = getLastNonConfigurationInstance();
		final Object[] dataList = (Object[]) data;
		// The activity is starting for the first time, load the data from the
		// site
		if (data != null) {
			// The activity was destroyed/created automatically
			m_projects = (ArrayList<RProject>) dataList[0];
		} else {
			m_projects = new ArrayList<RProject>();
		}

		this.m_adapter = new ProjectAdapter(getBaseContext(),
				R.layout.projectrow, R.layout.loadrow, m_projects);

		if (data != null) {
			m_adapter.itemsLoaded = (Integer) dataList[1];
			m_adapter.allItemsLoaded = (Boolean) dataList[2];
			m_adapter.page = (Integer) dataList[3];
		}
		setListAdapter(this.m_adapter);

		final EditText et = (EditText) findViewById(R.id.ExperimentSearchInput);
		et.setSingleLine(true);
		et.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s == null || s.length() == 0) {
					m_projects = new ArrayList<RProject>();
					m_adapter = new ProjectAdapter(getBaseContext(),
							R.layout.projectrow, R.layout.loadrow,
							m_projects);
					setListAdapter(m_adapter);
				} else {
					m_projects = new ArrayList<RProject>();
					m_adapter = new ProjectAdapter(getBaseContext(),
							R.layout.projectrow, R.layout.loadrow,
							m_projects);
					m_adapter.action = "search";
					m_adapter.query = s.toString();
					setListAdapter(m_adapter);
				}
			}

		});

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		RProject p = m_projects.get(position);

		Intent intent = new Intent();
		intent.putExtra("edu.uml.cs.isense.pictures.experiments.exp_id",
				p.project_id);
		//intent.putExtra("edu.uml.cs.isense.pictures.experiments.srate",
		//		p.srate);

		setResult(Activity.RESULT_OK, intent);
		finish();
	}

}
