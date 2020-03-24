package com.example.com.ecoassistant_03;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by amrbak on 16.02.2016.
 */
public class SPPService extends Service {

    //constants
    private final String TAG = this.getClass().getSimpleName();

    public final static String SPP_SERVICE_INTENT_ACTION_RFCOMM_CONNECTION_STATE_CAHNGED = "ACTION_RFCOMM_CONNECTION_STATE_CAHNGED";
    public final static String SPP_SERVICE_INTENT_KEY_RFCOMM_CONNECTION_STATE = "KEY_RFCOMM_CONNECTION_STATE";

    public final static String SPP_SERVICE_INTENT_ACTION_CAN_UPDATE_RATE = "ACTION_RFCOMM_TIME_STAMP";
    public final static String SPP_SERVICE_INTENT_KEY_CAN_UPDATE_RATE = "KEY_TIME_STAMP";

    //non FMS Msg
    public final static String SPP_SERVICE_INTENT_ACTION_NON_FMS_MSG = "ACTION_NON_FMS_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_NON_FMS_MSG = "KEY_NON_FMS_MSG_ARRIVED";

    //FMS Msg
    public final static String SPP_SERVICE_INTENT_ACTION_FULL_FMS_1 = "ACTION_FULL_FMS_MSG_1_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_FULL_FMS_1 = "KEY_FULL_FMS_MSG_1_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_FULL_FMS_2 = "ACTION_FULL_FMS_MSG_2_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_FULL_FMS_2 = "KEY_FULL_FMS_MSG_2_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_FULL_FMS_3 = "ACTION_FULL_FMS_MSG_3_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_FULL_FMS_3 = "KEY_FULL_FMS_MSG_3_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_FULL_FMS_4 = "ACTION_FULL_FMS_MSG_4_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_FULL_FMS_4 = "KEY_FULL_FMS_MSG_4_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_FULL_FMS_7 = "ACTION_FULL_FMS_MSG_7_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_FULL_FMS_7 = "KEY_FULL_FMS_MSG_7_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_FULL_FMS_8 = "ACTION_FULL_FMS_MSG_8_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_FULL_FMS_8 = "KEY_FULL_FMS_MSG_8_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_FULL_FMS_12 = "ACTION_FULL_FMS_MSG_12_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_FULL_FMS_12 = "KEY_FULL_FMS_MSG_12_ARRIVED";

