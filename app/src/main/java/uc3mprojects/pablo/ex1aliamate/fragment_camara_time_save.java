package uc3mprojects.pablo.ex1aliamate;

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



/**
 * Created by Pablo Alías Mateos on 02/10/2016.
 * Description: This is the first Android exercise
 */

public class fragment_camara_time_save extends Fragment {

    public static final int REQUEST_CAMERA = 10;                 // without extends Fragment, it would be just a void java class
    public static final int IMAGE_PERMISSION_REQUEST_CODE = 1;
    public static final int SURVEY_PERMISSION_REQUEST_CODE = 2;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 3;

    private View fragment_view;
    private View fragment_survey_view;
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
    private int clockButton_animationState = 0;
    Timer T;
    private int seconds_counter = 0;
    private int minutes_counter = 0;

    // ========================================================================================================================================
    // LIFE-CYCLE METHODS
    // ========================================================================================================================================

    /**
     * Creates and returns the view hierarchy associated with the fragment
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragment_view = inflater.inflate(R.layout.fragment_camera_time_save, container);     // to inflate fragment xml code
        fragment_survey_view = inflater.inflate(R.layout.fragment_survey, container);     // to inflate fragment xml code
        imageView_survey_picture = (ImageView) fragment_view.findViewById(R.id.imageView_survey_picture);  // All methods can access to imageView_survey_picture view

        dataInitialization();

        // LOCATION

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

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

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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
                Toast.makeText(getActivity(), "PERMISSION DENIED: Can not access the GPS. ", Toast.LENGTH_LONG).show();
        }


        // ANIMATION to make blik chronometer button. It will be stopped once clock button is pressed

        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(700); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        imageButton_clock = (ImageButton) fragment_view.findViewById(R.id.imageButton_clock);
        imageButton_clock.startAnimation(animation);
        clockButton_animationState = 1;

        // TIMER to count tasting time

        startTimer_tastingTime();

        // CAMERA BUTTON: Calling camera from fragment
        fragment_view.findViewById(R.id.imageButton_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeCamera();
            }
        });

        // CHRONOMETER BUTTON
        imageButton_clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clockButton_animationState == 1) {
                    T.cancel();
                    imageButton_clock.clearAnimation();
                    clockButton_animationState = 0;
                } else {
                    startTimer_tastingTime();
                    imageButton_clock.startAnimation(animation);
                    clockButton_animationState = 1;
                }
            }
        });

        // SAVE BUTTON
        fragment_view.findViewById(R.id.imageButton_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveSurvey();
                } else {
                    //permission failed, request
                    String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                       // Check the current SDK target version (run-time permissions => API 23)
                        requestPermissions(permissionRequest, SURVEY_PERMISSION_REQUEST_CODE);
                    } else
                        Toast.makeText(getActivity(), "PERMISSION DENIED: Can not access the memory. ", Toast.LENGTH_LONG).show();
                }

            }
        });

        return fragment_view;
    }

    /**
     * Allows the fragment to clean up resources associated with its view
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i ("VIVZ","destroying fragment view. Releasing resources");
        T.cancel();     // release timer resource
    }

// ========================================================================================================================================
    // OVERRIDE METHODS
    // ========================================================================================================================================

    // Method to store automatically the result when the camera native activity is called
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);      // data content the image in this case. This method wil receive a request code, in order to filter who has invoked it

        if (requestCode == REQUEST_CAMERA) {
            // We are hearing from the camera (this method only is valid if we do not specify the pat to store the image)
            //Bitmap cameraImage = (Bitmap) data.getExtras().get("data"); // accessing to the image
            //imageView_survey_picture.setImageBitmap(cameraImage);       // The image that has been captured will be shown in the fragment
            // Permission management for android 6.0: READ_STORAGE
            // ActivityCompat to be able to check permissions from a fragment
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showImageCaptured();
            } else {
                //permission failed, request
                String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {           // Check the current SDK target version (run-time permissions => API 23)
                    requestPermissions(permissionRequest, IMAGE_PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

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

            case IMAGE_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    showImageCaptured();
                else
                    Toast.makeText(getActivity(), "PERMISSION DENIED: Can not save the image. ", Toast.LENGTH_LONG).show();

                return;

            case SURVEY_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    saveSurvey();
                else
                    Toast.makeText(getActivity(), "PERMISSION DENIED: Can not save the survey. ", Toast.LENGTH_LONG).show();

                return;

            case LOCATION_PERMISSION_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), "PERMISSION DENIED: Can not access GPS ", Toast.LENGTH_LONG ).show();
                        return;
                    }
                    // To show the last known location until the new value is gotten
                    String locationProvider = LocationManager.GPS_PROVIDER;
                    Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                    showNewLocation(lastKnownLocation);
                    locationManager.requestLocationUpdates("gps", 5000, 5, locationListener); // The location will be get from gps, it will be checked every 5 seconds or when user moves 5 meters
                    return;


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
        textView_imageName =  (TextView) fragment_view.findViewById(R.id.textView_value_imgGalleryName);
        textView_imageName.setText(imageName);

    }

    /**
     *  To rotate a bitmap
     */

