package com.incode_it.spychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.incode_it.spychat.authorization.ActivityAuth;

import java.util.concurrent.CountDownLatch;

public class ActivitySplash extends AppCompatActivity {

    private final static String LOG_TAG = ActivitySplash.class.getSimpleName();
    private final CountDownLatch timeoutLatch = new CountDownLatch(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                // Wait for the splash timeout.
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) { }

                // Expire the splash page delay.
                timeoutLatch.countDown();
            }
        });
        thread.start();
        goMain();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Touch event bypasses waiting for the splash timeout to expire.
        timeoutLatch.countDown();
        return true;
    }

    protected void goMain() {
        goAfterSplashTimeout(new Intent(this, ActivityAuth.class));
    }

    private void goAfterSplashTimeout(final Intent intent) {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                // wait for the splash timeout expiry or for the user to tap.
                try {
                    timeoutLatch.await();
                } catch (InterruptedException e) {
                }

                ActivitySplash.this.runOnUiThread(new Runnable() {
                    public void run() {
                        startActivity(intent);
                        // finish should always be called on the main thread.
                        finish();
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }
}