    //FSM_1 Parameters
    public final static String SPP_SERVICE_INTENT_ACTION_ODOMETER = "ACTION_ODOMETER_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_ODOMETER = "KEY_ODOMETER_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_TFU = "ACTION_TFU_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_TFU = "KEY_TFU_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_ENGINE_HOURS = "ACTION_ENGINE_HOURS_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_ENGINE_HOURS = "KEY_ENGINE_HOURS_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_ACTUAL_SPEED = "ACTION_ACTUAL_SPEED_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_ACTUAL_SPEED = "KEY_ENGINE_ACTUAL_SPEED_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_ENGINE_SPEED = "ACTION_ENGINE_SPEED_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_ENGINE_SPEED = "KEY_ENGINE_SPEED_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_ENGINE_TORQUE = "ACTION_ENGINE_TORQUE_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_ENGINE_TORQUE = "KEY_ENGINE_TORQUE_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_DOWN_SWITCH = "ACTION_DOWN_SWITCH_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_DOWN_SWITCH = "KEY_DOWN_SWITCH_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_ACC_PEDAL_POS = "ACTION_ACC_PEDAL_POS_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_ACC_PEDAL_POS = "KEY_ACC_PEDAL_POS_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_BRAKE_SWITCH = "ACTION_BRAKE_SWITCH_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_BRAKE_SWITCH = "KEY_BRAKE_SWITCH_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_CLUTCH_SWITCH = "ACTION_CLUTCH_SWITCH_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_CLUTCH_SWITCH = "KEY_CLUTCH_SWITCH_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_CRUISE_ACTIVE = "ACTION_CRUISE_ACTIVE_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_CRUISE_ACTIVE = "KEY_CRUISE_ACTIVE_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_PTO = "ACTION_PTO_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_PTO = "KEY_PTO_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_FUEL_LEVEL = "ACTION_FUEL_LEVEL_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_FUEL_LEVEL = "KEY_FUEL_LEVEL_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_ENGINE_TEMP = "ACTION_ENGINE_TEMP_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_ENGINE_TEMP = "KEY_ENGINE_TEMP_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_TURBO_PRESSURE = "ACTION_TURBO_PRESSURE_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_TURBO_PRESSURE = "KEY_TURBO_PRESSURE_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_AXLE_WEIGHT_0 = "ACTION_AXLE_WEIGHT_0_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_AXLE_WEIGHT_0 = "KEY_AXLE_WEIGHT_0_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_AXLE_WEIGHT_1 = "ACTION_AXLE_WEIGHT_1_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_AXLE_WEIGHT_1 = "KEY_AXLE_WEIGHT_1_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_AXLE_WEIGHT_2 = "ACTION_AXLE_WEIGHT_2_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_AXLE_WEIGHT_2 = "KEY_AXLE_WEIGHT_2_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_AXLE_WEIGHT_3 = "ACTION_AXLE_WEIGHT_3_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_AXLE_WEIGHT_3 = "KEY_AXLE_WEIGHT_3_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_ACTION_SERVICE_DISTANCE = "ACTION_SERVICE_DISTANCE_MSG_ARRIVED";
    public final static String SPP_SERVICE_INTENT_KEY_SERVICE_DISTANCE = "KEY_SERVICE_DISTANCE_MSG_ARRIVED";

    //objects
    private BTCtrl mBTCtrl = null;
    private BCRCtrl mBCRCtrl = null;
    private CSPPCtrl mSPPCtrl = null;
    private ATSPPConnect mATSPPConnect = null;
    private Intent mintEKFService = null;
    private Intent mintRoadSlopService = null;

    private long mStartTime = 0;

    //variabes
    private boolean mIsACL_Connected = false;
    private boolean mIsCancelled = false;

