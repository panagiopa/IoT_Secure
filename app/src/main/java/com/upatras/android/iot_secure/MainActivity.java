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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    private String CMDCounter = "";

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
            aes = new AESGCM("BD6BE71BF6C229E4684129527334CE6F".getBytes());

            //TODO get key from database sqlite per day differs
            mAESCBC = new AESCBC("02c7bd67410dd84e19b81616700a2401".getBytes());

            aes_test();

            // [START initialize_database_ref]
            rootDatabase = FirebaseDatabase.getInstance().getReference();
            //get reference
            update_measures();
            // [END initialize_database_ref]

            getMeasures("2017", "8", "2", "01");

            //maintain Commands COunter
            MaintainCMDCounterCloud();

            //TODO create a protected database
            String query = "select sqlite_version() AS sqlite_version";
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(":memory:", null);
            Cursor cursor = db.rawQuery(query, null);
            String sqliteVersion = "";
            if (cursor.moveToNext()) {
                sqliteVersion = cursor.getString(0);
            }
            Log.d("SQLite", sqliteVersion);
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
        DatabaseReference measures = rootDatabase.child("realtime");
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

                Log.d("DATABASE", time + "=" + cipher);

                byte[] plaintext = new byte[0];
                try {
                    byte[] headerSaltAndCipherText = Base64.decode(cipher, Base64.DEFAULT);
                    plaintext = mAESCBC.decrypt(headerSaltAndCipherText);
                    String str = new String(plaintext, StandardCharsets.UTF_8);
                    String[] separated = str.split(",");
                    mRealDataTextView.setText("Temperature=" + separated[0] + "\nHumidity" + separated[1]);
                    Log.d("DATABASE", str);
                } catch (Exception e) {
                    Log.d("DATABASE", "FAIL");
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
        String s = "hello world 1234435675478658657356346543";
        byte[] b = s.getBytes();
        Log.d("AES", Arrays.toString(b));
        try {
            byte[] cipher = aes.encrypt(b);
            Log.d("AES", Arrays.toString(cipher));

            byte[] plaintext = aes.decrypt(cipher);
            Log.d("AES", Arrays.toString(plaintext));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //TODO Execute one and only Command
    private void ExecuteCmd() {
        Toast.makeText(this, "Executing Command", Toast.LENGTH_SHORT).show();
        //TODO ask for cloud Command Counter IV
        //COMMAND IS MAINTAIN NO NEED FOR ASK
        Log.d("COMMAND", CMDCounter);
        //TODO send encrypted data to cloud


        //TODO read IoT response encrypt data


        //TODO decrypt and read actual data


    }

    //TODO ask cloud single DATA
    private void MaintainCMDCounterCloud() {

        DatabaseReference MaintainCMDCounters = rootDatabase.child("users").child(currentUser.getUid()).child("CMDToggle");
        MaintainCMDCounters.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer test = dataSnapshot.getValue(Integer.class);
                Log.d("COMMAND", test.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}