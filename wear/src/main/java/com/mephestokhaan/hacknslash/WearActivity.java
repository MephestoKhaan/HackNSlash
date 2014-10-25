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
import android.widget.TextView;

import com.mephestokhaan.fft.RealDoubleFFT;

public class WearActivity extends Activity implements SensorEventListener, MessageReceiverListener
{

    private RealDoubleFFT transformer;
    private int blockSize = 256;
    private boolean recordingStarted = false;
    private AudioAnalyzer audioAnalyzerTask;

    private DrawView accelerationView;
    private DrawView audioView;
    private TextView mTextView;

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
                mTextView = (TextView) stub.findViewById(R.id.text);
                audioView = (DrawView) stub.findViewById(R.id.audioView);
                accelerationView = (DrawView) stub.findViewById(R.id.accelerationView);

                audioView.SetProperties(Color.RED, true);
                accelerationView.SetProperties(Color.BLUE,false);
            }
        });


        transformer = new RealDoubleFFT(blockSize);

        recordingStarted = true;
        audioAnalyzerTask = new AudioAnalyzer();
        audioAnalyzerTask.execute();

        dataCommunicator = new DataCommunicator(this,this);
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        registerDetector();
        dataCommunicator.Connect(true);
    }

    @Override
    protected void onStop()
    {
        unregisterDetector();
        dataCommunicator.Connect(false);
        super.onStop();
    }

    @Override
    public void onMessageReceived(String msg)
    {
        mTextView.setText(msg);
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
        dataCommunicator.SendMessage("HIT at " + System.currentTimeMillis());
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
