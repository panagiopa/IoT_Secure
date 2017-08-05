package com.upatras.android.iot_secure;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private TextView mRealDataTextView;
    private TextView mEmailTextView;
    private Button testButton;


    private AESGCM aes;
    private AESCBC mAESCBC;
    private FirebaseAuth mAuth;
    // [START declare_database_ref]
    private DatabaseReference rootDatabase;
    // [END declare_database_ref]
    private FirebaseUser currentUser;

    private IOTData IOT = new IOTData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            View headerView = navigationView.getHeaderView(0);

            // Views
            mEmailTextView = (TextView) headerView.findViewById(R.id.textUserEmail);
            mStatusTextView = (TextView) findViewById(R.id.status);
            mDetailTextView = (TextView) findViewById(R.id.detail);
            mRealDataTextView = (TextView) findViewById(R.id.textrealtest);

            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
                    currentUser.getEmail(), currentUser.isEmailVerified()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, currentUser.getUid()));

            if (mEmailTextView != null)
                mEmailTextView.setText(currentUser.getEmail());
            Log.d("Login", currentUser.getEmail());



            testButton = (Button) findViewById(R.id.buttonTest);
            testButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Code here executes on main thread after user presses button
                    ExecuteCmd();
                }
            });

            //GCM TEST
            aes = new AESGCM("b8eff1d661a9336d69370d545bbe51e1");

            //TODO get key from database sqlite per day differs
            mAESCBC = new AESCBC("867da24114289bd4bb3dcb1d8bd6da8c");

            aes_test();

            // [START initialize_database_ref]
            rootDatabase = FirebaseDatabase.getInstance().getReference();
            //get reference
            update_measures();
            // [END initialize_database_ref]

            getMeasures("2017", "8", "2", "01");

            //maintain Commands COunter
        //    MaintainCMDCounterCloud();
            CommandResponseMaintain();
            //TODO create a protected database
            /*
            String query = "select sqlite_version() AS sqlite_version";
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(":memory:", null);
            Cursor cursor = db.rawQuery(query, null);
            String sqliteVersion = "";
            if (cursor.moveToNext()) {
                sqliteVersion = cursor.getString(0);
            }
            Log.d("SQLite", sqliteVersion);
            */
            //writeNewUser(currentUser.getUid(), "test",currentUser.getEmail());
        } else {
            signOut();
        }

    }


    //TODO get massive data
    private void getMeasures(String year, String month, String day, String hour) {
        DatabaseReference measures = rootDatabase.child("measures").child(year).child(month).child(day).child(hour);
        measures.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> it = dataSnapshot.getChildren();
                String cipher = "";
                String time = "";
                Log.d("MASSIVE", time + "=" + cipher);
                for (DataSnapshot t : it) {
                    cipher = t.getValue(String.class);
                    time = t.getKey();
                    Log.d("MASSIVE", time + "=" + cipher);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    //TODO visualize data
    private void update_measures() {
        DatabaseReference measures = rootDatabase.child("realtime").child("measures");
        measures.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> it = dataSnapshot.getChildren();
                String cipher = "";
                String time = "";

                for (DataSnapshot t : it) {
                    cipher = t.getValue(String.class);
                    time = t.getKey();
                }

                Log.e("DATABASE", time + "=" + cipher);

                byte[] plaintext = new byte[0];
                try {
                    byte[] headerSaltAndCipherText = Base64.decode(cipher, Base64.DEFAULT);
                    plaintext = mAESCBC.decrypt(headerSaltAndCipherText);
                    String str = new String(plaintext, StandardCharsets.UTF_8);
                    Log.e("DATABASE", "STR = " + str);

                    try {
                        JSONObject obj = new JSONObject(str);
                        Iterator<String> iter = obj.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            try {
                                Object value = obj.get(key);
                                switch (key) {
                                    case "temp":
                                        IOT.setTemperature((int)value);
                                        break;
                                    case "hum":
                                        IOT.setHumidity((int)value);
                                        break;
                                    default:
                                        break;
                                }

                            } catch (JSONException e) {
                                // Something went wrong!
                            }
                        }
                    } catch (Throwable t) {
                        Log.e("DATABASE", "Could not parse malformed JSON: \"" + str + "\"");
                    }
                    //TODO Update UI function
                    mRealDataTextView.setText("TIME=" + time + "\nTemperature=" + IOT.getTemperature() + "\nHumidity=" + IOT.getHumidity());

                } catch (Exception e) {
                    Log.e("DATABASE", "FAIL");
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(this, EmailPasswordActivity.class);
        startActivity(intent);
        this.finish();

    }

    public void aes_test() {


    }
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //TODO Execute one and only Command
    private void ExecuteCmd() {
        if(IOT.getisSent()==false) {
            Toast.makeText(this, "Executing Command", Toast.LENGTH_SHORT).show();

            //COMMAND IS MAINTAIN NO NEED FOR ASK
            String ask = IOT.getCMDCounter();
            Log.e("AESGCM", "COMMAND=" + ask.toString());

            try {

                //CMD
                JSONObject jocmd = new JSONObject();
                jocmd.put("cmdid", 1);
                jocmd.put("cmddata", "NULL");

                String jsonstr1 = jocmd.toString();
                jsonstr1 = jsonstr1.replace(" ", "");
                byte[] cmd = jsonstr1.getBytes();

                //AAD
                JSONObject jo = new JSONObject();
                jo.put("uid", currentUser.getUid());
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                jo.put("time", ts);
                String jsonstr = jo.toString();
                jsonstr = jsonstr.replace(" ", "");

                byte[] aad = jsonstr.getBytes();

                // byte[] add = "0123456789".getBytes();
                byte[] cipher = aes.encrypt(cmd, ask, aad);


                Log.e("AESGCM", "Cipher=" + Base64.encodeToString(cipher, Base64.DEFAULT));

                // byte[] plaintext = aes.decrypt(cipher,ask);
                // String message1 = new String(plaintext);
                // Log.e("AESGCM", message1);
                HashMap<String, Object> result = new HashMap<>();

                result.put("AAD", jo.toString());
                result.put("COMMAND", Base64.encodeToString(cipher, Base64.DEFAULT));
                result.put("CMDCounter", ask);

                rootDatabase.child("realtime").child("commands").updateChildren(result);
                //WAIT FOR A RESPONSE
                IOT.setisSent(true);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void CommandResponseMaintain() {

        DatabaseReference MaintainCMDCounters = rootDatabase.child("realtime").child("commands");
        MaintainCMDCounters.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               // if(IOT.getisSent() == true) //get response
              //  {
                Iterable<DataSnapshot> it = dataSnapshot.getChildren();
                String value = "";
                String key = "";

                for (DataSnapshot t : it) {
                    value = t.getValue(String.class);
                    key = t.getKey();
                    if(key.equals("CMDCounter"))
                    {
                        IOT.setCMDCounter(value);
                        Log.e("RESPONSE", "COMMANDCOUNTER=" + value);
                    }
                    else if(key.equals("COMMAND"))
                    {
                        if((value.equals("ACK"))||(value.equals("NULL"))||(value.equals("NACK")))
                        {
                            if(IOT.getisSent()==true) {
                                IOT.setCMDResponse(value);
                                Log.e("RESPONSE", "GET RESPONSE");
                                //TODO Update UI
                                IOT.setisSent(false);
                            }
                        }
                    }
                    Log.e("RESPONSE", key + "=" + value);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}