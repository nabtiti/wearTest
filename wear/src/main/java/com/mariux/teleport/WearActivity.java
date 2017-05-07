package com.mariux.teleport;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WearActivity extends Activity implements SensorEventListener{

    private TextView mTextView;
    TeleportClient mTeleportClient;
    TeleportClient.OnSyncDataItemTask mOnSyncDataItemTask;
    TeleportClient.OnGetMessageTask mMessageTask;

    public static String SERVICE_CALLED_WEAR = "WearListClicked";
    private static final String TAG = "MainActivity";
    private TextView mTextViewStepCount;
    private TextView mTextViewStepDetect;
    private TextView mTextViewHeart;
    private TextView  mTextViewgravity;
    private boolean mResolvingError=false;
/////////////



    float SHAKE_THRESHOLD=3000;
    float last_x ;
    float last_y ;
    float last_z ;
    int flag =0;
    long flagTime =0L;
   long lastUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);



//        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
//        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
//            @Override
//            public void onLayoutInflated(WatchViewStub stub) {
//                mTextView = (TextView) stub.findViewById(R.id.text);
//            }
//        });

        //instantiate the TeleportClient with the application Context
        mTeleportClient = new TeleportClient(this);

        //Create and initialize task
        mOnSyncDataItemTask = new ShowToastOnSyncDataItemTask();
        mMessageTask = new ShowToastFromOnGetMessageTask();


        //let's set the two task to be executed when an item is synced or a message is received
        mTeleportClient.setOnSyncDataItemTask(mOnSyncDataItemTask);
        mTeleportClient.setOnGetMessageTask(mMessageTask);


        //alternatively, you can use the Builders like indicated here for SyncData and Message

        /*
        mTeleportClient.setOnSyncDataItemTaskBuilder(new TeleportClient.OnSyncDataItemTask.Builder() {
            @Override
            public TeleportClient.OnSyncDataItemTask build() {
                return new TeleportClient.OnSyncDataItemTask() {
                    @Override
                    protected void onPostExecute(DataMap result) {
                        String s = result.getString("string");
                        Toast.makeText(getApplicationContext(),"DataItem - "+s,Toast.LENGTH_SHORT).show();
                    }
                };
            }
        });

        */

        /*
        mTeleportClient.setOnGetMessageTaskBuilder(new TeleportClient.OnGetMessageTask.Builder() {
            @Override
            public TeleportClient.OnGetMessageTask build() {
                return new TeleportClient.OnGetMessageTask() {
                    @Override
                    protected void onPostExecute(String path) {
                        Toast.makeText(getApplicationContext(),"Message - "+path,Toast.LENGTH_SHORT).show();
                    }
                };
            }
        });
        */

        getStepCount();
        mTextViewStepCount = (TextView) findViewById(R.id.step_count);
        mTextViewStepDetect = (TextView) findViewById(R.id.step_detect);
        mTextViewHeart = (TextView) findViewById(R.id.heart);
        mTextViewgravity = (TextView) findViewById(R.id.gravity);
       // Button  help = (Button) findViewById(R.id.help);



    }

    @Override
    protected void onStart() {
        super.onStart();
        mTeleportClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTeleportClient.disconnect();

    }

    //Task to show the String from DataMap with key "string" when a DataItem is synced
    public class ShowToastOnSyncDataItemTask extends TeleportClient.OnSyncDataItemTask {

        protected void onPostExecute(DataMap dataMap) {

            String s = dataMap.getString("string");

            Toast.makeText(getApplicationContext(),"DataItem - "+s,Toast.LENGTH_SHORT).show();

            mTeleportClient.setOnSyncDataItemTask(new ShowToastOnSyncDataItemTask());
        }
    }

    //Task that shows the path of a received message
    public class ShowToastFromOnGetMessageTask extends TeleportClient.OnGetMessageTask {

        @Override
        protected void onPostExecute(String  path) {

            Toast.makeText(getApplicationContext(),"Message - "+path,Toast.LENGTH_SHORT).show();

            //let's reset the task (otherwise it will be executed only once)
            mTeleportClient.setOnGetMessageTask(new ShowToastFromOnGetMessageTask());
        }
    }




    public void sendMessage(View v) {

        mTeleportClient.setOnGetMessageTask(new ShowToastFromOnGetMessageTask());

        mTeleportClient.sendMessage("ساعدني", null);
    }

    public void sendStartActivityMessage(View v) {

        mTeleportClient.setOnGetMessageTask(new ShowToastFromOnGetMessageTask());

        mTeleportClient.sendMessage("startActivity", null);
    }


    String gravityMSG = "";

    private void getStepCount() {
        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        Sensor mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor mStepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Sensor sensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            String msg = "" + (int)event.values[0];
            mTextViewHeart.setText(msg);
            Log.d(TAG, msg);
        }
        else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            String msg = "Count: " + (int)event.values[0];
            mTextViewStepCount.setText(msg);
            Log.d(TAG, msg);
        }
        else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            String msg = "Detected at " + currentTimeStr();
            mTextViewStepDetect.setText(msg);
            Log.d(TAG, msg);
        }

        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime= System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float  x = event.values[SensorManager.DATA_X];
                float  y = event.values[SensorManager.DATA_Y];
                float  z = event.values[SensorManager.DATA_Z];

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    if (flagTime<(curTime-60*1000))
                    {
                        flag=0;
                        flagTime=curTime;
                    }
                    ++flag;
                    Log.d("sensor", "shake detected w/ speed: " + speed+":flag="+flag);
                    mTextViewgravity.setText("Shack Count: "+flag);
                    //Toast.makeText(this, "shake detected w/ speed: " + speed+":flag="+flag, Toast.LENGTH_SHORT).show();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }


//            gravityMSG=":"+event.values[0]+":\n"+event.values[1]+":\n"+event.values[2];
//
//            mTextViewgravity.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mTextViewgravity.setText(gravityMSG);
//                }
//            },100);
//
//            Log.d(TAG, gravityMSG);
        }
        else
            Log.d(TAG, "Unknown sensor type");
    }
        private String currentTimeStr() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(c.getTime());
    }

}
