package pl.treekt.fallingdetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.treekt.fallingdetector.data.DetectorContract;
import pl.treekt.fallingdetector.service.DetectorService;

public class MainActivity extends AppCompatActivity {

    public static final String ACTIVITY_PREFS = "MainPreferences";

    @BindView(R.id.contacts_button)
    Button contactsButton;
    @BindView(R.id.state_detector_button)
    Button stateDetectorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupOnClickListener();
        requestPermission();
        changeDetectorState(true);
    }


    private void setupOnClickListener() {
        stateDetectorButton.setOnClickListener(v -> changeDetectorState(false));
        contactsButton.setOnClickListener((View view) -> {
            Intent intentToOpenDetails = new Intent(this, ContactActivity.class);
            startActivity(intentToOpenDetails);
        });
    }

    private void changeDetectorState(boolean onlyDecoration){
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = getSharedPreferences(ACTIVITY_PREFS, MODE_PRIVATE).edit();
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(ACTIVITY_PREFS, MODE_PRIVATE);
        int state = preferences.getInt("detectorState", 0);
        if(state == 0){
            if(!onlyDecoration) {
                runService();
                editor.putInt("detectorState", 1).apply();
                changeDetectorStateButton(1);
            }else{
                changeDetectorStateButton(0);
            }
        }else if(state == 1){
            if(!onlyDecoration) {
                editor.putInt("detectorState", 0).apply();
                stopService();
                changeDetectorStateButton(0);
            }else{
                changeDetectorStateButton(1);
            }
        }
    }

    private void changeDetectorStateButton(int state){
        if(state == 0) {
            stateDetectorButton.setText(getString(R.string.enable_detector));
            stateDetectorButton.setBackground(getDrawable(R.color.colorPrimary));
        }else if(state == 1){
            stateDetectorButton.setText(getString(R.string.disable_detector));
            stateDetectorButton.setBackground(getDrawable(R.color.colorDisabled));
        }
    }

    private void runService() {
        Intent intent = new Intent(this, DetectorService.class);
        startService(intent);
    }
    private void stopService() {
        Intent intent = new Intent(this, DetectorService.class);
        stopService(intent);
    }

    @SuppressLint("NewApi")
    private void requestPermission() {
        String[] permissions = new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 10);
            }
        }
    }
}
