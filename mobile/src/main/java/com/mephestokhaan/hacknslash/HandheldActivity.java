package com.mephestokhaan.hacknslash;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;



public class HandheldActivity extends Activity implements MessageReceiverListener {

    private CheckBox serverCheck;
    private TextView ipText;

    private HandeldToHandeldCommunicator handeldToHandeldCommunicator;
    private HandeldToWatchCommunicator handeldToWatchCommunicator;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handeld);

        serverCheck = (CheckBox) findViewById(R.id.checkBox);
        ipText = (TextView) findViewById(R.id.iptext);
        handeldToWatchCommunicator = new HandeldToWatchCommunicator(this,this);
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

    public void SyncHandelds(View v)
    {
        if(handeldToHandeldCommunicator != null)
        {
            handeldToHandeldCommunicator.Stop();
            handeldToHandeldCommunicator = null;
        }
        handeldToHandeldCommunicator = new HandeldToHandeldCommunicator(ipText.getText().toString(), serverCheck.isChecked(),this,this);
    }

    public void sendTestMessage(View v)
    {
        if(handeldToHandeldCommunicator != null)
        {
            handeldToHandeldCommunicator.SendMessage("testing");
        }
    }

    @Override
    public void onHandheldWatchMessageReceived(String msg)
    {
        Log.i("WATCH SAYS: ",msg);
    }

    @Override
    public void onHandHeldHandheldMessageReceived(String msg)
    {
        Log.i("PHONE SAYS: ",msg);
    }
}
