package uc3mprojects.pablo.ex1aliamate;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Interpolator;

public class MainActivity extends AppCompatActivity {

// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ACTIVITY ATRIBUTES
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ACTIVITY LIFE-CYCLE METHODS
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // 1 // CREATE

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Calling survey activity clicking on the proper button

        findViewById(R.id.button_StartSurvey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SurveyActivity.class);  // To bind the main activity with other activity
                startActivity(intent);
            }
        });

        // Calling view survey activity clicking on the proper button

        findViewById(R.id.button_ViewSurvey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewSurveyActivity.class);  // To bind the main activity with other activity
                startActivity(intent);

            }
        });

    }

// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ACTIVITY METHODS
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


}   // end class MainActivity
