package com.mephestokhaan.hacknslash;


import android.os.Handler;
import android.util.Log;

/**
 * Created by MephestoKhaan on 25/10/2014.
 */
public class HitDetector
{
    private HitListener delegate;
    public long maxClashDelay = 300;

    public long maxWait = 1000;

    private long lastPlayer1Slash;
    private long lastPlayer2Slash;

    private boolean player1hits;
    private boolean player2hits;

    private Handler handler = new Handler();

    public HitDetector(HitListener delegate)
    {
        this.delegate = delegate;
    }

    public void Player1Slashes(String time)
    {
        long milis = Long.parseLong(time);
        handler.removeCallbacks(runnable);
        lastPlayer1Slash = milis;
        player1hits = true;
        Log.i("PLAYER 1", "" + lastPlayer1Slash);
        SlashesClash(false);
    }

    public void Player2Slashes(String time)
    {
        long milis = Long.parseLong(time);
        handler.removeCallbacks(runnable);
        lastPlayer2Slash = milis;
        player2hits = true;
        Log.i("PLAYER 2", "" + lastPlayer2Slash);
        SlashesClash(false);
    }


    public void SlashesClash(boolean timedout)
    {
        long difference = Math.abs(lastPlayer1Slash - lastPlayer2Slash);
        if(difference < maxClashDelay && player1hits && player2hits)
        {
            player1hits = player2hits = false;
            if(this.delegate != null)
            {
                this.delegate.onHitDetected(0);
            }
        }
        else if(player1hits && player2hits)
        {
            player1hits = player2hits = false;
            if(this.delegate != null)
            {
                this.delegate.onHitDetected(lastPlayer1Slash < lastPlayer2Slash? 1:2);
            }
        }
        else{
            if(timedout)
            {
                if(player1hits && !player2hits)
                {
                    if(this.delegate != null)
                    {
                        this.delegate.onHitDetected(1);
                    }
                }
                else if(player2hits && !player1hits)
                {
                    if(this.delegate != null)
                    {
                        this.delegate.onHitDetected(2);
                    }
                }
                player1hits = player2hits = false;
            }
            else {
                handler.postDelayed(runnable, maxWait);
            }
        }
    }

    private void ClashTimedOut()
    {
        SlashesClash(true);
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run() {
            ClashTimedOut();
        }
    };
}
