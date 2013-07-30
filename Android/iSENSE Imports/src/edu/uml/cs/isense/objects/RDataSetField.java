package edu.uml.cs.isense.objects;

/**
 * Class that includes information about a Data Set Field on iSENSE.
 * 
 * @author iSENSE Android Development Team
 */
public class RDataSetField {
    public int field_id;
    public String name="";
    public int type;
    public String unit="";  
    
    public static int TYPE_TIME = 1;
    public static int TYPE_NUM = 2;
    public static int TYPE_TEXT = 3;
    public static int TYPE_LAT = 4;
    public static int TYPE_LON = 5;
}
