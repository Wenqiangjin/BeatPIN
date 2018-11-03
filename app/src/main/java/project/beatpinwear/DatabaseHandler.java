package project.beatpinwear;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by G Anudeep Reddy on 23-Nov-17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private final static  String DATABASE_NAME = "Beats.db";
    private static final int DATABASE_VERSION = 1;

    private static final String COLUMN_BEATPIN_NUM = "BeatPin_No";

    private static final String TABLE_NAME = "Beats";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_BEATNO = "Beat_Num";
    public static final String COLUMN_ACTIONDOWN = "Action_Down";
    public static final String COLUMN_ACTIONUP = "Action_Up";

    private static final String TABLE_NAME_TAP_INT = "Tapping_Intervals";
    private static final String COLUMN_INTERVAL_NUM = "Interval_No";
    private static final String COLUMN_ONSET_TAP_INT = "Onset_Tap_Int";

    private static final String TABLE_NAME_FINAL = "Final_Results";
    private static final String COLUMN_TAPAVG = "TAP_AVG";
    private static final String COLUMN_TAPSD = "TAP_SD";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_BEATPIN_NUM + " INTEGER, " +
                    COLUMN_BEATNO + " INTEGER, " +
                    COLUMN_ACTIONDOWN + " TEXT, " +
                    COLUMN_ACTIONUP + " TEXT" +
                    ")";

    private static final String TABLE_TAP_INT_CREATE =
            "CREATE TABLE " + TABLE_NAME_TAP_INT + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_BEATPIN_NUM + " INTEGER, " +
                    COLUMN_INTERVAL_NUM + " INTEGER, " +
                    COLUMN_ONSET_TAP_INT + " TEXT)";

    private static final String TABLE_CREATE_FINAL =
            "CREATE TABLE " + TABLE_NAME_FINAL + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_INTERVAL_NUM + " INTEGER, " +
                    COLUMN_TAPAVG + " TEXT, " +
                    COLUMN_TAPSD + " TEXT)";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(TABLE_TAP_INT_CREATE);
        db.execSQL(TABLE_CREATE_FINAL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_TAP_INT);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_FINAL);
        onCreate(db);
    }

    public void deleteAll()  {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
        db.execSQL("delete from "+ TABLE_NAME_TAP_INT);
        db.execSQL("delete from "+TABLE_NAME_FINAL);
        db.close();
    }


    /*
                TABLE - 1
     */

    public boolean tableHasRows()    {
        SQLiteDatabase db = this.getReadableDatabase();
        String count = "SELECT count(*) FROM "+TABLE_NAME;
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
            if(icount>0)
                return true;
            else
                return false;
    }

    public int insert(int beatPinNum, int beatNo, ArrayList<Float> actionDowns, ArrayList<Float> actionUps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        int count = 0;

        for (int i = 0; i < beatNo; i++) {
            contentValues.put(COLUMN_BEATPIN_NUM, beatPinNum);
            contentValues.put(COLUMN_BEATNO, i + 1);
            contentValues.put(COLUMN_ACTIONDOWN, actionDowns.get(i));
            contentValues.put(COLUMN_ACTIONUP, actionUps.get(i));
            long result = db.insert(TABLE_NAME, null, contentValues);
            count += 1;
        }
        db.close();
        return count;
    }

    public int getFirstBeatPinLength(int beatPinNum)  {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;

        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_BEATPIN_NUM, COLUMN_BEATNO, COLUMN_ACTIONDOWN, COLUMN_ACTIONUP},
                COLUMN_BEATPIN_NUM + "=?", new String[]{String.valueOf(beatPinNum)}, null, null, null, null);

        if( cursor.moveToFirst())    {
            do {
                count += 1;
            } while (cursor.moveToNext());
        }
        db.close();
        if(count >= 1)
            return count;
        else
            return  0;
    }

    public int getTotalBeatPinNumber() {
        SQLiteDatabase db = this.getReadableDatabase();
        int beatPinNum = 0;

        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME, null);
        if(cursor.getCount() != 0) {
            cursor.moveToLast();

            do {
                beatPinNum = Integer.parseInt(cursor.getString(1));
            }while(cursor.moveToNext());
        }
        return beatPinNum;
    }

    /*
            TABLE - 2 (TAPPING INTERVALS OF ACTIONDOWNS)
     */

    public int insertTapIntervals(int beatPinNum, ArrayList<Float> tapIntervals) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        int count = 0;

        for (int i = 0; i < tapIntervals.size(); i++) {
            contentValues.put(COLUMN_BEATPIN_NUM, beatPinNum);
            contentValues.put(COLUMN_INTERVAL_NUM, i+1);
            contentValues.put(COLUMN_ONSET_TAP_INT, tapIntervals.get(i));
            db.insert(TABLE_NAME_TAP_INT, null, contentValues);
            count += 1;
        }
        db.close();
        return count;
    }

    public void getAllRows(int beatPinNum) {
        SQLiteDatabase db = this.getReadableDatabase();
        Float tapInterval;
        Cursor cursor =  db.query(TABLE_NAME_TAP_INT, new String[]{COLUMN_BEATPIN_NUM, COLUMN_ONSET_TAP_INT},
                COLUMN_BEATPIN_NUM + "=?", new String[]{String.valueOf(beatPinNum)},
                null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
            do {
                tapInterval = Float.parseFloat(cursor.getString(1));
    //            Log.d("Check", "Tapping interval: Database: "+tapInterval);
            } while (cursor.moveToNext());
        }
        db.close();
    }

    public Float[] getTappingIntervalAvgs( int beatCount) {
        SQLiteDatabase db = this.getReadableDatabase();
    //    Log.d("Check", "******Database Total beat Pins: : "+getTotalBeatPinNumber());
        int beatPinNum = getTotalBeatPinNumber();
        Float[] tapIntervalAvgs = new Float[beatCount-1];
        float avg;
    //    Log.d("Check", "Beat Count: "+beatCount);
        for(int i=1; i<beatCount; i++) {
            avg = 0;
            Cursor cursor = db.query(TABLE_NAME_TAP_INT, new String[]{COLUMN_INTERVAL_NUM, COLUMN_ONSET_TAP_INT},
                    COLUMN_INTERVAL_NUM + "=?", new String[]{String.valueOf(i)}, null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    avg = avg + Float.parseFloat(cursor.getString(1));
    //                Log.d("Check", "Inside Avgs Method. Tap Interval: " + cursor.getString(1));

                } while (cursor.moveToNext());
            }
            avg = avg / beatPinNum;
    //        Log.d("Check", "Inside Avgs Method. Avg of Column " + i + " = " + avg);
            tapIntervalAvgs[i - 1] = avg;
        }
        db.close();
        return tapIntervalAvgs;
    }

    public Float[] getTappingIntervalsSDs( int beatCount)    {
        SQLiteDatabase db = this.getReadableDatabase();
        int beatPinNum = getTotalBeatPinNumber();
        Float[] tapIntervalSds = new Float[beatCount-1];
        Float[] tapIntervals = new Float[beatPinNum];
    //    Log.d("Check", "size of tap intervals: "+tapIntervals.length+". BeatPin Total: "
    //        +beatPinNum+". Beat Count: "+beatCount);
        float sd, avg;
        int count;

        for(int i=1; i<beatCount; i++) {
            for(int j=0; j<beatPinNum; j++)
                tapIntervals[j] = Float.valueOf(0);
            avg = 0;
            sd = 0;
            count = 0;
            Cursor cursor = db.query(TABLE_NAME_TAP_INT, new String[]{COLUMN_INTERVAL_NUM, COLUMN_ONSET_TAP_INT},
                    COLUMN_INTERVAL_NUM + "=?", new String[]{String.valueOf(i)}, null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    avg = avg + Float.parseFloat(cursor.getString(1));
                    tapIntervals[count] = Float.valueOf(cursor.getString(1));
    //                Log.d("Check", "Inside Stand Dev's Method. TapInterval: "+tapIntervals[count]+". Count: "+count);
                    count += 1;
                } while (cursor.moveToNext());
            }
            avg = avg / beatPinNum;
    //        for(int j=0; j<beatPinNum; j++)
    //            Log.d("Check", "Column " +i+" - "+ tapIntervals[j]);
    //        Log.d("Check", "Inside Stand Dev's Method. Avg of Column " + i + " = " + avg);

            for(float value: tapIntervals)
                sd += Math.pow(value - avg, 2);
            sd = (float) Math.sqrt(sd/beatPinNum);
            tapIntervalSds[i-1] = sd;
    //        Log.d("Check", "Standard deviation of column "+i+" = "+tapIntervalSds[i-1]);
        }
        db.close();
        return tapIntervalSds;
    }

    /*
            TABLE - 3 (INSERTING AVERAGES AND STANDARD DEVIDATIONS IN TABLE
     */

    public void insertResults(Float[] avgs, Float[] sds, int beatCount)  {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        for (int i = 0; i < beatCount-1; i++) {
            contentValues.put(COLUMN_TAPAVG, avgs[i]);
            contentValues.put(COLUMN_TAPSD, sds[i]);
            contentValues.put(COLUMN_INTERVAL_NUM, i+1);
            db.insert(TABLE_NAME_FINAL, null, contentValues);
        }
        db.close();
    }

    public Float[] getAvgsResults(int beat_count)    {
        SQLiteDatabase db = this.getReadableDatabase();
        Float[] avgs = new Float[beat_count-1];
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME_FINAL, null);
        int count = 0;

        if(cursor.getCount() != 0)  {
            cursor.moveToFirst();
            do {
                avgs[count] = Float.valueOf(cursor.getString(2));
    //            Log.d("Check", "Average of tap "+(count+1)+" = "+avgs[count]);
                count += 1;
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return avgs;
    }

    public Float[] getSdResults(int beat_count) {
        SQLiteDatabase db = this.getReadableDatabase();
        Float[] sds = new Float[beat_count-1];
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME_FINAL, null);
        int count = 0;

        if(cursor.getCount() != 0)  {
            cursor.moveToFirst();
            do {
                sds[count] = Float.valueOf(cursor.getString(3));
    //            Log.d("Check", "Average of tap "+(count+1)+" = "+sds[count]);
                count += 1;
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sds;
    }

    public void deleteFinalTableRows()    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_FINAL, null, null);
        db.close();
    }



}
