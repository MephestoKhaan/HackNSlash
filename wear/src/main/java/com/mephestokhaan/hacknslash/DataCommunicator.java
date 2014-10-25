package com.mephestokhaan.hacknslash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


/**
 * Created by MephestoKhaan on 25/10/2014.
 */
public class DataCommunicator implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    GoogleApiClient googleClient;
    MessageReceiverListener messageDelegate;
    public DataCommunicator(Context context, MessageReceiverListener delegate)
    {
        messageDelegate = delegate;
        googleClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(context).registerReceiver(messageReceiver, messageFilter);
    }

    public void Connect(boolean connect)
    {
        if(googleClient == null) {
            return;
        }

        if(connect)
        {
            googleClient.connect();
        }
        else if(googleClient.isConnected())
        {
            googleClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        String message = "Hello wearable\n Via the data layer";
        new SendToDataLayerThread("/message_path", message).start();
    }
    @Override
    public void onConnectionSuspended(int cause){}
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){}

    public class MessageReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String message = intent.getStringExtra("message");
            if(messageDelegate != null)
            {
                messageDelegate.onMessageReceived(message);
            }
        }
    }

    class SendToDataLayerThread extends Thread
    {
        String path;
        String message;

        SendToDataLayerThread(String p, String msg)
        {
            path = p;
            message = msg;
        }

        public void run() {

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes())
            {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.toString(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess())
                {
                    Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                }
                else
                {
                    Log.v("myTag", "ERROR: failed to send Message");
                }
            }
        }
    }


}
