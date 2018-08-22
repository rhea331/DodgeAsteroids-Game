package ryanh.asteroids;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Ship.java
 * Ship class. Contains the x and y co-ordinates of the ship and drawing of said ship.
 */
class Ship {
    private int x;
    private int y;
    private double speed = 0;
    private int height;
    private Bitmap shipStraight;
    private Bitmap shipUp;
    private Bitmap shipDown;
    private static int MAX_SPEED = 4;
    /**
     * Constructor. Initializes the x, y co-ordinates and the paint.
     * @param x the x co-ordinate of the ship
     * @param y the y co-ordinate of the ship
     */
    Ship(int x, int y, Bitmap bmp1, Bitmap bmp2, Bitmap bmp3){
        this.x=x;
        this.y=y;
        shipStraight = bmp1;
        shipUp = bmp2;
        shipDown = bmp3;
        this.height = bmp1.getHeight();
    }

    void update(int canvasHeight){
        if(y>=0 && y<=canvasHeight) {
            y += speed;
        }
        if (y < 0){
            y=0;
            speed = 0;
        }
        else if (y>canvasHeight){
            y=canvasHeight;
            speed = 0;
        }
    }

    /**
     * Sets the x co-ordinate of the ship
     * @param x the new x co-ordinate
     */

    void setX(int x){
        this.x=x;
    }

    /**
     * Sets the y co-ordinate of the ship
     * @param y the new y co-ordinate
     */
    void setY(int y){
        this.y = y;
    }


    void setSpeed(double delta){
        speed = delta * MAX_SPEED;
    }
    /**
     * Returns the x co-ordinate of the ship
     * @return the x co-ordinate
     */
    int getX(){
        return x;
    }

    /**
     * Returns the y co-ordinate of the ship
     * @return the y co-ordinate
     */
    int getY(){
        return y;
    }

    /**
     * Draws the ship
     * @param canvas the canvas to be drawn on
     */
    void doDraw(Canvas canvas){
        if (speed > 1) {
            canvas.drawBitmap(shipUp, x - (height / 2), y - (height / 2), null);
        }else if (speed < -1){
            canvas.drawBitmap(shipDown, x - (height / 2), y - (height / 2), null);
        }else{
            canvas.drawBitmap(shipStraight, x - (height / 2), y - (height / 2), null);
        }
    }

}
