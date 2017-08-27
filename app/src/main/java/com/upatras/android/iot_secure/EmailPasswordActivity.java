/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.upatras.android.iot_secure;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.annotation.NonNull;

import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED;

public class EmailPasswordActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private String query = "";
    private String address = "";
    private BluetoothAdapter mBluetoothAdapter;

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField;
    private EditText mPasswordField;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    public String email="";
    private String password="";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothChatService mChatService = null;
    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    private SQLiteDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);
        mDetailTextView = (TextView) findViewById(R.id.detail);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.verify_email_button).setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        db = openOrCreateDatabase("iotdb.db", Context.MODE_PRIVATE, null);
        /*
        String sq = "SELECT aes,used_day FROM measures_aes";
        Cursor cursor = db.rawQuery(sq, null);

        if (cursor.moveToNext()) {
            Log.e("SQL",cursor.getString(1));
        }
        */


    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    //TODO PAIR DEVICE!!!!
    private void createAccount(String email, String password,View v) {

          if (!validateForm()) {
               return;
           }

       // this.email = "testere1@test.com";// email;
      //  this.password = "password123"; //password;

        this.email = email;
        this.password = password;
        // Register for broadcasts when discovery has finished
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        // take an instance of BluetoothAdapter - Bluetooth radio
        ///BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(EmailPasswordActivity.this, "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
            return;
        }else {

            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            // Initialize the BluetoothChatService to perform bluetooth connections
            mChatService = new BluetoothChatService(this, mHandler);

            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

            mStatusTextView.setText("IOT PAIRING!!!");

        }

    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                    //        setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                       //     mConversationArrayAdapter.clear();
                            mStatusTextView.setText("STATE_CONNECTED");
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                      //      setStatus(R.string.title_connecting);
                            mStatusTextView.setText("STATE_CONNECTING");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                        //    setStatus(R.string.title_not_connected);
                            mStatusTextView.setText("STATE_NONE");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                  //  mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.e("PAIR",readMessage);
                    if(readMessage.equals("pair"))
                    {
                        JSONObject jResult = new JSONObject();
                        try {
                            jResult.putOpt("email", EmailPasswordActivity.this.email);
                            jResult.putOpt("pass", EmailPasswordActivity.this.password);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        createDB();
                        //TODO sent email and password for account JSON format
                        sendMessagePair(jResult.toString());
                        showProgressDialog("Pairing process Please wait...");

                    }
                    else if(readMessage.equals("done"))
                    {
                        //TODO DONE
                        hideProgressDialog();
                        Toast.makeText(EmailPasswordActivity.this, "PAIRING COMPLETE SUCCESSFULLY",
                                Toast.LENGTH_LONG).show();



                    }
                    else if(readMessage.equals("fail"))
                    {
                        //TODO FAIL PAIRING
                        hideProgressDialog();

                    }
                    else if(readMessage.equals("exists"))
                    {
                        //TODO EXISTS ACCOUNT
                        Toast.makeText(EmailPasswordActivity.this, "EMAIL ALREADY EXISTS",
                                Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                    }
                    else
                    {
                        if(readMessage.equals("measures"))
                        {
                            EmailPasswordActivity.this.query = "INSERT INTO measures_aes(aes,used_day) VALUES(?,?)";

                        }
                        else if(readMessage.equals("commands"))
                        {
                            EmailPasswordActivity.this.query = "INSERT INTO commands_aes(aes,used_day) VALUES(?,?)";

                        }
                        try {
                            JSONObject obj = new JSONObject(readMessage);
                            //TODO CAPTURES KEYS FROM JSON FORMAT TO DB
                            db.execSQL(EmailPasswordActivity.this.query,new String []{(String)obj.get("key"),(String)obj.get("date")});
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }catch(SQLException mSQLException) {
                            // here you can catch all the exceptions
                            mSQLException.printStackTrace();
                        }
                    }


                  //  mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                //    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                //    if (null != activity) {
                //        Toast.makeText(activity, "Connected to "
                //                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                //    }
                    Log.e("PAIR","NAME = " + msg.getData().getString(Constants.DEVICE_NAME));
                    break;
                case Constants.MESSAGE_TOAST:
                  //  if (null != activity) {
                  //      Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                  //              Toast.LENGTH_SHORT).show();
                  //  }
                    Log.e("PAIR","TOAST = " +msg.getData().getString(Constants.TOAST));
                    hideProgressDialog();
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                    Log.e("PAIR","RESULT_OK");
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                    Toast.makeText(this, "Please wait for pair first",
                            Toast.LENGTH_SHORT).show();
                    Log.e("PAIR","REQUEST_CONNECT_DEVICE_INSECURE");
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                   // setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                   // getActivity().finish();
                }
            default:
                Log.e("PAIR","CODE:" + Integer.toString(requestCode));
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        //IF IS NOT PAIR PAIR!!
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        //execute pairing!!!
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            Log.e("PAIR",address);
            device.createBond();
        }
        else // If it's already paired remove bond and pair again
        {
            Log.e("PAIR","REMOVE");
            try {
                Method m = device.getClass()
                        .getMethod("removeBond", (Class[]) null);
                m.invoke(device, (Object[]) null);
            } catch (Exception e) {
                Log.e("PAIR", e.getMessage());
            }

        }
        Toast.makeText(this, "Executing Pairing request wait...",
                Toast.LENGTH_LONG).show();
        mStatusTextView.setText("PLease accept Pairing on IOT device");


    }


    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessagePair(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
                byte[] send = message.getBytes();
                mChatService.write(send);
        //    }
            //Toast.makeText(this, "SENDING", Toast.LENGTH_SHORT).show();
            // Get the message bytes and tell the BluetoothChatService to write

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
          //  mOutEditText.setText(mOutStringBuffer);
        }
    }


    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // Get the BluetoothDevice object from the Intent

                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                //if is not pair execute pairing!!!
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.e("PAIR","SUPER" + address);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            Log.e("PAIR", String.format("FUNC%d", mChatService.getState()));
                            if(mChatService.getState() != 3)
                            {
                                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                                mChatService.connect(device, true);
                                handler.postDelayed(this, 2000);
                            }

                        }
                    }, 1000); //1 sec delay

                }
                else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    //mChatService.connect(device, true);
                    //device.createBond();
                    Log.e("PAIR","BOND_BONDING");
                }
                else
                {
                    // Attempt to pair
                    device.createBond();
                    Log.e("PAIR", String.valueOf(device.getBondState()));
                    Toast.makeText(EmailPasswordActivity.this, "Pairing Failed",
                            Toast.LENGTH_SHORT).show();

                }

            }
        }
    };

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog(getString(R.string.loading));

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mStatusTextView.setText(R.string.auth_failed);
                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void sendEmailVerification() {
        // Disable button
        findViewById(R.id.verify_email_button).setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        findViewById(R.id.verify_email_button).setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(EmailPasswordActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private void createDB(){
        db.execSQL("DROP TABLE IF EXISTS measures_aes");
        db.execSQL("CREATE TABLE measures_aes (aes VARCHAR(256) PRIMARY KEY NOT NULL,used_day DATE NOT NULL )");
        db.execSQL("DROP TABLE IF EXISTS commands_aes");
        db.execSQL("CREATE TABLE commands_aes (aes VARCHAR(256) PRIMARY KEY NOT NULL,used_day DATE NOT NULL )");

    }
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    //TODO validate for strong password!!!!!
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        }
        else  if (!isEmailValid(email)) {
            mEmailField.setError("Not a valid email address.");
            valid = false;
        }
        else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        }
        else if (TextUtils.getTrimmedLength(password) < 6){
            mPasswordField.setError("Password length must be greater than 6.");
            valid = false;
        }
        else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.signed_in_buttons).setVisibility(View.VISIBLE);

            findViewById(R.id.verify_email_button).setEnabled(!user.isEmailVerified());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please enter the 4 digit password:");
            builder.setCancelable(false);
// Set up the input
            final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);
            builder.setCancelable(false);
// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String rnd = input.getText().toString();
                    Intent main = new Intent(EmailPasswordActivity.this,MainActivity.class);
                    main.putExtra("digit4", rnd);
                    EmailPasswordActivity.this.finish(); //nohistory = true no need to call
                    startActivity(main);
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

        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.signed_in_buttons).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString(),v);

        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.sign_out_button) {
            signOut();
        } else if (i == R.id.verify_email_button) {
           // sendEmailVerification();
            //sendMessagePair("SKATASFDS");
        }
    }

}
