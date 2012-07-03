package edu.uml.cs.isense.objects;

import java.util.ArrayList;

public class ExpLoaded {
	public ArrayList<Experiment> exp;
	boolean loaded;
	
	public ExpLoaded(ArrayList<Experiment> exp, boolean loaded) {
		this.loaded = loaded;
		this.exp    = exp;
	}
	
	public ExpLoaded() {
	}
	
	public ArrayList<Experiment> getExperiments(ExpLoaded pair) {
		return pair.exp;
	}
	
	public boolean getLoaded(ExpLoaded pair) {
		return pair.loaded;
	}
	
	public void setExperiments(ArrayList<Experiment> exp) {
		this.exp = exp;
	}
	
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}	
	
}