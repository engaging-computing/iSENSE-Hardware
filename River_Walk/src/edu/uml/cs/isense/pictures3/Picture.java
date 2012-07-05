package edu.uml.cs.isense.pictures3;

import java.io.File;

public class Picture {
    public File file;
    public double latitude;
    public double longitude;
    public String name;
    public String desc;
    public long time;
   
    public Picture(File f, double lat, double lon, String n, String d, long t) {
        file = f;
        latitude = lat;
        longitude = lon;
        name = n;
        desc = d;
        time = t;
    }
}