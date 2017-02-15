package com.example.david.myapplication;

        import android.Manifest;
        import android.app.Activity;
        import android.app.AlertDialog.Builder;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.Toast;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.widget.EditText;
        import eu.chainfire.libsuperuser.Shell;
        import android.view.View.OnClickListener;

        import com.google.android.gms.appindexing.Action;
        import com.google.android.gms.appindexing.AppIndex;
        import com.google.android.gms.appindexing.Thing;
        import com.google.android.gms.common.api.GoogleApiClient;

        import java.sql.*;
        import java.io.File;
        import java.util.ArrayList;
        import java.util.List;


        import static java.lang.System.out;
//Created by themakeinfo.com,Promote us !!!

class database_mifit {
    public static SQLiteDatabase db;
    EditText editName;
}


public class MainActivity extends Activity {

    Button load, recv, shut, sysui;
    DBhelper db;
    private ListView obj;
    EditText editName;
    private static final int PERMISSION_REQUEST_CODE = 1;
    boolean suAvailable = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DBhelper(this);
        int tracks[];
        suAvailable = Shell.SU.available();
        if (suAvailable) {
            /*File theDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Amazfit_Exporter");
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                try {
                    theDir.mkdir();
                } catch (SecurityException se) {
                    //handle it
                }
            }*/
            if (Build.VERSION.SDK_INT >= 23)
            {
                if (checkPermission())
                {
                    // Code for above or equal 23 API Oriented Device
                    // Your Permission granted already .Do next code
                } else {
                    requestPermission(); // Code for permission
                }
            }
            else
            {

                // Code for Below 23 API Oriented Device
                // Do next code
            }
            File folder = new File(Environment.getExternalStorageDirectory().toString()+"/Amazfit_Exporter");
            folder.mkdirs();
            List<String> fichero = Shell.SU.run("ls /data/data/com.xiaomi.hm.health/databases/origin_db_* | grep -v journal");
            String nombredb=fichero.get(0);
            String comando= "cp "+nombredb +" " + Environment.getExternalStorageDirectory().getPath() + "/Amazfit_Exporter/mifitdb.db";
            Shell.SU.run(comando);
            db.opendb();
            ArrayList array_list = db.getAllTracks();
            ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, array_list);

            obj = (ListView) findViewById(R.id.listView1);
            obj.setAdapter(arrayAdapter);
        } else {
            Toast.makeText(getApplicationContext(), "Phone not Rooted", Toast.LENGTH_SHORT).show();
        }
        obj.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                       @Override
                                       public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                           // TODO Auto-generated method stub
                                           int id_To_Search = arg2 + 1;

                                           Bundle dataBundle = new Bundle();
                                           dataBundle.putInt("id", id_To_Search);
                                           Intent intent = new Intent(getApplicationContext(),DisplayAct.class);

                                           intent.putExtras(dataBundle);
                                           startActivity(intent);
                                       }
        });




        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class StartUp extends AsyncTask<String, Void, Void> {


        database_mifit db;
        private Context context = null;
        boolean suAvailable = false;

        //Created by themakeinfo.com,Promote us !!!
        public StartUp setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        protected Void doInBackground(String... params) {
            suAvailable = Shell.SU.available();
            if (suAvailable) {

                // suResult = Shell.SU.run(new String[] {"cd data; ls"}); Shell.SU.run("reboot");
                switch (params[0]) {
                    case "load": {
                        db = new database_mifit();
                        db.db = openOrCreateDatabase(Environment.getExternalStorageDirectory().getPath() + "/origin_db_475434488", Context.MODE_PRIVATE, null);

                    }
                    break;
                }
            } else {
                Toast.makeText(getApplicationContext(), "Phone not Rooted", Toast.LENGTH_SHORT).show();
            }

            return null;
        }


    }


}