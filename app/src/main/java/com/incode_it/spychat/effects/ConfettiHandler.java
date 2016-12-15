package com.incode_it.spychat.effects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewGroup;

import com.github.jinatonic.confetti.ConfettiManager;
import com.github.jinatonic.confetti.ConfettiSource;
import com.github.jinatonic.confetti.ConfettoGenerator;
import com.github.jinatonic.confetti.confetto.BitmapConfetto;
import com.github.jinatonic.confetti.confetto.Confetto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfettiHandler {

    public static final int APPEAR_TOP = 0;
    public static final int APPEAR_BOTTOM = 1;
    public static final int APPEAR_CENTER = 2;

    private int appearance = APPEAR_TOP;

    private static int balloonVelocitySlow;
    private static int balloonVelocityNormal;
    private static int confettiSize;

    private ConfettiManager confettiManager;
    private ViewGroup container;
    private int[] colors;
    private ConfettoGenerator generator;

    @DrawableRes
    private int drawableRes;

    private ConfettiHandler(ViewGroup container) {
        this.container = container;
        ensureStaticResources(container);
    }

    public static ConfettiHandler getInstance(ViewGroup container) {
        return new ConfettiHandler(container);
    }

    public ConfettiHandler colors(int[] colors) {
        this.colors = colors;
        return this;
    }

    public ConfettiHandler image(@DrawableRes int drawableRes) {
        this.drawableRes = drawableRes;
        return this;
    }

    public ConfettiHandler size(int size) {
        confettiSize = size;
        return this;
    }

    public ConfettiHandler appear(int appearance) {
        this.appearance = appearance;
        return this;
    }

    public ConfettiHandler generator(ConfettoGenerator generator) {
        this.generator = generator;
        return this;
    }

    public ConfettiManager build() {

        ConfettiSource confettiSource;
        switch (appearance) {
            case APPEAR_BOTTOM: {
                confettiSource = new ConfettiSource(0, container.getHeight() + confettiSize,
                        container.getWidth(), container.getHeight() + confettiSize);
                break;
            }
            case APPEAR_CENTER: {
                confettiSource = new ConfettiSource(container.getWidth() / 2, container.getHeight() / 2);
                break;
            }
            default: {
                confettiSource = new ConfettiSource(0, -confettiSize,
                        container.getWidth(), -confettiSize);
            }
        }

        if (generator == null) {
            generator = getConfettiGenerator(container, colors, drawableRes, confettiSize);
        }

        confettiManager = new ConfettiManager(container.getContext(), generator, confettiSource, container);

        return confettiManager;
    }

    public ConfettiManager getConfettiManager() {
        return confettiManager;
    }

    public static ConfettoGenerator getConfettiGenerator(ViewGroup container, int[] colors, @DrawableRes int res, int size) {
        final List<Bitmap> bitmaps = generateConfettiBitmaps(container, colors, size, res);
        final int numBitmaps = bitmaps.size();
        return new ConfettoGenerator() {
            @Override
            public Confetto generateConfetto(Random random) {
                return new BitmapConfetto(bitmaps.get(random.nextInt(numBitmaps)));
            }
        };
    }

    public static ConfettoGenerator getConfettiGenerator(final List<Bitmap> bitmaps) {
        final int numBitmaps = bitmaps.size();
        return new ConfettoGenerator() {
            @Override
            public Confetto generateConfetto(Random random) {
                return new BitmapConfetto(bitmaps.get(random.nextInt(numBitmaps)));
            }
        };
    }

    public static List<Bitmap> generateConfettiBitmaps(ViewGroup container, int[] colors, int size, @DrawableRes int res) {
        final List<Bitmap> bitmaps = new ArrayList<>();
        for (int color : colors) {
            bitmaps.add(createBitmap(container, color, (int) (size * 0.8f), res));
            bitmaps.add(createBitmap(container, color, size, res));
            bitmaps.add(createBitmap(container, color, (int) (size * 1.4f), res));
        }
        return bitmaps;
    }

    public static Bitmap createBitmap(ViewGroup container, int color, int size, @DrawableRes int res)
    {
        Drawable drawable = container.getResources().getDrawable(res);
        //DrawableCompat.setTint(drawable, color);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, size, size);
        drawable.draw(canvas);

        return bitmap;
    }



    private void ensureStaticResources(ViewGroup container) {
        balloonVelocitySlow = 250;
        balloonVelocityNormal = 600;
        confettiSize = 350;
    }
}
