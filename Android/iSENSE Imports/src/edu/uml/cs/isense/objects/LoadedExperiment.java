package edu.uml.cs.isense.objects;

import java.util.ArrayList;

/**
 * An iSENSE experiment pair that allows us to determine if there are more
 * experiments to be loaded.
 * 
 * @author iSENSE Android-Development Team
 */
public class LoadedExperiment {
	public ArrayList<Experiment> exp;
	boolean loaded;

	public LoadedExperiment(ArrayList<Experiment> exp, boolean loaded) {
		this.loaded = loaded;
		this.exp = exp;
	}

	public LoadedExperiment() {
	}

	public ArrayList<Experiment> getExperiments(LoadedExperiment pair) {
		return pair.exp;
	}

	public boolean getLoaded(LoadedExperiment pair) {
		return pair.loaded;
	}

	public void setExperiments(ArrayList<Experiment> exp) {
		this.exp = exp;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

}