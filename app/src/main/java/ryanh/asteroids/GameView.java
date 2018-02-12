package ryanh.asteroids;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

import java.lang.Math;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    private final int TotalAsteroids = 20;
    private final double acceleration = 1.05;
    private static final String MY_PREFS_NAME = "Highscore";

    private Context context;
    private Random rand;

    private ArrayList<AsteroidObject> asteroids;
    private Ship ship;

    private GameState state = GameState.DEFAULT;

    private SurfaceHolder holder;
    private Thread thread;
    private int mCanvasWidth, mCanvasHeight;

    private int activeAsteroids = 1;
    private int counter = 0;
    private int lives = 3;
    private int score = 0;
    private int highScore = 0;
    private double speed = 2.0;


    public GameView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        holder = getHolder();
        holder.addCallback(this);
        ship = new Ship(-20,0);

        rand = new Random();
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, 0);
        highScore = prefs.getInt("highscore", 0);
    }

    public void doDraw(Canvas canvas) {
        Paint paint = new Paint();

        paint.setColor(Color.BLACK);
        canvas.drawRect(0,0,mCanvasWidth,mCanvasHeight, paint);

        ship.doDraw(canvas);

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        switch (state){
            case DEFAULT:
                canvas.drawText("TOUCH SCREEN TO START GAME", mCanvasWidth/2, mCanvasHeight/2, paint);
                break;
            case PLAYING:
                for (int i=0; i<activeAsteroids;i++){
                    asteroids.get(i).doDraw(canvas);
                }
                break;
            case GAMEOVER:
                canvas.drawText("GAME OVER   TOUCH SCREEN TO RESTART", mCanvasWidth / 2, mCanvasHeight / 2, paint);
                break;
        }


        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Lives: "+lives, 10,mCanvasHeight - 10, paint);
        canvas.drawText("Score: "+score, 10, 10, paint);
        canvas.drawText("High score: "+ highScore, 10, 20, paint);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        unpauseGame();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
        setSurfaceSize(w, h);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        pauseGame();
    }

    public void setSurfaceSize(int width, int height){
        mCanvasWidth = width;
        mCanvasHeight = height;
    }

    @Override
    public void run(){
        Thread myThread = Thread.currentThread();
        try {
            while(thread== myThread) {
                if (state == GameState.PLAYING) {
                    update();
                }
                Canvas canvas = holder.lockCanvas();
                doDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                Thread.sleep(10);
            }
        }catch(InterruptedException e){}
    }


    public void update(){
        for(int i=0; i<activeAsteroids; i++){
            AsteroidObject asteroid = asteroids.get(i);
            if(getDistance(ship.getX(), ship.getY(), asteroid.getX(), asteroid.getY()) <  mCanvasHeight/20){
                lives-=1;
                if(lives <= 0){
                    gameOver();
                    return;
                }
                speed=2;
                asteroid.setX(-mCanvasWidth/4);
            }
            if(asteroid.getX() < 0){
                asteroid.setX(mCanvasWidth+15);
                asteroid.setY(rand.nextInt(mCanvasHeight));
                score+=1;
            }else{asteroid.setX(asteroid.getX()-(int)speed);}
        }
        if(activeAsteroids < TotalAsteroids ){
            int gap = mCanvasWidth / 20;
            int variance = rand.nextInt(gap/2 + 1 + gap/2) - gap/2;
            if (asteroids.get(activeAsteroids-1).getX() < mCanvasWidth - gap + variance){
                activeAsteroids+=1;
            }

        }
        counter+=1;
        if(counter == 20) {
            if(speed < 10) {
                speed = speed * acceleration;
            }
            counter = 0;
        }
    }
    public void start() {
        score = 0;
        lives = 3;
        speed=2;
        activeAsteroids = 1;
        ship.setX(15);
        ship.setY(mCanvasHeight/2);
        asteroids = new ArrayList<AsteroidObject>();
        for (int i = 0; i < TotalAsteroids; i++) {
            asteroids.add(new AsteroidObject(mCanvasWidth+15, rand.nextInt(mCanvasHeight)));
        }
        state = GameState.PLAYING;
        thread = new Thread(this);
        thread.start();
    }

    public void moveShip(final boolean up){
        if (up) {
            if (ship.getY() > 0) {
                ship.setY(ship.getY() - 2);
            }
        } else {
            if (ship.getY() < mCanvasHeight) {
                ship.setY(ship.getY() + 2);
            }
        }
       /* if(!shipIsMoving.get()) {
            shipIsMoving.set(true);
            Thread movingShipThread = new Thread(new Runnable() {
                public void run() {
                    while (shipIsMoving.get()) {
                        if (up) {
                            if (ship.getY() > 0) {
                                ship.setY(ship.getY() - 1);
                            }
                        } else {
                            if (ship.getY() < mCanvasHeight) {
                                ship.setY(ship.getY() + 1);
                            }
                        }
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                        }
                    }
                }
            });
            movingShipThread.start();
        }*/
    }



    public void pauseGame(){
        thread.interrupt();
    }

    public void unpauseGame(){
        thread = new Thread(this);
        thread.start();
    }


    public GameState getState(){
        return state;
    }

    private double getDistance(int x1, int y1, int x2, int y2){
        double xDistance = Math.abs(x1-x2);
        double yDistance = Math.abs(y1-y2);
        return Math.sqrt(((xDistance*xDistance) + (yDistance*yDistance)));
    }

    private void gameOver(){
        state = GameState.GAMEOVER;
        ship.setX(-10);
        if (score> highScore){
            highScore = score;
            SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, 0).edit();
            editor.putInt("highscore", score);
            editor.apply();
        }
    }

}
