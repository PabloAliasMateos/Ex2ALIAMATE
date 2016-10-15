package uc3mprojects.pablo.ex1aliamate;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSurveyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_survey);
        // To get variables passed from the father activity
        Bundle bundle = getIntent().getExtras();
        String user_ID = bundle.getString("USER_ID");
        System.out.println(user_ID);

        //Toast.makeText(this, "ACTIVITY TO EDIT SURVEYS CREATED", Toast.LENGTH_LONG).show();

        //1- Read data base info corresponding to the selected user ID

        List<String> lines = readEX1_2016_USERS();
        int i = 0;
        String surveyInfoDBFormat ="";

        for (String t : lines) {
            System.out.println(t);
            if (t.equals(user_ID)){
                surveyInfoDBFormat = lines.get(i+1);
                SurveyInformation currentSurvey = recoverSurveyInformation (surveyInfoDBFormat);
               // System.out.println(surveyInfoDBFormat);
            }
            i ++;
        }

        // Generate survey object



    } // end onCreate

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


}
