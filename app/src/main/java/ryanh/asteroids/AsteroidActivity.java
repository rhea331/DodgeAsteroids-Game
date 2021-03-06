package ryanh.asteroids;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * AsteroidActivity.java
 * Sets up the layout of the screen, and the touch/accelerometer listeners.
 */

public class AsteroidActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor accelerometer;

    GameView mGameView;

    //variables for the phone angle.
    private static int MAX_ANGLETILT = 20;
    double defaultAngle;
    double xzAngle = 0;


    /**
     * Invoked when activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asteroid);

        //sets to landscape only.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Sets up the surfaceview
        mGameView = findViewById(R.id.gameView);

        //Gets the accelerometer and sets it up.
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        initListeners();

    }

    /**
     * Sets up the accelerometer.
     */
    public void initListeners(){
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

    }

    /**
     * Invoked when the activity is destroyed, unregisters the accelerometer.
     */
    @Override
    public void onDestroy(){
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    /**
     * Invoked when the back button is pressed, unregisters the accelerometer.
     */
    @Override
    public void onBackPressed() {
        mSensorManager.unregisterListener(this);
        super.onBackPressed();
    }

    /**
     * Invoked when the activity is being resumed, initializes the accelerometer.
     */
    @Override
    public void onResume() {
        initListeners();
        super.onResume();
    }


    /**
     * Invoked when the activity loses focus, unregisters the accelerometer and pauses the game.
     */
    @Override
    protected void onPause(){
        super.onPause();
        if(mGameView.getState() == GameState.PLAYING) {
            mGameView.pauseGame();
        }
        mSensorManager.unregisterListener(this);

    }

    /**
     * Invoked when the screen has been touched. If game is not currently playing, starts the game
     * @param event the touch event
     * @return always true
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(mGameView.getState() != GameState.PLAYING) {
            mGameView.start();
        }
        return true;
    }

    /**
     * Invoked when there is a change in an Sensor. Only care when it is the accelerometer
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //Uses x and z co-ordinates to make an angle to determine tilt
            float aX= event.values[0];
            float aZ= event.values[2];
            if (mGameView.getState() == GameState.PLAYING){
                //if playing, determines how far the screen is tilted from the 'default' angle.
                //PROBLEM: this does not like it when the phone is facing straight down
                xzAngle = Math.atan2(aX, aZ)/(Math.PI/180);
                double difference = ensureRange((int)(xzAngle- defaultAngle), -MAX_ANGLETILT, MAX_ANGLETILT);
                mGameView.moveShip(difference/MAX_ANGLETILT );
            } else{
                defaultAngle = Math.atan2(aX, aZ)/(Math.PI/180);
            }
        }
    }

    public int ensureRange( int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }


    //eh, aiming is overrated
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

}
