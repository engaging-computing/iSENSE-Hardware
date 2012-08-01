package edu.uml.cs.isense.genpics;

import java.io.File;

public class Picture {
    public File file;
    public double latitude;
    public double longitude;
    public String name;
    public long time;
   
    public Picture(File f, double lat, double lon, String n, long t) {
        file = f;
        latitude = lat;
        longitude = lon;
        name = n;
        time = t;
    }
}