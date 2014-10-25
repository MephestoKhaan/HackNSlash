package com.mephestokhaan.hacknslash;


import android.os.Handler;

/**
 * Created by MephestoKhaan on 25/10/2014.
 */
public class HitDetector
{
    private HitListener delegate;
    private long maxClashDelay = 100;
    private long expectedClientDelay = 100;

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
        int clash = SlashesClash();
        if(clash != 0)
        {
            if(this.delegate != null)
            {
                this.delegate.onHitDetected(clash);
            }
        }
        else
        {
            handler.postDelayed(runnable,maxClashDelay+expectedClientDelay);
        }
    }

    public void Player2Slashes()
    {
        handler.removeCallbacks(runnable);
        lastPlayer2Slash = System.currentTimeMillis() - expectedClientDelay;
        int clash = SlashesClash();
        if(clash != 0)
        {
            if(this.delegate != null)
            {
                this.delegate.onHitDetected(clash);
            }
        }
        else
        {
            handler.postDelayed(runnable,maxClashDelay);
        }
    }


    public int SlashesClash()
    {
        if(Math.abs(lastPlayer1Slash - lastPlayer2Slash) < maxClashDelay)
        {
            return 0;
        }
        return lastPlayer1Slash > lastPlayer2Slash? 1:2;

    }

    private void ClashTimedOut()
    {
        int hit = SlashesClash();
        if(this.delegate != null)
        {
            this.delegate.onHitDetected(hit);
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
