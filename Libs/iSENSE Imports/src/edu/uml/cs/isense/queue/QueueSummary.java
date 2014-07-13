package edu.uml.cs.isense.queue;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.uml.cs.isense.R;

public class QueueSummary extends Activity {
	
	public static final String SUMMARY_ARRAY = "summary_array";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.queue_summary);
		
		String[] sa = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			sa = extras.getStringArray(SUMMARY_ARRAY);
		}
		
		StringBuilder sb = new StringBuilder();
		String text = "";
		if (sa != null) {
			for (String s : sa) {
				sb.append(s).append("<br/>");
			}
		}
		text = sb.toString();
		
		final TextView message = (TextView) findViewById(R.id.queue_summary_text);
		message.setText(Html.fromHtml(text));
		
		final Button ok = (Button) findViewById(R.id.queue_summary_ok);
		ok.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

	}
	
}