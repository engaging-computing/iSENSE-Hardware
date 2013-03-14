package edu.uml.cs.isense.collector.splash;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.supplements.FontFitTextView;

public class SplashMain extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_main);

		final TextView appName = (FontFitTextView) findViewById(R.id.title_text);
		final Typeface tf1 = Typeface
				.createFromAsset(getAssets(), "BorisBlackBloxx.ttf");
		appName.setTypeface(tf1);

	}

}
