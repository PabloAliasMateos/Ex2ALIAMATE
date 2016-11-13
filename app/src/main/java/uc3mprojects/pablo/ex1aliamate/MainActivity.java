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
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity {

// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ACTIVITY ATRIBUTES
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final int MEMORY_PERMISSION_REQUEST_CODE = 2;
    Intent intent;

    // SERVER CONFIGURATION (IP + FILES)

    private String IP = "192.168.1.48";
    private String userName;
    private String userPass;
    private String serverIP;

    //String stringUrl= "http://localhost/webservice/read_DB.php";
    private String stringUrl_agents_read= "http://"+IP+"/webservice/read_DB_agents.php";
    private String stringUrl_agents_write= "http://"+IP+"/webservice/write_DB_agents.php";
    private String stringUrl_users_read= "http://"+IP+"/webservice/read_DB_users.php";
    private String stringUrl_users_write= "http://"+IP+"/webservice/write_DB_users.php";
    private String stringUrl_login= "http://"+IP+"/webservice/login.php";

    // VIEWS

    private EditText editText_register_agent_ID;
    private EditText editText_userName;
    private EditText editText_userPass;
    private EditText editText_serverIP;

    // VARIABLES

    private String agentID;
    private String string_DB_agents;
    private int flag_asinctasks = 0;




// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ACTIVITY LIFE-CYCLE METHODS
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // 1 // CREATE

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
       this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //BUTTON1 Calling survey activity clicking on the proper button

        findViewById(R.id.button_StartSurvey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SurveyActivity.class);  // To bind the main activity with other activity
                startActivity(intent);
            }
        });

        //BUTTON2 Calling view survey activity clicking on the proper button

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

        //BUTTON3 Connecting to the server to register a new agent

        findViewById(R.id.button_AddAgent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo= connMgr.getActiveNetworkInfo();

                if (networkInfo!= null && networkInfo.isConnected())
                {
                    editText_register_agent_ID = (EditText) findViewById(R.id.editText_register_agent_ID);
                    agentID = String.valueOf(editText_register_agent_ID.getText());

                    if (!agentID.equals("Agent ID") ) {

                        //new addAgent().execute(stringUrl_agents_write);
                        new newAgentRequest().execute (stringUrl_agents_read);

                    }

                    else
                        Toast.makeText(getBaseContext(), "Please, insert a valid Agent ID", Toast.LENGTH_LONG).show();
                   // new readAgentTable().execute(stringUrl_agents_read);
                }
                else {// error message for no network available}

                    Toast.makeText(getBaseContext(), "NO NETWORK AVAILABLE", Toast.LENGTH_LONG).show();

                }

            }
        });

        //BUTTON4 Login and checking that server is available and user knows database password

        findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo= connMgr.getActiveNetworkInfo();

                if (networkInfo!= null && networkInfo.isConnected())
                {

                    //1 get values introduced by user

                    editText_serverIP = (EditText) findViewById(R.id.editText_serverIP);
                    editText_userName = (EditText) findViewById(R.id.editText_userName);
                    editText_userPass = (EditText) findViewById(R.id.editText_userPass);

                    userName = String.valueOf(editText_userName.getText());
                    userPass = String.valueOf(editText_userPass.getText());
                    serverIP = String.valueOf(editText_serverIP.getText());

                    //2 Verifications

                    if (!userName.equals("User Name") && !userPass.equals("Password") && !serverIP.equals("Server IP")) {

                        //new addAgent().execute(stringUrl_agents_write);
                        //new newAgentRequest().execute (stringUrl_agents_read);

                        //3 Calling server php web service LOGIN file
                        stringUrl_login= "http://"+serverIP+"/webservice/login.php";
                        new login().execute(stringUrl_login);

                    }

                    else
                        Toast.makeText(getBaseContext(), "Please, complete all the fields", Toast.LENGTH_LONG).show();
                    // new readAgentTable().execute(stringUrl_agents_read);
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


    // =======================================================================================================
    // SERVER FUNCTIONALITIES
    // =======================================================================================================

    private class login extends AsyncTask <String, String, String>
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

                // Send LOGIN info

                // Set output stream
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

                // option 1

                /*
                wr.writeBytes("AgentID="+agentID);
                wr.flush();
                wr.close();
                */

                // option 2 (JSON)

                //Create JSONObject here
                JSONObject jsonParam = new JSONObject();
                try {
                    jsonParam.put("serverIP", serverIP);
                    jsonParam.put("userName", userName);
                    jsonParam.put("userPass", userPass);
                    System.out.println(jsonParam.toString());
                    //wr.writeBytes(URLEncoder.encode(jsonParam.toString(),"UTF-8"));
                    wr.writeBytes(jsonParam.toString());
                    wr.flush();
                    wr.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Read answer sent from php file
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
                    System.out.println(response.toString());
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
            return null; }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println(result);
            String aux = result.toString();
            // Check if it has been possible that php file connects to the server with the input values (username, password and server IP address)
            if (result.equals("Successful\r"))
                Toast.makeText(getBaseContext(), "LOGIN SUCCESSFUL", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getBaseContext(), "FAILED TO CONNECT TO THE SERVER (invalid login)", Toast.LENGTH_LONG).show();
        }
    }



    /**
     * To communicate with the server. Read agent table content using php file (server side)
     */

    private class readAgentTable extends AsyncTask <String, String, String>
    {
        //ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);

        URL url = null;

        @Override
        protected String doInBackground(String... urls) {

            //1- Enter URL address where your php file resides

            try {
                //url = new URL(urls[0]); // arg 0 is the URL
                //url = new URL("http://www.android.com/"); // arg 0 is the URL
                url = new URL(urls[0]); // arg 0 is the URL
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

                    string_DB_agents = inputStreamtoString (is);
                    System.out.println(string_DB_agents);

                    return contentAsString;
                }
                else {conn.disconnect();}

                if (is != null) is.close();

                return contentAsString;
            }

             catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }
    }


    /**
     * To communicate with the server. Add new row to DB agent table using php file (server side)
     */

    private class addAgent extends AsyncTask <String, String, String>
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
                // Send Agent info

                wr.writeBytes("AgentID="+agentID);
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
            return null; }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
        }
    }



    /**
     * To communicate with the server. Evaluate if there is an agent with the same name already stored
     */

    private class newAgentRequest extends AsyncTask <String, String, String>   // Check database + add if there is no agent with the same ID
    {
        //ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);

        URL url = null;

        @Override
        protected String doInBackground(String... urls) {

            //1- Enter URL address where your php file resides

            try {
                //url = new URL(urls[0]); // arg 0 is the URL
                //url = new URL("http://www.android.com/"); // arg 0 is the URL
                url = new URL(urls[0]); // arg 0 is the URL
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

                    string_DB_agents = inputStreamtoString (is);
                    System.out.println(string_DB_agents);

                    return string_DB_agents;
                }
                else {conn.disconnect();}

                if (is != null) is.close();

                return string_DB_agents;
            }

            catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //System.out.println(result);
            Toast.makeText(getBaseContext(), "\""+agentID+"\"", Toast.LENGTH_LONG).show();
            /*
            if (result.contains("\""+agentID+"\""))
                System.out.println("Coincidencia");
            else
                System.out.println("NO Coincidencia");*/

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
