package edu.uml.cs.isense.collector.splash;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.uml.cs.isense.collector.R;
import edu.uml.cs.isense.supplements.FontFitTextView;

public class SplashMain extends Fragment {
	
	public SplashMain() {	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//super.onCreateView(savedInstanceState);
		View rootView = inflater.inflate(R.layout.splash_main, container, false);
		Context c = getActivity().getApplicationContext();

		final TextView appName = (FontFitTextView) rootView.findViewById(R.id.title_text);
		final Typeface tf1 = Typeface
				.createFromAsset(c.getAssets(), "BorisBlackBloxx.ttf");
		appName.setTypeface(tf1);
		
		return rootView;
	}

}
