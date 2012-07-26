package edu.uml.cs.isense.objects;

public class Experiment {
    public int experiment_id;
    public int owner_id;
    public String name = "";
    public String description = "";
    public String timecreated = "";
    public String timemodified = "";
    public int default_read;
    public int default_join;
    public int featured;
    public int rating;
    public int rating_votes;
    public int hidden;
    public String timeobj = "";
    public String firstname = "";
    public String lastname = "";
    public String date_diff = "";
    public int session_count;
    public String provider_url = "";
    
    // New API fields:
    public String name_prefix = "";
    public String location = "";
    public int req_name;
    public int req_location;
    public int req_procedure;
    public int activity;
    public int recommended;
    public int srate;
    public int activity_for;
    public int closed;
    public String exp_image = "";
    // From getExperiments() only
    public String tags = "";
    public int contrib_count;
    public String rating_comp = "";
   
}
