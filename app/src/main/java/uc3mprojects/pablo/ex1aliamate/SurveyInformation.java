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
    /*
    private char Q11;  // 0
    private char Q21;  // 1
    private char Q22;  // 2
    private char Q23;  // 3
    private char Q24;  // 4
    private char Q25;  // 5
    private char Q31;  // 6
    private char Q32;  // 7
    private char Q33;  // 8
    private char Q41;  // 9
    private char Q42;  // 10
    private char Q43;  // 11
    private char Q44;  // 12
    private char Q45;  // 13
    private char Q46;  // 14
    private char Q47;  // 15
    */
    private char [] answers = new char [16];    // There are 16 questions

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

    // Set
    public void setSurveyStatus (int survey_status) {this.survey_status = survey_status;}
    public void setDate (String date) {this.date = date;}
    public void setStartingTime (String startingTime) {this.startingTime = startingTime;}
    public void setTastingTime (String tastingTime) {this.tastingTime = tastingTime;}
    public void setLocation (String location) {this.location = location;}
    public void setImage (String image) {this.image = image;}
    public void setAnswer (int index, char answer) {this.answers[index] = answer;}

    // Get
    public int getSurveyStatus () {return this.survey_status;}
    public String getDate () {return this.date;}
    public String getStartingTime () {return this.startingTime;}
    public String getTastingTime () {return this.tastingTime;}
    public String getLocation () {return this.location;}
    public String getImage () {return this.image;}
    public char getAnswer (int index) {return this.answers[index];}

    // General
    public String getSurveyDBFormat () {
        String SurveyDBFormat;
        SurveyDBFormat = "{"+"\"Q11\""+":"+"\""+getAnswer(0)+"\"" + "," + "\"Q21\""+":"+"\""+getAnswer(1)+"\"" + "," + "\"Q22\""+":"+"\""+getAnswer(2)+"\"" + "," + "\"Q23\""+":"+"\""+getAnswer(3)+"\"" + "," + "\"Q24\""+":"+"\""+getAnswer(4)+"\"" + "," +
                "\"Q25\""+":"+"\""+getAnswer(5)+"\"" + "," + "\"Q31\""+":"+"\""+getAnswer(6)+"\"" + "," + "\"Q32\""+":"+"\""+getAnswer(7)+"\"" + "," + "\"Q33\""+":"+"\""+getAnswer(8)+"\"" + "," + "\"Q41\""+":"+"\""+getAnswer(9)+"\"" + "," +
                "\"Q42\""+":"+"\""+getAnswer(10)+"\"" + "," + "\"Q43\""+":"+"\""+getAnswer(11)+"\"" + "," + "\"Q44\""+":"+"\""+getAnswer(12)+"\"" + "," + "\"Q45\""+":"+"\""+getAnswer(13)+"\"" + "," + "\"Q46\""+":"+"\""+getAnswer(14)+"\"" + "," +
                "\"Q47\""+":"+"\""+getAnswer(15)+"\"" + "," + "\"Photo\""+":"+"\""+getImage ()+"\""+","+"\"Starting Time\""+":"+"\""+getDate ()+" "+getStartingTime ()+"\""+","+"\"Tasting Time\""+":"+"\""+getTastingTime ()+"\""+","+"\"Location\""+":"+"\""+getLocation ()+"\""+"}";
        return SurveyDBFormat;
    }


}
