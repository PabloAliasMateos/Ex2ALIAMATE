package uc3mprojects.pablo.ex1aliamate;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ACTIVITY ATRIBUTES
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final int MEMORY_PERMISSION_REQUEST_CODE = 2;
    Intent intent;

// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ACTIVITY LIFE-CYCLE METHODS
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // 1 // CREATE

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
       this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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

                intent = new Intent(MainActivity.this, ViewSurveyActivity.class);  // To bind the main activity with other activity

                // Permission to read storage

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {       // checking SDK target version. Newest versions need run time permissions

                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    startActivity(intent);
                else {
                    //permission failed, request
                    String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    // Check the current SDK target version (run-time permissions => API 23)
                    requestPermissions(permissionRequest, MEMORY_PERMISSION_REQUEST_CODE);
                } // end request permission
            } else {
                    startActivity(intent);
            } // end check version

            }
        });

    }

// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ACTIVITY METHODS
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Method to handle run-time permissions
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MEMORY_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startActivity(intent);
                else {
                    Toast.makeText(this, "PERMISSION DENIED: Can not access memory. ", Toast.LENGTH_LONG).show();
                }
                break;
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


    /**
     * With this method, onStop and onDestroy methods are not called when back button is pressed
     */

    @Override
    public void onBackPressed() {

        // DIALOG TO EXIT OR NOT

        AlertDialog.Builder myBuild = new AlertDialog.Builder(this);
        myBuild.setMessage("Are you sure you want to close the application?");
        myBuild.setTitle("EXIT SURVEY ACQUIRING APPLICATION");
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


}   // end class MainActivity
