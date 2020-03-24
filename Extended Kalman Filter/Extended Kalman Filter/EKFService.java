package com.example.com.ecoassistant_03;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

/**
 * Created by amrbak on 14.03.2016.
 */
public class EKFService extends Service {

    private final String TAG = this.getClass().getSimpleName();

    private Intent mintGeoLocFiltered = null;
    public final static String EKFService_INTENT_ACTION_FILTERED_GEOLOC = "ACTION_FILTERED_GEOLOC";
    public final static String EKFService_INTENT_KEY_FILTERED_LAT = "KEY_FILTERED_LAT";
    public final static String EKFService_INTENT_KEY_FILTERED_LNG = "KEY_FILTERED_LNG";

    private BCRCtrl mBCRCtrl = null;
    private ATEKF mATEKF = null;
    private GPSCtrl mGPSCtrl = null;
    private EKF mEKF;
    private Intent mintMaxSpeedService = null;
    private File fileTestDrive = null;
    private boolean lock = false;

    private String mAbsoluteVelocity = "NAN";
    private double mAbsVel;
    private int mRFCommState = CSPPCtrl.RFCOMM_CONNECTED;

    /**
     * A broadcast receiver receives an integer value either 1 indicates that the RFComm is connected or 0 otherwise
     */
    private BroadcastReceiver mBCR_FRCOMM_CONNECTION_STATE_CHANGED = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(SPPService.SPP_SERVICE_INTENT_ACTION_RFCOMM_CONNECTION_STATE_CAHNGED)) {
                mRFCommState = intent.getIntExtra(SPPService.SPP_SERVICE_INTENT_KEY_RFCOMM_CONNECTION_STATE, -1);
                Log.i(TAG, SubTag.bullet("mBCR_FRCOMM_CONNECTION_STATE_CHANGED", "mRFCommState: " + mRFCommState));

                if (mRFCommState == CSPPCtrl.RFCOMM_CONNECTED) {

                } else if (mRFCommState == CSPPCtrl.RFCOMM_DISCONNECTED) {
                    Log.w(TAG, SubTag.bullet("mBCR_FRCOMM_CONNECTION_STATE_CHANGED", "EKFService stopped"));

                    if (mATEKF != null && mATEKF.getStatus() == AsyncTask.Status.RUNNING) {
                        mATEKF.cancel(true);
                    }

                    //if (mintMaxSpeedService != null)
                    //stopService(mintMaxSpeedService);
                    //stopSelf();
                }
            }
        }
    };

    /**
     * a broadcast receiver receives a string representing the absolute velocity of the vehicle
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

    @Override
    public void onCreate() {
        Log.w(TAG, "onCreate");

        this.mBCRCtrl = new BCRCtrl(this);
        this.mGPSCtrl = new GPSCtrl(this);
        this.mEKF = new EKF();

        if (!this.mBCRCtrl.isReg(this.mBCR_ABSOLUTE_VELOCTY_CHANGED)) {
            this.mBCRCtrl.reg(this.mBCR_ABSOLUTE_VELOCTY_CHANGED, SPPService.SPP_SERVICE_INTENT_ACTION_ACTUAL_SPEED);
        } else {
            Log.d(TAG, SubTag.msg("onCreate", "[mBCR_ABSOLUTE_VELOCTY_CHANGED] " + this.getResources().getString(com.example.com.ecoassistant_03.R.string.str_bcr_reg_error)));
        }

        if (!this.mBCRCtrl.isReg(this.mBCR_FRCOMM_CONNECTION_STATE_CHANGED)) {
            this.mBCRCtrl.reg(this.mBCR_FRCOMM_CONNECTION_STATE_CHANGED, SPPService.SPP_SERVICE_INTENT_ACTION_RFCOMM_CONNECTION_STATE_CAHNGED);
        } else {
            Log.d(TAG, SubTag.msg("onCreate", "[mBCR_FRCOMM_CONNECTION_STATE_CHANGED] " + this.getResources().getString(com.example.com.ecoassistant_03.R.string.str_bcr_reg_error)));
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand");

        this.mATEKF = new ATEKF();
        this.mATEKF.execute();

        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.w(TAG, "onBind");

        return null;
    }

    /**
     * the background thread of this asynctask will run as long as there is no GNSS signal received or the velocity has not been transmitted yet by SQUARELL. if there is GNSS signal and
     * a velocity  transmitted, then the EKFThread will start as well as the MaxSpeedService.
     */
    private class ATEKF extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.w(TAG, SubTag.msg("ATEKF.doInBackground"));

            while (!isCancelled() && (mGPSCtrl.getLat() < 0 || mGPSCtrl.getLng() < 0 || mAbsoluteVelocity.equals("NAN"))) {
                Log.v(TAG, SubTag.bullet("ATEKF.doInBackground", "waiting for EKF parameters"));
                Log.v(TAG, SubTag.bullet("ATEKF.doInBackground", "lat: " + mGPSCtrl.getLat()));
                Log.v(TAG, SubTag.bullet("ATEKF.doInBackground", "lng: " + mGPSCtrl.getLng()));
                Log.v(TAG, SubTag.bullet("ATEKF.doInBackground", "absolute velocity: " + mAbsoluteVelocity));
                SystemClock.sleep(1000);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.w(TAG, SubTag.msg("ATEKF.onPostExecute"));

            if (mRFCommState == CSPPCtrl.RFCOMM_CONNECTED) {

                fileTestDrive = IOCtrl.createEmptyFile("Testdrive.txt");
                EKFThread ekfThread = new EKFThread();
                ekfThread.start();

                mintMaxSpeedService = new Intent(getApplicationContext(), MaxSpeedService.class);
                startService(mintMaxSpeedService);

            } else {
                stopSelf();
            }
        }
    }

    private class EKFThread extends Thread {

        private double[][] corrP;
        private Jama.Matrix corrCovMatrix;

        private double YCartesian;
        private double XCartesian;
        private double Elevation;
        private double X;
        private double Y;
        private double v;
        private double vDot;
        private double gamma;
        private double gammaDot;
        private boolean init = false;
        private final static double semiMajorAxis = 6378137; //
        private final static double semiMinorAxis = 6356752.3142; //
        private double eccentricity;
        private double radOfCurvature;
        private final double pi = Math.atan(1) * 4;
        private final double rho = 180 / pi;
        private double Northing;
        private double Easting;
        private double EToLat;
        private double NToLng;

        @Override
        public void run() {
            super.run();

            corrP = new double[][]{{1, 0, 0, 0, 0, 0}, {0, 1, 0, 0, 0, 0}, {0, 0, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}, {0, 0, 0, 0, 1, 0}, {0, 0, 0, 0, 0, 1}};
            corrCovMatrix = new Jama.Matrix(corrP);

            while (mRFCommState == CSPPCtrl.RFCOMM_CONNECTED) {
                double sensLat = mGPSCtrl.getLat();
                double sensLng = mGPSCtrl.getLng();
                double sensAlt = mGPSCtrl.getAlt();

                if (!mAbsoluteVelocity.equals("NAN")) {
                    mAbsVel = Double.valueOf(mAbsoluteVelocity) / 3.6;// to convert metric units from km/h to m/s
                } else {
                    mAbsVel = 0.0;
                }

                sensLat /= rho;
                sensLng /= rho;

                if (!init) {
                    eccentricity = (((Math.pow(semiMajorAxis, 2)) - (Math.pow(semiMinorAxis, 2))) / (Math.pow(semiMinorAxis, 2)));
                    radOfCurvature = (Math.cos(sensLat) * Math.cos(sensLat)) + (semiMinorAxis / semiMajorAxis * semiMinorAxis / semiMajorAxis) * (Math.sin(sensLat) * Math.sin(sensLat));
                    radOfCurvature = Math.sqrt(radOfCurvature);
                    radOfCurvature = semiMajorAxis / radOfCurvature;
                    YCartesian = (radOfCurvature + sensAlt) * Math.cos(sensLat) * Math.sin(sensLng);
                    XCartesian = (radOfCurvature + sensAlt) * Math.cos(sensLat) * Math.cos(sensLng);
                    Y = YCartesian;
                    X = XCartesian;
                    v = mAbsVel;
                    vDot = 0;
                    gammaDot = 0;

                    init = true;
                }
                Elevation = (semiMinorAxis / semiMajorAxis * semiMinorAxis / semiMajorAxis * radOfCurvature + sensAlt) * Math.sin(sensLat);

                mEKF.estimate(Y, X, v, vDot, gammaDot, corrCovMatrix);

                mEKF.correct(mEKF.getEstimatedX(), mEKF.getEstimatedCov(), sensLat, sensLng, sensAlt, mAbsVel);
                double[][] corrXArray = mEKF.getCorrectedX().getArray();
                Y = corrXArray[0][0];
                X = corrXArray[1][0];
                v = corrXArray[2][0];
                vDot = corrXArray[3][0];
                //v = corrXArray[4][0];
                gammaDot = corrXArray[5][0];
                corrCovMatrix = mEKF.getCorrectedCov();

                double YToLng = 0;
                double XToLat = 0;

                //xyz->BLH
                double A1, A2, CO2PHI, V, EN, H0, TANPHI, HILF, TGPHI0, DH, DTGPHI, GRZW, C;
                C = (semiMajorAxis * semiMajorAxis) / semiMinorAxis;

                if ((semiMinorAxis - Elevation) > 95000) {
                    A1 = Math.sqrt(X * X + Y * Y);
                    A2 = Elevation * (1 + eccentricity);
                    TANPHI = A2 / A1;
                    double elev = 0.0;

                    for (; ; ) {
                        CO2PHI = 1 / (1 + TANPHI * TANPHI);
                        V = Math.sqrt(1 + eccentricity * CO2PHI);
                        EN = C / V;
                        H0 = elev;
                        elev = A1 / Math.sqrt(CO2PHI) - EN;
                        HILF = (elev / (EN + elev)) * eccentricity + 1;
                        TGPHI0 = TANPHI;
                        TANPHI = (A2 / A1) / HILF;
                        DH = Math.abs(elev - H0);
                        DTGPHI = Math.abs(TANPHI - TGPHI0);
                        GRZW = (.00005 / semiMajorAxis) * (1 + TANPHI * TANPHI);

                        if ((DH < .00005) && (DTGPHI < GRZW)) {
                            YToLng = Math.atan2(Y, X) * rho;
                            XToLat = Math.atan(TANPHI) * rho;
                            break;
                        }
                    }
                }
                //Log.i(TAG, SubTag.bullet("EKFThread", "XToLat: " + XToLat));

                GeodeticUtils.BLToNE(XToLat, YToLng);
                Northing = GeodeticUtils.getNorthing();
                Easting = GeodeticUtils.getEasting();
                //Log.i(TAG, SubTag.bullet("EKFThread", "Northing: " + Northing));
                //Log.i(TAG, SubTag.bullet("EKFThread", "Easting: " + Easting));

                GeodeticUtils.NEToBL(Northing, Easting);
                EToLat = GeodeticUtils.getLat();
                NToLng = GeodeticUtils.getLng();
                //Log.i(TAG, SubTag.bullet("EKFThread", "NToLng: " + NToLng));
                //Log.i(TAG, SubTag.bullet("EKFThread", "EToLat: " + EToLat));

                mintGeoLocFiltered = new Intent();
                mintGeoLocFiltered.setAction(EKFService.EKFService_INTENT_ACTION_FILTERED_GEOLOC);
                mintGeoLocFiltered.putExtra(EKFService.EKFService_INTENT_KEY_FILTERED_LAT, EToLat);
                mintGeoLocFiltered.putExtra(EKFService.EKFService_INTENT_KEY_FILTERED_LNG, NToLng);
                sendBroadcast(mintGeoLocFiltered);
            }

            stopSelf();
            stopService(mintMaxSpeedService);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy");

        if (mBCRCtrl.isReg(mBCR_ABSOLUTE_VELOCTY_CHANGED)) {
            Log.d(TAG, SubTag.bullet("onDestroy", "mBCRCtrl.unreg(mBCR_ABSOLUTE_VELOCTY_CHANGED)"));
            mBCRCtrl.unreg(mBCR_ABSOLUTE_VELOCTY_CHANGED);
        } else {
            Log.e(TAG, SubTag.bullet("onDestroy", "[mBCR_ABSOLUTE_VELOCTY_CHANGED]" + this.getResources().getString(com.example.com.ecoassistant_03.R.string.str_bcr_unreg_error)));
        }
        if (mBCRCtrl.isReg(mBCR_FRCOMM_CONNECTION_STATE_CHANGED)) {
            Log.d(TAG, SubTag.bullet("onDestroy", "mBCRCtrl.unreg(mBCR_FRCOMM_CONNECTION_STATE_CHANGED)"));
            mBCRCtrl.unreg(mBCR_FRCOMM_CONNECTION_STATE_CHANGED);
        } else {
            Log.e(TAG, SubTag.bullet("onDestroy", "[mBCR_FRCOMM_CONNECTION_STATE_CHANGED]" + this.getResources().getString(com.example.com.ecoassistant_03.R.string.str_bcr_unreg_error)));
        }
    }
}
