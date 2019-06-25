package pl.treekt.fallingdetector.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.Objects;

import pl.treekt.fallingdetector.ContactActivity;
import pl.treekt.fallingdetector.MainActivity;
import pl.treekt.fallingdetector.R;
import pl.treekt.fallingdetector.data.DetectorContract;

public class DetectorService extends Service implements SensorEventListener {

    private static final int STATIC_DELAY_COUNTER = 1;

    private final static int AXIS_X_INDEX = 0;
    private final static int AXIS_Y_INDEX = 1;
    private final static int AXIS_Z_INDEX = 2;

    private float currentAcceleration;
    private boolean isFreeFallStarted = false;
    private int sensorCounterDelay = 0;
    private float gravity[] = {0.0f, 0.0f, 0.0f};

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, 50000, new Handler());
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // low pass filter
        final float alpha = 0.8f;

        gravity[AXIS_X_INDEX] = alpha * gravity[AXIS_X_INDEX] + (1 - alpha) * event.values[AXIS_X_INDEX];
        gravity[AXIS_Y_INDEX] = alpha * gravity[AXIS_Y_INDEX] + (1 - alpha) * event.values[AXIS_Y_INDEX];
        gravity[AXIS_Z_INDEX] = alpha * gravity[AXIS_Z_INDEX] + (1 - alpha) * event.values[AXIS_Z_INDEX];

        float linearAccelerationX = event.values[AXIS_X_INDEX] - gravity[AXIS_X_INDEX];
        float linearAccelerationY = event.values[AXIS_Y_INDEX] - gravity[AXIS_Y_INDEX];
        float linearAccelerationZ = event.values[AXIS_Z_INDEX] - gravity[AXIS_Z_INDEX];

        currentAcceleration = getAccelerationSpeed(linearAccelerationX, linearAccelerationY, linearAccelerationZ);

//        acceleration = currentAcceleration - lastAcceleration;

        if (currentAcceleration <= 3) {
            isFreeFallStarted = true;
            sensorCounterDelay = 0;
        }

        if (isFreeFallStarted && sensorCounterDelay == STATIC_DELAY_COUNTER) {
            detectFalling();

        }

        sensorCounterDelay++;
    }

    private void detectFalling() {
        if (isFreeFallStarted) {
            if (currentAcceleration >= 20) {
                handleFallDetection();
                isFreeFallStarted = false;
            }
        }

        if (currentAcceleration > 3 && currentAcceleration < 20) {
            isFreeFallStarted = false;
        }
    }

    private float getAccelerationSpeed(float accelerationX, float accelerationY, float accelerationZ) {
        double xPow = Math.pow(accelerationX, 2);
        double yPow = Math.pow(accelerationY, 2);
        double zPow = Math.pow(accelerationZ, 2);

        return (float) Math.sqrt(xPow + yPow + zPow);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void handleFallDetection() {
        SingleShotLocationProvider.requestSingleUpdate(this, location -> {
            String message = new StringBuilder()
                    .append(location.getStreet())
                    .append(" ")
                    .append(location.getStreetNumber())
                    .append(" ")
                    .append(location.getCity())
                    .append(" ")
                    .append(location.getCountry())
                    .toString();

            showNotification(message);
            sendSmsMessage(message);
        });
    }

    private void showNotification(String message) {
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService
                (NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Test accelerometer")
                .setContentText(message)
                .setTicker("accelerometer")
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(101, notification);
    }

    private void sendSmsMessage(String smsMessage) {
        SharedPreferences preferences = getApplicationContext()
                .getSharedPreferences(ContactActivity.ACTIVITY_PREFS, MODE_PRIVATE);

        int phoneNumber = preferences.getInt(DetectorContract.DetectorEntry.COLUMN_NUMBER, 0);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(Integer.toString(phoneNumber), null,
                    this.getString(R.string.basic_fall_message) + smsMessage, null, null);
            Toast.makeText(this, R.string.detector_service_message_sent_toast, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }
}
