package com.mephestokhaan.hacknslash;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;



public class HandheldActivity extends Activity implements MessageReceiverListener {

    private TextView mTextView;
    private DataCommunicator dataCommunicator;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handeld);

        mTextView = (TextView) findViewById(R.id.textView);
        dataCommunicator = new DataCommunicator(this,this);
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
        dataCommunicator.Connect(true);
    }
    @Override
    protected void onStop()
    {
        dataCommunicator.Connect(false);
        super.onStop();
    }

    @Override
    public void onMessageReceived(String msg)
    {
        mTextView.setText(msg);
    }
}
