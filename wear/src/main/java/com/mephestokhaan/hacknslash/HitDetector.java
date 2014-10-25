package com.mephestokhaan.hacknslash;

import android.util.Log;

/**
 * Created by MephestoKhaan on 25/10/2014.
 */
public class HitDetector {

    private long maxDelay = 30;
    private long interPeakWait = 3000;

    private long lastPeak;
    private long lastAccelerationPeakTime;
    private long lastAudioPeakTime;

    private float lastAudio = 0;
    private float lastAcceleration = 0;
    private float accelerationThresold = 1f;
    private float audioThresold = 0.7f;

    public boolean AddAccelerationPeak(float acceleration)
    {
        if(acceleration < accelerationThresold)
        {
            return false;
        }

        lastAccelerationPeakTime = System.currentTimeMillis();
        lastAcceleration = acceleration;
        return ComparePeaks();
    }

    public boolean AddAudioPeak(float audio)
    {
        if(audio < audioThresold)
        {
            return false;
        }

        //Log.i("AUDIO",""+ audio);

        lastAudioPeakTime = System.currentTimeMillis();
        lastAudio = audio;
        return ComparePeaks();
    }

    public boolean ComparePeaks()
    {
        if(Math.abs(lastPeak - System.currentTimeMillis()) > interPeakWait
                &&  Math.abs(lastAccelerationPeakTime - lastAudioPeakTime) < maxDelay)
        {
            //Log.i("HIT", ""+Math.abs(lastAccelerationPeakTime - lastAudioPeakTime)+ " AUDIO: "+lastAudio + " ACC: "+lastAcceleration);
            lastPeak = System.currentTimeMillis();
            return true;
        }
        return false;
    }


}
