package com.example.com.ecoassistant_03;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Amr on 11/2/2015.
 */
public class CSPPCtrl implements Serializable{

    private final String TAG = this.getClass().getSimpleName();
    private final int SDK_VER = Build.VERSION.SDK_INT;

    public final static int RFCOMM_CONNECTED = 1;
    public final static int RFCOMM_DISCONNECTED = 0;
    public final static int RFCOMM_NOT_CREATED = -1;

    private String mMAC = null;
    private UUID mUUID = null;
    private Context mCTX = null;
    private BluetoothAdapter mBTAdapter = null;
    private BTCtrl mBTCtrl = null;

    private boolean mRFCSocketConnected = false;
    private BluetoothSocket mRFCSocket = null;
    private OutputStream mSocketOutStream = null;
    private InputStream mSocketInStream = null;

    /*private static File logFile = new File(Environment.getExternalStorageDirectory() + File.separator + "test.txt");
    private static OutputStream os = new FileOutputStream(CSPPCtrl.logFile);
    private static BufferedOutputStream bos = new BufferedOutputStream(CSPPCtrl.os);*/

    public CSPPCtrl(Context ctx, String mac, Parcelable[] uuids) {
        this.mCTX = ctx;
        this.mBTCtrl = new BTCtrl(this.mCTX);
        this.mBTAdapter = this.mBTCtrl.getBTAdapter();
        this.mMAC = mac;
        this.mUUID = UUID.fromString(uuids[0].toString());
        Log.w(TAG,SubTag.bullet("CSPPCtrl", "uuid[0].toString: " + uuids[0].toString()));
        Log.w(TAG, SubTag.bullet("CSPPCtrl", "this.mUUID: " + mUUID));
        //00001101-0000-1000-8000-00805F9B34FB
    }

    /**
     * Constructor
     * @param ctx
     * a reference to the context
     * @param mac
     * the hardware address of the remote device
     * @param uuid
     * the uuid of the remote device
     */
    public CSPPCtrl(Context ctx, String mac, String uuid) {
        this.mCTX = ctx;
        this.mBTCtrl = new BTCtrl(ctx);
        this.mBTAdapter = this.mBTCtrl.getBTAdapter();
        this.mMAC = mac;
        this.mUUID = UUID.fromString(uuid);
        Log.w(TAG,SubTag.bullet("CSPPCtrl", "uuid: " + uuid));
        Log.w(TAG, SubTag.bullet("CSPPCtrl", "this.mUUID: " + mUUID));
        //00001101-0000-1000-8000-00805F9B34FB
    }

    /**
     * this method attempts to connect to the remote device through the RFCOMM port.
     * @return
     * true if the RFCOMM is connected to successfully, false otherwise
     *
     */
    public boolean rfcConnect() {
        Log.w(TAG, SubTag.bullet("rfcConnect"));

        Log.d(TAG, SubTag.bullet("rfcConnect", "isRFCSocketConnected: " + this.isRFCSocketConnected()));

        if (this.mRFCSocket != null && this.mRFCSocket.isConnected()) {
            Log.w(TAG, SubTag.bullet("rfcConnect", "RFC-Socket was connected, it will be closed to establish a new connection"));
            this.closeRFCSocket();
        }

        BluetoothDevice remoteDevice = this.mBTAdapter.getRemoteDevice(this.mMAC);
        this.mBTAdapter.cancelDiscovery();

        if (SDK_VER >= 10)//2.3.3以上的设备需要用这个方式创建通信连接
            try {
                Log.i(TAG, SubTag.bullet("rfcConnect", ">>>>>>>Insecure RFC-Socket connecting<<<<<<<"));
                this.mRFCSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(this.mUUID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, SubTag.bullet("rfcConnect", "Creating Insecured RFC-Socket failed."));
            }
        else//创建SPP连接 API level 5
            try {
                Log.i(TAG, SubTag.bullet("rfcConnect", ">>>>>>>Secured RFC-Socket connecting<<<<<<<"));
                this.mRFCSocket = remoteDevice.createRfcommSocketToServiceRecord(this.mUUID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, SubTag.bullet("rfcConnect", "Creating Secured RFC-Socket failed."));
            }

        try {
            Log.d(TAG, SubTag.bullet("rfcConnect", "before .connect(): isRFCSocketConnected: " + this.isRFCSocketConnected()));
            this.mRFCSocket.connect();
            Log.d(TAG, SubTag.bullet("rfcConnect", "after .connect(): isRFCSocketConnected: " + this.isRFCSocketConnected()));
            this.mSocketOutStream = this.mRFCSocket.getOutputStream();
            this.mSocketInStream = this.mRFCSocket.getInputStream();
            this.mRFCSocketConnected = true;
            Log.i(TAG, SubTag.bullet("rfcConnect", "RFC-Socket: connected"));
        } catch (IOException e) {
            e.printStackTrace();
            this.disconnectRFCSocket();
            Log.e(TAG, SubTag.bullet("rfcConnect", "RFC-Socket status: not connected..maybe it is already connected...did you forget to " +
                    "close the socket before opening attempt 'after a successful connection attempt' OR check the UUID used??!! "));
        }

        Log.d(TAG, SubTag.subBullet("rfcConnect", "isRFCSocketConnected: " + this.isRFCSocketConnected()));

        return this.mRFCSocketConnected;
    }

