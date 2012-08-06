package edu.uml.cs.isense.collector.splash;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.collector.objects.FontFitTextView;

public class SplashMain extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_main);

		final TextView appName = (FontFitTextView) findViewById(R.id.title_text);
		final Typeface tf1 = Typeface
				.createFromAsset(getAssets(), "facets.otf");
		appName.setTypeface(tf1);

		final RelativeLayout abstractCircle = (RelativeLayout) findViewById(R.id.abstract_layout);
		final Animation rotate = AnimationUtils.loadAnimation(this,
				R.anim.fastspinner);
		abstractCircle.startAnimation(rotate);

	}

}
