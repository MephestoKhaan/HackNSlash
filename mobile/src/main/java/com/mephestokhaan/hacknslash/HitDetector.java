package com.mephestokhaan.hacknslash;


import android.os.Handler;
import android.util.Log;

/**
 * Created by MephestoKhaan on 25/10/2014.
 */
public class HitDetector
{
    private HitListener delegate;
    public long maxClashDelay = 400;
    public long expectedClientDelay = 200;

    private long lastPlayer1Slash;
    private long lastPlayer2Slash;

    private Handler handler = new Handler();

    public HitDetector(HitListener delegate)
    {
        this.delegate = delegate;
    }

    public void Player1Slashes()
    {
        handler.removeCallbacks(runnable);
        lastPlayer1Slash = System.currentTimeMillis();
        Log.i("PLAYER 1", "" + lastPlayer1Slash);
        int clash = SlashesClash();
        if(clash != 0)
        {
            handler.postDelayed(runnable,maxClashDelay+expectedClientDelay);
        }
        else
        {
            if(this.delegate != null)
            {
                this.delegate.onHitDetected(clash);
            }
        }
    }

    public void Player2Slashes()
    {
        handler.removeCallbacks(runnable);
        lastPlayer2Slash = System.currentTimeMillis() - expectedClientDelay;
        Log.i("PLAYER 2", "" + lastPlayer2Slash);
        int clash = SlashesClash();
        if(clash != 0)
        {
            handler.postDelayed(runnable,maxClashDelay);
        }
        else
        {
            if(this.delegate != null)
            {
                this.delegate.onHitDetected(clash);
            }
        }
    }


    public int SlashesClash()
    {
        long difference = Math.abs(lastPlayer1Slash - lastPlayer2Slash);
        if(difference < maxClashDelay)
        {
            return 0;
        }
        return lastPlayer1Slash > lastPlayer2Slash? 1:2;

    }

    private void ClashTimedOut()
    {
        int hit = SlashesClash();
        if (this.delegate != null) {
            this.delegate.onHitDetected(hit);
            lastPlayer2Slash = 0;
            lastPlayer1Slash = 0;
        }
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run() {
            ClashTimedOut();
        }
    };
}
