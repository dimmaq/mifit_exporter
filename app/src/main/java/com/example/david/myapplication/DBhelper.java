package com.example.david.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;
import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

public class DBhelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory().getPath() +"/Amazfit_Exporter/mifitdb.db";
    public static final String TRACKINGS_TABLE_NAME = "TRACKDATA";
    public static final String TRACKINGS_COLUMN_NAME = "TRACKID";
    public static final String ACTIVITY_COLUMN_NAME = "TYPE";
    public static final String ENDTIME_COLUMN_NAME = "ENDTIME";
    private HashMap hp;
    public SQLiteDatabase db;
    public DBhelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


    }
    public void opendb(){
        db=openOrCreateDatabase(DATABASE_NAME,null);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor getData() {
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from TRACKDATA  order by TRACKID DESC", null );
        return res;
    }
    public Cursor getRecord(int id) {
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from TRACKRECORD where TRACKID="+id+"", null );
        return res;
    }
    public int numberOfRows(){
       // SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TRACKINGS_TABLE_NAME);
        return numRows;
    }

    public ArrayList<Point> getPoints(int trackid) {
        ArrayList<Point> points_list = new ArrayList<Point>();
        Cursor res =  db.rawQuery( "select * from TRACKDATA where TRACKID="+trackid+"", null );
        res.moveToFirst();
        int size = res.getInt(res.getColumnIndex("SIZE"));
        String lldata = res.getString(res.getColumnIndex("BULKLL"));
        String altdata = res.getString(res.getColumnIndex("BULKAL"));
        String timedata = res.getString(res.getColumnIndex("BULKTIME"));
        String hrdata = res.getString(res.getColumnIndex("BULKHR"));

        String[] timestrings = timedata.split(";");
        int[] timestamps = new int[size];
        int timediff = 0;
        timestamps[0] = trackid;
        for(int i = 1; i < size; i++) {
            timediff = Integer.parseInt(timestrings[i]);
            if(timediff == 0 && i < 31) {
                timediff = 1;
            }
            timestamps[i] = timestamps[i - 1] + timediff;
        }

        String[] latlonstrings = lldata.split(";");
        long[] lats = new long[size];
        long[] lons = new long[size];
        for (int i = 0; i < size; i++) {
            String latlongs_sub[]= latlonstrings[i].split(",");
            if(i == 0) {
                lats[i] = Long.parseLong(latlongs_sub[0]);
                lons[i] = Long.parseLong(latlongs_sub[1]);
            }
            else
            {
                lats[i] = Long.parseLong(latlongs_sub[0]) + lats[i-1];
                lons[i] = Long.parseLong(latlongs_sub[1]) + lons[i-1];
            }
        }

        String[] altstrings = altdata.split(";");
        long[] alts = new long[size];
        for(int i = 0; i < size; i++) {
            alts[i] = Integer.parseInt(altstrings[i]) / 10;
        }

        String[] hrstrings = hrdata.split(";");
        int[] hrs = new int[size];
        boolean[] hasHrs = new boolean[size];
        int hr_cur = 0;
        for(int i = 0; i < hrstrings.length; i++) {
            String hr_subs[]= hrstrings[i].split(",");
            int shift = 1;
            int hrValue = Integer.parseInt(hr_subs[1]);
            if(i == 0) {
                shift = 0;
                hrs[0] = hrValue;
            }
            else {
                if(hr_subs[0].length() > 0)
                    shift = Integer.parseInt(hr_subs[0]);
                hasHrs[hr_cur] = true;
                hrs[hr_cur] = hrs[hr_cur - 1] + hrValue;
            }
            hr_cur++;
            for (int j = 1; j < shift; j++) {
                hasHrs[hr_cur] = false;
                hrs[hr_cur] = hrs[hr_cur - 1];
                hr_cur++;
            }
        }

        for(int i = 0; i < size; i++) {
            if(hasHrs[i]) {
                points_list.add(new Point(timestamps[i], lats[i], lons[i], alts[i], hrs[i]));
            }
            else {
                points_list.add(new Point(timestamps[i], lats[i], lons[i], alts[i]));
            }
        }

        return points_list;
    }

    public ArrayList<String> getAllTracks() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from TRACKDATA order by TRACKID DESC", null );
        res.moveToFirst();
        String activity;
        while(res.isAfterLast() == false){
            int type=res.getInt(res.getColumnIndex(ACTIVITY_COLUMN_NAME));
            switch (type)
            {
                case 1:
                    activity="\uD83C\uDFBD Running: ";
                    break;
                case 6:
                    activity ="\uD83D\uDEB6 Walking: ";
                    break;
                case 7:
                    activity ="\uD83C\uDF04 Trail:" ;
                    break;
                case 9:
                    activity ="\uD83D\uDEB4 Cycling: ";
                    break;
                default:
                    activity ="Other: ";
                    break;

            }
            Date date = new Date(res.getInt(res.getColumnIndex(TRACKINGS_COLUMN_NAME))*1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // the format of your date
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+5")); // give a timezone reference for formating (see comment at the bottom
            String formattedDate = sdf.format(date);
            array_list.add(activity + formattedDate);
            res.moveToNext();
        }
        return array_list;
    }
    public long[] getlatitudes(int trackid)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        String latlongstring;

        Cursor res =  db.rawQuery( "select BULKLL from TRACKDATA where TRACKID="+trackid+"", null );
        int id[] = new int[res.getCount()];
        res.moveToFirst();
        latlongstring= res.getString(res.getColumnIndex("BULKLL"));
        String[] latlongs=latlongstring.split(";");
        long[] latlist = new long[latlongs.length];

        int j = 0;
        for (int i = 0; i < latlongs.length; i++) {
            String latlongs_sub[]= latlongs[i].split(",");

                if(i == 0) {
                    latlist[j] = Long.parseLong(latlongs_sub[0]);
                }
                else
                {
                    long previous=latlist[j-1];

                    long diff=Long.parseLong(latlongs_sub[0]);
                    latlist[j] = previous+diff;
                }
                j++;

        }
        res.close();

        return latlist;
    }
    public long[] getlongitudes(int trackid)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        String latlongstring;

        Cursor res =  db.rawQuery( "select BULKLL from TRACKDATA where TRACKID="+trackid+"", null );
        int id[] = new int[res.getCount()];
        res.moveToFirst();
        latlongstring= res.getString(res.getColumnIndex("BULKLL"));
        String[] latlongs=latlongstring.split(";");
        long[] longlist = new long[latlongs.length];
        int j = 0;
        for (int i = 0; i < latlongs.length; i++) {
            String latlongs_sub[]= latlongs[i].split(",");

                if(i == 0) {
                    longlist[j] = Long.parseLong(latlongs_sub[1]);
                }
                else
                {
                    long previous=longlist[j-1];

                    long diff=Long.parseLong(latlongs_sub[1]);
                    longlist[j] = previous+diff;
                }
                j++;

        }
        res.close();

        return longlist;
    }
    public long[] getaltitudes(int trackid)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        String altstring;

        Cursor res =  db.rawQuery( "select BULKAL from TRACKDATA where TRACKID="+trackid+"", null );
        res.moveToFirst();
        altstring= res.getString(res.getColumnIndex("BULKAL"));
        String[] alts=altstring.split(";");
        long[] altlist = new long[alts.length];
        int j = 0;
        j = 0;
        for (int i = 0; i < alts.length; i++) {
                    altlist[i] = Long.parseLong(alts[i]);
                }


        res.close();

        return altlist;
    }
    public int[] gettimestamps(int trackid)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        String timestampsstring;

        Cursor res =  db.rawQuery( "select BULKTIME from TRACKDATA where TRACKID="+trackid+"", null );
        int id[] = new int[res.getCount()];
        res.moveToFirst();
        timestampsstring= res.getString(res.getColumnIndex("BULKTIME"));
        String[] timestamps=timestampsstring.split(";");
        int[] timestampslist = new int[timestamps.length];
        int j = 0;
        j = 0;
        for (int i = 0; i < timestamps.length; i++) {
                if(i == 0) {
                    timestampslist[i] = Integer.parseInt(timestamps[i]) + trackid;
                }
                else
                {
                    timestampslist[i] = timestampslist[i-1] + Integer.parseInt(timestamps[i]) ;
                }
            }

        res.close();

        return timestampslist;
    }
    public int[] getHR(int trackid)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        String hrcompoststring;

        Cursor res =  db.rawQuery( "select BULKHR from TRACKDATA where TRACKID="+trackid+"", null );
        int id[] = new int[res.getCount()];
        res.moveToFirst();
        hrcompoststring= res.getString(res.getColumnIndex("BULKHR"));
        String[] hrcompost=hrcompoststring.split(";");
        int[] hrlist = new int[hrcompost.length];
        int j = 0;
        for (int i = 0; i < hrcompost.length; i++) {
            String hrlist_sub[]= hrcompost[i].split(",");

            if(i == 0) {
                hrlist[j] = Integer.parseInt(hrlist_sub[1]);
            }
            else
            {
                int previous=hrlist[j-1];

                int diff=Integer.parseInt(hrlist_sub[1]);
                hrlist[j] = previous+diff;
            }
            j++;

        }
        res.close();

        return hrlist;
    }
}