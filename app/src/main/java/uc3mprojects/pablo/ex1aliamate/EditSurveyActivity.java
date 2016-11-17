package uc3mprojects.pablo.ex1aliamate;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class EditSurveyActivity extends AppCompatActivity {

    public static final int SURVEY_PERMISSION_REQUEST_CODE = 2;
    final String TAG = "Debug_txt";

    // Views

    private TextView textView_date;
    private TextView textView_startingTime;
    private TextView textView_tastingTime;
    private TextView textView_location;
    private TextView textView_imageName;
    private ImageView imageView_survey_picture;
    private RadioGroup radioGroup_survey;
    private RadioButton radioButton_survey;

    private ImageButton imageButton_clock;

    private  Switch switchPosttasting;

    // General

    private String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download"; // getExternalStorageDirectory does not refer to SD card! it refers to the root of the internal storage outside the app
    private SurveyInformation currentSurvey;
    private File directory;
    private String user_ID;
    private String serverIP;
    private String surveyStringJSON;
    private String stringUrl_users_edit;
    private int user_number;

    // ========================================================================================================================================
    // LIFE-CYCLE METHODS
    // ========================================================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_survey);

        //0- To get variables passed from the father activity

        Bundle bundle = getIntent().getExtras();
        user_ID = bundle.getString("USER_ID");
        serverIP = bundle.getString("serverIP");
        System.out.println(user_ID);
        //Toast.makeText(this, serverIP, Toast.LENGTH_LONG).show();

        //1- Read data base info corresponding to the selected user ID

        List<String> lines = readEX1_2016_USERS();
        int i = 0;
        String surveyInfoDBFormat ="";

        for (String t : lines) {
            System.out.println(t);
            if (t.equals(user_ID)){
                surveyInfoDBFormat = lines.get(i+1);
                user_number = (i/2)+1;  // /2 because of the structure of the document
                // Generate survey object
                 currentSurvey = recoverSurveyInformation (surveyInfoDBFormat);
               // System.out.println(surveyInfoDBFormat);
            }
            i ++;
        }

        //2- Update activity views

        dataInitialization(currentSurvey);

        // Save button

        //6- SAVE SURVEY BUTTON
        findViewById(R.id.imageButton_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String   message = "Are you sure you want to replace the stored survey?";
                String   tittle = "REPLACE SURVEY";

                // DIALOG TO UPDATE OR NOT

                AlertDialog.Builder myBuild = new AlertDialog.Builder(v.getContext());
                myBuild.setMessage(message);
                myBuild.setTitle(tittle);
                // Positive button
                myBuild.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // IF USER PRESSES OK

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {       // checking SDK target version. Newest versions need run time permissions

                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                            {


                            // save survey locally and DB

                                saveSurvey(currentSurvey);
                            }
                            else {
                                //permission failed, request
                                String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                                // Check the current SDK target version (run-time permissions => API 23)
                                requestPermissions(permissionRequest, SURVEY_PERMISSION_REQUEST_CODE);
                            } // end request permission
                        } else {
                            saveSurvey(currentSurvey);
                        } // end check version

                    }
                });
                // Negative button
                myBuild.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // IF USER PRESSES CANCEL
                        dialog.cancel();    // android internal function to keep alive the activity
                    }
                });

                AlertDialog dialog = myBuild.create();      // creating the dialog
                dialog.show();                              // showing the dialog


            } // en onClick
        });


    } // end onCreate


    // ========================================================================================================================================
    // OVERRIDE METHODS
    // ========================================================================================================================================


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case SURVEY_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    saveSurvey(currentSurvey);
                else
                    Toast.makeText(this, "PERMISSION DENIED: Can not save the survey. ", Toast.LENGTH_LONG).show();
                return;
