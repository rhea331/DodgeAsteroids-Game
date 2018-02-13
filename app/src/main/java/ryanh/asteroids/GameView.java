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

/**
 *  GameView.java
 *  SurfaceView that draws and updates the Asteroid Game.
 *
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    private static final int TotalAsteroids = 20;
    private static final String MY_PREFS_NAME = "Highscore";

    private final double acceleration = 1.05;

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

    /**
     * Constructor to create the GameView, initializes the ship and gets the current high score if
     * there was one beforehand.
     * @param context   the context of the activity
     * @param attrs     the attribute set
     */
    public GameView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        holder = getHolder();
        holder.addCallback(this);

        ship = new Ship(-20,0);

        rand = new Random();
        //Grabs a highscore if it has been saved previously.
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, 0);
        highScore = prefs.getInt("highscore", 0);
    }

    /**
     * Callback when the Surface is created, calls the function to start the updating and drawing loop
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        unpauseGame();
    }

    /**
     * Callback when surface dimension changes, generally at the start of creation and when it regains
     * focus. Calls function to set the surface size.
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
        setSurfaceSize(w, h);
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
        Thread myThread = Thread.currentThread();
        try { //keeps thread running until it is interrupted.
            while(thread== myThread) {
                if (state == GameState.PLAYING) { //if game is in progress, update
                    update();
                }
                Canvas canvas = holder.lockCanvas(); //locks canvas to draw
                doDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
                Thread.sleep(10); //limit speed of game
            }
        }catch(InterruptedException e){} //thread is asked to stop
    }
    /**
     * Handles the asteroid movement, collision detection, and update of total asteroids and speed.
     */
    public void update(){
        //updates positions of all active asteroids and check if they have collided with the ship
        for(int i=0; i<activeAsteroids; i++){
            AsteroidObject asteroid = asteroids.get(i);
            //simple distance check to see if a collision occured
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

        }
        //TODO: change update of speed to be time based, rather than 'counter' based
        counter+=1;
        if(counter == 20) {
            if(speed < 10) {
                speed = speed * acceleration; //bad physics
            }
            counter = 0;
        }
    }

    /**
     * Where the drawing of the game takes place. Draws background, ship, asteroids, and
     * live/score/ending text.
     * @param canvas
     */
    public void doDraw(Canvas canvas) {
        Paint paint = new Paint();

        //Draws the background, clears screen.
        paint.setColor(Color.BLACK);
        canvas.drawRect(0,0,mCanvasWidth,mCanvasHeight, paint);

        //Draws ship
        ship.doDraw(canvas);

        //Sets up if center text needs to be drawn.
        paint.setColor(Color.WHITE);
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
        score = 0;
        lives = 3;
        speed = 2;
        activeAsteroids = 1;
        ship.setX(15);
        ship.setY(mCanvasHeight/2);
        //Asteroids initialized here because canvas width/height is known after initial construct.
        asteroids = new ArrayList<AsteroidObject>();
        for (int i = 0; i < TotalAsteroids; i++) {
            asteroids.add(new AsteroidObject(mCanvasWidth+15, rand.nextInt(mCanvasHeight)));
        }
        state = GameState.PLAYING;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Moves the ship up or down
     * possible TODO: add dynamic acceleration depending on tilt
     * @param up TRUE: ship needs to be moved up
     *           FALSE: ship needs to be moved down
     */
    public void moveShip(boolean up){
        if (up) {
            if (ship.getY() > 0) {
                ship.setY(ship.getY() - 2);
            }
        } else {
            if (ship.getY() < mCanvasHeight) {
                ship.setY(ship.getY() + 2);
            }
        }
        //old code for when movement was touch based.
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


    /**
     * 'Pauses' the game, by killing the thread
     */
    public void pauseGame(){
        thread.interrupt();
    }

    /**
     * 'Unpauses' the game by starting a new thread.
     */
    public void unpauseGame(){
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
        ship.setX(-10);
        if (score> highScore){
            highScore = score;
            SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, 0).edit();
            editor.putInt("highscore", score);
            editor.apply();
        }
    }

}
