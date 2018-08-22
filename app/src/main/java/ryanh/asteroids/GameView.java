package ryanh.asteroids;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;

/**
 *  GameView.java
 *  SurfaceView/Thread that draws and updates the Asteroid Game.
 *
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    private static final int TotalAsteroids = 20;
    private static final int TotalStars = 30;
    private static final String MY_PREFS_NAME = "Highscore";

    private final double acceleration = 1.05;

    private Context context;
    private Random rand;

    private ArrayList<AsteroidObject> asteroids;
    private ArrayList<Integer> starX;
    private ArrayList<Integer> starY;
    private Ship ship;

    private GameState state = GameState.INITIALIZING;

    private SurfaceHolder holder;
    private Thread thread;
    private boolean running = true;
    private int mCanvasWidth, mCanvasHeight = 0;

    private int activeAsteroids = 1;
    private int counter = 0;
    private int lives = 3;
    private int score = 0;
    private int highScore = 0;
    private double speed = 2.0;
    private double delta = 0;

    /**
     * Constructor to create the GameView and gets the current high score if
     * there was one beforehand.
     * @param context   the context of the activity
     * @param attrs     the attribute set
     */
    public GameView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        holder = getHolder();
        holder.addCallback(this);
        rand = new Random();
        asteroids = new ArrayList<>();
        starX = new ArrayList<>();
        starY = new ArrayList<>();
        //Grabs a highscore if it has been saved previously.
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, 0);
        highScore = prefs.getInt("highscore", 0);
    }

    /**
     * Callback when the Surface is created
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    /**
     * Callback when surface dimension changes, generally at the start of creation and when it regains
     * focus. Calls function to set the surface size. If this is the first call, then initializes the game
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
        setSurfaceSize(w, h);
        if(state == GameState.INITIALIZING){

            Bitmap shipImage1 = BitmapFactory.decodeResource(getResources(),R.drawable.spaceship_1);
            Bitmap shipImage2 = BitmapFactory.decodeResource(getResources(),R.drawable.spaceship_2);
            Bitmap shipImage3 = BitmapFactory.decodeResource(getResources(),R.drawable.spaceship_3);

            float aspectRatio = shipImage1.getWidth() / (float) shipImage1.getHeight();

            shipImage1 = Bitmap.createScaledBitmap(shipImage1, mCanvasHeight/15, Math.round((mCanvasHeight/15)/ aspectRatio), false);
            shipImage2 = Bitmap.createScaledBitmap(shipImage2, mCanvasHeight/15, Math.round((mCanvasHeight/15)/ aspectRatio), false);
            shipImage3 = Bitmap.createScaledBitmap(shipImage3, mCanvasHeight/15, Math.round((mCanvasHeight/15)/ aspectRatio), false);

            ship = new Ship(30,mCanvasHeight/2, shipImage1, shipImage2, shipImage3);

            Bitmap asteroidImage = BitmapFactory.decodeResource(getResources(), R.drawable.asteroid);
            asteroidImage = Bitmap.createScaledBitmap(asteroidImage, mCanvasHeight/10, mCanvasHeight/10, true);
            for (int j = 0; j < TotalAsteroids; j++) {
                asteroids.add(new AsteroidObject(mCanvasWidth+15, rand.nextInt(mCanvasHeight), mCanvasHeight/10, asteroidImage));
            }

            for (int k=0; k < TotalStars; k++){
                starX.add(rand.nextInt(mCanvasWidth));
                starY.add(rand.nextInt(mCanvasHeight));
            }

            state = GameState.DEFAULT;
            resumeGame();
        }
    }

    /**
     * Callback when the Surface is destroyed, will stop thread from running.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        pauseGame();
    }

    /**
     * Used to remember the width and height of the surface, mostly used for drawing and boundaries.
     */
    public void setSurfaceSize(int width, int height){
        mCanvasWidth = width;
        mCanvasHeight = height;
    }

    /**
     * The game loop. Handles the updating and drawing of the game.
     */
    @Override
    public void run(){
        Canvas canvas = null;
        try {
            while (running) {
                if (!holder.getSurface().isValid()) {
                    continue;
                }
                if (state == GameState.PLAYING) { //if game is in progress, update
                    update();
                }
                canvas = holder.lockCanvas(); //locks canvas to draw
                doDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                try {
                    thread.sleep(10);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }finally{
            if(holder.lockCanvas() != null){
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
    /**
     * Handles the asteroid movement, collision detection, and update of total asteroids and speed.
     */
    public void update(){
        ship.update(mCanvasHeight);

        //updates positions of all active asteroids and check if they have collided with the ship
        for(int i=0; i<activeAsteroids; i++){
            AsteroidObject asteroid = asteroids.get(i);
            //simple distance check to see if a collision occurred
            if(getDistance(ship.getX(), ship.getY(), asteroid.getX(), asteroid.getY()) <  mCanvasHeight/20){
                lives-=1;
                if(lives <= 0){
                    gameOver();
                    return;
                }
                speed=2; //resets speed
                //sends asteroid to outside the left of the screen so it gets sent back to the right from below.
                asteroid.setX(-mCanvasWidth/4);
            }
            //if asteroids hits left side of screen, sends it back to the right again, with a different y co-ordinate
            if(asteroid.getX() < 0){
                asteroid.setX(mCanvasWidth+15);
                asteroid.setY(rand.nextInt(mCanvasHeight));
                score+=1; //yay
            }else{asteroid.setX(asteroid.getX()-(int)speed);} //otherwise move it by the speed
        }
         //addition of asteroids
        if(activeAsteroids < TotalAsteroids ){
            //add some variance in when the next asteroid will come out, so that the asteroids aren't
            //always the same distance from each other
            int gap = mCanvasWidth / 20;
            int variance = rand.nextInt(gap/2 + 1 + gap/2) - gap/2;
            if (asteroids.get(activeAsteroids-1).getX() < mCanvasWidth - gap + variance){ //check if the asteroid is past the certain distance
                activeAsteroids+=1;
            }

        }/*
        //TODO: change update of speed to be time based, rather than 'counter' based
        counter+=1;
        if(counter == 20) {
            if(speed < 10) {
                speed = speed * acceleration; //bad physics
            }
            for(AsteroidObject star:stars){
                if (star.getX() < 0){
                    star.setX(mCanvasWidth);
                    star.setY(rand.nextInt(mCanvasHeight));
                }else{star.setX(star.getX()-1);}
            }
            counter = 0;
        }*/
    }

    /**
     * Where the drawing of the game takes place. Draws background, ship, asteroids, and
     * live/score/ending text.
     * @param canvas the canvas to draw on
     */
    public void doDraw(Canvas canvas) {
        Paint paint = new Paint();

        //Draws the background, clears screen.
        paint.setColor(Color.BLACK);
        canvas.drawRect(0,0,mCanvasWidth,mCanvasHeight, paint);

        paint.setColor(Color.WHITE);
        for (int j = 0; j<TotalStars; j++){
            canvas.drawCircle(starX.get(j), starY.get(j), 1, paint);
        }

        //Draws ship
        ship.doDraw(canvas);

        paint.setTextAlign(Paint.Align.CENTER);
        //Different things are drawn depending on state
        switch (state){
            case DEFAULT: //Before the first game is started
                canvas.drawText("TOUCH SCREEN TO START GAME", mCanvasWidth/2, mCanvasHeight/2, paint);
                break;
            case PLAYING: //Game is in progress, draw all active asteroids
                for (int i=0; i<activeAsteroids;i++){
                    asteroids.get(i).doDraw(canvas);
                }
                break;
            case GAMEOVER: //Game has ended
                canvas.drawText("GAME OVER   TOUCH SCREEN TO RESTART", mCanvasWidth / 2, mCanvasHeight / 2, paint);
                break;
        }
        //Draws the lives, score and high score text
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Lives: "+lives, 10,mCanvasHeight - 10, paint);
        canvas.drawText("Score: "+score, 10, 10, paint);
        canvas.drawText("High score: "+ highScore, 10, 20, paint);
    }

    /**
     * Initializes the game, used for the first start and subsequent restarts, then starts the game.
     */
    public void start() {
        reset();
        state = GameState.PLAYING;
        resumeGame();
    }

    public void reset(){
        score = 0;
        lives = 3;
        speed = 2;
        activeAsteroids = 1;
        ship.setX(30);
        ship.setY(mCanvasHeight/2);
        for(AsteroidObject asteroid:asteroids){
            asteroid.setX(mCanvasWidth+15);
            asteroid.setY(rand.nextInt(mCanvasHeight));
        }
    }

    /**
     * Moves the ship up or down
     * possible TODO: add dynamic acceleration depending on tilt
     * @param delta TRUE: ship needs to be moved up
     *           FALSE: ship needs to be moved down
     */
    public void moveShip(double delta){
        ship.setSpeed(delta);
    }


    /**
     * 'Pauses' the game, by killing the thread
     */
    public void pauseGame(){
        running = false;
        thread.interrupt();
    }

    /**
     * 'Unpauses' the game by starting a new thread.
     */
    public void resumeGame(){
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Returns the state of the game, whether it's DEFAULT, PLAYING, OR GAMEOVER
     * @return state   the GameState
     */
    public GameState getState(){
        return state;
    }

    /**
     * Used to determine the distance between two points
     * @param x1 the x co-ordinate of the first point
     * @param y1 the y co-ordinate of the first point
     * @param x2 the x co-ordinate of the second point
     * @param y2 the y co-ordinate of the second point
     * @return the distance between the two points in pixels
     */
    private double getDistance(int x1, int y1, int x2, int y2){
        double xDistance = Math.abs(x1-x2);
        double yDistance = Math.abs(y1-y2);
        return Math.sqrt(((xDistance*xDistance) + (yDistance*yDistance)));
    }

    /**
     * Used when the game is over.
     * Sets state, and updates high score if current score is higher.
     */
    private void gameOver(){
        state = GameState.GAMEOVER;
        ship.setX(-20);
        if (score> highScore){
            highScore = score;
            SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, 0).edit();
            editor.putInt("highscore", score);
            editor.apply();
        }
    }

}
