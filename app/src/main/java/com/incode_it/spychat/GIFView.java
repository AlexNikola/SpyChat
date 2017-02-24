package com.incode_it.spychat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;

public class GIFView extends ImageView {

    private Movie movie;
    private long movieStart;

    public GIFView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = SystemClock.uptimeMillis();
        if (movieStart == 0) {
            movieStart = now;
        }

        if (movie != null && movie.duration() != 0) {
            long realTime = (now - movieStart) % movie.duration();
            movie.setTime((int) realTime);
            float min = Math.min(movie.width(), movie.height());
            //canvas.scale((float)this.getWidth() / min,(float)this.getHeight() / min);
            movie.draw(canvas, 0, 0);
            //canvas.restore();
            //Log.d("dfgdssg", "onDraw: " + getWidth());
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    public void setBytes(byte[] bytes) {
        movie = Movie.decodeByteArray(bytes, 0, bytes.length);
        if (movie != null) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(movie.width(), movie.height());
            setLayoutParams(lp);
            Log.d("dfgdssg", "setBytes: " + lp.width + " " + lp.height);
        }
        Log.d("dfgdssg", "setBytes: " + movie + " " + bytes.length);
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        if (movie != null) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(movie.width(), movie.height());
            setLayoutParams(lp);
            Log.d("dfgdssg", "setBytes: " + lp.width + " " + lp.height);
        }
    }

    public void setPath(String path) {
        movie = Movie.decodeFile(path);
        Log.d("dfgdssg", "setBytes: " + movie + " " + path);
    }
}
