package edu.uml.cs.isense.collector.splash;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.uml.cs.isense.collector.R;

public class SplashGuide extends Fragment {
	
	public SplashGuide() {	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.splash_guide, container, false);
		return rootView;
		
	}
    
}