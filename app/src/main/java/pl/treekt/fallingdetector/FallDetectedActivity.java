package pl.treekt.fallingdetector;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.dinuscxj.progressbar.CircleProgressBar;
import pl.treekt.fallingdetector.data.DetectorContract;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class FallDetectedActivity extends AppCompatActivity {

    @BindView(R.id.line_progress)
    CircleProgressBar circleProgressBar;
    @BindView(R.id.information_contact_recipent)
    TextView infoContactRecipentTextView;
    @BindView(R.id.false_alarm_btn)
    Button falseAlarmButton;

    private Integer maxTime;
    private String messageToContact;
    private int phoneNumber;

    private final int[] progressValue = {0};
    private String locationMessage;
    private Timer timer;

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detected);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.countingToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SharedPreferences preferences = getSharedPreferences(MainActivity.DETECTOR_PREFS, MODE_PRIVATE);
        maxTime = Integer.parseInt(preferences.getString(getString(R.string.settings_preference_time_key), getResources().getString(R.string.default_counting_time)));
        messageToContact = preferences.getString(getString(R.string.settings_preference_message_key), this.getString(R.string.default_contact_message));

        circleProgressBar.setProgressFormatter((progress, max) -> String.format("%d", (int) ((float) progress / (float) max * maxTime)));
        circleProgressBar.setMax(maxTime);
        timer = startProgressBarProcess();

        phoneNumber = preferences.getInt(DetectorContract.DetectorEntry.COLUMN_PHONE_NUMBER, 0);
        String fullName = preferences.getString(DetectorContract.DetectorEntry.COLUMN_FIRSTNAME, "FIRST LAST NAME");

        infoContactRecipentTextView.setText(getString(R.string.information_timer_recipent) + " "  + fullName + " na numer " + phoneNumber);
        falseAlarmButton.setOnClickListener(v -> cancelProgressBarProcess());
        locationMessage = getIntent().getStringExtra("locationMessage");
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
    }


    private Timer startProgressBarProcess() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                progressValue[0]++;
                runOnUiThread(() -> circleProgressBar.setProgress(progressValue[0]));
                if(progressValue[0] == maxTime){
                    runOnUiThread(() -> {
                        disableFalseAlarmButton();
                        sendSmsMessage();
                    });
                    cancel();
                }
            }
        }, 0, 1000);
        return timer;
    }

    private void cancelProgressBarProcess(){
        if(progressValue[0] < 60){
            timer.cancel();
            disableFalseAlarmButton();
        }
    }

    private void disableFalseAlarmButton(){
        falseAlarmButton.setBackground(getDrawable(R.color.colorDisabled));
        falseAlarmButton.setText(getString(R.string.counting_canceled));
        falseAlarmButton.setClickable(false);
    }

    private void sendSmsMessage() {
        try {
            String message = this.getString(R.string.basic_fall_message) + " " + locationMessage + ".\n" + messageToContact;
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> messageParts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(Integer.toString(phoneNumber), null, messageParts, null, null);
            Toast.makeText(this, this.getString(R.string.detector_service_message_sent_toast), Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }






}