    private BroadcastReceiver mBCR_ACL_CONNECTED = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, SubTag.msg("mBCR_ACL_CONNECTED", "BluetoothDevice.ACTION_ACL_CONNECTED"));

            mIsACL_Connected = true;
            Log.d(TAG, SubTag.bullet("mBCR_ACL_CONNECTED", "mIsACL_Connected: " + mIsACL_Connected));

            BluetoothDevice btDevice = ((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            String state = mBTCtrl.interpretBondState(btDevice.getBondState());
        }
    };
    private BroadcastReceiver mBCR_ACL_DISCONNECT_REQUESTED = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, SubTag.msg("mBCR_ACL_DISCONNECT_REQUESTED", "BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED"));

            BluetoothDevice btDevice = ((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            String state = mBTCtrl.interpretBondState(btDevice.getBondState());
        }
    };
    private BroadcastReceiver mBCR_ACL_DISCONNECTED = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, SubTag.msg("mBCR_ACL_DISCONNECTED", "BluetoothDevice.ACTION_ACL_DISCONNECTED"));

            mIsACL_Connected = false;
            Log.d(TAG, SubTag.bullet("mBCR_ACL_CONNECTED", "mIsACL_Connected: " + mIsACL_Connected));

            BluetoothDevice btDevice = ((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            String state = mBTCtrl.interpretBondState(btDevice.getBondState());
        }
    };

    /**
     *cal this method if you want to cancel the ATSPPConnect which is responsible for connecting to SPP
     */
    private void cancelATSPPConnect() {
        Log.w(TAG, SubTag.msg("cancelATSPPConnect"));

        if (this.mATSPPConnect != null && this.mATSPPConnect.getStatus() != AsyncTask.Status.FINISHED) {
            Log.v(TAG, SubTag.msg("cancelATSPPConnect", "mATSPPConnect will be cancelled"));

            try {
                new CancelATSPPConnect().execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * to execute the asynctask responsible for disconnecting the BT connectivity
     */
    private void rfcommDisconnect() {
        Log.w(TAG, SubTag.msg("rfcommDisconnect"));

        if (this.mSPPCtrl.isRFCSocketConnected() == CSPPCtrl.RFCOMM_CONNECTED) {

            new ATRFCommDisconnect().execute();
            /*try {
                new ATRFCommDisconnect().execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate");

        this.mBTCtrl = new BTCtrl(this);
        this.mBCRCtrl = new BCRCtrl(this);
        this.mSPPCtrl = new CSPPCtrl(this, this.getResources().getString(R.string.spp_mac), this.getResources().getString(R.string.spp_uuid));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand");

        this.mATSPPConnect = new ATSPPConnect();
        this.mATSPPConnect.execute();

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

        if (this.mSPPCtrl != null) {
            this.mSPPCtrl.tx("!DIROF");
            Toast.makeText(this, "Disconnecting RFComm Socket", Toast.LENGTH_SHORT).show();
            this.rfcommDisconnect();
            Toast.makeText(this, "RFComm Socket Disconnected", Toast.LENGTH_SHORT).show();

            Intent intRFCommState = new Intent();
            intRFCommState.setAction(SPP_SERVICE_INTENT_ACTION_RFCOMM_CONNECTION_STATE_CAHNGED);
            intRFCommState.putExtra(SPP_SERVICE_INTENT_KEY_RFCOMM_CONNECTION_STATE, CSPPCtrl.RFCOMM_DISCONNECTED);
            sendBroadcast(intRFCommState);
        }
        this.mIsCancelled = true;
        this.cancelATSPPConnect();

    }

    /**
     * connecting to RFCOMM blocks, so the .connect method will be called on a background thread
     */
    private class ATSPPConnect extends AsyncTask<Void, Void, Void> {

        private int mWaitTime = getApplicationContext().getResources().getInteger(R.integer.int_long_max_wait_time);
        private int mSleepTime = getApplicationContext().getResources().getInteger(R.integer.int_sleep_time);

        private boolean mRFCSocketConnected = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.w(TAG, SubTag.msg("ATSPPConnect.onPreExecute"));

            if (!mBCRCtrl.isReg(mBCR_ACL_CONNECTED)) {
                mBCRCtrl.reg(mBCR_ACL_CONNECTED, BluetoothDevice.ACTION_ACL_CONNECTED);
            } else {
                Log.d(TAG, SubTag.msg("ATSPPBond.onPreExecute", "[mBCR_ACL_CONNECTED] " + getApplicationContext().getResources().getString(R.string.str_bcr_reg_error)));
            }
            if (!mBCRCtrl.isReg(mBCR_ACL_DISCONNECT_REQUESTED)) {
                mBCRCtrl.reg(mBCR_ACL_DISCONNECT_REQUESTED, BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            } else {
                Log.d(TAG, SubTag.msg("ATSPPBond.onPreExecute", "[mBCR_ACL_DISCONNECT_REQUESTED] " + getApplicationContext().getResources().getString(R.string.str_bcr_reg_error)));
            }
            if (!mBCRCtrl.isReg(mBCR_ACL_DISCONNECTED)) {
                mBCRCtrl.reg(mBCR_ACL_DISCONNECTED, BluetoothDevice.ACTION_ACL_DISCONNECTED);
            } else {
                Log.d(TAG, SubTag.msg("ATSPPBond.onPreExecute", "[mBCR_ACL_DISCONNECTED] " + getApplicationContext().getResources().getString(R.string.str_bcr_reg_error)));
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.w(TAG, SubTag.msg("ATSPPConnect.doInBackground"));

            int waitTime = this.mWaitTime;
            while (!isCancelled() && ((this.mRFCSocketConnected = mSPPCtrl.rfcConnect()) != true) && waitTime > 0) {
                Log.d(TAG, SubTag.bullet("ATSPPConnect.doInBackground", "wait for SPP to connect: " + ((getApplicationContext().getResources().getInteger(R.integer.int_long_max_wait_time) - waitTime)) / 1000 + " seconds"));

                SystemClock.sleep(this.mSleepTime);
                waitTime -= this.mSleepTime;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.w(TAG, SubTag.msg("ATSPPConnect.onPostExecute"));

            mIsACL_Connected = false;

            if (mBCRCtrl.isReg(mBCR_ACL_CONNECTED)) {
                mBCRCtrl.unreg(mBCR_ACL_CONNECTED);
            } else {
                Log.e(TAG, SubTag.bullet("ATSPPConnect.onPostExecute", "[mBCR_ACL_CONNECTED]" + getApplicationContext().getResources().getString(R.string.str_bcr_unreg_error)));
            }
            if (mBCRCtrl.isReg(mBCR_ACL_DISCONNECT_REQUESTED)) {
                mBCRCtrl.unreg(mBCR_ACL_DISCONNECT_REQUESTED);
            } else {
                Log.e(TAG, SubTag.bullet("ATSPPConnect.onPostExecute", "[mBCR_ACL_DISCONNECT_REQUESTED]" + getApplicationContext().getResources().getString(R.string.str_bcr_unreg_error)));
            }
            if (mBCRCtrl.isReg(mBCR_ACL_DISCONNECTED)) {
                mBCRCtrl.unreg(mBCR_ACL_DISCONNECTED);
            } else {
                Log.e(TAG, SubTag.bullet("ATSPPConnect.onPostExecute", "[mBCR_ACL_DISCONNECTED]" + getApplicationContext().getResources().getString(R.string.str_bcr_unreg_error)));
            }

            if (this.mRFCSocketConnected) {
                Log.i(TAG, SubTag.bullet("ATSPPConnect.onPostExecute", "mRFCSocketConnected: " + this.mRFCSocketConnected));

                /*RFCommSentinelThread rfcommSentinelThread = new RFCommSentinelThread();
                rfcommSentinelThread.start();*/

                mintEKFService = new Intent(SPPService.this, EKFService.class);
                startService(mintEKFService);

                mintRoadSlopService = new Intent(SPPService.this, RoadSlopeService.class);
                startService(mintRoadSlopService);

                SPPThread sppThread = new SPPThread();
                sppThread.start();
                sppThread.tx("!DIRON");

                Intent intRFCommState = new Intent();
                intRFCommState.setAction(SPP_SERVICE_INTENT_ACTION_RFCOMM_CONNECTION_STATE_CAHNGED);
                intRFCommState.putExtra(SPP_SERVICE_INTENT_KEY_RFCOMM_CONNECTION_STATE, CSPPCtrl.RFCOMM_CONNECTED);
                sendBroadcast(intRFCommState);

            } else {
                Log.i(TAG, SubTag.bullet("ATSPPConnect.onPostExecute", "mRFCSocketConnected: " + this.mRFCSocketConnected));
                Log.i(TAG, SubTag.bullet("ATSPPConnect.onPostExecute", "SPPService will stop."));
                stopSelf();
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.w(TAG, SubTag.msg("ATSPPConnect.onCancelled"));

            mIsACL_Connected = false;

            if (mBCRCtrl.isReg(mBCR_ACL_CONNECTED)) {
                mBCRCtrl.unreg(mBCR_ACL_CONNECTED);
            } else {
                Log.e(TAG, SubTag.bullet("ATSPPConnect.onPostExecute", "[mBCR_ACL_CONNECTED]" + getApplicationContext().getResources().getString(R.string.str_bcr_unreg_error)));
            }
            if (mBCRCtrl.isReg(mBCR_ACL_DISCONNECT_REQUESTED)) {
                mBCRCtrl.unreg(mBCR_ACL_DISCONNECT_REQUESTED);
            } else {
                Log.e(TAG, SubTag.bullet("ATSPPConnect.onPostExecute", "[mBCR_ACL_DISCONNECT_REQUESTED]" + getApplicationContext().getResources().getString(R.string.str_bcr_unreg_error)));
            }
            if (mBCRCtrl.isReg(mBCR_ACL_DISCONNECTED)) {
                mBCRCtrl.unreg(mBCR_ACL_DISCONNECTED);
            } else {
                Log.e(TAG, SubTag.bullet("ATSPPConnect.onPostExecute", "[mBCR_ACL_DISCONNECTED]" + getApplicationContext().getResources().getString(R.string.str_bcr_unreg_error)));
            }
        }
    }

    /**
     * to disconnect the RFCOMM. this operation is done on the background thread
     */
    private class ATRFCommDisconnect extends AsyncTask<Void, Void, Void> {

        private int mWaitTime = getApplicationContext().getResources().getInteger(R.integer.int_short_max_wait_time);
        private int mSleepTime = getApplicationContext().getResources().getInteger(R.integer.int_sleep_time);

        @Override
        protected Void doInBackground(Void... params) {
            Log.w(TAG, SubTag.msg("ATRFCommDisconnect.doInBackground"));

            mSPPCtrl.disconnectRFCSocket();

            int waitTime = this.mWaitTime;
            while ((mSPPCtrl.isRFCSocketConnected() != CSPPCtrl.RFCOMM_DISCONNECTED) && waitTime > 0) {
                Log.v(TAG, SubTag.bullet("ATRFCommDisconnect.doInBackground", "wait for disconnecting RFComm Socket: " + ((getApplicationContext().getResources().getInteger(R.integer.int_short_max_wait_time) - waitTime)) / 1000 + " seconds"));
                SystemClock.sleep(this.mSleepTime);
                waitTime -= this.mSleepTime;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.w(TAG, SubTag.msg("ATRFCommDisconnect.onPostExecute"));

            switch (mSPPCtrl.isRFCSocketConnected()) {
                case CSPPCtrl.RFCOMM_CONNECTED:
                    Log.wtf(TAG, SubTag.bullet("ATRFCommDisconnect", "RFComm socket still connected"));
                    break;

                case CSPPCtrl.RFCOMM_DISCONNECTED:
                    Log.v(TAG, SubTag.bullet("ATRFCommDisconnect", "RFComm socket disconnected"));

                    /*Intent intRFCommState = new Intent();
                    intRFCommState.setAction(SPP_SERVICE_INTENT_ACTION_RFCOMM_CONNECTION_STATE_CAHNGED);
                    intRFCommState.putExtra(SPP_SERVICE_INTENT_KEY_RFCOMM_CONNECTION_STATE, mSPPCtrl.isRFCSocketConnected());
                    sendBroadcast(intRFCommState);*/

                    break;

                case CSPPCtrl.RFCOMM_NOT_CREATED:
                    Log.wtf(TAG, SubTag.bullet("ATRFCommDisconnect", "RFComm socket was not created"));
                    break;
            }
        }
    }

    /**
     * to cancel the asynctask ATSPPConnect
     */
    private class CancelATSPPConnect extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.w(TAG, SubTag.msg("CancelATSPPConnect.doInBackground"));

            mATSPPConnect.cancel(true);

            return null;
        }
    }

    /*private class RFCommSentinelThread extends Thread {

        private Intent intRFCommState = null;

        @Override
        public void run() {
            super.run();

            while (mSPPCtrl.isRFCSocketConnected() == CSPPCtrl.RFCOMM_CONNECTED) {

            }

            stopSelf();
        }
    }*/

    /**
     * core thread that broadcasts all the CAN-Bus information to all other components
     */
    private class SPPThread extends Thread {

        public void tx(String command) {
            mSPPCtrl.tx(command);
        }

        @Override
        public void run() {
            super.run();

            while (!mIsCancelled && (mSPPCtrl.isRFCSocketConnected() == CSPPCtrl.RFCOMM_CONNECTED)) {
                Intent intNonFMSMsg = null;
                Intent intFullFMSMsg = null;
                Intent intSubFMS = null;
                StringBuilder sbFMSBody = null;

                String fmsMsg = mSPPCtrl.rxLine();

                if (fmsMsg != null && !fmsMsg.equals("")) {
                    FMSParser parser = new FMSParser();
                    String[] splittedFMSMsg = parser.getParams(fmsMsg);
                    int level = parser.whatFMSLevel(splittedFMSMsg);

                    if ((TimeUtils.getTSSec() - mStartTime) != 0) {
                        intSubFMS = new Intent(SPPService.SPP_SERVICE_INTENT_ACTION_CAN_UPDATE_RATE);
                        intSubFMS.putExtra(SPPService.SPP_SERVICE_INTENT_KEY_CAN_UPDATE_RATE, (TimeUtils.getTSSec() - mStartTime));
                        Log.d(TAG, SubTag.bullet("SPPThread", "samplingRate: " + (TimeUtils.getTSSec() - mStartTime)));
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intSubFMS);
                        mStartTime = TimeUtils.getTSSec();
                    }

                    switch (level) {
                        case 0:
                            intNonFMSMsg = new Intent();
                            intNonFMSMsg.setAction(SPP_SERVICE_INTENT_ACTION_NON_FMS_MSG);
                            intNonFMSMsg.putExtra(SPP_SERVICE_INTENT_KEY_NON_FMS_MSG, fmsMsg);
                            sendBroadcast(intNonFMSMsg);
                            break;

                        case 1:
                            sbFMSBody = parser.concatSplitted(splittedFMSMsg);
                            intFullFMSMsg = new Intent();
                            intFullFMSMsg.setAction(SPP_SERVICE_INTENT_ACTION_FULL_FMS_1);
                            intFullFMSMsg.putExtra(SPP_SERVICE_INTENT_KEY_FULL_FMS_1, sbFMSBody.toString());
                            sendBroadcast(intFullFMSMsg);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_ODOMETER);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_ODOMETER, splittedFMSMsg[1]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_TFU);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_TFU, splittedFMSMsg[2]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_ENGINE_HOURS);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_ENGINE_HOURS, splittedFMSMsg[3]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_ACTUAL_SPEED);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_ACTUAL_SPEED, splittedFMSMsg[4]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_ENGINE_SPEED);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_ENGINE_SPEED, splittedFMSMsg[5]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_ENGINE_TORQUE);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_ENGINE_TORQUE, splittedFMSMsg[6]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_DOWN_SWITCH);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_DOWN_SWITCH, splittedFMSMsg[7]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_ACC_PEDAL_POS);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_ACC_PEDAL_POS, splittedFMSMsg[8]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_BRAKE_SWITCH);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_BRAKE_SWITCH, splittedFMSMsg[9]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_CLUTCH_SWITCH);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_CLUTCH_SWITCH, splittedFMSMsg[10]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_CRUISE_ACTIVE);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_CRUISE_ACTIVE, splittedFMSMsg[11]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_PTO);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_PTO, splittedFMSMsg[12]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_FUEL_LEVEL);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_FUEL_LEVEL, splittedFMSMsg[13]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_ENGINE_TEMP);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_ENGINE_TEMP, splittedFMSMsg[14]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_TURBO_PRESSURE);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_TURBO_PRESSURE, splittedFMSMsg[15]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_AXLE_WEIGHT_0);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_AXLE_WEIGHT_0, splittedFMSMsg[16]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_AXLE_WEIGHT_1);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_AXLE_WEIGHT_1, splittedFMSMsg[17]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_AXLE_WEIGHT_2);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_AXLE_WEIGHT_2, splittedFMSMsg[18]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_AXLE_WEIGHT_3);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_AXLE_WEIGHT_3, splittedFMSMsg[19]);
                            sendBroadcast(intSubFMS);

                            intSubFMS = new Intent();
                            intSubFMS.setAction(SPP_SERVICE_INTENT_ACTION_SERVICE_DISTANCE);
                            intSubFMS.putExtra(SPP_SERVICE_INTENT_KEY_SERVICE_DISTANCE, splittedFMSMsg[20]);
                            sendBroadcast(intSubFMS);
                            break;

                        case 2:
                            sbFMSBody = parser.concatSplitted(splittedFMSMsg);
                            intFullFMSMsg = new Intent();
                            intFullFMSMsg.setAction(SPP_SERVICE_INTENT_ACTION_FULL_FMS_2);
                            intFullFMSMsg.putExtra(SPP_SERVICE_INTENT_KEY_FULL_FMS_2, sbFMSBody.toString());
                            sendBroadcast(intFullFMSMsg);
                            break;

                        case 3:
                            sbFMSBody = parser.concatSplitted(splittedFMSMsg);
                            intFullFMSMsg = new Intent();
                            intFullFMSMsg.setAction(SPP_SERVICE_INTENT_ACTION_FULL_FMS_3);
                            intFullFMSMsg.putExtra(SPP_SERVICE_INTENT_KEY_FULL_FMS_3, sbFMSBody.toString());
                            sendBroadcast(intFullFMSMsg);
                            break;

                        case 4:
                            sbFMSBody = parser.concatSplitted(splittedFMSMsg);
                            intFullFMSMsg = new Intent();
                            intFullFMSMsg.setAction(SPP_SERVICE_INTENT_ACTION_FULL_FMS_4);
                            intFullFMSMsg.putExtra(SPP_SERVICE_INTENT_KEY_FULL_FMS_4, sbFMSBody.toString());
                            sendBroadcast(intFullFMSMsg);
                            break;

                        case 5:
                            break;

                        case 6:
                            break;

                        case 7:
                            sbFMSBody = parser.concatSplitted(splittedFMSMsg);
                            intFullFMSMsg = new Intent();
                            intFullFMSMsg.setAction(SPP_SERVICE_INTENT_ACTION_FULL_FMS_7);
                            intFullFMSMsg.putExtra(SPP_SERVICE_INTENT_KEY_FULL_FMS_7, sbFMSBody.toString());
                            sendBroadcast(intFullFMSMsg);
                            break;

                        case 8:
                            sbFMSBody = parser.concatSplitted(splittedFMSMsg);
                            intFullFMSMsg = new Intent();
                            intFullFMSMsg.setAction(SPP_SERVICE_INTENT_ACTION_FULL_FMS_8);
                            intFullFMSMsg.putExtra(SPP_SERVICE_INTENT_KEY_FULL_FMS_8, sbFMSBody.toString());
                            sendBroadcast(intFullFMSMsg);
                            break;

                        case 9:
                            break;

                        case 10:
                            break;

                        case 11:
                            break;

                        case 12:
                            sbFMSBody = parser.concatSplitted(splittedFMSMsg);
                            intFullFMSMsg = new Intent();
                            intFullFMSMsg.setAction(SPP_SERVICE_INTENT_ACTION_FULL_FMS_12);
                            intFullFMSMsg.putExtra(SPP_SERVICE_INTENT_KEY_FULL_FMS_12, sbFMSBody.toString());
                            sendBroadcast(intFullFMSMsg);
                            break;

                        default:
                            Log.wtf(TAG, SubTag.bullet("SPPThread.run", "Unhandled case. unknown FMS msg level encountered"));
                    }
                }
            }
            stopSelf();
        }
    }
}