    private static Bitmap rotateImage(Bitmap src, float degree)
    {
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
        textView_date =  (TextView) fragment_view.findViewById(R.id.textView_value_date);
        textView_date.setText(date);

        // STARTING TIME
        sdf = new SimpleDateFormat("HH:mm:ss");
        String startingTime = sdf.format(new Date());
        textView_startingTime =  (TextView) fragment_view.findViewById(R.id.textView_value_starting_time);
        textView_startingTime.setText(startingTime);

        // TASTING TIME
        String tastingTime = "00:00:00";
        textView_tastingTime =  (TextView) fragment_view.findViewById(R.id.textView_value_tasting_time);
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
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String tastingTime_min_sec;
                        textView_tastingTime =  (TextView) fragment_view.findViewById(R.id.textView_value_tasting_time);
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
     * To save the survey
     */

    private void saveSurvey() {


        //1- Save survey information into txt file
        try {
            SurveyInformation mySurvey = new SurveyInformation ();      // class to store survey values
            readSurveyValues(mySurvey);                                 // Read the survey values
            directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File survey_info = new File (directory,"EX1_2016_USERS.txt");
            if (survey_info.exists()){
                FileWriter survey_info_writer = new FileWriter(survey_info, true); // true => not overwrite the previous content
                survey_info_writer.append(System.getProperty("line.separator") + "Pablo");
                survey_info_writer.flush();
                survey_info_writer.close();
            }
            else {
                FileWriter survey_info_writer = new FileWriter(survey_info, true); // true => not overwrite the previous content
                survey_info_writer.append("Pablo");
                survey_info_writer.flush();
                survey_info_writer.close();
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

        mySurvey.setSurveyStatus (1) ;

        //1- Check if all the fields are filled

        // Header values
        textView_imageName = (TextView) fragment_view.findViewById(R.id.textView_value_imgGalleryName);
        textView_date = (TextView) fragment_view.findViewById(R.id.textView_value_date);
        textView_startingTime = (TextView) fragment_view.findViewById(R.id.textView_value_starting_time);
        textView_tastingTime = (TextView) fragment_view.findViewById(R.id.textView_value_tasting_time);
        textView_location = (TextView) fragment_view.findViewById(R.id.textView_value_location);

        if (textView_imageName.getText().equals("-") || textView_date.getText().equals("-") || textView_startingTime.getText().equals("-") || textView_tastingTime.getText().equals("-") || textView_location.getText().equals("-")) {
            mySurvey.setSurveyStatus (0) ;
            return -1;
        }

        // Survey values

        RadioGroup g = (RadioGroup) fragment_survey_view.findViewById(R.id.radioGroup_1_1);
        TextView texttext = (TextView) fragment_survey_view.findViewById(R.id.textView_group_1);

        if (g.getCheckedRadioButtonId() != -1){
            int selected = g.getCheckedRadioButtonId();
            System.out.println(selected);
        }
        else
        {
            texttext.setText("Pablo");
            System.out.println("No radiobutton is selected");
        }


        //2- Fill class attributes
        mySurvey.setSurveyStatus (0) ;
        mySurvey.setDate ("string") ;
        mySurvey.setStartingTime ("string") ;
        mySurvey.setTastingTime ("string") ;
        mySurvey.setLocation ("string") ;
        mySurvey.setImage ("string") ;

        return 0;
    }

    /**
     *
     * To show the location value in the proper textView
     */

    private void showNewLocation(Location location) {

        textView_location =  (TextView) fragment_view.findViewById(R.id.textView_value_location);
        textView_location.setText(String.format("%.4f",location.getLatitude())+" - "+ String.format("%.4f",location.getAltitude()));

    }

} // End class
