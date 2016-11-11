package uc3mprojects.pablo.ex1aliamate;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class MainActivity extends AppCompatActivity {

// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ACTIVITY ATRIBUTES
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final int MEMORY_PERMISSION_REQUEST_CODE = 2;
    Intent intent;

    //String stringUrl= "http://localhost/webservice/read_DB.php";
    String stringUrl= "http://127.0.0.2/webservice/read_DB.php";

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

        // Connecting to the server to register a new agent

        findViewById(R.id.button_AddAgent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo= connMgr.getActiveNetworkInfo();

                if (networkInfo!= null && networkInfo.isConnected())
                {
                    new addAgent().execute(stringUrl);
                }
                else {// error message for no network available}

                    Toast.makeText(getBaseContext(), "NO NETWORK AVAILABLE", Toast.LENGTH_LONG).show();

                }

            }
        });

    } // end onCrete

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


    /**
     * To communicate with the server
     */

    private class addAgent extends AsyncTask <String, String, String>
    {
        //ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);

        URL url = null;

        @Override
        protected String doInBackground(String... urls) {

            //1- Enter URL address where your php file resides

            try {
                //url = new URL(urls[0]); // arg 0 is the URL
                //url = new URL("http://www.android.com/"); // arg 0 is the URL
                url = new URL("http://192.168.1.48/webservice/read_DB.php"); // arg 0 is the URL
            } catch (MalformedURLException e) {
                //Toast.makeText(getBaseContext(), "ERROR CREATING URL OBJECT", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            //2- Setup HttpURLConnection class to send and receive data from php and mysql and get the data

            try {

                // Configuration

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(1000 /*milliseconds*/);
                conn.setConnectTimeout(1500/*milliseconds*/);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                // Get data
                InputStream is = null;
                String contentAsString = "";
                int response = conn.getResponseCode();
                if (response == 200) {
                    is = conn.getInputStream();
                    conn.disconnect();

                    // Convert input string to string
                    String string_DB = "";
                    string_DB = inputStreamtoString (is);
                    System.out.println(string_DB);

                    return contentAsString;
                }
                else {conn.disconnect();}

                if (is != null) is.close();

                return contentAsString;
            }

             catch (IOException e) {
                 System.out.println("pablo_______");
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }


    // To convert input stream to string


    String inputStreamtoString (InputStream is) {

        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return total.toString();

    }


}   // end class MainActivity
