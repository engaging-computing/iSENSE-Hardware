package edu.uml.cs.isense.collector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
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

	private void fillScrollQueue(LinearLayout scrollQueue) {
		for (DataSet ds : DataCollector.uploadQueue) {
			switch (ds.type) {
			case DATA:
				View data = View.inflate(mContext, R.layout.queueblock_data,
						scrollQueue);
				
				TextView eid = (TextView) data.findViewById(R.id.experimentid);
				eid.setText(ds.getEID());
				
				TextView desc = (TextView) data.findViewById(R.id.description);
				desc.setText(ds.getDesc());
				
				scrollQueue.addView(data);
				
				break;
			case PIC:
				View pic = View.inflate(mContext, R.layout.queueblock_pic,
						scrollQueue);
				break;
			}

		}
	}

}
