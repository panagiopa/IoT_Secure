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


import java.security.Key;

import java.util.Arrays;

import javax.crypto.Cipher;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private AQuery aq;
    private AjaxReq ajaxreq;
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

        test();
        start();

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

    public void test()
    {
        String url = "http://192.168.10.4/test.php?id=1";
        ajaxreq.AjaxRequest(url);

        Log.d("TEST", "arr: " + Arrays.toString(ajaxreq.getRetObj()));

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

    byte[] b = "CC37B4C4EBFBAF9788D34DA1AF2A0984".getBytes();

    Key key = new SecretKeySpec(b ,"AES");
    // the output is sent to users
    byte[] encrypt(byte[] src) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV(); // See question #1
        assert iv.length == 12; // See question #2
        byte[] cipherText = cipher.doFinal(src);
        assert cipherText.length == src.length + 16; // See question #3
        byte[] message = new byte[12 + src.length + 16]; // See question #4
        System.arraycopy(iv, 0, message, 0, 12);
        System.arraycopy(cipherText, 0, message, 12, cipherText.length);
        return message;
    }

    byte[] decrypt(byte[] message) throws Exception {
        if (message.length < 12 + 16) throw new IllegalArgumentException();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec params = new GCMParameterSpec(128, message, 0, 12);
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        return cipher.doFinal(message, 12, message.length - 12);
    }


    public void aes_test()
    {
        String s = "hello world 1234435675478658657356346543";
        byte[] b = s.getBytes();
        Log.d("AES",Arrays.toString(b));
        try {
            byte[] cipher = encrypt(b);
            Log.d("AES",Arrays.toString(cipher));

            byte[] plaintext = decrypt(cipher);
            Log.d("AES",Arrays.toString(plaintext));
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
