package uc3mprojects.pablo.ex1aliamate;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class SurveyActivity extends AppCompatActivity { // without extends Fragment, it would be just a void java class (heritance)

    // Request permissions codes

    public static final int REQUEST_CAMERA = 10;
    public static final int IMAGE_PERMISSION_REQUEST_CODE = 1;
    public static final int SURVEY_PERMISSION_REQUEST_CODE = 2;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 3;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 4;

    // Views

    private TextView textView_date;
    private TextView textView_startingTime;
    private TextView textView_tastingTime;
    private TextView textView_location;
    private TextView textView_imageName;
    private ImageView imageView_survey_picture;                  // Now all the methods can access to this View
    private ImageButton imageButton_clock;


    private TextView textView_date_label;
    private TextView textView_startingTime_label;
    private TextView textView_tastingTime_label;
    private TextView textView_location_label;
    private TextView textView_imageName_label;


    // General

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download"; // getExternalStorageDirectory does not refer to SD card! it refers to the root of the internal storage outside the app
    private Uri imageURI;                                        // Uri of the last captured image
    private String imageName = "";
    private File directory;
    private File imageFile;
    Timer T;
    private int timerState = 0;                                  // To add start stop functionality to clock butto
    private int clockButton_animationState = 0;
    private int seconds_counter = 0;
    private int minutes_counter = 0;
    private int current_index_value = 0;                       // To detect if the current survey has been saved before and store index value
    private int tried_save = 0;

    // Report tags

    final String TAG = "States_lifeCycle";
    final String TAG2 = "Location_debug";


    // ========================================================================================================================================
    // LIFE-CYCLE METHODS
    // ========================================================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar (valid when activity is created by extends from activity, not AppCompatActivity)
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_survey);

        imageView_survey_picture = (ImageView) findViewById(R.id.imageView_survey_picture);  // All methods can access to imageView_survey_picture view

        //1- VALUES INITIALIZATION

        dataInitialization(savedInstanceState);  // savedInstanceState == null => first onCreate call | savedInstanceState != null => recover info from the previous state when user changes the orientation

        //2- LOCATION

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider, following requestLocationUpdates function parameters
                showNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
                // This method will be called when GPS is enabled
            }

            public void onProviderDisabled(String provider) {
                // This method will be called when GPS is disabled
                //Toast.makeText(this, "PERMISSION DENIED: Can not access GPS. Please, activte GPS", Toast.LENGTH_LONG).show();
                Intent gps_intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);   // To launch the menu to enable GPS
                startActivity(gps_intent);
            }
        };

        // Register the listener with the Location Manager to receive location updates

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // To show the last known location until the new value is gotten
            String locationProvider = LocationManager.GPS_PROVIDER;
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            showNewLocation(lastKnownLocation);
            // To start checking the location
            locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);       // The location will be get from gps, it will be checked every 5 seconds or when user moves 5 meters
        } else {
            //permission failed, request
            String[] permissionRequest = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                           // Check the current SDK target version (run-time permissions => API 23)
                requestPermissions(permissionRequest, LOCATION_PERMISSION_REQUEST_CODE);
            } else
                Toast.makeText(this, "PERMISSION DENIED: Can not access the GPS. ", Toast.LENGTH_LONG).show();
        }

        //3- TIMER to count tasting time

        // animation to make blik chronometer button. It will be stopped once clock button is pressed
        final Animation animation = new AlphaAnimation(1, 0);                       // Change alpha from fully visible to invisible
        animation.setDuration(700);                                                 // duration - half a second
        animation.setInterpolator(new LinearInterpolator());                        // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE);                               // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE);                                 // Reverse animation at the end so the button will fade back in
        imageButton_clock = (ImageButton) findViewById(R.id.imageButton_clock);


        if (savedInstanceState == null) { // first onCreate call => tasting time timer starts automatically

            imageButton_clock.startAnimation(animation);
            startTimer_tastingTime();
            clockButton_animationState = 1;
            timerState = 1;

        } else {  // change orientation

            if (clockButton_animationState == 1) { // To continue temporizing when user change the orientation if previously user was temporizing
                imageButton_clock.startAnimation(animation);
                startTimer_tastingTime();
            }

        }


        //4- CAMERA BUTTON: Calling camera from fragment
        findViewById(R.id.imageButton_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {       // checking SDK target version. Newest versions need run time permissions

                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                        invokeCamera();
                    else
                    {
                        //permission failed, request
                        String[] permissionRequest = {Manifest.permission.CAMERA};
                        // Check the current SDK target version (run-time permissions => API 23)
                        requestPermissions(permissionRequest, CAMERA_PERMISSION_REQUEST_CODE);
                    } // end request permission
                }
                else
                {
                    invokeCamera();
                } // end check version*/

                invokeCamera();

            }
        });

        //5- CHRONOMETER BUTTON
        imageButton_clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clockButton_animationState == 1) {
                    T.cancel();
                    imageButton_clock.clearAnimation();
                    clockButton_animationState = 0;
                    timerState = 0;
                } else {
                    startTimer_tastingTime();
                    imageButton_clock.startAnimation(animation);
                    clockButton_animationState = 1;
                    timerState = 1;
                }
            }
        });

        //6- SAVE SURVEY BUTTON
        findViewById(R.id.imageButton_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message;
                String tittle;

                if (current_index_value == 0) {     // First time that save button is pressed
                    message = "Are you sure you want to save the current survey?";
                    tittle = "SAVE SURVEY";
                } else {                               // To make some modifications (for example, wron answer)
                    message = "Are you sure you want to replace the stored survey?";
                    tittle = "REPLACE SURVEY";
                }

                // DIALOG TO SAVE OR NOT

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
                                saveSurvey();
                            else {
                                //permission failed, request
                                String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                                // Check the current SDK target version (run-time permissions => API 23)
                                requestPermissions(permissionRequest, SURVEY_PERMISSION_REQUEST_CODE);
                            } // end request permission
                        } else {
                            saveSurvey();
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

        Log.d(TAG, "MainActivity: onCreate()");

    } // end onCreate

    @Override
    protected void onRestart() {
        super.onRestart();

        // Restart listener when app goes back to foreground

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);

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

        if (tried_save == 1) { // If user has tried to save the survey previously with error, visual indicators should appear again at rotate screen time
            SurveyInformation mySurvey = new SurveyInformation();
            readSurveyValues(mySurvey);
            checkSurveyComplete(mySurvey);
        }

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

        // Remove the GPS listener (battery management)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);

        Log.d(TAG, "MainActivity: onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // GPS managemet is done through onStop method. Activity lifecycle => always onStop befor onDestroy

        // Release timer. Only if timer is active, otherwise it has been already deleted
        if (clockButton_animationState == 1)
            T.cancel();

        Log.d(TAG, "MainActivity: onDestroy()");
    }


    // ========================================================================================================================================
    // OVERRIDE METHODS
    // ========================================================================================================================================

    /**
     * With this method, onStop and onDestroy methods are not called when back button is pressed
     */

    @Override
    public void onBackPressed() {

        // DIALOG TO EXIT OR NOT

        AlertDialog.Builder myBuild = new AlertDialog.Builder(this);
        myBuild.setMessage("Are you sure you want to close the current survey?");
        myBuild.setTitle("EXIT SURVEY");
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

    /**
     * Method to catch the result of an activity called with onActivityResult
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);      // data content the image in this case. This method wil receive a request code, in order to filter who has invoked it

        switch (requestCode) {

            case REQUEST_CAMERA:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {        // Check the current SDK target version (run-time permissions => API 23)
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        showImageCaptured(imageName);
                    } else {
                        //permission failed, request
                        String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissionRequest, IMAGE_PERMISSION_REQUEST_CODE);
                    }
                } // end check version

                return;

        } // end switch

    } // end onActivityResult

    /**
     * Method to handle run-time permissions
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case CAMERA_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    invokeCamera();
                else
                    Toast.makeText(this, "PERMISSION DENIED: Can not open the camera. ", Toast.LENGTH_LONG).show();
                return;

            case IMAGE_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    showImageCaptured(imageName);
                else
                    Toast.makeText(this, "PERMISSION DENIED: Can not save the image. ", Toast.LENGTH_LONG).show();
                return;

            case SURVEY_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    saveSurvey();
                else
                    Toast.makeText(this, "PERMISSION DENIED: Can not save the survey. ", Toast.LENGTH_LONG).show();
                return;

            case LOCATION_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // To show the last known location until the new value is gotten
                    String locationProvider = LocationManager.GPS_PROVIDER;
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                    showNewLocation(lastKnownLocation);
                    // To start checking the location
                    locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
                }

                else {

                    Toast.makeText(this, "PERMISSION DENIED: Can not access GPS.", Toast.LENGTH_LONG ).show();
                    finish(); // The app requires GPS
                }
                    // To show the last known location until the new value is gotten

                return;


        } // end switch

    }

    /**
     * Method to save data and do not lose it when changing from portrait to land scape
     */

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        /**
        savedInstanceState.putBoolean("MyBoolean", true);
        savedInstanceState.putDouble("myDouble", 1.9);
        savedInstanceState.putInt("MyInt", 1);*/

        textView_imageName = (TextView) findViewById(R.id.textView_value_imgGalleryName);
        textView_date = (TextView) findViewById(R.id.textView_value_date);
        textView_startingTime = (TextView) findViewById(R.id.textView_value_starting_time);
        textView_tastingTime = (TextView) findViewById(R.id.textView_value_tasting_time);
        textView_location = (TextView) findViewById(R.id.textView_value_location);

        savedInstanceState.putString("imageName", (String) textView_imageName.getText());
        savedInstanceState.putString("date", (String) textView_date.getText());
        savedInstanceState.putString("startingTime", (String) textView_startingTime.getText());
        savedInstanceState.putString("tastingTime", (String) textView_tastingTime.getText());
        savedInstanceState.putString("location", (String) textView_location.getText());
        savedInstanceState.putInt("minutes_counter",minutes_counter);
        savedInstanceState.putInt("seconds_counter",seconds_counter);
        savedInstanceState.putInt("clockButton_animationState",clockButton_animationState);
        savedInstanceState.putInt("timer_state",timerState);
        savedInstanceState.putInt("current_index_value",current_index_value);
        savedInstanceState.putInt("tried_save",tried_save);

        // etc.
    }

    // ========================================================================================================================================
    // NON-OVERRIDE METHODS
    // ========================================================================================================================================

    /**
     * To invoke the camera through intent
     */

    private void invokeCamera() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // To specify a folder to store the images => putExtra
        //File directory = new File (storagePath);                    // Directory of the file
        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);                    // Directory of the file
        imageName = getImageName ();                         // Name of the file
        imageFile = new File (directory, imageName);           // File
        imageURI = Uri.fromFile(imageFile);                     // Uri of the file. It is necessary for putExtra

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);   // Now we are telling that we want to store the image into this specific folder
        // startActivityForResult -> to get something in return (in this case a picture)
        startActivityForResult(cameraIntent,REQUEST_CAMERA);     // The value of the request code (REQUEST_CAMERA) is irrelevant, just must be unique

    }

    /**
     * To calculate image name
     * @return
     */

    private String getImageName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmss");
        String imageName = "img" + sdf.format(new Date()) +".jpg";
        return imageName;
    }

    /**
     *  To show the image in imageView. It is necessary to provide run-time permissions and manifest permissions for android 6.0
     */

    private void showImageCaptured(String image_name) {

        String imagePath = storagePath + "/" + image_name ;
        Bitmap myImg = BitmapFactory.decodeFile(imagePath);
        // Without resize the image, when it is large, it leads into memory allocation error
        int h = 100; // height in pixels
        int w = 50; // width in pixels
        myImg = Bitmap.createScaledBitmap(myImg, h, w, true);
        imageView_survey_picture.setImageBitmap(rotateImage(myImg, 90));
        textView_imageName =  (TextView) findViewById(R.id.textView_value_imgGalleryName);
        textView_imageName.setText(image_name);

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
    private void dataInitialization(Bundle savedInstanceState) {

        // DATE
        if (savedInstanceState == null) {    // The first that onCreate is called, savedInstanceState parameter received by onCreate is null (nothing to save)

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String date = sdf.format(new Date());
            textView_date =  (TextView) findViewById(R.id.textView_value_date);
            textView_date.setText(date);
        }
        else {

            textView_date =  (TextView) findViewById(R.id.textView_value_date);
            textView_date.setText(savedInstanceState.getString("date"));
        }

        // STARTING TIME
        if (savedInstanceState == null) {    // The first that onCreate is called, savedInstanceState parameter received by onCreate is null (nothing to save)

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String startingTime = sdf.format(new Date());
            textView_startingTime =  (TextView) findViewById(R.id.textView_value_starting_time);
            textView_startingTime.setText(startingTime);
        }
        else {

            textView_startingTime =  (TextView) findViewById(R.id.textView_value_starting_time);
            textView_startingTime.setText(savedInstanceState.getString("startingTime"));
        }

        // TASTING TIME
        if (savedInstanceState == null) {    // The first that onCreate is called, savedInstanceState parameter received by onCreate is null (nothing to save)

            String tastingTime = "00:00";
            textView_tastingTime =  (TextView) findViewById(R.id.textView_value_tasting_time);
            textView_tastingTime.setText(tastingTime);
        }
        else {

            textView_tastingTime =  (TextView) findViewById(R.id.textView_value_tasting_time);
            textView_tastingTime.setText(savedInstanceState.getString("tastingTime"));
        }

        // IMAGE NAME

        if (savedInstanceState != null) {

            textView_imageName =  (TextView) findViewById(R.id.textView_value_imgGalleryName);
            textView_imageName.setText(savedInstanceState.getString("imageName"));
        }

        // IMAGE VIEW

        if (savedInstanceState != null) {

            if (!savedInstanceState.getString("imageName").equals("-")) {  // if an image has been capture previously
                showImageCaptured(savedInstanceState.getString("imageName"));
            }
        }

        // LOCATION

        if (savedInstanceState != null) {

            textView_location =  (TextView) findViewById(R.id.textView_value_location);
            textView_location.setText(savedInstanceState.getString("location"));
        }

        // TIMER VALUES

        if (savedInstanceState != null) {

            minutes_counter = savedInstanceState.getInt("minutes_counter");
            seconds_counter = savedInstanceState.getInt("seconds_counter");
            timerState = savedInstanceState.getInt("timerState");
            clockButton_animationState = savedInstanceState.getInt("clockButton_animationState");
        }

        // SAVE OR REPLACE SURVEY
        if (savedInstanceState != null) {

            current_index_value = savedInstanceState.getInt("current_index_value");
        }

        // VISUAL INDICATORS

        if (savedInstanceState != null) {

            tried_save = savedInstanceState.getInt("tried_save");
            if (tried_save == 1) { // If user has tried to save the survey previously with error, visual indicators should appear again at rotate screen time
                SurveyInformation mySurvey = new SurveyInformation();
                readSurveyValues(mySurvey);
                checkSurveyComplete (mySurvey);
            }
        }


    }

    /**
     *
     * To start a timer for checking tasting time
     */

    private void startTimer_tastingTime() {

        T=new Timer();      // Every time that T.cancel() is called, T object is destroyed => it is mandatory to instantiate it again
        T.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String tastingTime_min_sec;
                        textView_tastingTime =  (TextView) findViewById(R.id.textView_value_tasting_time);
                        if (seconds_counter == 60){
                            seconds_counter = 0;
                            minutes_counter++;
                        }
                        //tastingTime_min_sec = Integer.toString(minutes_counter) + ":" + Integer.toString(seconds_counter);
                        tastingTime_min_sec = String.format("%02d", minutes_counter) + ":" + String.format("%02d", seconds_counter);
                        textView_tastingTime.setText(tastingTime_min_sec);
                        seconds_counter++;
                    }
                });
            }
        }, 1000, 1000);
    }

    /**
     *
     * To show the location value in the proper textView
     */

    private void showNewLocation(Location location) {

       if (location != null) {  // Sometimes it can return null location, making crash the app if it is not handled correctly
           textView_location = (TextView) findViewById(R.id.textView_value_location);
           textView_location.setText(String.format(Locale.ROOT,"%.4f", location.getLatitude()) + " - " + String.format(Locale.ROOT,"%.4f", location.getAltitude()));  // Locale.ROOT to force . as separator instead a comma => easier to parse txt file
       }
        else {

           Log.d (TAG2,"Null location");

       }


    }

     /**
     *
     * To save the survey
     */

    private void saveSurvey() {

        // 0- User has tried to save at least once

        tried_save = 1;

        //1- Save survey information into txt file

        try {

            SurveyInformation mySurvey = new SurveyInformation ();      // class to store survey values
            readSurveyValues(mySurvey);                                 // Reads the survey values and stores the content into SurveyInformation object

            if (mySurvey.getSurveyStatus () == 1){                      // Status = 1 => completed => save the survey. This flag is changed in readSurveyValues

                directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File survey_info = new File (directory,"EX1_2016_USERS.txt");

                if (survey_info.exists()){ // there are more surveys stored previously

                    // add/change new/current survey

                    if (current_index_value == 0) { // First save of the current survey => SAVE SURVEY

                        //1- Reads the number of surveys stored

                        FileReader survey_info_reader = new FileReader(survey_info);
                        BufferedReader b = new BufferedReader(survey_info_reader);
                        String last_user_index = b.readLine();              // user index is stored in the first line. readline increments linepointer

                        //2- Stores survey ID (USER_1,USER_2, ...) + survey values

                        int new_index_value = Integer.parseInt(last_user_index, 10) + 1;

                        //2.1 - number of surveys

                        List<String> lines = new ArrayList<String>();       // List which stores all document lines
                        lines.add(String.valueOf(new_index_value));         // First line = number of surveys
                        String aux;
                        while((aux = b.readLine())!=null) {                 // This loop starts reading the second line (each readline usage increments linepointer)
                            lines.add(aux);
                        }

                        b.close();

                        FileWriter survey_info_writer = new FileWriter(survey_info, false); // false => overwrite the previous content

                        for (int i = 0; i<lines.size();i++){    // Reload the content with the updated values
                            if (i == 0) {survey_info_writer.append(lines.get(i));}
                            else {survey_info_writer.append(System.getProperty("line.separator") + lines.get(i));}
                        }
                        survey_info_writer.flush();
                        survey_info_writer.close();

                        current_index_value = new_index_value;      // now the following time that save button is pressed, it detecs that it is a correction operation
                        survey_info_writer = new FileWriter(survey_info, true); // true => not overwrite the previous content
                        survey_info_writer.append(System.getProperty("line.separator") + "USER_" + current_index_value);
                        survey_info_writer.append(System.getProperty("line.separator") + mySurvey.getSurveyDBFormat());
                        survey_info_writer.flush();
                        survey_info_writer.close();

                    }
                    else {  // Modify current survey saved previously => REPLACE SURVEY

                        //1- Reads the number of surveys stored

                        FileReader survey_info_reader = new FileReader(survey_info);
                        BufferedReader b = new BufferedReader(survey_info_reader);
                        String last_user_index = b.readLine();              // user index is stored in the first line. readline increments linepointer

                        survey_info_reader = new FileReader(survey_info);
                        b = new BufferedReader(survey_info_reader);
                        List<String> lines = new ArrayList<String>();                // List which stores all document lines
                        String aux2;
                        while((aux2 = b.readLine())!=null) {
                            lines.add(aux2);
                        }
                        b.close();

                        lines.set(lines.size()-1,mySurvey.getSurveyDBFormat());    // replace the proper line (the last one => file index = lines.size - 1)
                       // lines.set(lines.size()-1,"Pablo");    // replace the proper line (the last one => file index = lines.size - 1)

                        FileWriter survey_info_writer = new FileWriter(survey_info, false); // false => overwrite the previous content

                        for (int i = 0; i<lines.size();i++){    // Reload the content with the updated values
                            if (i == 0) {survey_info_writer.append(lines.get(i));}
                            else {survey_info_writer.append(System.getProperty("line.separator") + lines.get(i));}
                        }
                        survey_info_writer.flush();
                        survey_info_writer.close();
                    }
                }
                else { // if there is no txt file to store surveys => create it
                    FileWriter survey_info_writer = new FileWriter(survey_info, true); // true => not overwrite the previous content
                    survey_info_writer.append("1");                                // there is one survey stored, the current survey
                    survey_info_writer.append(System.getProperty("line.separator") + "USER_1");
                    survey_info_writer.append(System.getProperty("line.separator") + mySurvey.getSurveyDBFormat());
                    survey_info_writer.flush();
                    survey_info_writer.close();
                    current_index_value = 1;
                }

                Toast.makeText(this, "Survey saved successfully.", Toast.LENGTH_LONG).show();

            } // end if
            else {

                Toast.makeText(this, "Survey incomplete. Please, complete the survey before save it.", Toast.LENGTH_LONG).show();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    } // end save_survey

    /**
     *
     * Set surveyStatus attribute of mySurvey object in case that survey is completed, otherwise => visual idicators
     */

    private void checkSurveyComplete (SurveyInformation mySurvey) {

        mySurvey.setSurveyStatus (1) ;

        //1- Check if all the fields are filled. If not, highlight those which are not completed

        // Header values

        textView_imageName = (TextView) findViewById(R.id.textView_value_imgGalleryName);
        textView_date = (TextView) findViewById(R.id.textView_value_date);
        textView_startingTime = (TextView) findViewById(R.id.textView_value_starting_time);
        textView_tastingTime = (TextView) findViewById(R.id.textView_value_tasting_time);
        textView_location = (TextView) findViewById(R.id.textView_value_location);

        textView_imageName_label = (TextView) findViewById(R.id.textView_label_imgGalleryName);
        textView_date_label = (TextView) findViewById(R.id.textView_label_date);
        textView_startingTime_label = (TextView) findViewById(R.id.textView_label_starting_time);
        textView_tastingTime_label = (TextView) findViewById(R.id.textView_label_tasting_time);
        textView_location_label = (TextView) findViewById(R.id.textView_label_location);


        if (textView_imageName.getText().equals("-")) {mySurvey.setSurveyStatus (0) ;textView_imageName_label.setTextColor(getResources().getColor(R.color.survey_header_incomplete));}
        else {textView_imageName_label.setTextColor(getResources().getColor(R.color.white));}

        if (textView_date.getText().equals("-")) {mySurvey.setSurveyStatus (0) ;textView_date_label.setTextColor(getResources().getColor(R.color.survey_header_incomplete));}
        else {textView_date_label.setTextColor(getResources().getColor(R.color.white));}

        if (textView_startingTime.getText().equals("-")) {mySurvey.setSurveyStatus (0) ;textView_startingTime_label.setTextColor(getResources().getColor(R.color.survey_header_incomplete));}
        else {textView_startingTime_label.setTextColor(getResources().getColor(R.color.white));}

        if (textView_tastingTime.getText().equals("-")) {mySurvey.setSurveyStatus (0) ;textView_tastingTime_label.setTextColor(getResources().getColor(R.color.survey_header_incomplete));}
        else {textView_tastingTime_label.setTextColor(getResources().getColor(R.color.white));}

        if (textView_location.getText().equals("-")) {mySurvey.setSurveyStatus (0) ;textView_location_label.setTextColor(getResources().getColor(R.color.survey_header_incomplete));}
        else {textView_location_label.setTextColor(getResources().getColor(R.color.white));}

        // Timer state (user must stop the timer to select a tasting time)

        if (clockButton_animationState == 1){mySurvey.setSurveyStatus (0) ; textView_tastingTime_label.setTextColor(getResources().getColor(R.color.survey_header_incomplete));}
        else {textView_tastingTime_label.setTextColor(getResources().getColor(R.color.white));}

        // Survey values

        // Q11
        RadioGroup radioGroup_1_1 = (RadioGroup) findViewById(R.id.radioGroup_1_1);
        if (radioGroup_1_1.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_1_1.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete)); }
        else {radioGroup_1_1.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q21
        RadioGroup radioGroup_2_1 = (RadioGroup) findViewById(R.id.radioGroup_2_1);
        if (radioGroup_2_1.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_2_1.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_2_1.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q22
        RadioGroup radioGroup_2_2 = (RadioGroup) findViewById(R.id.radioGroup_2_2);
        if (radioGroup_2_2.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_2_2.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_2_2.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q23
        RadioGroup radioGroup_2_3 = (RadioGroup) findViewById(R.id.radioGroup_2_3);
        if (radioGroup_2_3.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_2_3.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_2_3.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q24
        RadioGroup radioGroup_2_4 = (RadioGroup) findViewById(R.id.radioGroup_2_4);
        if (radioGroup_2_4.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_2_4.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_2_4.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q25
        RadioGroup radioGroup_2_5 = (RadioGroup) findViewById(R.id.radioGroup_2_5);
        if (radioGroup_2_5.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_2_5.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_2_5.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q31
        RadioGroup radioGroup_3_1 = (RadioGroup) findViewById(R.id.radioGroup_3_1);
        if (radioGroup_3_1.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_3_1.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_3_1.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q32
        RadioGroup radioGroup_3_2 = (RadioGroup) findViewById(R.id.radioGroup_3_2);
        if (radioGroup_3_2.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_3_2.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_3_2.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q33
        RadioGroup radioGroup_3_3 = (RadioGroup) findViewById(R.id.radioGroup_3_3);
        if (radioGroup_3_3.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_3_3.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_3_3.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q41
        RadioGroup radioGroup_4_1 = (RadioGroup) findViewById(R.id.radioGroup_4_1);
        if (radioGroup_4_1.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_4_1.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_4_1.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q42
        RadioGroup radioGroup_4_2 = (RadioGroup) findViewById(R.id.radioGroup_4_2);
        if (radioGroup_4_2.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_4_2.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_4_2.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q43
        RadioGroup radioGroup_4_3 = (RadioGroup) findViewById(R.id.radioGroup_4_3);
        if (radioGroup_4_3.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_4_3.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_4_3.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q44
        RadioGroup radioGroup_4_4 = (RadioGroup) findViewById(R.id.radioGroup_4_4);
        if (radioGroup_4_4.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_4_4.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_4_4.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q45
        RadioGroup radioGroup_4_5 = (RadioGroup) findViewById(R.id.radioGroup_4_5);
        if (radioGroup_4_5.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_4_5.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_4_5.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q46
        RadioGroup radioGroup_4_6 = (RadioGroup) findViewById(R.id.radioGroup_4_6);
        if (radioGroup_4_6.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_4_6.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_4_6.setBackgroundColor(getResources().getColor(android.R.color.transparent));}
        // Q47
        RadioGroup radioGroup_4_7 = (RadioGroup) findViewById(R.id.radioGroup_4_7);
        if (radioGroup_4_7.getCheckedRadioButtonId() == -1){ mySurvey.setSurveyStatus (0) ; radioGroup_4_7.setBackgroundColor(getResources().getColor(R.color.survey_answer_incomplete));  }
        else {radioGroup_4_7.setBackgroundColor(getResources().getColor(android.R.color.transparent));}

    }

    /**
     *
     * To show the location value in the proper textView
     */
    private int readSurveyValues(SurveyInformation mySurvey) {


        checkSurveyComplete(mySurvey) ;      // only if all the fields are completed, this value will keep constant

        if (mySurvey.getSurveyStatus() == 0) {return -1;}
        else
        {

        RadioGroup radioGroupSurvey;

        //2- Fill class attributes

        // Header values
        mySurvey.setDate ((String) textView_date.getText()) ;
        mySurvey.setStartingTime ((String) textView_startingTime.getText()) ;
        mySurvey.setTastingTime ((String) textView_tastingTime.getText()) ;
        mySurvey.setLocation ((String) textView_location.getText()) ;
        mySurvey.setImage ((String) textView_imageName.getText()) ;

        // Survey values

        // Q11
        radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_1_1);
        mySurvey.setAnswer(0,getAnswerNumber(radioGroupSurvey));  // index 0 => Q11 (first question). The second parameter returns the radiobutton number selected of the specific radiogroup
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q21
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_2_1);
        mySurvey.setAnswer(1,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q22
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_2_2);
        mySurvey.setAnswer(2,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q23
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_2_3);
        mySurvey.setAnswer(3,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q24
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_2_4);
        mySurvey.setAnswer(4,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q25
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_2_5);
        mySurvey.setAnswer(5,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q31
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_3_1);
        EditText editText_agent_ID = (EditText) findViewById(R.id.editText_agent_ID);
        mySurvey.setAgentID(String.valueOf(editText_agent_ID.getText()));
        mySurvey.setAnswer(6,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q32
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_3_2);
        mySurvey.setAnswer(7,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q33
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_3_3);
        mySurvey.setAnswer(8,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q41
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_4_1);
        mySurvey.setAnswer(9,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q42
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_4_2);
        mySurvey.setAnswer(10,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q43
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_4_3);
        mySurvey.setAnswer(11,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q44
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_4_4);
        mySurvey.setAnswer(12,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q45
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_4_5);
        mySurvey.setAnswer(13,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q46
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_4_6);
        mySurvey.setAnswer(14,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));
        // Q47
            radioGroupSurvey = (RadioGroup) findViewById(R.id.radioGroup_4_7);
        mySurvey.setAnswer(15,getAnswerNumber(radioGroupSurvey));
        System.out.println (getAnswerNumber(radioGroupSurvey));

        return 0;}
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
     *
     * To get the code of the selected radiogroup : Qxy
     */

    private String getQuestionCode(RadioGroup radioGroup) {

        String questionCode = "Q";
        String id_radioButton = getResources().getResourceEntryName(radioGroup.getCheckedRadioButtonId()); // To recover string id, not number. Inside this string is the answer number
        int index;
        char x;
        char y;

        index = id_radioButton.indexOf("_",0);  // Return the index of the first occurrence of "_", starting in 0
        x = id_radioButton.charAt(index+1);       // The name of ids of views: _x_y_z, where x and y is the question code => first _ = index => index + 1 = x, index + 3 = y
        y = id_radioButton.charAt(index+3);       // The name of ids of views: _x_y_z, where x and y is the question code => first _ = index => index + 1 = x, index + 3 = y
        questionCode = questionCode + x + y;

        return questionCode;
    }

} // End class
