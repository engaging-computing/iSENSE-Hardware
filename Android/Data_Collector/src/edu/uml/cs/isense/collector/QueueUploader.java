package edu.uml.cs.isense.collector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.objects.DataSet;

public class QueueUploader extends Activity implements OnClickListener {

	private Context mContext;
	private LinearLayout scrollQueue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.queueprompt);
		super.onCreate(savedInstanceState);

		mContext = this;

		Button upload = (Button) findViewById(R.id.upload);
		upload.setOnClickListener(this);

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

		scrollQueue = (LinearLayout) findViewById(R.id.scrollqueue);
		fillScrollQueue(scrollQueue);
	}

	// Works through list of data to be uploaded and creates the list of blocks
	private void fillScrollQueue(LinearLayout scrollQueue) {
		String previous = "";

		for (final DataSet ds : DataCollector.uploadQueue) {
			switch (ds.type) {
			case DATA:
				View data = View.inflate(mContext, R.layout.queueblock_data,
						null);

				makeBlock(data, ds);
				previous = checkPrevious(previous, scrollQueue,
						(String) ds.getName());

				scrollQueue.addView(data);
				ds.setUploadable(true);

				data.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						CheckedTextView ctv = (CheckedTextView) v
								.findViewById(R.id.name);
						ctv.toggle();

						if (ctv.isChecked())
							ctv.setCheckMarkDrawable(R.drawable.bluechecksmall);
						else
							ctv.setCheckMarkDrawable(R.drawable.red_x);

						ds.setUploadable(ctv.isChecked());

					}

				});
				break;

			case PIC:
				View pic = View
						.inflate(mContext, R.layout.queueblock_pic, null);

				makeBlock(pic, ds);
				previous = checkPrevious(previous, scrollQueue,
						(String) ds.getName());

				scrollQueue.addView(pic);
				pic.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						CheckedTextView ctv = (CheckedTextView) v
								.findViewById(R.id.name);
						ctv.toggle();

						if (ctv.isChecked())
							ctv.setCheckMarkDrawable(R.drawable.bluechecksmall);
						else
							ctv.setCheckMarkDrawable(R.drawable.red_x);

						ds.setUploadable(ctv.isChecked());

					}

				});
				break;
			}

		}

	}

	// Adds empty space after experiment groups
	private String checkPrevious(String previous, LinearLayout scrollQueue,
			String ds) {

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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.check_layout:

			CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.name);
			ctv.toggle();

			if (ctv.isChecked())
				ctv.setCheckMarkDrawable(R.drawable.bluechecksmall);
			else
				ctv.setCheckMarkDrawable(R.drawable.red_x);

			break;

		case R.id.upload:
			for (DataSet ds : DataCollector.uploadQueue) {
				if (ds.isUploadable()) {
					boolean success = ds.upload();
					if (success)
						DataCollector.uploadQueue.remove(ds);
				}
			}

			setResult(RESULT_OK);
			finish();
			break;

		case R.id.cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
	}

}
