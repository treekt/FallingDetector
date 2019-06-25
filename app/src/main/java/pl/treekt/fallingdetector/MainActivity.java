package pl.treekt.fallingdetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.treekt.fallingdetector.service.DetectorService;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.settings_button)
    Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupOnClickListener();
        requestPermission();
        runService();
    }

    private void setupOnClickListener() {
        settingsButton.setOnClickListener((View view) -> {
            Intent intentToOpenDetails = new Intent(this, ContactActivity.class);
            startActivity(intentToOpenDetails);
        });
    }

    private void runService() {
        Intent intent = new Intent(this, DetectorService.class);
        startService(intent);
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