    /**
     * use this method after rfcConnect() method connects successfully to the remote device to send data via RFCOMM outputStream
     * @param requestCommand
     * the command or the data to be sent to the remote device via RFCOMM
     */
    public void tx(String requestCommand) {
        Log.w(TAG, SubTag.bullet("tx"));

        if ((requestCommand != null) && (!requestCommand.equals(""))) {
            if (this.mSocketOutStream != null) {

                try {
                    //this.mSocketOutStream.flush();
                    this.mSocketOutStream.write((requestCommand + "\r\n").getBytes());
                    //this.mSocketOutStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, SubTag.subBullet("tx", "IOException: " + e.getMessage()));
                } finally {
                    Log.i(TAG, SubTag.subBullet("tx", "requestCommand: " + requestCommand));
                    //CConversation.writeTx(requestCommand);
                }

            } else {
                Log.e(TAG, SubTag.subBullet("tx", "mSocketOutStream = null"));
            }
        } else {
            Log.e(TAG, SubTag.subBullet("tx", "data = null or empty"));
        }
    }

    /**
     * use this method after rfcConnect() method connects successfully to the remote device to receive from the remote device via RFCOMM
     * @return
     * the data received from the remote device via RFCOMM, null if inputStream socket is attempted to access without initialization
     */
    public String rxLine() {
        //Log.w(TAG, SubTag.bullet("rxLine"));

        if (this.mSocketInStream != null) {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(this.mSocketInStream));

            String line = null;
            try {
                line = bReader.readLine();
                //Log.i(TAG, CSubTag.subBullet("rxLine", "line: " + line));
            } catch (IOException e) {
                e.printStackTrace();
                //Log.i(TAG, CSubTag.subBullet("rxLine", "line: " + line));
            } finally {
                //Log.i(TAG, SubTag.subBullet("rxLine", "line: " + line));
                //CConversation.writeRx(line);
                return line;
            }
        } else {
            return null;
        }

    }

    /**
     * to check if the RFC-socket is connected or not
     *
     * @return -1 -> mRFCSocket object is null
     * 0 -> disconnected
     * 1 -> connected
     */
    public int isRFCSocketConnected() {
        //Log.w(TAG, SubTag.bullet("isRFCSocketConnected"));

        if (this.mRFCSocket != null) {
            if (this.mRFCSocket.isConnected()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    /**
     * call this method if you are ready to disconnect the BT connectivity
     */
    public void disconnectRFCSocket() {
        Log.w(TAG, SubTag.bullet("disconnectRFCSocket"));

        this.closeInStream();
        this.closeOutStream();
        this.closeRFCSocket();
    }

    /**
     * call this method when are willing to terminate the BT connection. This method will close the outputStream socket
     */
    private void closeOutStream() {
        Log.w(TAG, SubTag.bullet("closeOutStream"));

        if (this.mSocketOutStream != null) {
            try {
                this.mSocketOutStream.flush();
                Log.v(TAG, SubTag.subBullet("closeOutStream", "flushing successful"));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, SubTag.subBullet("closeOutStream", "flushing failed"));
            }
            try {
                this.mSocketOutStream.close();
                this.mSocketOutStream = null;
                Log.v(TAG, SubTag.subBullet("closeOutStream", "closing successful"));
            } catch (IOException e) {
                e.printStackTrace();
                this.mSocketOutStream = null;
                Log.e(TAG, SubTag.subBullet("closeOutStream", "closing failed"));
            }
        } else {
            Log.e(TAG, SubTag.subBullet("closeOutStream", "mSocketOutStream == null. maybe the outputStream closed before."));
        }
    }

    /**
     * call this method when are willing to terminate the BT connection. This method will close the inputStream socket
     */
    private void closeInStream() {
        Log.w(TAG, SubTag.bullet("closeInStream"));

        if (this.mSocketInStream != null) {
            try {
                this.mSocketInStream.close();
                this.mSocketInStream = null;
                Log.v(TAG, SubTag.subBullet("closeInStream", "closing successful"));
            } catch (IOException e) {
                e.printStackTrace();
                this.mSocketInStream = null;
                Log.e(TAG, SubTag.subBullet("closeInStream", "closing failed"));
            }
        } else {
            Log.e(TAG, SubTag.subBullet("closeInStream", "mSocketInStream == null"));
        }
    }

    /**
     * call this method when you finished transmitting and receiving data via REFCOMM to close it and to release the allocated resources.
     */
    private void closeRFCSocket() {
        Log.w(TAG, SubTag.bullet("closeRFCSocket"));

        if (this.mRFCSocket != null) {
            if (this.isRFCSocketConnected() == SysConstants.RFCSocketState.CONNECTED.getREFCSocketState()) {
                try {
                    this.mRFCSocket.close();
                    this.mRFCSocketConnected = false;
                    this.mRFCSocket = null;
                    Log.v(TAG, SubTag.subBullet("closeRFCSocket", "closing RFC-socket: successful"));
                } catch (IOException e) {
                    e.printStackTrace();
                    this.mRFCSocketConnected = false;
                    this.mRFCSocket = null;
                    Log.e(TAG, SubTag.subBullet("closeRFCSocket", "closing RFC-socket: failed"));
                }
            }
        } else {
            Log.e(TAG, SubTag.subBullet("closeRFCSocket", "mRFCSocket == null"));
        }
    }
}
