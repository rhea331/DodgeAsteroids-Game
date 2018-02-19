package ryanh.asteroids;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * AsteroidObject.java
 * Used for the asteroids. Stores the x and y co-ordinates of the asteroid, and drawing.
 */
class AsteroidObject {
    private int x, y;
    private Paint paint;

    /**
     * Constructor to create an asteroid. Sets up the co-ordinates and paint.
     * @param x  the x co-ordinate of the asteroid
     * @param y  the y co-ordinate of the asteroid
     */
    AsteroidObject(int x, int y){
        this.x = x;
        this.y = y;
        paint = new Paint();
        paint.setColor(Color.GRAY);
    }

    /**
     * Sets the x co-ordinate of the asteroid
     * @param x the new x co-ordinate
     */
    void setX(int x){
        this.x= x;
    }

    /**
     * Sets the y co-ordinate of the asteroid
     * @param y the new x co-ordinate
     */
    void setY(int y){
        this.y = y;
    }

    /**
     * Returns the x co-ordinate of the asteroid
     * @return the x co-ordinate
     */
    int getX(){
        return x;
    }

    /**
     * Returns the y co-ordinate of the asteroid
     * @return the y co-ordinate
     */
    int getY() {return y;}

    /**
     * Draws the asteroid
     * @param canvas the canvas where it is to be drawn.
     */
    void doDraw(Canvas canvas, int radius){
        canvas.drawCircle(x,y, radius, paint);
    }


}
