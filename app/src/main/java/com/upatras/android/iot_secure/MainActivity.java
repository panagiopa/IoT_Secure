package com.upatras.android.iot_secure;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;

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

import com.androidquery.AQuery;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView mStatusTextView;
    private TextView mDetailTextView;

    private TextView mEmailTextView;
    private Button testButton;

    private AQuery aq;
    private AjaxReq ajaxreq;
    private AESGCM aes;
    private FirebaseAuth mAuth;
    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
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

            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
                    currentUser.getEmail(), currentUser.isEmailVerified()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, currentUser.getUid()));

            if(mEmailTextView!=null)
                mEmailTextView.setText(currentUser.getEmail());
Log.d("Login", currentUser.getEmail());


            testButton = (Button) findViewById(R.id.buttonTest);
            testButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Code here executes on main thread after user presses button
                    ExecuteCmd();
                }
            });

            //Instantiate AQuery Object
            aq = new AQuery(this);
            ajaxreq = new AjaxReq(aq);
    /*
            try {
                test();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
            //  start();

            aes = new AESGCM("BD6BE71BF6C229E4684129527334CE6F".getBytes());

            aes_test();

            // [START initialize_database_ref]
            mDatabase = FirebaseDatabase.getInstance().getReference();
            // [END initialize_database_ref]


            writeNewUser(currentUser.getUid(), "test",currentUser.getEmail());
        }else
        {
            signOut();
        }

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
            Toast.makeText(this, "Logging out",Toast.LENGTH_SHORT).show();
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

    //TODO may remove aquery completly or use only in pairing
    public void test() throws IOException {
        String url = "http://192.168.10.4/test.php?id=1";
        ajaxreq.AjaxGetRequest(url);

        Log.d("TEST", "arr: " + Arrays.toString(ajaxreq.getRetObj()));

        ajaxreq.test();
//TODO TEST login credentials
        URL url1 = new URL("http://10.0.1.75");
        HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Log.d("SITE",in.toString());
        } finally {
            urlConnection.disconnect();
        }

    }

    private boolean started = false;
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d("TEST", "arr: " + Arrays.toString(ajaxreq.getRetObj()));
            if(started) {
                start();
            }
        }
    };

    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void start() {
        started = true;
        handler.postDelayed(runnable, 5000);
    }




    public void aes_test()
    {
        String s = "hello world 1234435675478658657356346543";
        byte[] b = s.getBytes();
        Log.d("AES",Arrays.toString(b));
        try {
            byte[] cipher = aes.encrypt(b);
            Log.d("AES",Arrays.toString(cipher));

            byte[] plaintext = aes.decrypt(cipher);
            Log.d("AES",Arrays.toString(plaintext));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void writeNewUser(String userId, String name, String email) {
        //TODO save user IVs for data exchange

        mDatabase.child("users").child(userId).child("email").setValue(email);
        mDatabase.child("users").child(userId).child("username").setValue(name);
    }

    //TODO Execute one and only Command
    private void ExecuteCmd()
    {
        Toast.makeText(this, "Executing Command",Toast.LENGTH_SHORT).show();
        //TODO ask for cloud Command Counter IV


        //TODO send encrypted data to cloud


        //TODO read IoT response encrypt data


        //TODO decrypt and read actual data


    }

}