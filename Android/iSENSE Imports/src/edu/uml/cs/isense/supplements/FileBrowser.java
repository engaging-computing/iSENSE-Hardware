package edu.uml.cs.isense.supplements;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import edu.uml.cs.isense.R;

public class FileBrowser extends Activity implements OnClickListener {

	Button bread1, bread2, bread3;
	File parent1, parent2;
	ListView fileList;
	Button OKBtn, cancelBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_filebrowser);

		bread1 = (Button) findViewById(R.id.btn_bread1);
		bread2 = (Button) findViewById(R.id.btn_bread2);
		bread3 = (Button) findViewById(R.id.btn_bread3);
		fileList = (ListView) findViewById(R.id.list_files);
		OKBtn = (Button) findViewById(R.id.btn_ok);
		cancelBtn = (Button) findViewById(R.id.btn_cancel);

		OKBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		bread1.setOnClickListener(this);
		bread2.setOnClickListener(this);
		bread3.setOnClickListener(this);

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
			fileList.setAdapter(adapter);
			//get references to the directories up the tree
			parent1 = newDir.getParentFile();
			if(parent1 != null)	{
				parent2 = parent1.getParentFile();
			} else {
				parent2 = null;
			}
			//Build the breadcrumbs
			if(parent2 != null) {
				bread3.setText(" "+newDir.getName());
				bread3.setVisibility(View.VISIBLE);
				bread2.setText(" "+parent1.getName()+" >");
				bread2.setVisibility(View.VISIBLE);
				bread1.setText(parent2.getName()+" >");
			} else if(parent1 != null) {
				bread3.setVisibility(View.GONE);
				bread2.setText(" "+newDir.getName());
				bread2.setVisibility(View.VISIBLE);
				bread1.setText(" "+parent1.getName()+" >");
			} else {
				bread3.setVisibility(View.GONE);
				bread2.setVisibility(View.GONE);
				bread1.setText(" "+newDir.getName());
			}
			
			fileList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> adapter, View v, int pos, long l) {
					File f = currDir.get(pos);
					if(f.isDirectory()) {
						changeDir(f);
					}
				}
			});
		}
	}

	public void onClick(View v) {
		if( v == bread1 && parent2 != null) {
			changeDir(parent2);
		} else if( v == bread2 && parent2 != null && parent1 != null ) {
			changeDir(parent1);
		} else if( v == bread1 && parent2 == null && parent1 != null ) {
			changeDir(parent1);
		}
	}

}
