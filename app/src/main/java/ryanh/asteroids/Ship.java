package ryanh.asteroids;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Ship.java
 * Ship class. Contains the x and y co-ordinates of the ship and drawing of said ship.
 * TODO: dynamic ship size, change drawing to an actual ship
 */
class Ship {
    private int x;
    private int y;
    private Paint paint;

    /**
     * Constructor. Initializes the x, y co-ordinates and the paint.
     * @param x the x co-ordinate of the ship
     * @param y the y co-ordinate of the ship
     */
    Ship(int x, int y){
        this.x=x;
        this.y=y;
        paint = new Paint();
        paint.setColor(Color.BLUE);
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
        int w = canvas.getWidth();
        int h = canvas.getHeight(); // for when I get around to drawing it dynamically

        Path path = new Path();
        path.moveTo(x-5, y-5);
        path.lineTo(x-5, y+5);
        path.lineTo(x+5, y);
        path.lineTo(x-5, y-5);
        path.close();
        canvas.drawPath(path, paint);
    }

}
