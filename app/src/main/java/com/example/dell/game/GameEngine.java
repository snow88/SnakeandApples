package com.example.dell.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;


public class GameEngine extends SurfaceView implements Runnable{
    Context context;
    int X, Y;

    int BLOCKS_X = 30;
    int blocksizepx;
    int BLOCKS_Y;

    volatile boolean playing;
    Thread gameThread;
    Canvas canvas;
    SurfaceHolder surfaceHolder;
    Paint paint;

    int snakelength;
    int[] snake_xcells, snake_ycells;
    enum Direction{UP, RIGHT, DOWN, LEFT};
    Direction direction = Direction.RIGHT;
    int foodx, foody;
    int score;

    private long next_frame_at;
    private final long framespersec = 7;

    public GameEngine(Context context, Point size) {
        super(context);
        this.context = context;
        X = size.x;
        Y = size.y;
        blocksizepx = X/BLOCKS_X;
        BLOCKS_Y = Y/blocksizepx;
        surfaceHolder = getHolder();
        paint = new Paint();
        snake_xcells = new int[200];
        snake_ycells = new int[200];

        newgame();
    }

    @Override
    public void run() {
        while(playing) {
            if(updateRequired()) {
                update();
                draw();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getX()>X/4 && event.getX()<X*3/4) {
                if (event.getY()<Y/4)
                    direction = Direction.UP;
                else if (event.getY()>Y*3/4)
                    direction = Direction.DOWN;
            }
            else if (event.getX()<X/4 && event.getY()>Y/4 && event.getY()<Y*3/4){
                direction = Direction.LEFT;
            }
            else if (event.getX()>X*3/4 && event.getY()>Y/4 && event.getY()<Y*3/4){
                direction = Direction.RIGHT;
            }
        }
        return true;
    }

    private void newgame() {
        snakelength = 1;
        snake_xcells[0] = BLOCKS_X/2;
        snake_ycells[0] = BLOCKS_Y/2;
        score = 0;

        putfood();
        next_frame_at = System.currentTimeMillis();
    }

    private void putfood() {
        Random r = new Random();
        foodx = r.nextInt(BLOCKS_X - 3) + 1;
        foody = r.nextInt(BLOCKS_Y - 3) + 1;
    }

    private void eatfood() {
        snakelength++;
        score++;
        putfood();
    }

    private void movesnake() {
        for (int i=snakelength; i>0; i--) {
            snake_xcells[i] = snake_xcells[i-1];
            snake_ycells[i] = snake_ycells[i-1];
        }
        switch (direction) {
            case RIGHT: snake_xcells[0]++;
                break;
            case LEFT: snake_xcells[0]--;
                break;
            case DOWN: snake_ycells[0]++;
                break;
            case UP: snake_ycells[0]--;
                break;
        }
    }

    private boolean detectdeath() {
        if (snake_xcells[0]>BLOCKS_X || snake_xcells[0]<0 || snake_ycells[0]<0 || snake_ycells[0]>BLOCKS_Y)
            return true;
        return false;
    }

    private boolean updateRequired() {
        if(next_frame_at <= System.currentTimeMillis()){
            next_frame_at = System.currentTimeMillis() + 1000 / framespersec;
            return true;
        }
        return false;
    }

    private void update() {
        if (snake_xcells[0] == foodx && snake_ycells[0] == foody)
            eatfood();
        movesnake();
        if (detectdeath())
            newgame();
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            Drawable d = getResources().getDrawable(R.drawable.grass);
            d.setBounds(0,0,X,Y);
            d.draw(canvas);
            paint.setColor(Color.WHITE);
            paint.setTextSize(60);
            canvas.drawText("Score: " + score, 40, 80, paint);
            d = getResources().getDrawable(R.drawable.snakecell);
            for (int i=0; i<=snakelength; i++) {
                d.setBounds(snake_xcells[i]*blocksizepx, snake_ycells[i]*blocksizepx,
                        (snake_xcells[i]+1)*blocksizepx, (snake_ycells[i]+1)*blocksizepx);
                d.draw(canvas);
            }
            d = getResources().getDrawable(R.drawable.apple);
            d.setBounds(foodx*blocksizepx-15, foody*blocksizepx-15,
                    (foodx+1)*blocksizepx+15, (foody+1)*blocksizepx+15);
            d.draw(canvas);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause()
    {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.d("GAME", "in pause");
            e.printStackTrace();
        }
    }

    public void resume()
    {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}
