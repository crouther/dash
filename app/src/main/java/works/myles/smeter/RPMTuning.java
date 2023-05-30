package works.myles.smeter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.Manifest;
import android.os.Bundle;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class RPMTuning extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //Class Scope Variables
    public int MY_PERMISSIONS_RECORD_AUDIO = 5;
    public AudioDispatcher dispatcher;

    public ImageView rpmdl, rpmfr, rpmli;
    public TextView rpm, readOut;
    public String currentRPM = " ";

    public RPM rev = new RPM();
    public Spinner spin;

    public boolean testFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpmtuning);

        //Graphic Variable Declarations
        rpmdl = (ImageView) findViewById(R.id.rpmDial);
        rpmfr = (ImageView) findViewById(R.id.rpmFrameCali);
        rpmli = (ImageView) findViewById(R.id.rpmLinesCali);
        rpm = (TextView) findViewById(R.id.rpm);
        readOut = (TextView) findViewById(R.id.readOut);

        spin = (Spinner) findViewById(R.id.rpmSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, rev.revolutions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setOnItemSelectedListener(this);

        startRPM();

        //Reset Hz Gauge on Needle Tap
        rpmdl.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startRPM();
            }
        });

        //Records Hz reading on TextView Selection
        rpm.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                int testValue;

                try {
                    testValue = 0;
                }
                catch (Exception e){ testValue = 0; }

                readOut.setText("Recording " + testValue + "\nas " + currentRPM + " RPMs");
            }
        });
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!dispatcher.isStopped()) dispatcher.stop();
        rev.save("config.txt", this);
        if(!testFinished) System.exit(0);
    }

    /** Additional Methods **/
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
        currentRPM = arg0.getItemAtPosition(pos).toString();
        Toast.makeText(getApplicationContext(), "Revolutions (Hz): "+arg0.getItemAtPosition(pos).toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO - Custom Code
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        finish();
        startActivity(getIntent());
    }

    /**
     * startRPM()
     *
     * Task: Manage audio data and initiate permission access and audio data collection on
     * users current engine performance and reports collected data on display.
     */
    void startRPM(){

        try { Thread.sleep(200); }
        catch (InterruptedException e) { e.printStackTrace(); }

        //Confirms Audio permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
        }
        //Starts Audio Processing if permissions are approved by user
        else {

            //Builds Audio recorder and processor objects for third party library
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
            dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                        AudioEvent audioEvent) {
                    final float pitchInHz = pitchDetectionResult.getPitch();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            //Converts and rounds speed to one's place mph
                            double rpmHz = pitchInHz;
                            rpmHz =  Math.floor(rpmHz);

                            if(rpmHz < 0){}
                            else {
                                //Sets text and rpm needle rotation to reflect current revolution
                            }
                        }
                    });
                }
            }));

            //Initializes Audio Processing Thread
            new Thread(dispatcher, "Audio Dispatcher").start();
        }
    }

    public void charts(View view)
    {
        testFinished = true;
        Intent intent = new Intent(RPMTuning.this, tuningChart.class);
        startActivity(intent);
    }
}