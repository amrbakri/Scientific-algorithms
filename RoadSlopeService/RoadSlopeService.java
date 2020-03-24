package com.example.com.ecoassistant_03;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by amrbak on 23.06.2016.
 */
public class RoadSlopeService extends Service implements SensorEventListener {

    private final String TAG = this.getClass().getSimpleName();

    public final static String RoadSlopeService_INTENT_ACTION_ROAD_SLOPE_MEASURED = "Action_Road_Slope_Measured";
    public final static String RoadSlopeService_INTENT_KEY_ROAD_SLOPE_Value = "Key_Road_Slope_Value";
    private BCRCtrl mBCRCtrl = null;
    private String mAbsoluteVelocity = "NAN";
    private int mRFCommState = CSPPCtrl.RFCOMM_CONNECTED;
    private ATRoadSlope mATRoadSlope = null;

    private Timer mTimerRoadSlope = null;
    private String mUpdatedPrevSpeed = null;
    private String mUpdatedCurrSpeed = null;
    private double mSlopeAngle;
    private boolean mFixedTimerInitiated = false;
    private boolean mScheduledTimerInitiated = false;

    private float mAccX;
    private float mAccY;
    private float mAccZ;

    private long mSamplingRate;

    /**
     * this is the time sampling rate
     */
    private double dt = 1;

    private SensorManager mSensMgr = null;
    private Sensor mSensAcc = null;

