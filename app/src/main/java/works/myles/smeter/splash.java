package works.myles.smeter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class splash extends AppCompatActivity {

    public FullscreenActivity act = new FullscreenActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Applies Design Layout
        setContentView(R.layout.activity_splash);

        //Confirms Audio permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, act.MY_PERMISSIONS_RECORD_AUDIO);
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Confirms coarse location services permissions
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                    }

                }
                else {ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); }
                return;
            }

            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {recreate();}
                else {ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2); }
                return;
            }

            case 5: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Confirms fine location services permissions
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }

                }
                else {ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, act.MY_PERMISSIONS_RECORD_AUDIO);}
                return;
            }
        }
    }

    public void startCluster(View view)
    {
        Intent intent = new Intent(splash.this, FullscreenActivity.class);
        startActivity(intent);
    }

    public void settings(View view)
    {
        Intent intent = new Intent(splash.this, SettingsActivity.class);
        startActivity(intent);
    }

}