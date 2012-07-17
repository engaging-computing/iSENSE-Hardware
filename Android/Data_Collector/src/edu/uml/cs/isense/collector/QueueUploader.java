package edu.uml.cs.isense.collector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.objects.DataSet;

public class QueueUploader extends Activity {

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.queueprompt);
		super.onCreate(savedInstanceState);

		mContext = this;

		LinearLayout scrollQueue = (LinearLayout) findViewById(R.id.scrollqueue);
		fillScrollQueue(scrollQueue);
	}

	// Works through list of data to be uploaded and creates the list of blocks
	private void fillScrollQueue(LinearLayout scrollQueue) {
		String previous = "";

		for (DataSet ds : DataCollector.uploadQueue) {
			switch (ds.type) {
			case DATA:
				View data = View.inflate(mContext, R.layout.queueblock_data,
						null);

				makeBlock(data, ds);
				previous = checkPrevious(previous, scrollQueue, (String) ds.getName());
				scrollQueue.addView(data);

				break;

			case PIC:
				View pic = View
						.inflate(mContext, R.layout.queueblock_pic, null);

				makeBlock(pic, ds);				
				previous = checkPrevious(previous, scrollQueue, (String) ds.getName());
				scrollQueue.addView(pic);

				break;
			}

			
		}

	}

	// Adds empty space after experiment groups
	private String checkPrevious(String previous, LinearLayout scrollQueue, String ds) {
		LinearLayout space = new LinearLayout(mContext);
		space.setPadding(0, 10, 0, 10);
		
		if ((!previous.equals(ds)) && (!previous.equals("")))
			scrollQueue.addView(space);
		return ds;
	}

	// Fills the text fields in the list element blocks
	private void makeBlock(View view, DataSet ds) {
		CheckedTextView ctv = (CheckedTextView) view.findViewById(R.id.name);
		ctv.setText(ds.getName() + " - " + ds.getType());

		TextView eid = (TextView) view.findViewById(R.id.experimentid);
		eid.setText(ds.getEID());

		TextView desc = (TextView) view.findViewById(R.id.description);
		desc.setText(ds.getDesc());
	}

}
