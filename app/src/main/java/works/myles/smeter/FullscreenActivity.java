package works.myles.smeter;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.util.Scanner;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class FullscreenActivity extends AppCompatActivity implements LocationListener {

    public int MY_PERMISSIONS_RECORD_AUDIO = 5;
    public AudioDispatcher dispatcher;
    public double m, b, test;
    public String stdmet = "mph";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Applies Design Layout
        setContentView(R.layout.activity_fullscreen);

        //Image objects
        ImageView spdl = (ImageView) findViewById(R.id.speedDial);
        ImageView rpmdl = (ImageView) findViewById(R.id.rpmDial);

        initMB();

        //Confirm Necessary Permissions to start metric gathering
        requestPermissions();

        //Needle onClick Listeners to start Metric Displays
        spdl.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startSpeedometer();
            }
        });
        rpmdl.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startRPM();
            }
        });

        fullDesign();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dispatcher.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        startRPM();
        startSpeedometer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!dispatcher.isStopped()) dispatcher.stop();
    }

    public void requestPermissions(){

        //Confirms fine location services permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        //Confirms coarse location services permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        //Confirms Audio permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
        }
    }

    /**
     * onRequestPermissionsResult(...)
     *
     * Task: Handling the results of permission request
     *
     * Parameters: intRequest (User defined permission code value),
     *             String permissions[] (Android defined permissions name/title such as CAMERA),
     *             int[] grantResults (User responses to the required permissions at runtime
     *             from Toast prompt)
     **/
     public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

         switch (requestCode) {

             case 1: {
                 // If request is cancelled, the result arrays are empty.
                 if (grantResults.length > 0
                         && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                     startSpeedometer();
                 } else {
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                 }
                 return;
             }

             case 2: {
                 // If request is cancelled, the result arrays are empty.
                 if (grantResults.length > 0
                 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                     startSpeedometer();
                 } else {
                 ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                 }
                 return;
             }

             case 5: {
                 // If request is cancelled, the result arrays are empty.
                 if (grantResults.length > 0
                         && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                     startRPM();
                 } else {
                     ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
                 }
                 return;
             }
         }
     }

    /**
     * statusCheck()
     *
     * Task: Confirms Location Services are enabled prompts user if services are disabled
     */
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    /**
     * buildAlertMessageNoGps()
     *
     * Task: Prompts user if services are disabled from statusCheck()
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, you need it enabled. Do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * startSpeedometer()
     *
     * Task: Manage location data and initiate permission access and location data collection on
     * users current speed and distance.
     */
    public void startSpeedometer() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        else {
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            this.onLocationChanged(null);

            statusCheck();
        }
    }

    /**
     * LocationListener Method Overrides
     * onLocationChanged(Location location)
     *
     * Task: Update Speedometer Needle and user visible text on current speed
     *
     * Other Methods unimplemented
     **/
    @Override
    public void onLocationChanged(Location location) {

        statusCheck();

        //Updated UI Elements
        TextView speedTxt = (TextView) this.findViewById(R.id.speed);
        ImageView speedNeedle = (ImageView) this.findViewById(R.id.speedDial);

        //If speed or location data hasn't change from previous position set's metrics to 0
        if(location == null){
            speedTxt.setText(" - - " + stdmet);
            speedNeedle.setRotation(190);
        }

        //Updates current speed if there's distance from most recent origin change
        else{

            double nCurrentSpeed = (double) location.getSpeed();

            //Converts and rounds speed to one's place mph
            if(stdmet.equals("mph")) {
                nCurrentSpeed *= 2.23694;
                nCurrentSpeed = Math.floor(nCurrentSpeed);
            }

            nCurrentSpeed =  Math.floor(nCurrentSpeed * 100) / 100; //Hundredths precision

            //Sets text and speed needle rotation to reflect current speed
            speedTxt.setText(" " + ((int) nCurrentSpeed) + " " + stdmet);
            speedNeedle.setRotation((float) (190 + (nCurrentSpeed * 1.5)));
        }
    }

    ///UNUSED ABSTRACT LOCATION METHOD IMPLEMENTATIONS
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}

    /**
     * startRPM()
     *
     * Task: Manage audio data and initiate permission access and audio data collection on
     * users current engine performance and reports collected data on display.
     */
    void startRPM(){

        //Confirms Audio permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
        }

        // Recursively updates/confirms permission
        // status before starting RPM processing
        else {

            //Builds Audio recorder and processor objects for third party library
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
            dispatcher.addAudioProcessor(
                    new PitchProcessor(
                            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                            22050, 1024, new PitchDetectionHandler() {

                @Override
                public void handlePitch(
                        PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                    final float pitchInHz = pitchDetectionResult.getPitch();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //Updated UI Elements
                            TextView rpmTxt = (TextView) findViewById(R.id.rpm);
                            ImageView rpmNeedle = (ImageView) findViewById(R.id.rpmDial);

                            //Converts and rounds speed to one's place mph
                            double rpmHz = (pitchInHz - b) / m; //from Tuning formula
                            if (test < 0) rpmHz = (pitchInHz * 60) / 4;
                            rpmHz =  Math.floor(rpmHz);

                            if(rpmHz < 0) rpmHz = 0;

                            //Sets text and rpm needle rotation to reflect current revolutions
                            rpmTxt.setText(" " + ((int) rpmHz) + " RPM");
                            if(rpmHz < 6000) {
                                rpmNeedle.setRotation((int) (58 ));
                            }
                            else { rpmNeedle.setRotation((int) (238)); }
                        }
                    });
                }
            }));
            new Thread(dispatcher, "Audio Dispatcher").start();
        }
    }

    void initMB(){
        try {
            Scanner scanner = new Scanner(this.openFileInput("config.txt"));
            m = Double.valueOf(scanner.next());
            b = Double.valueOf(scanner.next());
        }
        catch (Exception e) {
            e.printStackTrace();
            b = 0;
            m = 1;
            test = -1;
        }
    }

    void fullDesign(){

        //Find Shared Preferences Settings from Menu
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean sensors = prefs.getBoolean("showSensors", true);
        boolean rpms = prefs.getBoolean("showRPM", true);
        boolean mphKmHr = prefs.getBoolean("showMPHKMHr", true);
        boolean stdORmet = prefs.getBoolean("MPHKmHr", false);

        //Check user toggled visual preferences, change additional graphic visibility
        if (!sensors) {
            LinearLayout tp = findViewById(R.id.tp);
            LinearLayout bp = findViewById(R.id.bp);
            tp.setVisibility(LinearLayout.GONE);
            bp.setVisibility(LinearLayout.GONE);
        }

        if(!rpms){
            TextView revs = findViewById(R.id.rpm);
            revs.setVisibility(TextView.GONE);
        }

        if(!mphKmHr){
            TextView speed = findViewById(R.id.speed);
            speed.setVisibility(TextView.GONE);
        }

        if(stdORmet){stdmet = "km/h";}
    }
}