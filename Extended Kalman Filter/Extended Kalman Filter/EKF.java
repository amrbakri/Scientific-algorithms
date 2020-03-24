package com.example.com.ecoassistant_03;

/**
 * Created by amrbak on 14.03.2016.
 */
public class EKF {

    private final String TAG = this.getClass().getSimpleName();

    //private static final double R = 6371; //in km
    private final double dt = 1;
    private double gamma;

    private double tse11;
    private double tse12;
    private double tse13;
    private double tse14;
    private double tse15;
    private double tse16;

    private double tse21;
    private double tse22;
    private double tse23;
    private double tse24;
    private double tse25;
    private double tse26;

    private double tse31;
    private double tse32;
    private double tse33;
    private double tse34;
    private double tse35;
    private double tse36;

    private double tse41;
    private double tse42;
    private double tse43;
    private double tse44;
    private double tse45;
    private double tse46;

    private double tse51;
    private double tse52;
    private double tse53;
    private double tse54;
    private double tse55;
    private double tse56;

    private double tse61;
    private double tse62;
    private double tse63;
    private double tse64;
    private double tse65;
    private double tse66;

    private double tsv11;
    private double tsv12;
    private double tsv13;
    private double tsv14;
    private double tsv15;
    private double tsv16;

    private double zY;
    private double zX;

    //tarnsformation matrix
    private double[][] HElems = { {1,0,0,0,0,0}, {0,1,0,0,0,0}, {0,0,1,0,0,0}, {0,0,0,1,0,0}, {0,0,0,0,1,0}, {0,0,0,0,0,1} };
    private Jama.Matrix HMatrix = new Jama.Matrix(HElems);

    private double[][] zhElems = { {1,0,0,0,0,0}, {0,1,0,0,0,0}, {0,0,1,0,0,0}, {0,0,0,0,0,0}, {0,0,0,0,0,0}, {0,0,0,0,0,0} };
    private Jama.Matrix zhMatrix = new Jama.Matrix(zhElems);
    private double[][] zhvElems;
    private Jama.Matrix zhvMatrix;

    private double[][] iElems = { {1,0,0,0,0,0}, {0,1,0,0,0,0}, {0,0,1,0,0,0}, {0,0,0,1,0,0}, {0,0,0,0,1,0}, {0,0,0,0,0,1} };
    private Jama.Matrix iMatrix = new Jama.Matrix(iElems);


    //double[][] rElems = { {1,0,0,0,0,0}, {0,1,0,0,0,0}, {0,0,.000000001,0,0,0}, {0,0,0,.000000001,0,0}, {0,0,0,0,1,0}, {0,0,0,0,0,1} };
    double[][] rElems = { {100,0,0,0,0,0}, {0,100,0,0,0,0}, {0,0,.0000000000001,0,0,0}, {0,0,0,.0000000000001,0,0}, {0,0,0,0,100,0}, {0,0,0,0,0,100} };
    private Jama.Matrix rMatrix = new Jama.Matrix(rElems);

    private double[][] qArray = { {1,0,0,0,0,0}, {0,1,0,0,0,0}, {0,0,.000000001,0,0,0}, {0,0,0,.000000001,0,0}, {0,0,0,0,1,0}, {0,0,0,0,0,1} };
    private Jama.Matrix Q = new Jama.Matrix(qArray);

    private double[][] tseElems;
    private Jama.Matrix tseMatrix;
    private double [][] tsvElems;
    private Jama.Matrix tsvMatrix;
    private Jama.Matrix estimatedX;
    private Jama.Matrix estimatedCov;
    private Jama.Matrix z;
    private Jama.Matrix k;
    private Jama.Matrix corrCov;
    private Jama.Matrix corrX;

    private final static double semiMajorAxis = 6378137; //m
    private final static double semiMinorAxis = 6356752.3142; //m
    private double eccentricity;
    private double radOfCurvature;
    private final double pi = Math.atan(1)*4;
    private final double rho = 180/pi;

    /**
     * estimation step
     * @param Y
     * vehicle position in terms of cartesian coordinates Y
     * @param X
     * vehicle position in terms of cartesian coordinates X
     * @param v
     * absolute velocity
     * @param vdot
     * acceleration
     * @param gammadot
     * yaw angle
     * @param corrCov
     * corrected covarience matrix
     */
    public void estimate(double Y, double X, double v, double vdot, double gammadot, Jama.Matrix corrCov) {

        gamma = Math.atan(Y / X);

        tse11 = 1;
        tse12 = 0;
        tse13 = dt * Math.cos(gamma) - (dt * dt * gammadot * Math.sin(gamma) )/2;
        tse14 = (dt * dt * Math.cos(gamma))/2;
        tse15 = ((-vdot * Math.sin(gamma))/2 - ((gammadot * v * Math.cos(gamma))/2)* dt*dt) - (v * Math.sin(gamma) * dt);
        tse16 = (-dt * dt * v * Math.sin(gamma))/2;

        tse21 = 0;
        tse22 = 1;
        tse23 = (gammadot * Math.cos(gamma) * dt * dt)/2 + Math.sin(gamma) * dt;
        tse24 = (dt * dt *Math.sin(gamma))/2;
        tse25 = ((vdot * Math.cos(gamma))/2 - ((gammadot * v * Math.sin(gamma))/2) * dt * dt) + (v * Math.cos(gamma) * dt);
        tse26 = (dt * dt * v * Math.cos(gamma))/2;

        tse31 = 0;
        tse32 = 0;
        tse33 = 1;
        tse34 = dt;
        tse35 = 0;
        tse36 = 0;

        tse41 = 0;
        tse42 = 0;
        tse43 = 0;
        tse44 = 1;
        tse45 = 0;
        tse46 = 0;

        tse51 = 0;
        tse52 = 0;
        tse53 = 0;
        tse54 = 0;
        tse55 = 1;
        tse56 = dt;

        tse61 = 0;
        tse62 = 0;
        tse63 = 0;
        tse64 = 0;
        tse65 = 0;
        tse66 = 1;

        tsv11 = Y;
        tsv12 = X;
        tsv13 = v;
        tsv14 = vdot;
        tsv15 = gamma;
        tsv16 = gammadot;

        this.tseElems = new double [][] { {tse11, tse12, tse13, tse14, tse15, tse16}, {tse21, tse22, tse23, tse24, tse25, tse26}, {tse31, tse32, tse33, tse34, tse35, tse36},
                {tse41, tse42, tse43, tse44, tse45, tse46}, {tse51, tse52, tse53, tse54, tse55, tse56}, {tse61, tse62, tse63, tse64, tse65, tse66} };
        this.tseMatrix = new Jama.Matrix(this.tseElems);

        this.tsvElems = new double[][]{ {Y}, {X}, {v}, {vdot}, {gamma}, {gammadot} };
        this.tsvMatrix = new Jama.Matrix(this.tsvElems);

        this.estimatedX = this.tseMatrix.times(this.tsvMatrix);

        Jama.Matrix estCovPart1 = this.tseMatrix.times(corrCov);
        this.estimatedCov = estCovPart1.times(tseMatrix.transpose());

        this.estimatedCov = this.estimatedCov.plus(Q);
    }

