package edu.uml.cs.isense.supplements;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.uml.cs.isense.R;

public class FileAdapter extends ArrayAdapter<File> {
	public ArrayList<File> items;
	private Context mContext;
	private int resourceId;
	String[] fileFilters;

	public FileAdapter(Context context, int resourceId, ArrayList<File> items) {
		super(context, resourceId, items);
		this.items = items;
		mContext = context;
		this.resourceId = resourceId;
	}
	
	public void setFileFilters(String[] newFilters) {
		fileFilters = newFilters;
	}

	public int getCount() {
		return items.size();
	}

	public File getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(resourceId, parent, false);
		
		if (items.size() != 0) {
			File f = items.get(position);
			if (f != null) {
				TextView row = (TextView) v.findViewById(R.id.filerow);
				if(fileFilters != null && f.isFile()) {
					String extension = f.getName().substring(f.getName().lastIndexOf('.') + 1).toLowerCase(Locale.US);
					boolean matches = false;
					for(String s : fileFilters) {
						s = s.toLowerCase(Locale.US);
						if(s.equals(extension)) matches = true;
					}
					if(matches == false) {
						v.setEnabled(false);
						v.setBackgroundColor(Color.LTGRAY);
						row.setTextColor(Color.WHITE);
					}
				}
				if (row != null) {
					row.setText(f.getName());
					if (f.isDirectory()) {
						Drawable iconDir = mContext.getResources().getDrawable(R.drawable.icon_folder);
						row.setCompoundDrawablesWithIntrinsicBounds(iconDir, null, null, null);
					} else {
						Drawable iconDir = mContext.getResources().getDrawable(R.drawable.icon_file);
						row.setCompoundDrawablesWithIntrinsicBounds(iconDir, null, null, null);
					}
				}
			}
		}
		return v;
	}

}