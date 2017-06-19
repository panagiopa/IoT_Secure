package com.example.android.iot_secure;

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
import com.androidquery.AQuery;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

//TODO Login screen
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private AQuery aq;
    private AjaxReq ajaxreq;
    private AESGCM aes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //Instantiate AQuery Object
        aq = new AQuery(this);
        ajaxreq = new AjaxReq(aq);

        try {
            test();
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();


        aes = new AESGCM("BD6BE71BF6C229E4684129527334CE6F".getBytes());

        aes_test();

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

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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

}
