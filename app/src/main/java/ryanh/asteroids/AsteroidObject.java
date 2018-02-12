package ryanh.asteroids;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


class AsteroidObject {
    private int x, y;
    private Paint paint;

    AsteroidObject(int x, int y){
        this.x = x;
        this.y = y;
        paint = new Paint();
        paint.setColor(Color.GRAY);
    }

    void doDraw(Canvas canvas){
        canvas.drawCircle(x,y, canvas.getHeight()/20, paint);
    }

    void setX(int x){
        this.x= x;
    }

    void setY(int y){
        this.y = y;
    }

    int getX(){
        return x;
    }

    int getY() {return y;}
}
