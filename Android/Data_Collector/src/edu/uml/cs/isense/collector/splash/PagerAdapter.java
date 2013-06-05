package edu.uml.cs.isense.collector.splash;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class PagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragments;
	private List<String> titles;
	
	public PagerAdapter(FragmentManager fm) {
		super(fm);
		fragments = new ArrayList<Fragment>();
		titles = new ArrayList<String>();
	}
	
	public void addFragment(Fragment mFrag, String mTitle) {
		fragments.add(mFrag);
		titles.add(mTitle);
	}

	@Override
	public Fragment getItem(int which) {
		return fragments.get(which);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}
	
	@Override
	public String getPageTitle(int which) {
		return titles.get(which);
	}

}
