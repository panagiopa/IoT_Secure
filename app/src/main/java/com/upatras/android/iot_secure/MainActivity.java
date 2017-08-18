package com.upatras.android.iot_secure;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

import android.support.v7.app.AlertDialog;
import android.text.InputType;
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

import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private TextView mRealDataTextView;
    private TextView mEmailTextView;
    private ToggleButton mToggleButton;


    private AESGCM mAESGCM;
    private AESCBC mAESCBC;
    private FirebaseAuth mAuth;
    // [START declare_database_ref]
    private DatabaseReference rootDatabase;
    // [END declare_database_ref]
    private FirebaseUser currentUser;

    private IOTData IOT = new IOTData();

    private SQLiteDatabase db;

    private String CURRENT_AES_KEY_GCM="";
    private static Integer RAND_PASS;

    private AlertDialog alertinc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            //INIT UI
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

            mToggleButton = (ToggleButton) findViewById(R.id.toggleButton);
            mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // The toggle is enabled
                        ExecuteCmd();
                        mToggleButton.setEnabled(false);
                    } else {
                        // The toggle is disabled
                        ExecuteCmd();
                        mToggleButton.setEnabled(false);
                    }
                }
            });

            AlertDialog.Builder builderinc = new AlertDialog.Builder(this);
            builderinc.setMessage("Incorrect password");
            builderinc.setCancelable(true);

            builderinc.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            signOut();
                        }
                    });

            alertinc = builderinc.create();


            //TODO PROMPT get 4 DIGIT PASSWORD

            try
            {
                Intent i = getIntent();
                String rndstr = i.getStringExtra("digit4");

                RAND_PASS = Integer.parseInt(rndstr);
                //9577;
                boolean ret = getAESGCMKey();

                if(ret ==true) {
                    initFunctionallity();

                }else
                {
                    alertinc.show();
                }
            }
            catch (NumberFormatException nfe)
            {
                Log.e("SKATA","SKATA1");
                // bad data - set to sentinel
                //PROMPT
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Please enter the 4 digit password:");
                builder.setCancelable(false);
                // Set up the input
                final EditText input = new EditText(this);
                //TODO PASSWORD INPUT
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                builder.setCancelable(false);
                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String rnd = input.getText().toString();
                        RAND_PASS = Integer.parseInt(rnd);
                        boolean ret = getAESGCMKey();
                        if(ret ==true) {
                            initFunctionallity();


                        }else
                        {
                            alertinc.show();
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        signOut();
                    }
                });

                builder.show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                alertinc.show();

            }

        } else {
            signOut();
        }

    }


    private void initFunctionallity()
    {
        mAESGCM = new AESGCM(CURRENT_AES_KEY_GCM);

        //TODO get key from database sqlite per day differs
        mAESCBC = new AESCBC("c7753ebfaceca06f973eb20c4ca348bf");

        // [START initialize_database_ref]
        rootDatabase = FirebaseDatabase.getInstance().getReference();
        //get reference
        update_measures();
        // [END initialize_database_ref]

        CommandResponseMaintain();
    }

    private boolean getAESGCMKey(){
        String userid = bytesToHex(currentUser.getUid().getBytes());
        String hexkey = userid.substring(0,28) + String.format("%04x", this.RAND_PASS);
        String salt = userid.substring(28,44);
        Log.e("SQL",salt);
        Log.e("SQL",hexkey);
        AESGCM aesdb = new AESGCM(hexkey);
        db = openOrCreateDatabase("iotdb.db", Context.MODE_PRIVATE, null);
        // Get the date today using Calendar object.
        Date today = Calendar.getInstance().getTime();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Cursor cursor = db.rawQuery("SELECT aes FROM commands_aes WHERE used_day=?", new String[]{df.format(today)});
        String cipher = "";
        if (cursor.moveToNext()) {
            cipher = cursor.getString(0);
        }

        Log.e("SQL","Cipher=" + cipher);
        byte[] bcipher = Base64.decode(cipher, Base64.DEFAULT);
        try {
            byte[] plaintext = aesdb.decrypt(bcipher,salt,df.format(today).getBytes());
            String str = new String(plaintext, StandardCharsets.UTF_8);
            CURRENT_AES_KEY_GCM = str;
            Log.e("SQL",str);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SQL",e.toString());
            return false;

        }
    }


    //TODO get massive data
    private void getMeasures(String year, String month, String day, String hour) {
        DatabaseReference measures = rootDatabase.child("measures").child(year).child(month).child(day).child(hour);
        measures.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                HashMap<String,Integer> x = new HashMap<>();
                Iterable<DataSnapshot> it = dataSnapshot.getChildren();
                String cipher = "";
                String time = "";
                Log.e("MASSIVE", time + "=" + cipher);
                for (DataSnapshot t : it) {
                    cipher = t.getValue(String.class);
                    time = t.getKey();

                    byte[] headerSaltAndCipherText = Base64.decode(cipher, Base64.DEFAULT);
                    try {
                        byte[] plaintext = mAESCBC.decrypt(headerSaltAndCipherText);
                        String str = new String(plaintext, StandardCharsets.UTF_8);
                        Log.e("MASSIVE", str);
                        JSONObject obj = new JSONObject(str);
                        Iterator<String> iter = obj.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            try {
                                Object value = obj.get(key);
                                switch (key) {
                                    case "temp":
                                       // IOT.setTemperature((int)value);
                                        break;
                                    case "hum":
                                       // IOT.setHumidity((int)value);
                                        x.put(time,(int)value);
                                        break;
                                    default:
                                        break;
                                }

                            } catch (JSONException e) {
                                // Something went wrong!
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                String time="";
                String MESCounter = "";
                String AAD = "";
                for (DataSnapshot t : it) {

                    String key = t.getKey();
                    if(key.equals("MESCounter"))
                    {
                        MESCounter = t.getValue(String.class);
                    }
                    else if(key.equals("DATA"))
                    {
                        cipher = t.getValue(String.class);

                    }
                    else if(key.equals("AAD"))
                    {
                        AAD = t.getValue(String.class);
                        try {
                            JSONObject obj = new JSONObject(AAD);
                            Iterator<String> iter = obj.keys();
                            while (iter.hasNext()) {
                                String jkey = iter.next();
                                try {
                                    Object value = obj.get(jkey);
                                    switch (jkey) {
                                        case "time":
                                            time = value.toString();
                                            break;
                                        default:
                                            break;
                                    }

                                } catch (JSONException e) {
                                    // Something went wrong!
                                }
                            }
                        } catch (Throwable t1) {
                            Log.e("DATABASE", "Could not parse malformed JSON: \"" + AAD + "\"");
                        }


                    }
                }

                byte[] plaintext = new byte[0];
                try {
                    byte[] bcipher = Base64.decode(cipher, Base64.DEFAULT);
                    plaintext = mAESGCM.decrypt(bcipher,MESCounter,AAD.getBytes());
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

                byte[] cipher = mAESGCM.encrypt(cmd, ask, aad);


                Log.e("AESGCM", "Cipher=" + Base64.encodeToString(cipher, Base64.DEFAULT));

                // byte[] plaintext = aes.decrypt(cipher,ask);
                // String message1 = new String(plaintext);
                // Log.e("AESGCM", message1);
                HashMap<String, Object> result = new HashMap<>();

                result.put("AAD", jo.toString());
                result.put("COMMAND", Base64.encodeToString(cipher, Base64.DEFAULT));
                result.put("CMDCounter", ask);

                rootDatabase.child("realtime").child("commands").setValue(result);
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
                                mToggleButton.setEnabled(true);
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