    public Jama.Matrix getEstimatedX () {
        //Log.i(TAG, SubTag.bullet("getEstimatedX", "estimated Y: " + this.estimatedX.getArray()[0][0]));
        //Log.i(TAG, SubTag.bullet("getEstimatedX", "estimated X: " + this.estimatedX.getArray()[1][0]));
        return this.estimatedX;
    }
    public Jama.Matrix getEstimatedCov () {
        return this.estimatedCov;
    }

    /**
     * correction step
     * @param estimatedX
     * estimated state transition vector
     * @param estimatedCov
     * estimated covarience matrix
     * @param sensLat
     * latitude from the GNSS sensor
     * @param sensLng
     * longitude from GNSS sensor
     * @param sensAlt
     * altitude from GNSS sensor
     * @param sensV
     * absolute velocity
     */
    public void correct (Jama.Matrix estimatedX, Jama.Matrix estimatedCov, double sensLat, double sensLng, double sensAlt,double sensV) {
        //K
        Jama.Matrix kGainpart1 = estimatedCov.times(this.HMatrix.transpose());
        Jama.Matrix kGainPart2 = this.HMatrix.times(estimatedCov).times(this.HMatrix.transpose());
        Jama.Matrix kGainPart3 = kGainPart2.plus(this.rMatrix);
        Jama.Matrix kGainPart4 = kGainPart3.inverse();
        k = kGainpart1.times(kGainPart4);

        eccentricity = (Math.pow(semiMajorAxis, 2)-Math.pow(semiMinorAxis, 2))/Math.pow(semiMinorAxis, 2);
        radOfCurvature = ( Math.cos(sensLat) * Math.cos(sensLat) ) + (semiMinorAxis/semiMajorAxis * semiMinorAxis/semiMajorAxis) * (Math.sin(sensLat) * Math.sin(sensLat));
        radOfCurvature = Math.sqrt(radOfCurvature);
        radOfCurvature = semiMajorAxis/radOfCurvature;
        //z
        zY = (radOfCurvature + sensAlt) * Math.cos(sensLat) * Math.sin(sensLng);
        zX = (radOfCurvature + sensAlt) * Math.cos(sensLat) * Math.cos(sensLng);

        this.zhvElems = new double [][] { {zY}, {zX}, {sensV}, {estimatedX.getArray()[3][0]}, {Math.atan(zY/zX)}, {estimatedX.getArray()[5][0]} };
        this.zhvMatrix = new Jama.Matrix(this.zhvElems);

        z = this.zhMatrix.times(zhvMatrix);

        //corrCov
        Jama.Matrix corrCovPart1 = k.times(HMatrix);
        Jama.Matrix corrCovPart2 = iMatrix.minus(corrCovPart1);
        this.corrCov = corrCovPart2.times(estimatedCov);

        //corrX
        Jama.Matrix corrXPart1 = z.minus(estimatedX);
        Jama.Matrix corrXPart2 = k.times(corrXPart1);
        this.corrX = estimatedX.plus(corrXPart2);

        if (sensV <= 0.3) {// m/s
            double[][] rElems = { {100,0,0,0,0,0}, {0,100,0,0,0,0}, {0,0,.0001,0,0,0}, {0,0,0,.0001,0,0}, {0,0,0,0,100,0}, {0,0,0,0,0,100} };//Y, X, V, ......
            rMatrix = new Jama.Matrix(rElems);//R matrix: measurements covariance matrix
        } else {
            double[][] rElems = { {.0001,0,0,0,0,0}, {0,.0001,0,0,0,0}, {0,0,.0001,0,0,0}, {0,0,0,.0001,0,0}, {0,0,0,0,.0001,0}, {0,0,0,0,0,.0001} };
            rMatrix = new Jama.Matrix(rElems);
        }
    }

    public Jama.Matrix getCorrectedX () {
        //Log.i(TAG, SubTag.bullet("getCorrectedX", "corrected Y: " + this.corrX.getArray()[0][0]));
        //Log.i(TAG, SubTag.bullet("getCorrectedX", "corrected X: " + this.corrX.getArray()[1][0]));
        return this.corrX;
    }
    public Jama.Matrix getCorrectedCov () {
        return this.corrCov;
    }
}