    /**
     * a broadcast receiver that receives the absolute velocity from SQAURELL. this receiver will be executed once the SPP_SERVICE_INTENT_ACTION_ACTUAL_SPEED 
	 is sent
     */
    BroadcastReceiver mBCR_ABSOLUTE_VELOCTY_CHANGED = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(SPPService.SPP_SERVICE_INTENT_ACTION_ACTUAL_SPEED)) {
                mAbsoluteVelocity = intent.getStringExtra(SPPService.SPP_SERVICE_INTENT_KEY_ACTUAL_SPEED);

                /*if (mAbsoluteVelocity != null && !mAbsoluteVelocity.isEmpty()) {
                    //Log.d(TAG, SubTag.bullet("mBCR_ABSOLUTE_VELOCTY_CHANGED", "mAbsoluteVelocity: " + mAbsoluteVelocity));
                }*/
            }
        }
    };

    /**
     * a broadcast receiver that receives the update rate "Sampling Rate" at which CANbus data are sent. this receiver will be executed once the SPP_SERVICE_INTENT_ACTION_TIME_STAMP is
     * sent
     */
    BroadcastReceiver mBCR_CAN_UPDATE_RATE = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(SPPService.SPP_SERVICE_INTENT_ACTION_CAN_UPDATE_RATE)) {
                mSamplingRate = intent.getLongExtra(SPPService.SPP_SERVICE_INTENT_KEY_CAN_UPDATE_RATE, -1);

                /*if (mAbsoluteVelocity != null && !mAbsoluteVelocity.isEmpty()) {
                    //Log.d(TAG, SubTag.bullet("mBCR_ABSOLUTE_VELOCTY_CHANGED", "mAbsoluteVelocity: " + mAbsoluteVelocity));
                }*/
            }
        }
    };

    private boolean mInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate");

        this.mBCRCtrl = new BCRCtrl(this);

        if (!this.mBCRCtrl.isReg(this.mBCR_ABSOLUTE_VELOCTY_CHANGED)) {
            this.mBCRCtrl.reg(this.mBCR_ABSOLUTE_VELOCTY_CHANGED, SPPService.SPP_SERVICE_INTENT_ACTION_ACTUAL_SPEED);
        } else {
            Log.d(TAG, SubTag.msg("onCreate", "[mBCR_ABSOLUTE_VELOCTY_CHANGED] " + this.getResources().getString(com.example.com.ecoassistant_03.R.string.str_bcr_reg_error)));
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mBCR_CAN_UPDATE_RATE, new IntentFilter(SPPService.SPP_SERVICE_INTENT_ACTION_CAN_UPDATE_RATE));

        this.mSensMgr = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        this.mSensAcc = this.mSensMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mSensMgr.registerListener(this, this.mSensAcc, SensorManager.SENSOR_DELAY_NORMAL);

        this.mTimerRoadSlope = new Timer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand");

        if (!this.mInitialized) {
            this.mInitialized = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mATRoadSlope = new ATRoadSlope();
                mATRoadSlope.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mATRoadSlope = new ATRoadSlope();
                mATRoadSlope.execute();
            }
        }

        int appStatus = intent.getIntExtra(ActMain.ActMain_INTENT_KEY_APP_STATUS, -1);
        if (appStatus == ActMain.sAppExited) {

            if (mATRoadSlope != null && mATRoadSlope.getStatus() == AsyncTask.Status.RUNNING) {
                mATRoadSlope.cancel(true);
            }
            mTimerRoadSlope.purge();
            mTimerRoadSlope.cancel();
            stopSelf();
        }

        return Service.START_NOT_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.w(TAG, "onBind");

        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy");

        if (mBCRCtrl.isReg(mBCR_ABSOLUTE_VELOCTY_CHANGED)) {
            Log.d(TAG, SubTag.bullet("unregFMSMsgReceiver", "mBCRCtrl.unreg(mBCR_ABSOLUTE_VELOCTY_CHANGED)"));
            mBCRCtrl.unreg(mBCR_ABSOLUTE_VELOCTY_CHANGED);
        } else {
            Log.e(TAG, SubTag.bullet("unregFMSMsgReceiver", "[mBCR_ABSOLUTE_VELOCTY_CHANGED]" + this.getResources().getString(R.string.str_bcr_unreg_error)));
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBCR_CAN_UPDATE_RATE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccX = event.values[0];
            mAccY = event.values[1];
            mAccZ = event.values[2];

            /*Log.d(TAG, SubTag.bullet("onSensorChanged", "AccX: " + mAccX));
            Log.d(TAG, SubTag.bullet("onSensorChanged", "AccY: " + mAccY));
            Log.d(TAG, SubTag.bullet("onSensorChanged", "AccZ: " + mAccZ));*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * the background thread of this asynctask will be executed as long as the absolute velocity transmitted via SQUARELL is NAN, else it will finish executing and the a timer
     * will start to calculate the slope of the road
     * will be updated
     */
    private class ATRoadSlope extends AsyncTask<Void, Void, Void> {

        private int mSleepTime = getApplicationContext().getResources().getInteger(R.integer.int_sleep_time);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.w(TAG, SubTag.msg("ATRoadSlope.onPreExecute"));
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.w(TAG, SubTag.msg("ATRoadSlope.doInBackground"));

            while (!isCancelled() && mAbsoluteVelocity.equals("NAN")) {
                Log.d(TAG, SubTag.bullet("ATRoadSlope.doInBackground", "waiting to receive Vehicle velocity"));
                SystemClock.sleep(this.mSleepTime);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.w(TAG, SubTag.msg("ATRoadSlope.onCancelled"));

            //stopSelf();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.w(TAG, SubTag.msg("ATRoadSlope.onPostExecute"));

            if (mTimerRoadSlope != null) {
                mUpdatedPrevSpeed = mAbsoluteVelocity;
                mTimerRoadSlope.scheduleAtFixedRate(new UpdateRoadSlope(mUpdatedPrevSpeed, mAbsoluteVelocity), 0, (1000));
            }
        }

        private class UpdateRoadSlope extends TimerTask {

            private double mCurrentVelocity;
            private double mPreviousVelocity;

            public UpdateRoadSlope(String previousVelocity, String currentVelocity) {
                this.mPreviousVelocity = Double.valueOf(previousVelocity);
                this.mCurrentVelocity = Double.valueOf(currentVelocity);

                Log.d(TAG, SubTag.bullet("UpdateRoadSlope", "this.mPreviousVelocity: " + this.mPreviousVelocity));
                Log.d(TAG, SubTag.bullet("UpdateRoadSlope", "this.mCurrentVelocity: " + this.mCurrentVelocity));
            }

            @Override
            public void run() {
                mSlopeAngle = Math.asin(((double) mAccY - (this.mCurrentVelocity - this.mPreviousVelocity)/mSamplingRate) / 9.81) * (180 / Math.PI);
                mUpdatedPrevSpeed = String.valueOf(this.mCurrentVelocity);

                Intent intRoadSlopeBroadcast = new Intent(RoadSlopeService.RoadSlopeService_INTENT_ACTION_ROAD_SLOPE_MEASURED);
                intRoadSlopeBroadcast.putExtra(RoadSlopeService.RoadSlopeService_INTENT_KEY_ROAD_SLOPE_Value, mSlopeAngle);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intRoadSlopeBroadcast);

                Log.d(TAG, SubTag.bullet("UpdateRoadSlope", "mSlopeAngle: " + mSlopeAngle));
            }
        }
    }
}
