/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.common.logger.Log;
import java.util.Arrays;

//This fragment controls Bluetooth to communicate with other devices.
public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    //UI Elements (Buttons and Textviews)
    private Button mLeftButton;
    private Button mRightButton;
    private Button mFwdButton;
    private Button mBackButton;
    private Switch mLEDSwitch;
    private Switch mCameraSwitch;
    private TextView mTemperature;
    private TextView mHumidity;
    private TextView mBrightness;
    private TextView mBatteryVoltage;
    private TextView mCOReading;

    //Sensor Variables
    private int iTemp;
    private String sTemp;
    private int iHumidity;
    private String sHumidity;
    private int iBrightness;
    private String sBrightness;

    //Various Variables
    private int control_code;  //Control code to be sent to HC-06
    private String mConnectedDeviceName = null; //Name of the connected device
    private StringBuffer mOutStringBuffer; //String buffer for outgoing messages
    private BluetoothAdapter mBluetoothAdapter = null; //Local Bluetooth adapter
    private BluetoothChatService mChatService = null; //Member object for the chat services
    private boolean bt_manually_enabled = false; // Used for autodisabling BT on exit
    private boolean mCamControl; //Stores Position of Camera Control Switch
    private boolean mLED; //Stores Position of LED Switch
    public String incoming_bit;
    public String recieved_data;
    public String shortbit;
    public String longbit;
    public String recieved_bit;
    public String [] split = new String[4];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is supported by this device.", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }




    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if (mBluetoothAdapter.isEnabled())
            bt_manually_enabled = false;
        else if(!mBluetoothAdapter.isEnabled())
            bt_manually_enabled = true;

        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onDestroy() {
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mLeftButton = (Button) view.findViewById(R.id.leftbtn);
        mRightButton = (Button) view.findViewById(R.id.rightbtn);
        mFwdButton = (Button) view.findViewById(R.id.fwdbtn);
        mBackButton = (Button) view.findViewById(R.id.backbtn);
        mCameraSwitch = (Switch) view.findViewById(R.id.camSwitch);
        mLEDSwitch = (Switch) view.findViewById(R.id.LEDSwitch);
        mTemperature = (TextView) view.findViewById(R.id.mTemp);
        mHumidity = (TextView) view.findViewById(R.id.mHumidity);
        mBrightness = (TextView) view.findViewById(R.id.mBrightness);


        String videoURL = "http://192.168.1.1:8080";
        WebView video = (WebView)view.findViewById(R.id.video_webview);
        video.loadUrl(videoURL);

        control_code = 0;

        mCameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
             mCamControl= true;
          }
         else if (!isChecked){
              mCamControl = false;
            }
        }
    });

        mLEDSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mLED = true;
                    sendMessage(192);
                }
                else if (!isChecked){
                    mLED = false;
                    sendMessage(192);
                }
            }
        });

        mFwdButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !mCamControl){
                    control_code += 128;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP && !mCamControl){
                    control_code -= 128;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_DOWN && mCamControl){
                    control_code = 2;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP && mCamControl){
                    control_code = 0;
                    sendMessage(control_code);
                }
                return false;
            }
        });

        mBackButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !mCamControl){
                    control_code += 64;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP && !mCamControl){
                    control_code -= 64;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_DOWN && mCamControl){
                    control_code = 1;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP && mCamControl){
                    control_code = 0;
                    sendMessage(control_code);
                }
                return false;
            }
        });

        mRightButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !mCamControl){
                    control_code += 32;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP && !mCamControl){
                    control_code -= 32;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_DOWN && mCamControl){
                    control_code = 8;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP && mCamControl){
                    control_code = 0;
                    sendMessage(control_code);
                }
                return false;
            }
        });

        mLeftButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !mCamControl) {
                    control_code += 16;
                    sendMessage(control_code);
                } else if (event.getAction() == MotionEvent.ACTION_UP && !mCamControl) {
                    control_code -= 16;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_DOWN && mCamControl) {
                    control_code = 4;
                    sendMessage(control_code);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP && mCamControl){
                    control_code = 0;
                    sendMessage(control_code);
                }
                return false;
            }
        });
    }


    //Set up the UI, buttons and background operations for chat.
    private void setupChat() {
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }
    public void sendMessage(int message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

            mChatService.write(message);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);

    }

    //Updates the bluetooth status in the top bar
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }
    //Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;

                case Constants.MESSAGE_READ:
                    //Assign recieved data to strings for display

                        byte[] readBuf = (byte[]) msg.obj;

                    String incoming_bit = new String(readBuf, 0, msg.arg1);

                        Log.d(TAG,"incoming:"+incoming_bit);

                    if (incoming_bit.length() < 3){
                        shortbit = incoming_bit;
                        //Log.d(TAG,"short: "+shortbit);
                    }

                    else if (incoming_bit.length() > 7){
                        longbit = incoming_bit;
                        //Log.d(TAG,"long: "+longbit);
                    }

                    recieved_data = (shortbit + longbit);
                    Log.d(TAG,"concated data: " + recieved_data);

                    split = recieved_data.split("[/]" , 3);

                    try {
                        sTemp = split[0];
                        sHumidity = split[1];
                        sBrightness = split[2];
                    } catch (ArrayIndexOutOfBoundsException e){
                        Arrays.fill(split, "0");
                    }


                    //Set variables as textview
                    mTemperature.setText("Temperature: " + sTemp + "°C  ");
                    mHumidity.setText("  Humidity: " + sHumidity + "%"  );
                    mBrightness.setText("  LDR Reading: " + sBrightness);

                    break;

                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;

            }

        }
    };
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true); //deleting this prevents crash
                }
                break;

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }
    //Establishes connection with HC-06 Bluetooth Transciever
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address); // Get the BluetoothDevice object
        mChatService.connect(device, secure);  // Attempt to connect to the device
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }

            case R.id.quit: {
                if (bt_manually_enabled){
                    mBluetoothAdapter.disable();
                }
                System.exit(0);
                return true;
            }

            case R.id.about:{
                Toast about = Toast.makeText(getActivity(),R.string.aboutContent,Toast.LENGTH_LONG);
                TextView v = (TextView) about.getView().findViewById(android.R.id.message);
                if( v != null) v.setGravity(Gravity.CENTER);
                about.show();
            }
        }
        return false;
    }
}
