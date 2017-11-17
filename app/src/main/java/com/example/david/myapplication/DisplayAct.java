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
        String activity;
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
                Date dateformat = new Date(rs.getInt(rs.getColumnIndex(TRACKINGS_COLUMN_NAME))*1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+1")); // give a timezone reference for formating (see comment at the bottom
                String date = sdf.format(dateformat);
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss"); // the format of your date
                sdf2.setTimeZone(TimeZone.getTimeZone("GMT+1")); // give a timezone reference for formating (see comment at the bottom
                final String date2 = sdf2.format(dateformat);
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
                int final_timestamp=rs_2.getInt(rs_2.getColumnIndex(DBhelper.ENDTIME_COLUMN_NAME));
                int initial_timestamp=rs.getInt(rs.getColumnIndex(TRACKINGS_COLUMN_NAME));
                int length_int = final_timestamp - initial_timestamp;
                int hours = length_int/3600;
                int minutes = ((length_int-(hours*3600))/60);
                int seconds = (length_int-(hours*3600) -minutes*60);
                String length= String.valueOf(hours) + ":"+ String.valueOf(minutes) + ":" + String.valueOf(seconds);
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
                            writer.println("  <time>"+year+"-"+month+"-"+day+"T"+hour+":"+minute+":"+second+".000Z</time>");
                            writer.println(" </metadata>");
                            writer.println(" <trk>");
                            writer.println("  <trkseg>");
                            long latitudes[]= mydb.getlatitudes(trackid);
                            long longitudes[]=mydb.getlongitudes(trackid);
                            long altitudes[]=mydb.getaltitudes(trackid);
                            int timestamps[]=mydb.gettimestamps(trackid);
                            int HR[]=mydb.getHR(trackid);
                            int j=0;
                            for(int i=0;i<timestamps.length;i++)
                            {
                                Date dateformatpoint = new Date(timestamps[i]*1000L);
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
                                writer.println("   <trkpt lon='"+longitudes[i] / 100000000.0 +"' lat='"+latitudes[i] / 100000000.0 +"'>");
                                if(altitudes[i] != -200000.0)
                                    writer.println("    <ele>"+altitudes[i]/10.0+"</ele>");
                                writer.println("    <time>"+yearpoint+"-"+monthpoint+"-"+daypoint+"T"+hourpoint+":"+minutepoint+":"+secondpoint+".000Z</time>");
                                writer.println("    <extensions>");
                                writer.println("     <gpxtpx:TrackPointExtension>");
                                if(i< HR.length)
                                    writer.println(" 	 <gpxtpx:hr>"+HR[i]+"</gpxtpx:hr>");
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
}