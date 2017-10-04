package com.example.android.drumkit;

import android.hardware.SensorListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;
import android.media.MediaPlayer;

import static android.R.attr.baseline;
import static android.R.attr.x;
import static android.view.View.Y;
import static android.view.View.Z;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    TextView azimuth, pitch, roll, base, soundType;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    //private Sensor mRotation;

    private float[] mAccelerometerReading = new float[3];
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientationAngles = new float[3];
    private float[] mMagnetometerReading = new float[3];

    private MediaPlayer bigbeat;
    private MediaPlayer drumbeat;

    int set = 0;
    String sound;
    boolean flickUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create Sensor Manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Accelerometer Sensor
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Magnetometer Sensor
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Register Sensor Listener
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);

        //Use TextViews
        azimuth = (TextView) findViewById(R.id.azimuth_text_view);
        pitch = (TextView) findViewById(R.id.pitch_text_view);
        roll = (TextView) findViewById(R.id.roll_text_view);
        base = (TextView) findViewById(R.id.baseline_text_view);
        soundType = (TextView) findViewById(R.id.sound_text_view);

        //Sets MediaPlayers
        bigbeat = MediaPlayer.create(this, R.raw.bigbeat);
        drumbeat = MediaPlayer.create(this, R.raw.drumbeat);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        }
        else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        }

        updateOrientationAngles();
        double baseline = 3.14;

        //Calculates rotation values in degrees
        double z = (mOrientationAngles[0]*180)/Math.PI;
        double x = -(mOrientationAngles[1]*180)/Math.PI;
        double y = (mOrientationAngles[2]*180)/Math.PI;

        //Determines a baseline (to set drums around)
        if(set == 0){
            //Change set if pitch < 22, and roll is between -20 and +25
            if (z != 0 && z < 180){
                set = 1;
                baseline = z;
            }
        }

        //Determines flick
        if (x > 45){
            flickUp = true;
        }
        if (flickUp && x < 30){
            flickUp = false;
            base.setText("Baseline: " + baseline);
            if(z >= 0){
                drumbeat.start();
            }
            else if (z < 0){
                bigbeat.start();
            }
        }

        //Displays rotation values
        azimuth.setText("Azimuth (Z): " + z);
        pitch.setText("Pitch (X): " + x);
        roll.setText("Roll (Y): " + y);
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        mSensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

        // "mOrientationAngles" now has up-to-date information.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
