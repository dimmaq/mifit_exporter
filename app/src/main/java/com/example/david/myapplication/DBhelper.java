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
    public static final String DISTANCE_COLUMN_NAME = "DISTANCE";
    public static final String CAL_COLUMN_NAME = "CAL";
    public static final String AVGHR_COLUMN_NAME = "AVGHR";
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

    public ArrayList<Lap> getLaps(int trackid, int totalDistance, ArrayList<Point> points) {
        int points_count = points.size();
        ArrayList<Lap> laps_list = new ArrayList<Lap>();
        Cursor res =  db.rawQuery( "select * from TRACKDATA where TRACKID="+trackid+"", null );
        res.moveToFirst();
        //String pacedata = res.getString(res.getColumnIndex("BULKPACE")); // it's speed, but what? 0.72;0.72;0.51;0.47;0.47;0.47
        String lapsdata = res.getString(res.getColumnIndex("KILOMARKED")); // №,lapTime,geohash,gpsidx,aveHr,currentTime;....

        /*
        It's speed.
        String[] pacestrs = pacedata.split(";");
        if (pacestrs.length != points_count)
            return null;
        for(int i = 0; i < points_count; i++) {

        }
        */



        int startTime = trackid;
        String[] lapsstrings = lapsdata.split(";");
        int pointIdx = 0;
        for(int i = 0; i < lapsstrings.length; i++) {
            String[] lapdat = lapsstrings[i].split(",");
            if (lapdat.length < 6)
                return null;
            // №,lapTime,geohash,gpsidx,aveHr,currentTime
            int allTime = Integer.parseInt(lapdat[5]) + trackid; // currentTime
            int time = Integer.parseInt(lapdat[1]); // lapTime
            int dist = 1000; // 1km
            int maxSpeed = 0;
            int avgHr = Integer.parseInt(lapdat[4]);
            int maxHr = 0;
            int cadence = 0;
            int cadenceSum = 0;
            ArrayList<Point> lapPoints = new ArrayList<>();

            // append points
            for (int j = pointIdx; j < points_count; j++) {
                Point point = points.get(j);
                // point in lap
                if (point.getTimestamp() < allTime) {
                    // HR
                    int hr = point.getHr();
                    if (hr > maxHr)
                        maxHr = hr;
                    // CAdence
                    cadenceSum = cadenceSum + point.getCadence();
                    // first lap point
                    if (lapPoints.size() == 0)
                        point.setLapStart(true);
                    lapPoints.add(point);
                    pointIdx = j + 1;
                } else {
                    // next lap
                    pointIdx = j;
                    break;
                }
            }

            // avg cadence
            cadence = (int) Math.round((double)cadenceSum / lapPoints.size());
            laps_list.add(new Lap(startTime, time, dist, maxSpeed, avgHr, maxHr, cadence, lapPoints));
            startTime = allTime;
        }

        // last points to last lap
        if (pointIdx < points_count) {
            int time = points.get(points_count - 1).getTimestamp() - points.get(pointIdx-1).getTimestamp();
            int dist = totalDistance - laps_list.size() * 1000;
            int maxSpeed = 0;
            int avgHr;
            int hrSum = 0;
            int maxHr = 0;
            int cadence;
            int cadenceSum = 0;
            ArrayList < Point > lapPoints = new ArrayList<>();
            for (int i = pointIdx; i < points_count; i++) {
                Point p = points.get(i);
                int hr = p.getHr();
                hrSum = hrSum + hr;
                if (maxHr < hr)
                    maxHr = hr;
                cadenceSum = cadenceSum + p.getCadence();
                lapPoints.add(p);
            }
            avgHr = (int) Math.round((double) hrSum / lapPoints.size());
            cadence = (int) Math.round((double) cadenceSum / lapPoints.size());

            // last lap
            if (lapPoints.size() > 0)
                laps_list.add(new Lap(startTime, time, dist, maxSpeed, avgHr, maxHr, cadence, lapPoints));
        }

        return laps_list;
    }

    public ArrayList<Point> getPoints(int trackid) {
        ArrayList<Point> points_list = new ArrayList<Point>();
        Cursor res =  db.rawQuery( "select * from TRACKDATA where TRACKID="+trackid+"", null );
        res.moveToFirst();
        String lldata = res.getString(res.getColumnIndex("BULKLL"));
        String altdata = res.getString(res.getColumnIndex("BULKAL"));
        String timedata = res.getString(res.getColumnIndex("BULKTIME"));
        String hrdata = res.getString(res.getColumnIndex("BULKHR"));
        String gaitdata = res.getString(res.getColumnIndex("BULKGAIT")); // sec,XXX,XXX,cadens;....;....


        String[] timestrings = timedata.split(";");
        // int size = res.getInt(res.getColumnIndex("SIZE")); When data received from MiFit account, no Size is set in table
        int size = timestrings.length;
        int[] timestamps = new int[size];
        int timediff = 0;
        timestamps[0] = trackid;
        for(int i = 1; i < size; i++) {
            timediff = Integer.parseInt(timestrings[i]);
            if(timediff == 0 && i < 31) {
                timediff = 1;
            }
            // 2;0 situations to 1;1
            else if(timediff == 0 && (timestamps[i-1] - timestamps[i-2]) == 2) {
                timestamps[i-1] = timestamps[i-2] + 1;
                timediff = 1;
            }
            // 2;1;0 situations to 1;1;1
            else if(timediff == 0 && (timestamps[i-1] - timestamps[i-2]) == 1 && (timestamps[i-2] - timestamps[i-3]) == 2) {
                timestamps[i-2] = timestamps[i-3] + 1;
                timestamps[i-1] = timestamps[i-2] + 1;
                timediff = 1;
            }
            else if(timediff == 0)
                timediff = 1;
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
        boolean[] hasHrs= new boolean[size];
        if (hrstrings.length > size ) {
            ArrayList <Integer> hrList = new ArrayList <>();
            int hrVal = 0;
            for (int i = 0; i < hrstrings.length; i++) {
                String hr_subs[] = hrstrings[i].split(",");
                int hrDif = Integer.parseInt(hr_subs[1]);
                int shift = 1;
                if (hr_subs[0].length() > 0)
                    shift = Integer.parseInt(hr_subs[0]);
                if (i == 0)
                    hrVal = hrDif;
                else
                    hrVal = hrVal + hrDif;

                do {
                    hrList.add(hrVal);
                    shift--;
                } while (shift > 0);
            }
            double k = (double)hrList.size() / (double)size;
            for(int i = 0; i < size; i++) {
                long hrIdx = Math.round((k * (i + 1)) - 1);
                if (hrIdx >= hrList.size())
                    hrIdx = hrList.size();
                else if (hrIdx < 0)
                    hrIdx = 0;

                hrs[i] = hrList.get((int)hrIdx);
                hasHrs[i] = true;
            }


        } else {
            int hr_cur = 0;
            for (int i = 0; i < hrstrings.length && hr_cur < size; i++) {
                String hr_subs[] = hrstrings[i].split(",");
                int hrValue = Integer.parseInt(hr_subs[1]);
                int shift = 1;
                if (hr_subs[0].length() > 0)
                    shift = Integer.parseInt(hr_subs[0]);
                if (i == 0) {
                    hrs[0] = hrValue;
                    hasHrs[0] = false;
                    hr_cur++;
                    shift--;
                }
                for (int j = 1; j < shift && hr_cur < size; j++) {
                    hrs[hr_cur] = hrs[hr_cur - 1];
                    hasHrs[hr_cur] = false;
                    hr_cur++;
                }
                if (i != 0 && hr_cur < size) {
                    hrs[hr_cur] = hrs[hr_cur - 1] + hrValue;
                    hasHrs[hr_cur] = true;
                    hr_cur++;
                }
            }
        }

        // cadence
        String[] gaitstrings = gaitdata.split(";");
        int cadenceArrLen = gaitstrings.length;
        int[] cadenceTimes = new int[cadenceArrLen];
        int[] cadenceVals = new int[cadenceArrLen];
        for(int i = 0; i < gaitstrings.length; i++) {
            String[] gaitstrs = gaitstrings[i].split(",");
            if (gaitstrs.length < 4)
                return null;
            cadenceVals[i] = Integer.parseInt(gaitstrs[3]);
            int time = Integer.parseInt(gaitstrs[0]);
            if (i == 0) {
                cadenceTimes[0] = time + trackid;
            } else {
                cadenceTimes[i] = cadenceTimes[i-1] + time;
            }
        }
        // fill zero
        int cadval = 0;
        for (int i = cadenceArrLen - 1; i >= 0; i--) {
            if (cadenceVals[i] > 0)
                cadval = cadenceVals[i];
            else if (cadval > 0)
                cadenceVals[i] = cadval;
        }
        // find avg
        int[] cadences = new int[size];
        int cadenceIdx = 0;
        for(int i = 0; i < size; i++) {
            int time = timestamps[i];
            int cadSum = 0;
            int cadCnt = 0;
            for (int j = cadenceIdx; j < cadenceArrLen; j ++) {
                if (cadenceTimes[j] > time) {
                    cadenceIdx = j;
                    break;
                }
                else {
                    cadSum = cadSum + cadenceVals[j];
                    cadCnt++;
                }
            }
            if (cadCnt > 0)
                cadences[i] = (int) Math.round((double)cadSum / cadCnt);
        }
        // fill zero
        cadval = 0;
        for (int i = size - 1; i >= 0; i--) {
            if (cadences[i] > 0)
                cadval = cadences[i];
            else if (cadval > 0)
                cadences[i] = cadval;
        }



        for(int i = 0; i < size; i++) {
            if(hasHrs[i]) {
                points_list.add(new Point(timestamps[i], lats[i], lons[i], alts[i], cadences[i], hrs[i]));
            }
            else {
                points_list.add(new Point(timestamps[i], lats[i], lons[i], alts[i], cadences[i]));
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