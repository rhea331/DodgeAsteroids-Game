package ryanh.asteroids;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;



public class AsteroidActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor accelerometer;

    GameView mGameView;

    double defaultAngle = 0;
    double xzAngle = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asteroid);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mGameView = findViewById(R.id.gameView);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        initListeners();

    }

    public void initListeners(){
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    public void onDestroy(){
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mSensorManager.unregisterListener(this);
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        initListeners();
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        System.out.println("paused");
        if(mGameView.getState() == GameState.PLAYING) {
            mGameView.pauseGame();
        }
        mSensorManager.unregisterListener(this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(mGameView.getState() != GameState.PLAYING) {
            defaultAngle = xzAngle;
            mGameView.start();
        }
        return true;
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float aX= event.values[0];
            float aZ= event.values[2];
            xzAngle = Math.atan2(aX, aZ)/(Math.PI/180);
            if (mGameView.getState() == GameState.PLAYING){
                if (xzAngle < defaultAngle-10){
                    mGameView.moveShip(true);
                }else if(xzAngle > defaultAngle+10){
                    mGameView.moveShip(false);
                }
            }
            /*
            xGravity = event.values[0];
            float ZGravity = event.values[2];
            System.out.println("x: "+event.values[0]+"\t y: "+event.values[1]+"\t z: "+event.values[2]);
            if (mGameView.getState() == GameState.PLAYING){
                if (xGravity < defaultXGravity-1){
                    mGameView.moveShip(true);
                }else if(xGravity > defaultXGravity+1){
                    mGameView.moveShip(false);
                }
            }*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

}
