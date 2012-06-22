package edu.uml.cs.pincomm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ViewData extends Activity implements OnClickListener {
	Button yes, no;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_page);
		
		yes = (Button) findViewById(R.id.yes);
		no =  (Button) findViewById(R.id.no);
		
		yes.setOnClickListener(this);
		no.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == yes) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(Isense.sessionUrl));
			startActivity(i);
		}
		
		finish();
	}	
}
