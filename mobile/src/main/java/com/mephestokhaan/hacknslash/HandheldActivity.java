package com.mephestokhaan.hacknslash;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.w3c.dom.Text;


public class HandheldActivity extends Activity implements MessageReceiverListener, HitListener {

    private CheckBox serverCheck;
    private TextView ipText;

    private TextView player1Text ,player2Text;
    private int player1Lives, player2Lives;

    private HandeldToHandeldCommunicator handeldToHandeldCommunicator;
    private HandeldToWatchCommunicator handeldToWatchCommunicator;

    private HitDetector hitDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handeld);

        serverCheck = (CheckBox) findViewById(R.id.checkBox);
        ipText = (TextView) findViewById(R.id.iptext);
        player1Text = (TextView) findViewById(R.id.player1lives);
        player2Text = (TextView) findViewById(R.id.player2lives);

        handeldToWatchCommunicator = new HandeldToWatchCommunicator(this,this);

        UpdateScores();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        handeldToWatchCommunicator.Connect(true);
    }
    @Override
    protected void onStop()
    {
        handeldToWatchCommunicator.Connect(false);
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.handeld, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void SyncHandelds(View v)
    {
        if(handeldToHandeldCommunicator != null)
        {
            handeldToHandeldCommunicator.Stop();
            handeldToHandeldCommunicator = null;
        }
        handeldToHandeldCommunicator = new HandeldToHandeldCommunicator(ipText.getText().toString(), serverCheck.isChecked(),this,this);

        if(serverCheck.isChecked())
        {
            hitDetector = new HitDetector(this);
        }
    }

    private void UpdateScores()
    {
        this.runOnUiThread(new Runnable() {
            public void run() {
                player1Text.setText(""+player1Lives);
                player2Text.setText(""+player2Lives);
            }
        });
    }


    public void Restart(View v)
    {
        if(serverCheck.isChecked()) {
            player1Lives = player2Lives = 0;

            UpdateScores();
        }
    }

    @Override
    public void onHandheldWatchMessageReceived(String msg)
    {
        if(serverCheck.isChecked())
        {
            hitDetector.Player1Slashes();
        }
        else
        {
            if(handeldToHandeldCommunicator != null) {
                handeldToHandeldCommunicator.SendMessage("slash");
            }
        }
    }

    @Override
    public void onHandHeldHandheldMessageReceived(String msg)
    {
        if(msg.equalsIgnoreCase("slash"))
        {
            if(serverCheck.isChecked())
            {
                hitDetector.Player2Slashes();
            }
        }
    }

    @Override
    public void onHitDetected(int player)
    {
        switch (player)
        {
            case 1:
                player1Lives++;
                break;
            case 2:
                player2Lives++;
                break;
            default:
                break;
        }

        UpdateScores();

    }
}
