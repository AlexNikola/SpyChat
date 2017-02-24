package com.incode_it.spychat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
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
            canvas.restore();
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
        Log.d("dfgdssg", "setBytes: " + movie + " " + bytes.length);
    }

    public void setPath(String path) {
        movie = Movie.decodeFile(path);
        Log.d("dfgdssg", "setBytes: " + movie + " " + path);
    }
}
