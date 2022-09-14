package com.timeapp.clock;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TimeHandler timeHandler = null;
    private boolean mPaused;
    private Object mPauseLock = null;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Workaround for NetworkOnMainThreadException to enable connections to
        // NTP server on main thread.

        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.
                Builder().
                permitAll().
                build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        // Draws our clock
        TextView textView = findViewById(R.id.txt_time);

        // Handles fetching NTP requests and updates textView
        timeHandler = new TimeHandler(textView);
        mPauseLock = new Object();

        new Thread(() -> {
            while (true) {
                // Required to update textView from another thread than the main one
                runOnUiThread(timeHandler);

                // Halts or resume operation in thread as we pause and resume the app
                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void onPause() {
        super.onPause();
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    public void onResume() {
        super.onResume();
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }
}

