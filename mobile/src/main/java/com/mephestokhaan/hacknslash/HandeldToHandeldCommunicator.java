package com.mephestokhaan.hacknslash;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by MephestoKhaan on 25/10/2014.
 */

class HandeldToHandeldCommunicator
{
    private Context context;
    private String IP, PORT;
    private boolean listening = true;

    private UDPReceiver receiver = new UDPReceiver();

    public HandeldToHandeldCommunicator(String ip, String port, Context context)
    {
        this.IP = ip;
        this.PORT = port;
        this.context = context;
        this.listening = true;

        receiver.execute();

    }

    public void Stop()
    {
        listening = false;
        receiver.cancel(true);
    }

    public void SendMessage(String message)
    {
        new UDPSender().execute(message);
    }

    class UDPSender extends AsyncTask<String, Void, Void>
    {
        protected Void doInBackground(String... urls)
        {
            if (urls.length > 0)
            {
                try
                {
                    String ip = IP;
                    int port = Integer.parseInt(PORT);
                    String message = urls[0];
                    sendOverUDP(InetAddress.getByName(ip), port, message);
                } catch (IOException e) {
                    Log.e("SEND", "error sendig " + urls[0]);
                }
            }

            return null;
        }

        private void sendOverUDP(InetAddress IP, int PORT, String message) throws IOException {

            DatagramSocket socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), IP, PORT);
            socket.send(packet);
            socket.close();
        }
    };

    class UDPReceiver extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            byte[] lMsg = new byte[4096];
            DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
            DatagramSocket ds = null;

            try
            {
                ds = new DatagramSocket(Integer.parseInt(PORT));

                while(listening)
                {
                    ds.receive(dp);

                    Intent messageIntent = new Intent();
                    messageIntent.putExtra("handheld", new String(lMsg, 0, dp.getLength()));
                    messageIntent.setAction(Intent.ACTION_SEND);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (ds != null)
                {
                    ds.close();
                }
            }

            return null;
        }
    };

}


