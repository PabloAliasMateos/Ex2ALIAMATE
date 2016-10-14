package uc3mprojects.pablo.ex1aliamate;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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
import java.util.List;

public class ViewSurveyActivity extends AppCompatActivity {

    private int number_surveys = 0;
    private List<String> lines = new ArrayList<String>();                // List which stores all document lines

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    //Add dynamically elements to the scroll view (all surveys stored). The number of elements to be added is the number of surveys stored => read .txt file

    //0- Create a file object to access EX1_2016_USERS.txt

        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File survey_info = new File (directory,"EX1_2016_USERS.txt");

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

    //1- Access main scroll view

        LayoutInflater inflater;
        inflater = (LayoutInflater)  this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View mainLayout = inflater.inflate(R.layout.activity_view_survey, null);
        ScrollView sv = (ScrollView) mainLayout.findViewById(R.id.surveys_scroll_view);

    //2- Create a LinearLayout element to be added => scroll view only one direct child

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        // Element to be added to linear layout

        TextView tv;

        for (int i = 0; i < number_surveys ; i ++) {

            tv = new TextView(this);
        // Add text to linear layout
            tv.setText(lines.get(i*2));
            ll.addView(tv);
        }

     //3- Add the LinearLayout element to the ScrollView (only this direct child from the point of view of scroll view)

        sv.addView(ll);

    //4- Display the main view

        setContentView(mainLayout);



    } // en onCreate

    private SurveyInformation recoverSurveyInformation (String surveyInfoDBFormat) {

        SurveyInformation surveyObject;  // To recover all the fields


        return  surveyObject;
    }

} // end class

