package edu.uml.cs.isense.proj;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
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
	private ArrayList<RProject> m_projects;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.projects);
		
		setResult(Activity.RESULT_CANCELED);

		m_projects = new ArrayList<RProject>();
		m_adapter = new ProjectAdapter(getBaseContext(),
				R.layout.projectrow, R.layout.loadrow,
				m_projects);
		m_adapter.query = "";
		setListAdapter(m_adapter);

		final EditText et = (EditText) findViewById(R.id.ExperimentSearchInput);
		et.setSingleLine(true);
		et.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

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
		intent.putExtra("project_id", p.project_id);

		setResult(Activity.RESULT_OK, intent);
		finish();
	}

}
