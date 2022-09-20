package com.timeapp.clock;

import android.annotation.SuppressLint;
import android.widget.TextView;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;

public class TimeHandler implements Runnable {

    private long timeDiff = 0;
    private long prevMillis = 0;
    private long currMillis = 0;
    private final TextView textView;
    private boolean isNtpActive = false;

    /***
     * TimeHandler
     * handles drawing the time on the main display and NTP connections
     */
    public TimeHandler(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void run() {
            getTime();
            drawTime();
        }

    /***
     * Calculate whether it's time to request the time from the NTP server
     *
     * @return boolean
     */
    private boolean ntpTimer() {
        currMillis = System.currentTimeMillis();
        return (currMillis - prevMillis >= 30000);
    }

    /***
     * Tries to connect to NTP server and receive a timestamp if either condition is met:
     * 1) We don't have an active connection to an NTP server.
     * 2) 30 seconds has passed since we last connected to a server.
     */
    private void getTime() {

        if(!isNtpActive || ntpTimer()) {
            NTPUDPClient ntpudpClient = new NTPUDPClient();
            // Attempt to connect to an NTP server to fetch the time, and calculate
            // the offset to device time if successful.
            try {
                String server = "se.pool.ntp.org";
                InetAddress inetAddress = InetAddress.getByName(server);

                TimeInfo timeInfo = ntpudpClient.getTime(inetAddress);
                long ntpTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                timeDiff = System.currentTimeMillis() - ntpTime;
                isNtpActive = true;

            } catch (IOException e) {
                e.printStackTrace();
                timeDiff = 0;
                isNtpActive = false;
            }
            ntpudpClient.close();
            prevMillis = currMillis;
        }
    }

    /***
     * Draw the current time to the textView field. If time has been received from
     * an NTP server, display the offset between NTP and device time. If we have
     * failed to connect to the NTP server, display a message that we're offline.
     */
    @SuppressLint("SimpleDateFormat")
    private void drawTime() {
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("HH:mm:ss");
        String date = sdf.format( System.currentTimeMillis() - timeDiff);

        date = isNtpActive ? date + " ("+timeDiff+"ms)" : date + " (offline)";
        textView.setText(date);
    }

}


