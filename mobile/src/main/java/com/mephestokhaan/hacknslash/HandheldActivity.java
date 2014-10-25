package com.mephestokhaan.hacknslash;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;



public class HandheldActivity extends Activity implements MessageReceiverListener {

    boolean isServer;

    private TextView mTextView;
    private HandeldToWatchCommunicator handeldToWatchCommunicator;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handeld);

        mTextView = (TextView) findViewById(R.id.textView);
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

    @Override
    public void onMessageReceived(String msg)
    {
        mTextView.setText(msg);
    }
}
