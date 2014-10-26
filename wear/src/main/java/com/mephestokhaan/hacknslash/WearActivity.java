package com.mephestokhaan.hacknslash;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mephestokhaan.fft.RealDoubleFFT;

public class WearActivity extends Activity implements SensorEventListener, MessageReceiverListener
{
    private boolean BLACK_WATCH = true;

    private RealDoubleFFT transformer;
    private int blockSize = 256;
    private boolean recordingStarted = false;
    private AudioAnalyzer audioAnalyzerTask;

    private DrawView accelerationView;
    private DrawView audioView;
    private TextView livesText;
    private TextView mesageText;
    private ImageButton repeatButton;
    private RelativeLayout rootLayout;

    private SensorManager mSensorManager;
    private SlashDetector slashDetector = new SlashDetector();
    private DataCommunicator dataCommunicator;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
        {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                livesText = (TextView) stub.findViewById(R.id.livesTextView);
                mesageText = (TextView) stub.findViewById(R.id.mesageText);
                audioView = (DrawView) stub.findViewById(R.id.audioView);
                accelerationView = (DrawView) stub.findViewById(R.id.accelerationView);
                repeatButton = (ImageButton) stub.findViewById(R.id.imageButton);
                rootLayout = (RelativeLayout) stub.findViewById(R.id.rootlayout);
                setLionMode(!BLACK_WATCH);
            }
        });


        transformer = new RealDoubleFFT(blockSize);

        recordingStarted = true;
        audioAnalyzerTask = new AudioAnalyzer();
        audioAnalyzerTask.execute();

        dataCommunicator = new DataCommunicator(this,this);
    }

    private void setLionMode(boolean lionMode)
    {
        audioView.SetProperties(lionMode? Color.YELLOW : Color.RED, true);
        accelerationView.SetProperties(lionMode ? Color.BLUE : Color.GREEN, false);
        repeatButton.setImageResource(lionMode ? R.drawable.replayyellow : R.drawable.replayred);
        rootLayout.setBackgroundResource(lionMode ? R.drawable.lion_logo : R.drawable.cocodrile_logo);
        slashDetector.audioThresold = lionMode?0.6f : 0.6f;
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        recordingStarted = true;
        registerDetector();
        dataCommunicator.Connect(true);
    }

    @Override
    protected void onStop()
    {
        recordingStarted = false;
        unregisterDetector();
        dataCommunicator.Connect(false);
        super.onStop();
    }

    @Override
    public void onMessageReceived(String msg)
    {
        if(msg.contains("start"))
        {
            repeatButton.setVisibility(View.INVISIBLE);
            mesageText.setText("");
        }
        if(msg.contains("lives"))
        {
            livesText.setText(msg.split(":")[1]);
        }
        if(msg.contains("lose"))
        {
            repeatButton.setVisibility(View.VISIBLE);
            mesageText.setText("YOU LOSE");
        }
        if(msg.contains("win"))
        {
            repeatButton.setVisibility(View.VISIBLE);
            mesageText.setText("YOU WIN");
        }
    }

    public void requestRepeat(View v)
    {
        dataCommunicator.SendMessage("repeat");
    }

    private void registerDetector()
    {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterDetector()
    {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float acceleration = (float)Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2)) - 9.81f;
        acceleration /= 10f;
       if( slashDetector.AddAccelerationPeak(acceleration))
       {
           SendHit();
       }
        if(accelerationView != null) {
            accelerationView.SetPercentage(acceleration);
        }
    }

    public void SendHit()
    {
        dataCommunicator.SendMessage("slash");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    private class AudioAnalyzer extends AsyncTask<Void, double[], Void>
    {
        private AudioRecord audioRecord;
        private int frequency = 8000;
        private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        @Override
        protected Void doInBackground(Void... params) {

            if(isCancelled()){
                return null;
            }
            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT, frequency,
                    channelConfiguration, audioEncoding, bufferSize);
            int bufferReadResult;
            short[] buffer = new short[blockSize];
            double[] toTransform = new double[blockSize];
            try{
                audioRecord.startRecording();
            }
            catch(IllegalStateException e){
                Log.e("Recording failed", e.toString());

            }
            while (recordingStarted) {
                bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                if(isCancelled())
                    break;

                for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                    toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                }

                transformer.ft(toTransform);
                publishProgress(toTransform);
                if(isCancelled()) {
                    break;
                }
            }

            try{
                audioRecord.stop();
            }
            catch(IllegalStateException e){
                Log.e("Stop failed", e.toString());

            }

            return null;
        }

        protected void onProgressUpdate(double[]... toTransform)
        {
            float averageLowFreqAudio = (float)toTransform[0][1];
            averageLowFreqAudio = Math.abs(averageLowFreqAudio);
            if(slashDetector.AddAudioPeak(averageLowFreqAudio))
            {
                SendHit();
            }
            if(audioView != null)
            {
                audioView.SetPercentage(averageLowFreqAudio);
            }

        }

        protected void onPostExecute(Void result) {
            try{
                audioRecord.stop();
            }
            catch(IllegalStateException e){
                Log.e("Stop failed", e.toString());

            }
            audioAnalyzerTask.cancel(true);
        }

    }

}
