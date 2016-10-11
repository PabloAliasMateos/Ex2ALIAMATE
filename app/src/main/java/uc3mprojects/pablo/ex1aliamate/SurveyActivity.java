package uc3mprojects.pablo.ex1aliamate;

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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SurveyActivity extends AppCompatActivity {

    public static final int REQUEST_CAMERA = 10;                 // without extends Fragment, it would be just a void java class
    public static final int IMAGE_PERMISSION_REQUEST_CODE = 1;
    public static final int SURVEY_PERMISSION_REQUEST_CODE = 2;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 3;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 4;


    private TextView textView_date;
    private TextView textView_startingTime;
    private TextView textView_tastingTime;
    private TextView textView_location;
    private TextView textView_imageName;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private ImageView imageView_survey_picture;                  // Now all the methods can access to this View
    private ImageButton imageButton_clock;
    private String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download"; // getExternalStorageDirectory does not refer to SD card! it refers to the root of the internal storage outside the app
    private Uri imageURI;                                        // Uri of the last captured image
    private String imageName = "";
    private File directory;
    private File imageFile;
    Timer T;
    private int timerState = 0;
    private int clockButton_animationState = 0;
    private int seconds_counter = 0;
    private int minutes_counter = 0;

    // ========================================================================================================================================
    // LIFE-CYCLE METHODS
    // ========================================================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        imageView_survey_picture = (ImageView) findViewById(R.id.imageView_survey_picture);  // All methods can access to imageView_survey_picture view

        //1- VALUES INITIALIZATION

        dataInitialization();

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
            locationManager.requestLocationUpdates("gps", 5000, 5, locationListener); // The location will be get from gps, it will be checked every 5 seconds or when user moves 5 meters
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

        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(700); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        imageButton_clock = (ImageButton) findViewById(R.id.imageButton_clock);
        imageButton_clock.startAnimation(animation);
        clockButton_animationState = 1;

        startTimer_tastingTime();
        timerState = 1;

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

        // SAVE BUTTON
        findViewById(R.id.imageButton_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {       // checking SDK target version. Newest versions need run time permissions

                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        saveSurvey();
                    else
                    {
                        //permission failed, request
                        String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        // Check the current SDK target version (run-time permissions => API 23)
                        requestPermissions(permissionRequest, SURVEY_PERMISSION_REQUEST_CODE);
                    } // end request permission
                }
                else
                {
                    saveSurvey();
                } // end check version

            }
        });

    } // end onCreate

    // ========================================================================================================================================
    // OVERRIDE METHODS
    // ========================================================================================================================================

    // Method to catch the result of an activity called with onActivityResult

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);      // data content the image in this case. This method wil receive a request code, in order to filter who has invoked it

        switch (requestCode) {

            case REQUEST_CAMERA:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {        // Check the current SDK target version (run-time permissions => API 23)
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        showImageCaptured();
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
     * @param requestCode
     * @param permissions
     * @param grantResults
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
                    showImageCaptured();
                else
                    Toast.makeText(this, "PERMISSION DENIED: Can not save the image. ", Toast.LENGTH_LONG).show();
                return;

            case SURVEY_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    saveSurvey();
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

    private void showImageCaptured() {

        String imagePath = storagePath + "/" + imageName ;
        Bitmap myImg = BitmapFactory.decodeFile(imagePath);
        imageView_survey_picture.setImageBitmap(rotateImage(myImg, 90));
        textView_imageName =  (TextView) findViewById(R.id.textView_value_imgGalleryName);
        textView_imageName.setText(imageName);

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
    private void dataInitialization() {

        // DATE
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String date = sdf.format(new Date());
        textView_date =  (TextView) findViewById(R.id.textView_value_date);
        textView_date.setText(date);

        // STARTING TIME
        sdf = new SimpleDateFormat("HH:mm:ss");
        String startingTime = sdf.format(new Date());
        textView_startingTime =  (TextView) findViewById(R.id.textView_value_starting_time);
        textView_startingTime.setText(startingTime);

        // TASTING TIME
        String tastingTime = "00:00";
        textView_tastingTime =  (TextView) findViewById(R.id.textView_value_tasting_time);
        textView_tastingTime.setText(tastingTime);

    }

    /**
     *
     * To start a timer for checking tasting time
     */

    private void startTimer_tastingTime() {
        T=new Timer();
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

        textView_location =  (TextView) findViewById(R.id.textView_value_location);
        textView_location.setText(String.format("%.4f",location.getLatitude())+" - "+ String.format("%.4f",location.getAltitude()));

    }

    /**
     *
     * To save the survey
     */

    private void saveSurvey() {


        //1- Save survey information into txt file
        try {
            SurveyInformation mySurvey = new SurveyInformation ();      // class to store survey values
            readSurveyValues(mySurvey);                                 // Read the survey values

            if (mySurvey.getSurveyStatus () == 1){                      // Status = 1 => completed => save the survey

                directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File survey_info = new File (directory,"EX1_2016_USERS.txt");

                if (survey_info.exists()){
                    FileWriter survey_info_writer = new FileWriter(survey_info, true); // true => not overwrite the previous content
                    survey_info_writer.append(System.getProperty("line.separator") + mySurvey.getSurveyDBFormat());
                    survey_info_writer.flush();
                    survey_info_writer.close();
                }
                else {
                    FileWriter survey_info_writer = new FileWriter(survey_info, true); // true => not overwrite the previous content
                    survey_info_writer.append(mySurvey.getSurveyDBFormat());
                    survey_info_writer.flush();
                    survey_info_writer.close();
                }
            } // end if
            else {

                Toast.makeText(this, "Survey incomplete. Please, complete the survey before save it.", Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     *
     * To show the location value in the proper textView
     */
    private int readSurveyValues(SurveyInformation mySurvey) {

        mySurvey.setSurveyStatus (1) ;      // only if all the fields are completed, this value will keep constant

     //1- Check if all the fields are filled

        // Header values

        textView_imageName = (TextView) findViewById(R.id.textView_value_imgGalleryName);
        textView_date = (TextView) findViewById(R.id.textView_value_date);
        textView_startingTime = (TextView) findViewById(R.id.textView_value_starting_time);
        textView_tastingTime = (TextView) findViewById(R.id.textView_value_tasting_time);
        textView_location = (TextView) findViewById(R.id.textView_value_location);

        if (textView_imageName.getText().equals("-") || textView_date.getText().equals("-") || textView_startingTime.getText().equals("-") || textView_tastingTime.getText().equals("-") || textView_location.getText().equals("-")) {
            mySurvey.setSurveyStatus (0) ;
            return -1;
        }

        // Timer state (user must stop the timer to select a tasting time)

        if (timerState == 1){
            mySurvey.setSurveyStatus (0) ;
            return -1;
        }

        // Survey values

        RadioGroup radioGroup_1_1 = (RadioGroup) findViewById(R.id.radioGroup_1_1);

        if (radioGroup_1_1.getCheckedRadioButtonId() == -1){
            mySurvey.setSurveyStatus (0) ;
            return -1;
        }


        //2- Fill class attributes

        mySurvey.setDate ((String) textView_date.getText()) ;
        mySurvey.setStartingTime ((String) textView_startingTime.getText()) ;
        mySurvey.setTastingTime ((String) textView_tastingTime.getText()) ;
        mySurvey.setLocation ((String) textView_location.getText()) ;
        mySurvey.setImage ((String) textView_imageName.getText()) ;

        return 0;
    }



} // End class
