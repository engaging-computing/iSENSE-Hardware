package edu.uml.cs.isense.objects;

import java.util.ArrayList;

/**
 * Used to get user specific information including their experiments, sessions,
 * and images.
 * 
 * @author iSENSE Android-Development Team
 */
public class Item {
	public ArrayList<Experiment> e = new ArrayList<Experiment>();
	public ArrayList<Session> s = new ArrayList<Session>();
	public ArrayList<Image> i = new ArrayList<Image>();
}