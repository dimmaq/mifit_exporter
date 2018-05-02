package com.example.david.myapplication;

/**
 * Created by David on 31/01/2017.
 */

import android.os.Bundle;
        import android.app.Activity;
        import android.app.AlertDialog;

        import android.content.DialogInterface;
        import android.content.Intent;
        import android.database.Cursor;

import android.os.Environment;
import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;

        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import static com.example.david.myapplication.DBhelper.TRACKINGS_COLUMN_NAME;
import static java.lang.System.out;

public class DisplayAct extends Activity {
    int from_Where_I_Am_Coming = 0;
    private DBhelper mydb ;
    Button export;
    TextView dat ;
    TextView donex ;
    TextView donexpath ;
    TextView activit;
    TextView lengt;
    int id_To_Update = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displaytrack);
        dat = (TextView) findViewById(R.id.editTextdate);
        activit = (TextView) findViewById(R.id.editTextactivity);
        lengt = (TextView) findViewById(R.id.editTextlength);
        donex = (TextView) findViewById(R.id.editTextDone);
        donex.setFocusable(false);
        donex.setClickable(false);
        donex.setVisibility(View.INVISIBLE);
        donexpath = (TextView) findViewById(R.id.editTextDonepath);
        donexpath.setFocusable(false);
        donexpath.setClickable(false);
        donexpath.setVisibility(View.INVISIBLE);
        final String activity;
        export = (Button) findViewById(R.id.exportbutton);
        mydb = new DBhelper(this);
        mydb.opendb();
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            int Value = extras.getInt("id");

            if(Value>0){
                //means this is the view part not the add contact part.
                Cursor rs = mydb.getData();
                rs.moveToPosition(Value-1);
                //id_To_Update = Value;
                //rs.moveToFirst();
                final Date dateformat = new Date(rs.getInt(rs.getColumnIndex(TRACKINGS_COLUMN_NAME))*1000L);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
                //sdf.setTimeZone(TimeZone.getTimeZone("GMT+5")); // give a timezone reference for formating (see comment at the bottom
                final String date = sdf.format(dateformat);
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss"); // the format of your date
                //sdf2.setTimeZone(TimeZone.getTimeZone("GMT+5")); // give a timezone reference for formating (see comment at the bottom
                final String date2 = sdf2.format(dateformat);
                final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // the format of your date
                final String date3 = sdf3.format(dateformat);
                sdf2 = new SimpleDateFormat("yyyy");
                final String year = sdf2.format(dateformat);
                sdf2 = new SimpleDateFormat("MM");
                final String month = sdf2.format(dateformat);
                sdf2 = new SimpleDateFormat("dd");
                final String day = sdf2.format(dateformat);
                sdf2 = new SimpleDateFormat("HH");
                final String hour = sdf2.format(dateformat);
                sdf2 = new SimpleDateFormat("mm");
                final String minute = sdf2.format(dateformat);
                sdf2 = new SimpleDateFormat("ss");
                final String second = sdf2.format(dateformat);
                int activity_index= rs.getInt(rs.getColumnIndex(DBhelper.ACTIVITY_COLUMN_NAME));
                switch (activity_index){
                    case 1:
                        activity="Running";
                        break;
                    case 6:
                        activity ="Walking";
                        break;
                    case 7:
                        activity ="Trail";
                        break;
                    case 9:
                        activity ="Cycling";
                        break;
                    default:
                        activity ="Others";
                        break;

                }
                final int trackid= rs.getInt(rs.getColumnIndex(TRACKINGS_COLUMN_NAME));
                Cursor rs_2 = mydb.getRecord(trackid);
                rs_2.moveToFirst();
                final int final_timestamp=rs_2.getInt(rs_2.getColumnIndex(DBhelper.ENDTIME_COLUMN_NAME));
                final int initial_timestamp=rs.getInt(rs.getColumnIndex(TRACKINGS_COLUMN_NAME));
                final int length_int = final_timestamp - initial_timestamp;
                int hours = length_int/3600;
                int minutes = ((length_int-(hours*3600))/60);
                int seconds = (length_int-(hours*3600) -minutes*60);
                String length= String.valueOf(hours) + ":"+ String.valueOf(minutes) + ":" + String.valueOf(seconds);


                final int total_distance = rs_2.getInt(rs_2.getColumnIndex(DBhelper.DISTANCE_COLUMN_NAME));
                final int total_calories = rs_2.getInt(rs_2.getColumnIndex(DBhelper.CAL_COLUMN_NAME));
                final String avg_hr = rs_2.getString(rs_2.getColumnIndex(DBhelper.AVGHR_COLUMN_NAME));


                if (!rs.isClosed())  {
                    rs.close();
                }
                Button b = (Button)findViewById(R.id.exportbutton);
                b.setVisibility(View.VISIBLE);

                dat.setText((CharSequence)date);
                dat.setFocusable(false);
                dat.setClickable(false);

                activit.setText((CharSequence)activity);
                activit.setFocusable(false);
                activit.setClickable(false);

                lengt.setText((CharSequence)length);
                lengt.setFocusable(false);
                lengt.setClickable(false);


                export.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            PrintWriter writer = new PrintWriter(Environment.getExternalStorageDirectory().getPath() +"/Amazfit_Exporter/"+date2+".gpx", "UTF-8");

                            writer.println("<?xml version='1.0' encoding='UTF-8'?>");
                            writer.println("<gpx version='1.1' creator='Amazfit_export by dvd_ath' xsi:schemaLocation='http://www.topografix.com/GPX/1/1");
                            writer.println("                                 http://www.topografix.com/GPX/1/1/gpx.xsd");
                            writer.println("                                 http://www.garmin.com/xmlschemas/GpxExtensions/v3");
                            writer.println("                                 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd");
                            writer.println("		                            http://www.garmin.com/xmlschemas/TrackPointExtension/v1");
                            writer.println("                                 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd' xmlns='http://www.topografix.com/GPX/1/1' xmlns:gpxtpx='http://www.garmin.com/xmlschemas/TrackPointExtension/v1' xmlns:gpxx='http://www.garmin.com/xmlschemas/GpxExtensions/v3' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>");
                            writer.println(" <metadata>");
                            writer.println("  <link href='https://github.com/botmakerdvd/amazfit_exporter'>");
                            writer.println("    <text>Amazfit exporter</text>");
                            writer.println("  </link>");
                            writer.println("  <time>"+year+"-"+month+"-"+day+"T"+hour+":"+minute+":"+second+".000+05:00</time>");
                            writer.println(" </metadata>");
                            writer.println(" <trk>");
                            writer.println("  <trkseg>");
                            ArrayList<Point> points = mydb.getPoints(trackid);

                            for(Point p : points)
                            {
                                Date dateformatpoint = new Date(p.getTimestamp() * 1000L);
                                SimpleDateFormat sdf2point = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss"); // the format of your date
                                sdf2point.setTimeZone(TimeZone.getTimeZone("GMT+1")); // give a timezone reference for formating (see comment at the bottom
                                sdf2point = new SimpleDateFormat("yyyy");
                                final String yearpoint = sdf2point.format(dateformatpoint);
                                sdf2point = new SimpleDateFormat("MM");
                                final String monthpoint = sdf2point.format(dateformatpoint);
                                sdf2point = new SimpleDateFormat("dd");
                                final String daypoint = sdf2point.format(dateformatpoint);
                                sdf2point = new SimpleDateFormat("HH");
                                final String hourpoint = sdf2point.format(dateformatpoint);
                                sdf2point = new SimpleDateFormat("mm");
                                final String minutepoint = sdf2point.format(dateformatpoint);
                                sdf2point = new SimpleDateFormat("ss");
                                final String secondpoint = sdf2point.format(dateformatpoint);
                                writer.println("   <trkpt lon='"+p.getLon() / 100000000.0 +"' lat='"+p.getLat() / 100000000.0 +"'>");
                                if(p.getAlt() != -200000.0)
                                    writer.println("    <ele>"+p.getAlt()/10.0+"</ele>");
                                writer.println("    <time>"+yearpoint+"-"+monthpoint+"-"+daypoint+"T"+hourpoint+":"+minutepoint+":"+secondpoint+".000+05:00</time>");
                                writer.println("    <extensions>");
                                writer.println("     <gpxtpx:TrackPointExtension>");
                                if(p.isHasHR())
                                    writer.println(" 	 <gpxtpx:hr>"+p.getHr()+"</gpxtpx:hr>");
                                writer.println("     </gpxtpx:TrackPointExtension>");
                                writer.println("    </extensions>");
                                writer.println("   </trkpt>");
                            }

                            writer.println("  </trkseg>");
                            writer.println(" </trk>");
                            writer.println("</gpx>");
                            writer.close();
                            donex.setText(getString(R.string.Exported));
                            donex.setFocusable(false);
                            donex.setClickable(false);
                            donex.setVisibility(View.VISIBLE);
                            donexpath.setText((CharSequence)Environment.getExternalStorageDirectory().getPath() +"/Amazfit_Exporter/");
                            donexpath.setFocusable(false);
                            donexpath.setClickable(false);
                            donexpath.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            // do something
                        }


                    }
                });

                // TCX Export
                Button exportTcx = (Button) findViewById(R.id.exporttcxbutton);
                exportTcx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try
                        {
                            ArrayList<Point> points = mydb.getPoints(trackid);

                            int maxhr = 0;
                            for (Point p : points) {
                                if (p.hr > maxhr)
                                    maxhr = p.hr;
                            }

                            PrintWriter writer = new PrintWriter(Environment.getExternalStorageDirectory().getPath() +"/Amazfit_Exporter/"+date2+".tcx", "UTF-8");

                            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                            writer.println("<TrainingCenterDatabase xsi:schemaLocation=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd\" xmlns:ns5=\"http://www.garmin.com/xmlschemas/ActivityGoals/v1\" xmlns:ns3=\"http://www.garmin.com/xmlschemas/ActivityExtension/v2\" xmlns:ns2=\"http://www.garmin.com/xmlschemas/UserProfile/v2\" xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns4=\"http://www.garmin.com/xmlschemas/ProfileExtension/v1\">");
                            writer.println("<Activities>");
                            writer.println("    <Activity Sport=\"" + activity + "\">");
                            writer.println("    <Id>" + date3 + "</Id>");
                            writer.println("    <Lap StartTime=\"" + date3 + "\">");
                            writer.println("        <TotalTimeSeconds>" + length_int + "</TotalTimeSeconds>");
                            writer.println("        <DistanceMeters>" + total_distance + "</DistanceMeters>");
                            //writer.println("        <MaximumSpeed></MaximumSpeed>");
                            writer.println("        <Calories>" + total_calories + "</Calories>");
                            writer.println("        <AverageHeartRateBpm><Value>" + avg_hr + "</Value></AverageHeartRateBpm>");
                            writer.println("        <MaximumHeartRateBpm><Value>" + maxhr + "</Value></MaximumHeartRateBpm>");
                            writer.println("        <Intensity>Active</Intensity>");
                            writer.println("        <TriggerMethod>Manual</TriggerMethod>");
                            writer.println("        <Track>");
                            for (Point p : points)
                            {
                                Date time = new Date(p.getTimestamp() * 1000L);
                                String timeStr = sdf3.format(time);

                                writer.println("            <Trackpoint>");
                                writer.println("                <Time>" + timeStr + "</Time>");
                                //writer.println("                <DistanceMeters>" + p.getDist() + "</DistanceMeters>");
                                writer.println("                <Position>");
                                writer.println("                    <LatitudeDegrees>" + p.getLat() / 100000000.0 +  "</LatitudeDegrees>");
                                writer.println("                    <LongitudeDegrees>" + p.getLon() / 100000000.0 + "</LongitudeDegrees>");
                                writer.println("                </Position>");
                                if(p.getAlt() != -200000.0)
                                    writer.println("                <AltitudeMeters>" + p.getAlt()/10.0 + "</AltitudeMeters>");
                                if (p.isHasHR())
                                    writer.println("                <HeartRateBpm><Value>" + p.getHr() + "</Value></HeartRateBpm>");
                                writer.println("            </Trackpoint>");
                            }


                            writer.println("        </Track></Lap></Activity></Activities></TrainingCenterDatabase>");
                            writer.close();


                            //----------------------------------------------------------------------------------

                            ArrayList<Lap> laps = mydb.getLaps(trackid, total_distance, points);

                            writer = new PrintWriter(Environment.getExternalStorageDirectory().getPath() +"/Amazfit_Exporter/"+date2+"_laps.tcx", "UTF-8");

                            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                            writer.println("<TrainingCenterDatabase xsi:schemaLocation=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd\" xmlns:ns5=\"http://www.garmin.com/xmlschemas/ActivityGoals/v1\" xmlns:ns3=\"http://www.garmin.com/xmlschemas/ActivityExtension/v2\" xmlns:ns2=\"http://www.garmin.com/xmlschemas/UserProfile/v2\" xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns4=\"http://www.garmin.com/xmlschemas/ProfileExtension/v1\">");
                            writer.println("<Activities>");
                            writer.println("    <Activity Sport=\"" + activity + "\">");
                            writer.println("    <Id>" + date3 + "</Id>");
                            for (int i = 0; i < laps.size(); i++) {
                                Lap lap = laps.get(i);

                                Date startTime = new Date(lap.getStartTime()*1000L);
                                String startTimeStr = sdf3.format(startTime);
                                int calories = (int) Math.round((double) total_calories / total_distance * lap.getDist());

                                writer.println("    <Lap StartTime=\"" + startTimeStr + "\">");
                                writer.println("        <TotalTimeSeconds>" + lap.getTime() + "</TotalTimeSeconds>");
                                writer.println("        <DistanceMeters>" + lap.getDist() + "</DistanceMeters>");
                                //writer.println("        <MaximumSpeed></MaximumSpeed>");
                                writer.println("        <Calories>" + calories + "</Calories>");
                                writer.println("        <AverageHeartRateBpm><Value>" + lap.getAvgHr() + "</Value></AverageHeartRateBpm>");
                                writer.println("        <MaximumHeartRateBpm><Value>" + lap.getMaxHr() + "</Value></MaximumHeartRateBpm>");
                                writer.println("        <Intensity>Active</Intensity>");
                                writer.println("        <Cadence>" + lap.getCadence() + "</Cadence>");
                                writer.println("        <TriggerMethod>Distance</TriggerMethod>"); // Manual
                                writer.println("        <Track>");
                                for (Point p : lap.getPoints())
                                {
                                    Date time = new Date(p.getTimestamp() * 1000L);
                                    String timeStr = sdf3.format(time);

                                    writer.println("            <Trackpoint>");
                                    writer.println("                <Time>" + timeStr + "</Time>");
                                    //writer.println("                <DistanceMeters>" + p.getDist() + "</DistanceMeters>");
                                    writer.println("                <Position>");
                                    writer.println("                    <LatitudeDegrees>" + p.getLat() / 100000000.0 +  "</LatitudeDegrees>");
                                    writer.println("                    <LongitudeDegrees>" + p.getLon() / 100000000.0 + "</LongitudeDegrees>");
                                    writer.println("                </Position>");
                                    if(p.getAlt() != -200000.0)
                                        writer.println("                <AltitudeMeters>" + p.getAlt()/10.0 + "</AltitudeMeters>");
                                    if (p.isHasHR())
                                        writer.println("                <HeartRateBpm><Value>" + p.getHr() + "</Value></HeartRateBpm>");
                                    writer.println("                <Cadence>" + p.getCadence() + "</Cadence>");
                                    writer.println("            </Trackpoint>");
                                }


                                writer.println("        </Track></Lap>");
                            }
                            writer.println("</Activity></Activities></TrainingCenterDatabase>");
                            writer.close();


                            donex.setText(getString(R.string.Exported));
                            donex.setFocusable(false);
                            donex.setClickable(false);
                            donex.setVisibility(View.VISIBLE);
                            donexpath.setText((CharSequence)Environment.getExternalStorageDirectory().getPath() +"/Amazfit_Exporter/");
                            donexpath.setFocusable(false);
                            donexpath.setClickable(false);
                            donexpath.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            Toast.makeText(v.getContext(), e.toString(), Toast.LENGTH_LONG).show();
                        }


                    }
                });

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Bundle extras = getIntent().getExtras();

        if(extras !=null) {
            int Value = extras.getInt("id");
            /*if(Value>0){
                getMenuInflater().inflate(R.menu.display_contact, menu);
            } else{
                getMenuInflater().inflate(R.menu.menu_main, menu);
            }*/
        }
        return true;
    }



    public void export(View view) {
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            int Value = extras.getInt("id");

        }
    }

    public void ExportTcxClick(View view)
    {
        Toast.makeText(this, "!!!", Toast.LENGTH_LONG).show();
    }
}