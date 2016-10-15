package uc3mprojects.pablo.ex1aliamate;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
        surveyObject.setStartingTime(array_aux[1]);
        // Tasting Time
        surveyObject.setTastingTime(surveyInformation.get("Tasting Time"));
        // Location
        surveyObject.setLocation(surveyInformation.get("Location"));
        // Image
        surveyObject.setImage(surveyInformation.get("Photo"));


        // SURVEY INFO

        surveyObject.setAnswer(0,surveyInformation.get("Q11").charAt(0)); // all the answer index has 1 character => charAt is valid to "cast" (is not a real cast) string to char
        surveyObject.setAnswer(1,surveyInformation.get("Q21").charAt(0));
        surveyObject.setAnswer(2,surveyInformation.get("Q22").charAt(0));
        surveyObject.setAnswer(3,surveyInformation.get("Q23").charAt(0));
        surveyObject.setAnswer(4,surveyInformation.get("Q24").charAt(0));
        surveyObject.setAnswer(5,surveyInformation.get("Q25").charAt(0));
        surveyObject.setAnswer(6,surveyInformation.get("Q31").charAt(0));
        surveyObject.setAnswer(7,surveyInformation.get("Q32").charAt(0));
        surveyObject.setAnswer(8,surveyInformation.get("Q33").charAt(0));
        surveyObject.setAnswer(9,surveyInformation.get("Q41").charAt(0));
        surveyObject.setAnswer(10,surveyInformation.get("Q42").charAt(0));
        surveyObject.setAnswer(11,surveyInformation.get("Q43").charAt(0));
        surveyObject.setAnswer(12,surveyInformation.get("Q44").charAt(0));
        surveyObject.setAnswer(13,surveyInformation.get("Q45").charAt(0));
        surveyObject.setAnswer(14,surveyInformation.get("Q46").charAt(0));
        surveyObject.setAnswer(15,surveyInformation.get("Q47").charAt(0));

        return  surveyObject;
    }

    /**
     * To add a preview to layout scroll view and show the main layout
     */

    private int showSurveysPreview (int numberOfSurveys, List<String> txtContent) {

        // LAYOUTS

        // Main activity layout
        LayoutInflater inflater;
        inflater = (LayoutInflater)  this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainLayout = inflater.inflate(R.layout.activity_view_survey, null);
        // Scroll view inside main layout to show the stored surveys
        ScrollView sv = (ScrollView) mainLayout.findViewById(R.id.surveys_scroll_view);
        //LinearLayout element to be added => scroll view only one direct child. Each survey info will be added in this layout
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params_ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params_ll.setMargins(0, 20, 0, 0); // To specify the outside margins among each survey // int left, int top, int right, int bottom
        // Relative layout to show the information of each survey. One relative layout will be added to linear layout per survey
        RelativeLayout rl = new RelativeLayout(this);

        // VIEWS

        // Survey info elements to be added to linear layout

        TextView tv;
        ImageButton btn;
        int ID_btn;
        int ID_txt;

        TextView tv_date;
        TextView tv_startingTime;
        TextView tv_tastingTime;
        TextView tv_location;
        TextView tv_image;
        TextView tv_date_title;
        TextView tv_startingTime_title;
        TextView tv_tastingTime_title;
        TextView tv_location_title;
        TextView tv_image_title;

        int ID_tv_date;
        int ID_tv_startingTime;
        int ID_tv_tastingTime;
        int ID_tv_location;
        int ID_tv_image;
        int ID_tv_date_title;
        int ID_tv_startingTime_title;
        int ID_tv_tastingTime_title;
        int ID_tv_location_title;
        int ID_tv_image_title;

        // Relative layout params for the objects that are going to be added

        RelativeLayout.LayoutParams params_tv_date ;
        RelativeLayout.LayoutParams params_tv_startingTime ;
        RelativeLayout.LayoutParams params_tv_tastingTime ;
        RelativeLayout.LayoutParams params_tv_location ;
        RelativeLayout.LayoutParams params_tv_image ;
        RelativeLayout.LayoutParams params_tv_date_title ;
        RelativeLayout.LayoutParams params_tv_startingTime_title ;
        RelativeLayout.LayoutParams params_tv_tastingTime_title ;
        RelativeLayout.LayoutParams params_tv_location_title ;
        RelativeLayout.LayoutParams params_tv_image_title ;

        RelativeLayout.LayoutParams params_btn ;
        RelativeLayout.LayoutParams params_tv ;

        // LOOP FOR ADDING VIEWS DYNAMICALLY

        // SurveyInformation object for recovering txt info of each survey and display its info
        SurveyInformation currentSurvey = new SurveyInformation();

        // Loop to add all surveys information
        for (int i = 0; i < numberOfSurveys ; i ++) {

            //1- Reinitialization

            currentSurvey = new SurveyInformation();

            rl = new RelativeLayout(this); // relative layout contains survey info => for each survey, one relative layout

            tv = new TextView(this);
            btn = new ImageButton(this);
            tv_date= new TextView(this);
            tv_startingTime= new TextView(this);
            tv_tastingTime= new TextView(this);
            tv_location= new TextView(this);
            tv_image= new TextView(this);
            tv_date_title= new TextView(this);
            tv_startingTime_title= new TextView(this);
            tv_tastingTime_title= new TextView(this);
            tv_location_title= new TextView(this);
            tv_image_title= new TextView(this);

            //2- Setting new IDs for each view

            btn.setId(i+1);  // ID must be > 0
            tv.setId(i+2);
            tv_date.setId(i+3);
            tv_startingTime.setId(i+4);
            tv_tastingTime.setId(i+5);
            tv_location.setId(i+6);
            tv_image.setId(i+7);
            tv_date_title.setId(i+8);
            tv_startingTime_title.setId(i+9);
            tv_tastingTime_title.setId(i+10);
            tv_location_title.setId(i+11);
            tv_image_title.setId(i+12);

            ID_btn = btn.getId();
            ID_txt = tv.getId();
            ID_tv_date = tv_date.getId();
            ID_tv_startingTime = tv_startingTime .getId();
            ID_tv_tastingTime = tv_tastingTime.getId();
            ID_tv_location = tv_location.getId();
            ID_tv_image = tv_image.getId();
            ID_tv_date_title = tv_date_title.getId();
            ID_tv_startingTime_title = tv_startingTime_title.getId();
            ID_tv_tastingTime_title = tv_tastingTime_title.getId();
            ID_tv_location_title = tv_location_title.getId();
            ID_tv_image_title = tv_image_title.getId();

            //3- Layout parameters to allocate views inside relative layout

            // This is the way to set layout params of each view as in xml from java
            params_btn = new RelativeLayout.LayoutParams(200,200);
            params_btn.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params_btn.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params_btn.setMargins(0,20,20,0);
            params_tv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            params_tv.setMargins(15,15,5,15);       // int left, int top, int right, int bottom

            // TITLES

            params_tv_date_title = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_date_title.setMargins(30,30,5,0);  // int left, int top, int right, int bottom

            params_tv_startingTime_title = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_startingTime_title.addRule(RelativeLayout.BELOW,ID_tv_date_title);
            params_tv_startingTime_title.addRule(RelativeLayout.ALIGN_LEFT,ID_tv_date_title);

            params_tv_tastingTime_title = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_tastingTime_title.addRule(RelativeLayout.BELOW,ID_tv_startingTime_title);
            params_tv_tastingTime_title.addRule(RelativeLayout.ALIGN_LEFT,ID_tv_startingTime_title);

            params_tv_location_title = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_location_title.addRule(RelativeLayout.BELOW,ID_tv_tastingTime_title);
            params_tv_location_title.addRule(RelativeLayout.ALIGN_LEFT,ID_tv_tastingTime_title);

            params_tv_image_title = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_image_title.addRule(RelativeLayout.BELOW,ID_tv_location_title);
            params_tv_image_title.addRule(RelativeLayout.ALIGN_LEFT,ID_tv_location_title);
            params_tv_image_title.setMargins(0,0,0,30);  // int left, int top, int right, int bottom

            // VALUES

            params_tv_date = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_date.addRule(RelativeLayout.RIGHT_OF,ID_tv_date_title);
            params_tv_date.addRule(RelativeLayout.ALIGN_TOP,ID_tv_date_title);

            params_tv_startingTime = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_startingTime.addRule(RelativeLayout.RIGHT_OF,ID_tv_startingTime_title);
            params_tv_startingTime.addRule(RelativeLayout.BELOW,ID_tv_date);

            params_tv_tastingTime = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_tastingTime.addRule(RelativeLayout.RIGHT_OF,ID_tv_tastingTime_title);
            params_tv_tastingTime.addRule(RelativeLayout.BELOW,ID_tv_startingTime);

            params_tv_location = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_location.addRule(RelativeLayout.RIGHT_OF,ID_tv_location_title);
            params_tv_location.addRule(RelativeLayout.BELOW,ID_tv_tastingTime);

            params_tv_image = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params_tv_image.addRule(RelativeLayout.RIGHT_OF,ID_tv_image_title);
            params_tv_image.addRule(RelativeLayout.BELOW,ID_tv_location);


            //4- Updating views following the information stored inside txt file

            // Loading resource eye (imageButton)
            btn.setImageResource(R.drawable.eye);
            btn.setAdjustViewBounds(true);
            btn.setScaleType(ImageButton.ScaleType.FIT_CENTER);


            // Titles
            tv_date_title.setText("Date:");
            tv_image_title.setText("Image:");
            tv_startingTime_title.setText("Starting Time:");
            tv_tastingTime_title.setText ("Tasting Time:");
            tv_location_title.setText("Location:");

            // Create an object with the information
            currentSurvey = recoverSurveyInformation (txtContent.get(i*2 +1));
            // update views
            tv_date.setText(currentSurvey.getDate());
            tv_image.setText(currentSurvey.getImage());
            tv_startingTime.setText(currentSurvey.getStartingTime());
            tv_tastingTime.setText(currentSurvey.getTastingTime());
            tv_location.setText(currentSurvey.getLocation());

            // Add views to relative layout

            rl.addView(btn, params_btn);
            // Connecting activity to the button
            btn = ((ImageButton) rl.findViewById(ID_btn));
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), "ACTIVITY TO EDIT SURVEYS CREATED", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ViewSurveyActivity.this, EditSurveyActivity.class);  // To bind the ViewSurveyActivity activity with EditSurveyActivity activity
                    startActivity(intent);
                }
            });
            //5- Loading views into relative layout

            rl.addView(tv_date_title,params_tv_date_title);
            rl.addView(tv_startingTime_title,params_tv_startingTime_title);
            rl.addView(tv_tastingTime_title,params_tv_tastingTime_title);
            rl.addView(tv_location_title,params_tv_location_title);
            rl.addView(tv_image_title,params_tv_image_title);

            rl.addView(tv_date,params_tv_date);
            rl.addView(tv_startingTime,params_tv_startingTime);
            rl.addView(tv_tastingTime,params_tv_tastingTime);
            rl.addView(tv_location,params_tv_location);
            rl.addView(tv_image,params_tv_image);

            /*
            btn = (Button) rl.findViewById(ID_btn);
            btn.setLayoutParams(params_btn);
            tv = (TextView) rl.findViewById(ID_txt);
            params_tv.addRule(RelativeLayout.RIGHT_OF,ID_btn);
            params_tv.addRule(RelativeLayout.CENTER_VERTICAL,ID_btn);
            tv.setLayoutParams(params_tv);*/

            rl.setBackgroundColor(Color.rgb(255,178,102));  // red, green, blue
            // Add relative layout to linear layout (linear layout can have several direct childs)
            ll.addView(rl,params_ll);
        }

        //3- Add the LinearLayout element to the ScrollView (only this direct child from the point of view of scroll view)
        sv.addView(ll);

        //4- Display the main view
        setContentView(mainLayout);

        return  1;
    }



} // end class