/*
            case LOCATION_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "PERMISSION DENIED: Can not access GPS ", Toast.LENGTH_LONG ).show();
                        return;
                    }
                // To show the last known location until the new value is gotten
                String locationProvider = LocationManager.GPS_PROVIDER;
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                showNewLocation(lastKnownLocation);
                locationManager.requestLocationUpdates("gps", 5000, 5, locationListener); // The location will be get from gps, it will be checked every 5 seconds or when user moves 5 meters
                return;
*/

        } // end switch

    }



    @Override
    public void onBackPressed() {

        // DIALOG TO EXIT OR NOT

        AlertDialog.Builder myBuild = new AlertDialog.Builder(this);
        myBuild.setMessage("Are you sure you want to close the current survey?");
        myBuild.setTitle("EXIT EDIT/VIEW SURVEY");
        // Positive button
        myBuild.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // IF USER PRESSES OK
                finish();       // android internal function to close the activity
            }
        });
        // Negative button
        myBuild.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // IF USER PRESSES CANCEL
                dialog.cancel();    // android internal function to keep alive the activity
            }
        });

        AlertDialog dialog = myBuild.create();      // creating the dialog
        dialog.show();                              // showing the dialog

        return;
    }



    // ========================================================================================================================================
    // NON-OVERRIDE METHODS
    // ========================================================================================================================================

    /**
     *  To store all lines contented into EX1_2016_USERS.txt into a list
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

    public SurveyInformation recoverSurveyInformation (String surveyInfoDBFormat) {

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
        // Agent ID
        surveyObject.setAgentID(surveyInformation.get("Q31"));


        // SURVEY INFO

        surveyObject.setAnswer(0,surveyInformation.get("Q11").charAt(0)); // all the answer index has 1 character => charAt is valid to "cast" (is not a real cast) string to char
        surveyObject.setAnswer(1,surveyInformation.get("Q21").charAt(0));
        surveyObject.setAnswer(2,surveyInformation.get("Q22").charAt(0));
        surveyObject.setAnswer(3,surveyInformation.get("Q23").charAt(0));
        surveyObject.setAnswer(4,surveyInformation.get("Q24").charAt(0));
        surveyObject.setAnswer(5,surveyInformation.get("Q25").charAt(0));
        surveyObject.setAnswer(6,'1');                                      // always is checked
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
     *  To show the image in imageView. It is necessary to provide run-time permissions and manifest permissions for android 6.0
     */

    private void showImageCaptured(String image_name) {


        String imagePath = storagePath + "/" + image_name ;
        Bitmap myImg = BitmapFactory.decodeFile(imagePath);
        // Without resize the image, when it is large, it leads into memory allocation error
        int h = dpToPx(90); // height in pixels
        int w = dpToPx(50); // width in pixels
        myImg = Bitmap.createScaledBitmap(myImg, h, w, true);
        imageView_survey_picture = (ImageView) findViewById(R.id.imageView_survey_picture);
        imageView_survey_picture.setImageBitmap(rotateImage(myImg, 90));



    }

    /**
     *  To rotate a bitmap
     */

    private static Bitmap rotateImage(Bitmap src, float degree) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        Bitmap bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        return bmp;
    }

    /**
     *  To initialize values
     *
     */
    private void dataInitialization(SurveyInformation currentSurvey) {

        // DATE

            textView_date =  (TextView) findViewById(R.id.textView_value_date);
            textView_date.setText(currentSurvey.getDate());

        // STARTING TIME

            textView_startingTime =  (TextView) findViewById(R.id.textView_value_starting_time);
            textView_startingTime.setText(currentSurvey.getStartingTime());

        // TASTING TIME

            textView_tastingTime =  (TextView) findViewById(R.id.textView_value_tasting_time);
            textView_tastingTime.setText(currentSurvey.getTastingTime());

        // IMAGE NAME

            textView_imageName =  (TextView) findViewById(R.id.textView_value_imgGalleryName);
            textView_imageName.setText(currentSurvey.getImage());

        // IMAGE VIEW

            showImageCaptured(currentSurvey.getImage());

        // LOCATION

            textView_location =  (TextView) findViewById(R.id.textView_value_location);
            textView_location.setText(currentSurvey.getLocation());

        // SURVEY

            char [] survey_values = currentSurvey.getAnswers();

            for ( int i = 0; i < 16 ; i++ ) {

                System.out.println(survey_values[i]);

                switch (i) {

                    case 0: //Q11

                        if (survey_values[i] == '1') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_1_1_1);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '2') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_1_1_2);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '3') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_1_1_3);
                            radioButton_survey.setChecked(true);}
                        break;

                    case 1: //Q21

                        if (survey_values[i] == '1') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_1_1);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '2') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_1_2);
                            radioButton_survey.setChecked(true);}
                        break;

                    case 2: //Q22

                        if (survey_values[i] == '1') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_2_1);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '2') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_2_2);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '3') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_2_3);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '4') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_2_4);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '5') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_2_5);
                            radioButton_survey.setChecked(true);}
                        break;

                    case 3: //Q23

                        if (survey_values[i] == '1') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_3_1);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '2') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_3_2);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '3') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_3_3);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '4') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_3_4);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '5') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_3_5);
                            radioButton_survey.setChecked(true);}
                        break;

                    case 4: //Q24

                        if (survey_values[i] == '1') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_4_1);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '2') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_4_2);
                            radioButton_survey.setChecked(true);}
                        break;

                    case 5: //Q25

                        if (survey_values[i] == '1') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_5_1);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '2') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_5_2);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '3') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_5_3);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '4') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_5_4);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '5') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_2_5_5);
                            radioButton_survey.setChecked(true);}
                        break;

                    case 6: //Q31

                        if (survey_values[i] == '1') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_1_1);
                            radioButton_survey.setChecked(true);
                            EditText editText_agent_ID = (EditText) findViewById(R.id.editText_agent_ID);
                            editText_agent_ID.setText(currentSurvey.getAgentID());

                        }
                        break;

                    case 7: //Q32

                        if (survey_values[i] == '1') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_2_1);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '2') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_2_2);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '3') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_2_3);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '4') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_2_4);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '5') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_2_5);
                            radioButton_survey.setChecked(true);}
                        break;

                    case 8: //Q33

                        if (survey_values[i] == '1') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_3_1);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '2') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_3_2);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '3') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_3_3);
                            radioButton_survey.setChecked(true);}
                        if (survey_values[i] == '4') {
                            radioButton_survey = (RadioButton) findViewById(R.id.radioButton_3_3_4);
                            radioButton_survey.setChecked(true);}
                        break;

                    case 9: //Q41

                        // Check if user has agreed post tasting interview
                        if (survey_values[i] != 'x') {      // if == 'x' means that user has not agreed post tasting interview

                            showPostTastingInterview();     // This method only is necessary to be called once

                            switchPosttasting = (Switch) findViewById(R.id.switch_post_tasting);
                            switchPosttasting.setChecked(true);
                            switchPosttasting.setClickable(false);

                            if (survey_values[i] == '1') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_1_1);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '2') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_1_2);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '3') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_1_3);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '4') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_1_4);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '5') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_1_5);
                                radioButton_survey.setChecked(true);}
                        }
                        else {

                            switchPosttasting = (Switch) findViewById(R.id.switch_post_tasting);
                            switchPosttasting.setChecked(false);
                            switchPosttasting.setClickable(false);

                        }
                        break;

                    case 10: //Q42

                        if (survey_values[i] != 'x') {      // if == 'x' means that user has not agreed post tasting interview

                            if (survey_values[i] == '1') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_2_1);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '2') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_2_2);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '3') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_2_3);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '4') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_2_4);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '5') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_2_5);
                                radioButton_survey.setChecked(true);}
                        }
                        break;

                    case 11: //Q43

                        if (survey_values[i] != 'x') {      // if == 'x' means that user has not agreed post tasting interview
                            if (survey_values[i] == '1') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_3_1);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '2') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_3_2);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '3') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_3_3);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '4') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_3_4);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '5') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_3_5);
                                radioButton_survey.setChecked(true);}
                        }
                        break;

                    case 12: //Q44

                        if (survey_values[i] != 'x') {      // if == 'x' means that user has not agreed post tasting interview
                            if (survey_values[i] == '1') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_4_1);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '2') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_4_2);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '3') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_4_3);
                                radioButton_survey.setChecked(true);}
                        }

                    case 13: //Q45

                        if (survey_values[i] != 'x') {      // if == 'x' means that user has not agreed post tasting interview
                            if (survey_values[i] == '1') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_5_1);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '2') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_5_2);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '3') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_5_3);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '4') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_5_4);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '5') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_5_5);
                                radioButton_survey.setChecked(true);}
                        }
                        break;

                    case 14: //Q46

                        if (survey_values[i] != 'x') {      // if == 'x' means that user has not agreed post tasting interview
                            if (survey_values[i] == '1') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_6_1);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '2') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_6_2);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '3') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_6_3);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '4') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_6_4);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '5') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_6_5);
                                radioButton_survey.setChecked(true);}
                        }
                        break;

                    case 15: //Q47
                        if (survey_values[i] != 'x') {      // if == 'x' means that user has not agreed post tasting interview
                            if (survey_values[i] == '1') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_7_1);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '2') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_7_2);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '3') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_7_3);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '4') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_7_4);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '5') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_7_5);
                                radioButton_survey.setChecked(true);}
                            if (survey_values[i] == '6') {
                                radioButton_survey = (RadioButton) findViewById(R.id.radioButton_4_7_6);
                                radioButton_survey.setChecked(true);}
                        }
                        break;

                } // end switch

            }  // End for

        // Survey values
        // Q11
       // radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_1_1);
        //radioButton_survey = (RadioButton) findViewById(R.id.radioButton_1_1_1);
        //radioButton_survey.setChecked(true);
        //radioGroup_survey.check();

        /*
        mySurvey.setAnswer(0,getAnswerNumber(radioGroup_1_1));  // index 0 => Q11 (first question). The second parameter returns the radiobutton number selected of the specific radiogroup
        System.out.println (getAnswerNumber(radioGroup_1_1));
        // Q21
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_1);
        mySurvey.setAnswer(1,getAnswerNumber(radioGroup_2_1));
        System.out.println (getAnswerNumber(radioGroup_2_1));
        // Q22
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_2);
        mySurvey.setAnswer(2,getAnswerNumber(radioGroup_2_2));
        System.out.println (getAnswerNumber(radioGroup_2_2));
        // Q23
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_3);
        mySurvey.setAnswer(3,getAnswerNumber(radioGroup_2_3));
        System.out.println (getAnswerNumber(radioGroup_2_3));
        // Q24
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_4);
        mySurvey.setAnswer(4,getAnswerNumber(radioGroup_2_4));
        System.out.println (getAnswerNumber(radioGroup_2_4));
        // Q25
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_5);
        mySurvey.setAnswer(5,getAnswerNumber(radioGroup_2_5));
        System.out.println (getAnswerNumber(radioGroup_2_5));
        // Q31
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_3_1);
        mySurvey.setAnswer(6,getAnswerNumber(radioGroup_3_1));
        System.out.println (getAnswerNumber(radioGroup_3_1));
        // Q32
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_3_2);
        mySurvey.setAnswer(7,getAnswerNumber(radioGroup_3_2));
        System.out.println (getAnswerNumber(radioGroup_3_2));
        // Q33
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_3_3);
        mySurvey.setAnswer(8,getAnswerNumber(radioGroup_3_3));
        System.out.println (getAnswerNumber(radioGroup_3_3));
        // Q41
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_1);
        mySurvey.setAnswer(9,getAnswerNumber(radioGroup_4_1));
        System.out.println (getAnswerNumber(radioGroup_4_1));
        // Q42
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_2);
        mySurvey.setAnswer(10,getAnswerNumber(radioGroup_4_2));
        System.out.println (getAnswerNumber(radioGroup_4_2));
        // Q43
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_3);
        mySurvey.setAnswer(11,getAnswerNumber(radioGroup_4_3));
        System.out.println (getAnswerNumber(radioGroup_4_3));
        // Q44
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_4);
        mySurvey.setAnswer(12,getAnswerNumber(radioGroup_4_4));
        System.out.println (getAnswerNumber(radioGroup_4_4));
        // Q45
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_5);
        mySurvey.setAnswer(13,getAnswerNumber(radioGroup_4_5));
        System.out.println (getAnswerNumber(radioGroup_4_5));
        // Q46
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_6);
        mySurvey.setAnswer(14,getAnswerNumber(radioGroup_4_6));
        System.out.println (getAnswerNumber(radioGroup_4_6));
        // Q47
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_7);
        mySurvey.setAnswer(15,getAnswerNumber(radioGroup_4_7));
        System.out.println (getAnswerNumber(radioGroup_4_7));*/


    }

    /**
     *
     * To save the survey
     */

    private void saveSurvey(SurveyInformation mySurvey) {

        //1- Update survey information into txt file

        try {

            updateSurveyValues(mySurvey);                                 // Update SurveyInformation object

            // LOCALLY

            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File survey_info = new File (directory,"EX1_2016_USERS.txt");

            // Modify current survey saved previously => REPLACE SURVEY

            //Reads and store txt file content

            if (survey_info.exists()){

            FileReader survey_info_reader = new FileReader(survey_info);
            BufferedReader b = new BufferedReader(survey_info_reader);
            b = new BufferedReader(survey_info_reader);
            List<String> lines = new ArrayList<String>();                // List which stores all document lines
            String aux2;
            while((aux2 = b.readLine())!=null) {
                lines.add(aux2);
            }
            b.close();

            // Get line to replace

            int line_index = 0;
            int i = 0;
            for (String t : lines) {

                if (t.equals(user_ID)) {
                    line_index = i+1;  // The line to be replaced (DB information relative to selected user) is user ID line + 1
                }
                i ++;
            }

            // Replace line in list

            lines.set(line_index,mySurvey.getSurveyDBFormat());    // replace the proper line (the last one => file index = lines.size - 1)

            // Debugg

            i = 0;
            for (String t : lines) {

                Log.d(TAG, "Debug_txt:" + lines.get(i));
                System.out.println(lines.get(i));
                i ++;
            }

            // Update txt file with the new content

            FileWriter survey_info_writer = new FileWriter(survey_info, false); // false => overwrite the previous content

            for (i = 0; i<lines.size();i++){    // Reload the content with the updated values
                if (i == 0) {survey_info_writer.append(lines.get(i));}
                else {survey_info_writer.append(System.getProperty("line.separator") + lines.get(i));}
            }
            survey_info_writer.flush();
            survey_info_writer.close();



            // DATABASE

                //3 Calling server php web service edit_survey file
                surveyStringJSON = mySurvey.getSurveyDBFormat();
                try {
                    JSONObject obj = new JSONObject(surveyStringJSON);
                    obj.put("surveyIndex",user_number);
                    System.out.println(obj.toString());
                    surveyStringJSON = obj.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                stringUrl_users_edit= "http://"+serverIP+"/webservice/edit_survey.php";
                new editSurveyDB().execute(stringUrl_users_edit);




            } // end if file exists

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * To update SurveyInformation object with new survey values
     */
    private int updateSurveyValues(SurveyInformation mySurvey) {

        // Header values

        textView_imageName = (TextView) findViewById(R.id.textView_value_imgGalleryName);
        textView_date = (TextView) findViewById(R.id.textView_value_date);
        textView_startingTime = (TextView) findViewById(R.id.textView_value_starting_time);
        textView_tastingTime = (TextView) findViewById(R.id.textView_value_tasting_time);
        textView_location = (TextView) findViewById(R.id.textView_value_location);

        // Survey values

        // Q11
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_1_1);
        mySurvey.setAnswer(0,getAnswerNumber(radioGroup_survey));  // index 0 => Q11 (first question). The second parameter returns the radiobutton number selected of the specific radiogroup
        System.out.println (getAnswerNumber(radioGroup_survey));
        // Q21
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_1);
        mySurvey.setAnswer(1,getAnswerNumber(radioGroup_survey));
        System.out.println (getAnswerNumber(radioGroup_survey));
        // Q22
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_2);
        mySurvey.setAnswer(2,getAnswerNumber(radioGroup_survey));
        System.out.println (getAnswerNumber(radioGroup_survey));
        // Q23
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_3);
        mySurvey.setAnswer(3,getAnswerNumber(radioGroup_survey));
        System.out.println (getAnswerNumber(radioGroup_survey));
        // Q24
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_4);
        mySurvey.setAnswer(4,getAnswerNumber(radioGroup_survey));
        System.out.println (getAnswerNumber(radioGroup_survey));
        // Q25
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_2_5);
        mySurvey.setAnswer(5,getAnswerNumber(radioGroup_survey));
        System.out.println (getAnswerNumber(radioGroup_survey));
        // Q31
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_3_1);
        EditText editText_agent_ID = (EditText) findViewById(R.id.editText_agent_ID);
        mySurvey.setAgentID(String.valueOf(editText_agent_ID.getText()));
        mySurvey.setAnswer(6,getAnswerNumber(radioGroup_survey));
        System.out.println (getAnswerNumber(radioGroup_survey));
        // Q32
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_3_2);
        mySurvey.setAnswer(7,getAnswerNumber(radioGroup_survey));
        System.out.println (getAnswerNumber(radioGroup_survey));
        // Q33
        radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_3_3);
        mySurvey.setAnswer(8,getAnswerNumber(radioGroup_survey));
        System.out.println (getAnswerNumber(radioGroup_survey));

        // Post-tasting interview
        switchPosttasting = (Switch) findViewById(R.id.switch_post_tasting);
        if (switchPosttasting.isChecked()){
            // Q41
            radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_1);
            mySurvey.setAnswer(9,getAnswerNumber(radioGroup_survey));
            System.out.println (getAnswerNumber(radioGroup_survey));
            // Q42
            radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_2);
            mySurvey.setAnswer(10,getAnswerNumber(radioGroup_survey));
            System.out.println (getAnswerNumber(radioGroup_survey));
            // Q43
            radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_3);
            mySurvey.setAnswer(11,getAnswerNumber(radioGroup_survey));
            System.out.println (getAnswerNumber(radioGroup_survey));
            // Q44
            radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_4);
            mySurvey.setAnswer(12,getAnswerNumber(radioGroup_survey));
            System.out.println (getAnswerNumber(radioGroup_survey));
            // Q45
            radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_5);
            mySurvey.setAnswer(13,getAnswerNumber(radioGroup_survey));
            System.out.println (getAnswerNumber(radioGroup_survey));
            // Q46
            radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_6);
            mySurvey.setAnswer(14,getAnswerNumber(radioGroup_survey));
            System.out.println (getAnswerNumber(radioGroup_survey));
            // Q47
            radioGroup_survey = (RadioGroup) findViewById(R.id.radioGroup_4_7);
            mySurvey.setAnswer(15,getAnswerNumber(radioGroup_survey));
            System.out.println (getAnswerNumber(radioGroup_survey));
        }

        return 0;
    }

    /**
     *
     * To get the radiobutton selected of the desired radiogroup (the answer is a char value to write it directly into txt file without casting it)
     */

    private char getAnswerNumber(RadioGroup radioGroup) {

        char answerNumber = '0';
        String id_radioButton = getResources().getResourceEntryName(radioGroup.getCheckedRadioButtonId()); // To recover string id, not number. Inside this string is the answer number

        int index;
        index = id_radioButton.indexOf("_",0);  // Return the index of the first occurrence of "_", starting in 0
        index += 5;                             // The name of ids of views: _x_y_z, where z is the number of the answer => first _ = index => index + 5 = z
        answerNumber = id_radioButton.charAt(index); // it returns the character at the specified index

        return answerNumber;
    }

    /**
     *  To show post tasting interview in case of user agrees
     */

    private void showPostTastingInterview() {

        RadioGroup postTastingRadioGroup;

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_1);
        postTastingRadioGroup.setVisibility(View.VISIBLE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_2);
        postTastingRadioGroup.setVisibility(View.VISIBLE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_3);
        postTastingRadioGroup.setVisibility(View.VISIBLE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_4);
        postTastingRadioGroup.setVisibility(View.VISIBLE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_5);
        postTastingRadioGroup.setVisibility(View.VISIBLE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_6);
        postTastingRadioGroup.setVisibility(View.VISIBLE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_7);
        postTastingRadioGroup.setVisibility(View.VISIBLE);

    }

    /**
     *  To hide post tasting interview in case of user does not agree or does not finish the interview
     */

    private void hidePostTastingInterview() {

        RadioGroup postTastingRadioGroup;

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_1);
        postTastingRadioGroup.setVisibility(View.GONE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_2);
        postTastingRadioGroup.setVisibility(View.GONE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_3);
        postTastingRadioGroup.setVisibility(View.GONE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_4);
        postTastingRadioGroup.setVisibility(View.GONE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_5);
        postTastingRadioGroup.setVisibility(View.GONE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_6);
        postTastingRadioGroup.setVisibility(View.GONE);

        postTastingRadioGroup = (RadioGroup) this.findViewById(R.id.radioGroup_4_7);
        postTastingRadioGroup.setVisibility(View.GONE);

    }


    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }


    /**
     * To communicate with the server and edit a stored row with the new values
     */

    private class editSurveyDB extends AsyncTask<String, String, String>
    {


        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null;

            try {
                // Configuration
                URL url;
                url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                // Set output stream
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

                // Send Survey info

                //Create JSONObject here

                /*
                JSONObject jsonParam = new JSONObject();
                try {
                    jsonParam.put("agentID", agentID);
                    System.out.println(jsonParam.toString());
                    //wr.writeBytes(URLEncoder.encode(jsonParam.toString(),"UTF-8"));
                    wr.writeBytes(jsonParam.toString());
                    wr.flush();
                    wr.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

                wr.writeBytes(surveyStringJSON);
                wr.flush();
                wr.close();

                if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ){

                    // Read the answer from the server
                    InputStream is = conn.getInputStream();

                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuffer response = new StringBuffer();
                    while((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();
                    return response.toString();
                }else{
                    InputStream err = conn.getErrorStream();
                }

                return "Done";
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(conn != null) {
                    conn.disconnect();
                }
            }
            return "Failed"; }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("Successful\r"))
                Toast.makeText(getBaseContext(), "SURVEY UPDATED SUCCESSFULLY", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getBaseContext(), "ERROR UPDATING SURVEY", Toast.LENGTH_LONG).show();
        }
    }


} // end class
