package uc3mprojects.pablo.ex1aliamate;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewSurveyActivity extends AppCompatActivity {


    final String TAG = "States_lifeCycle";

    // ========================================================================================================================================
    // LIFE-CYCLE METHODS
    // ========================================================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    //Add dynamically elements to the scroll view (all surveys stored). The number of elements to be added is the number of surveys stored => read .txt file

        // Reads the number of surveys stored
        int surveysNumber = getNumberOfSurveys();

        // Get txt content
        List<String> txtContent = readEX1_2016_USERS();

        //Add a preview of all surveys to the scroll view and show the min layout
        showSurveysPreview (surveysNumber, txtContent);

    } // end onCreate


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "MainActivity: onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity: onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity: onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity: onDestroy()");
    }

    // ========================================================================================================================================
    // OVERRIDE METHODS
    // ========================================================================================================================================


    // ========================================================================================================================================
    // NON-OVERRIDE METHODS
    // ========================================================================================================================================

    /**
     * To recover the information contented into DB string and create an associated SurveyInformation object
     */

    private int getNumberOfSurveys () {

        int numberOfSurveys;
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File survey_info = new File (directory,"EX1_2016_USERS.txt");

        int number_surveys = -1;

        //Reads the number of surveys stored

        if (survey_info.exists()){  // if file exists => there are surveys stored

            FileReader survey_info_reader = null;

            try {
                survey_info_reader = new FileReader(survey_info);
                BufferedReader b = new BufferedReader(survey_info_reader);
                // number of surveys
                String last_user_index = null;              // user index is stored in the first line. readline increments linepointer
                last_user_index = b.readLine();
                number_surveys = Integer.parseInt(last_user_index, 10);
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else  // if file does not exist
        {
            Toast.makeText(this, "There are no surveys stored.", Toast.LENGTH_LONG).show();
            finish();
        }

        return  number_surveys;
    } // end getNumberOfSurveys

    /**
     *      To store all lines contented into EX1_2016_USERS.txt into a list
     */

    List<String> readEX1_2016_USERS () {

        List<String> lines = new ArrayList<String>();                // List which stores all document lines
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File survey_info = new File (directory,"EX1_2016_USERS.txt");

        if (survey_info.exists()){  // if file exists => there are surveys stored

            FileReader survey_info_reader = null;

            try {
                survey_info_reader = new FileReader(survey_info);
                BufferedReader b = new BufferedReader(survey_info_reader);
                b.readLine();       // to skip txt first line
                // Store content of EX1_2016_USERS.txt
                String aux2;
                while((aux2 = b.readLine())!=null) {
                    lines.add(aux2);
                }
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else  // if file does not exist
        {
            Toast.makeText(this, "There are no surveys stored.", Toast.LENGTH_LONG).show();
            finish();
        }

        return lines;
    } // end readEX1_2016_USERS

    /**
     * To recover the information contented into DB string and create an associated SurveyInformation object
     */

    private SurveyInformation recoverSurveyInformation (String surveyInfoDBFormat) {

        SurveyInformation surveyObject = new SurveyInformation();  // To recover all the fields

        //1- Split string by "," (all the fields are separated one each other by this symbol)
        String[] tokens = surveyInfoDBFormat.split(",");  // Now the info is stored as follows: tokens[0] = {"Q11":"1", tokens[1] = "Q21":"1", ... , tokens[n] = "Q11":"1", "Location":"40.3438 - 744.0000"}
        tokens[0] = tokens [0].substring(1);              // to get the string from position 1 => delete first } =>  // Now the info is stored as follows: tokens[0] = "Q11":"1", ...
        tokens[tokens.length -1] = tokens [tokens.length -1].substring(0,tokens [tokens.length -1].length() -1);      // to delete the last }

        /*
        for (String t : tokens)
            System.out.println(t);      // Test*/

        //2- Create a map which stores clue-value elements (like python dictionary)

        Map<String, String> surveyInformation = new HashMap<String, String>();
        String clue;
        String value;
        String [] aux;

        for (String t : tokens) {

            aux = t.split("\"");        // Now the info is stored as follows: aux[0] = " , aux[1] = Q11, aux[2] = :, aux[3] = 1 ... => aux[1] = clue, aux[3] = value
            surveyInformation.put(aux[1], aux[3]);               // put (clue, value)
            System.out.println(aux[1]);  // TEST: print the value associated to the clue
            System.out.println(surveyInformation.get(aux[1]));  // TEST: print the value associated to the clue
        }

        // Fill surveyObject with the values

        // HEADER INFO

        // Date and starting time
        String string_aux;
        String [] array_aux;
        array_aux = surveyInformation.get("Starting Time").split(" ");  // Date is stored inside Starting Time field as follows: 15/10/2016 08:19:56
        surveyObject.setDate(array_aux[0]);
        surveyObject.setStartingTime(array_aux[3]);
        // Tasting Time
        surveyObject.setStartingTime(surveyInformation.get("Tasting Time"));
        // Location
        surveyObject.setStartingTime(surveyInformation.get("Location"));
        // Image
        surveyObject.setImage(surveyInformation.get("Photo"));
        // Location
        surveyObject.setStartingTime(surveyInformation.get("Location"));

        // SURVEY INFO

        surveyObject.setAnswer(0,surveyInformation.get("Q11").charAt(0)); // all the answer index has 1 character => charAt is valid to "cast" (is not a real cast) string to char
        surveyObject.setAnswer(0,surveyInformation.get("Q21").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q22").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q23").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q24").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q25").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q31").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q32").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q33").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q41").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q42").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q43").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q44").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q45").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q46").charAt(0));
        surveyObject.setAnswer(0,surveyInformation.get("Q47").charAt(0));

        return  surveyObject;
    }

    /**
     * To add a preview to layout scroll view and show the main layout
     */

    private int showSurveysPreview (int numberOfSurveys, List<String> txtContent) {

        LayoutInflater inflater;
        inflater = (LayoutInflater)  this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View mainLayout = inflater.inflate(R.layout.activity_view_survey, null);
        ScrollView sv = (ScrollView) mainLayout.findViewById(R.id.surveys_scroll_view);

        //2- Create a LinearLayout element to be added => scroll view only one direct child
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        // Elements to be added to linear layout

        RelativeLayout rl = new RelativeLayout(this);
        TextView tv;
        Button btn;
        int ID_btn;
        int ID_txt;

        // Relative layout paramas for the objects that are going to be added

        RelativeLayout.LayoutParams params_btn = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams params_tv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < numberOfSurveys ; i ++) {

            // This is the way to set layout params of each view as in xml from java
            params_btn = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_btn.setMargins(5,15,5,15);
            params_tv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            params_tv.setMargins(15,15,5,15);       // int left, int top, int right, int bottom

            rl = new RelativeLayout(this);
            tv = new TextView(this);
            btn = new Button(this);

            // Configure elements
            btn.setId(i+1);  // ID must be > 0
            tv.setId(i+1+2);
            ID_btn = btn.getId();
            ID_txt = tv.getId();
            tv.setText(txtContent.get(i*2+1));  // in text file: user name + data + user name + data .... => i*2 +1 =  DB format survey info, i*2 = user identifier
            btn.setText(txtContent.get(i*2));
            recoverSurveyInformation (txtContent.get(i*2 +1));
            // Add text and button to relative layout
            rl.addView(tv);
            rl.addView(btn);
            // Load relative layout params of each element
            btn = (Button) rl.findViewById(ID_btn);
            btn.setLayoutParams(params_btn);
            tv = (TextView) rl.findViewById(ID_txt);
            params_tv.addRule(RelativeLayout.RIGHT_OF,ID_btn);
            params_tv.addRule(RelativeLayout.CENTER_VERTICAL,ID_btn);
            tv.setLayoutParams(params_tv);

            // Add relative layout to linear layout (linear layout can have several direct childs)
            ll.addView(rl);
        }

        //3- Add the LinearLayout element to the ScrollView (only this direct child from the point of view of scroll view)
        sv.addView(ll);

        //4- Display the main view
        setContentView(mainLayout);

        return  1;
    }



} // end class

