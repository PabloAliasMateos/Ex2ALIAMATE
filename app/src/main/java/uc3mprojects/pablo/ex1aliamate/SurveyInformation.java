package uc3mprojects.pablo.ex1aliamate;

/**
 * Created by Usuario on 11/10/2016.
 */

public class SurveyInformation {

    // Class variables
    private int survey_status;      // 0 => not completed | 1 => completed
    private String date;
    private String startingTime;
    private String tastingTime;
    private String location;
    private String image;
    //String answers [4][7];

    // Constructor
    public SurveyInformation () {

        survey_status = 0;
        date = "-";
        startingTime = "-";
        tastingTime = "-";
        location = "-";
        image = "-";

    }

    // Methods

    public void setSurveyStatus (int survey_status) {this.survey_status = survey_status;}
    public void setDate (String date) {this.date = date;}
    public void setStartingTime (String startingTime) {this.startingTime = startingTime;}
    public void setTastingTime (String tastingTime) {this.tastingTime = tastingTime;}
    public void setLocation (String location) {this.location = location;}
    public void setImage (String image) {this.image = image;}

    public int getSurveyStatus () {return this.survey_status;}
    public String getDate () {return this.date;}
    public String getStartingTime () {return this.startingTime;}
    public String getTastingTime () {return this.tastingTime;}
    public String getLocation () {return this.location;}
    public String getImage () {return this.image;}

}
