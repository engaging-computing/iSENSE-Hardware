package edu.uml.cs.isense.supplements;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import edu.uml.cs.isense.R;

/**
 * A dialog activity that can be launched from any other activity to
 * present the user with an interface to pick files from the device's 
 * storage. It can be passed an array of case-insensitive strings 
 * representing allowed file extensions as an intent extra.
 * 
 * @author Nick Ver Voort of the iSENSE Android-Development Team
 */

public class FileBrowser extends Activity implements OnClickListener {

	ImageButton breadUp;
	File parentDir;
	ListView fileList;
	TextView breadCrumbs;
	Button cancelBtn;
	View selectedItem;
	String[] fileFilters;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_filebrowser);

		fileFilters = getIntent().getStringArrayExtra("filefilter");

		setResult(RESULT_CANCELED);

		breadUp = (ImageButton) findViewById(R.id.btn_up);
		breadCrumbs = (TextView) findViewById(R.id.txt_path);
		fileList = (ListView) findViewById(R.id.list_files);
		cancelBtn = (Button) findViewById(R.id.btn_cancel);

		cancelBtn.setOnClickListener(this);
		breadUp.setOnClickListener(this);

		File sdBase = Environment.getExternalStorageDirectory();
		changeDir(sdBase);
	}

	public void changeDir(File newDir) {
		String storageState = Environment.getExternalStorageState();
		if(!storageState.equals(Environment.MEDIA_MOUNTED)) {
			//handle error here
		} else {
			//Show the files in the list
			final ArrayList<File> currDir = new ArrayList<File>(Arrays.asList(newDir.listFiles()));
			FileAdapter adapter = new FileAdapter(this, R.layout.filerow, currDir);
			adapter.setFileFilters(fileFilters);
			fileList.setAdapter(adapter);
			//Disable the parent button if there is no parent directory
			parentDir = newDir.getParentFile();
			if(parentDir == null) {
				breadUp.setEnabled(false);
			} else {
				breadUp.setEnabled(true);
			}

			String crumbPath = newDir.getName();
			if(parentDir != null) {
				crumbPath = parentDir.getName() + " > " + crumbPath;
				File grandparentDir = parentDir.getParentFile();
				if(grandparentDir != null) {
					crumbPath = grandparentDir.getName() + " > " + crumbPath;
				}
			}
			breadCrumbs.setText(crumbPath);		

			//Handle clicking on a file
			fileList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> adapter, View v, int pos, long l) {
					if(v.isEnabled()) {
						File f = currDir.get(pos);
						if(f.isDirectory()) {
							changeDir(f);
						} else {
							Intent result = new Intent();
							result.putExtra("filepath", f.getAbsolutePath());
							setResult(RESULT_OK, result);
							finish();
						}
					}
				}
			});
		}
	}

	public void onClick(View v) {
		if( v == breadUp ) {
			changeDir(parentDir);
		} else if ( v == cancelBtn ) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

}
