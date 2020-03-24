package example.com.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by amrbak on 04.02.2016.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private final String TAG = this.getClass().getSimpleName();

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "GEOLOC_00.db";
    private static final String DATABASE_TABLE_NAME = "NODE_TEST_00";
    private static final String INDEX_LAT_LNG = "INDEX_LAT_LNG_IDX";
    private static final String INDEX_LAT = "INDEX_LAT_IDX";
    private static final String INDEX_LNG = "INDEX_LNG_IDX";
    private Context mCtx = null;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mCtx = context;

        //Log.i(TAG, "DB_PATH: " + this.mCtx.getDatabasePath("GEOLOC.db"));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(TAG, "onCreate");

        db.execSQL(" CREATE TABLE " + DATABASE_TABLE_NAME + " ( " +
                BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                "rownum INTEGER, " +
                "lat DOUBLE, " +
                "lng DOUBLE, " +
                "maxspeed TEXT " +
                " ); ");

        //db.execSQL(" CREATE INDEX " + INDEX_LAT_LNG + " ON " + DATABASE_TABLE_NAME + " (lat,lng); ");
        db.execSQL(" CREATE INDEX " + INDEX_LAT + " ON " + DATABASE_TABLE_NAME + " (lat); ");
        db.execSQL(" CREATE INDEX " + INDEX_LNG + " ON " + DATABASE_TABLE_NAME + " (lng); ");
    }

    public void insertRow(int rowNum, double lat, double lng, String maxSpeed) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("rownum", rowNum);
        contentValues.put("lat", lat);
        contentValues.put("lng", lng);
        contentValues.put("maxspeed", maxSpeed);

        db.insert(DATABASE_TABLE_NAME, null, contentValues);

    }
    public String getNodeID(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor c = (SQLiteCursor) db.rawQuery("SELECT nodeid FROM " + DATABASE_TABLE_NAME + " WHERE "+
                BaseColumns._ID+" = "+
                Long.toString(id) +" AND nodeid IS NOT NULL ", null);
        String r;
        c.moveToFirst();
        if (c.getCount() == 0) {
            return "";
        } else {
            r = c.getString(0);
        }
        c.close();
        db.close();
        return r;
    }

    public double getLat(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor c = (SQLiteCursor) db.rawQuery("SELECT lat FROM " + DATABASE_TABLE_NAME + " WHERE "+
                BaseColumns._ID+" = "+
                Long.toString(id) +" AND lat IS NOT NULL ", null);
        double r;
        c.moveToFirst();
        if (c.getCount() == 0) {
            return -1;
        } else {
            r = c.getDouble(0);
        }
        c.close();
        db.close();
        return r;
    }

    public double getLng(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor c = (SQLiteCursor) db.rawQuery("SELECT lng FROM " + DATABASE_TABLE_NAME + " WHERE "+
                BaseColumns._ID+" = "+
                Long.toString(id) +" AND lat IS NOT NULL ", null);
        double r;
        c.moveToFirst();
        if (c.getCount() == 0) {
            return -1;
        } else {
            r = c.getDouble(0);
        }
        c.close();
        db.close();
        return r;
    }

    public ArrayList<Node> findSpeedLimitAt(String lat, String lng) {
        ArrayList<Node> list = new ArrayList<>();
        //.subString(start, end); end index includes the real part and the dot "." 49.xxxx xxxx
        String lat2 = lat.substring(0, 4);
        String lng2 = lng.substring(0, 3);
        Double latHB = Double.valueOf(lat);//higher bound
        Double lngHB = Double.valueOf(lng);//higher bound
        Double latLB = Double.valueOf(lat2);//lower bound
        Double lngLB = Double.valueOf(lng2);//lower bound

        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor c = (SQLiteCursor) db.rawQuery("SELECT rownum, lat, lng, maxspeed FROM " + DATABASE_TABLE_NAME +
                " WHERE lat BETWEEN " + latLB + " AND " + latHB + " And lng BETWEEN " + lngLB + " AND " + lngHB +"", null);

        c.moveToFirst();
        if (c.getCount() == 0) {
            return null;
        }

        do {
            list.add(new Node(c.getInt(c.getColumnIndexOrThrow("rownum")), c.getDouble(c.getColumnIndexOrThrow("lat")),
                    c.getDouble(c.getColumnIndexOrThrow("lng")), c.getString(c.getColumnIndexOrThrow("maxspeed")) ));
        } while (c.moveToNext());

        c.close();
        db.close();
        return list;
    }

    public String getMaxSpeed(Double lat, Double lng) {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor c = (SQLiteCursor) db.rawQuery("SELECT maxspeed FROM " + DATABASE_TABLE_NAME + " WHERE lat = " + lat + " And lng = " + lng, null);
        String r;
        c.moveToFirst();
        if (c.getCount() == 0) {
            return "";
        } else {
            r = c.getString(0);
        }
        c.close();
        db.close();
        return r;
    }

    public String getMaxSpeed(int rownum) {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor c = (SQLiteCursor) db.rawQuery("SELECT maxspeed FROM " + DATABASE_TABLE_NAME + " WHERE rownum = " + rownum, null);
        String r;
        c.moveToFirst();
        if (c.getCount() == 0) {
            return "";
        } else {
            r = c.getString(0);
        }
        c.close();
        db.close();
        return r;
    }

    public int getTotalRowsInDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor c = (SQLiteCursor) db.rawQuery("SELECT * FROM " + DATABASE_TABLE_NAME, null);

        c.moveToFirst();
        int r = c.getCount();
        c.close();
        db.close();
        return r;
    }

    public void deleteALLRows() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE_NAME, null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade");
    }
}
