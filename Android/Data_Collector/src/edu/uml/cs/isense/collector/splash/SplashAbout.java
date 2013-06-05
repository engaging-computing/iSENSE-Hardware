package edu.uml.cs.isense.collector.splash;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.uml.cs.isense.collector.R;

public class SplashAbout extends Fragment {
	
	public SplashAbout() {	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.splash_about, container, false);
		return rootView;
		
	}
    
}
