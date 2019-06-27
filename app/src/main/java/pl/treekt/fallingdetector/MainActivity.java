package pl.treekt.fallingdetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import pl.treekt.fallingdetector.data.DetectorContract;
import pl.treekt.fallingdetector.service.DetectorService;

public class MainActivity extends AppCompatActivity {

    public static final String DETECTOR_PREFS = "DetectorPreferences";

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

        PreferenceManager.setDefaultValues(this, MainActivity.DETECTOR_PREFS, MODE_PRIVATE, R.xml.preferences, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupOnClickListener() {
        stateDetectorButton.setOnClickListener(v -> changeDetectorState(false));
        contactsButton.setOnClickListener((View view) -> {
            Intent intentContacts = new Intent(this, ContactActivity.class);
            startActivity(intentContacts);
        });
    }

    private void changeDetectorState(boolean onlyDecoration) {
        SharedPreferences preferences = getSharedPreferences(DETECTOR_PREFS, MODE_PRIVATE);

        boolean isDetectorRunning = isServiceRunning(DetectorService.class);
        boolean isSelectedContact = preferences.getInt(DetectorContract.DetectorEntry.COLUMN_PHONE_NUMBER, 0) != 0;

        if (isDetectorRunning) {
            if (onlyDecoration) {
                changeDetectorStateButton(1);
            } else {
                stopService();
                changeDetectorStateButton(0);
            }
        } else {
            if (onlyDecoration) {
                changeDetectorStateButton(0);
            } else {
                if(isSelectedContact){
                    runService();
                    changeDetectorStateButton(1);
                }else {
                    Toast.makeText(this, getString(R.string.contact_not_selected_information), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void changeDetectorStateButton(int state) {
        if (state == 0) {
            stateDetectorButton.setText(getString(R.string.enable_detector));
            stateDetectorButton.setBackground(getDrawable(R.color.colorPrimary));
        } else if (state == 1) {
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
