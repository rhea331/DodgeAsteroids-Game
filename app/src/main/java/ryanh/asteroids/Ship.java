package ryanh.asteroids;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;


class Ship {
    private int x;
    private int y;
    private Paint paint;

    Ship(int x, int y){
        this.x=x;
        this.y=y;
        paint = new Paint();
        paint.setColor(Color.BLUE);
    }

    void setX(int x){
        this.x=x;
    }

    void setY(int y){
        this.y = y;
    }

    void doDraw(Canvas canvas){
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        Path path = new Path();
        path.moveTo(x-5, y-5);
        path.lineTo(x-5, y+5);
        path.lineTo(x+5, y);
        path.lineTo(x-5, y-5);
        path.close();
        canvas.drawPath(path, paint);
    }

    int getX(){
        return x;
    }

    int getY(){
        return y;
    }
}
