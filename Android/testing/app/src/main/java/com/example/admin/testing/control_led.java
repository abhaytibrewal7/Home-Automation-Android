package com.example.admin.testing;




//    import java.io.BufferedReader;
import java.io.IOException;
//    import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;



import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import  android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.Runnable;



public class control_led extends Activity {
    private static final String TAG = "control_led";

    Button btnOn, btnOff, chk;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inputStream = null;
    private BluetoothDevice device;
    boolean stopThread;
    byte buffer[];
    volatile boolean stopWorker;
    int readBufferPosition;
    byte[] readBuffer;
    Thread workerThread;
    private static control_led parent;
    int bytesAvailable;


    Handler h;
    final int RECIEVE_MESSAGE = 1;		// Status  for Handler
    private StringBuilder sb = new StringBuilder();

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "98:D3:32:10:4E:E4";

    /**
     * Called when the activity is first created.
     */

    String a = null;
    private TextView ab = null;

    String status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_led);
        device = getIntent().getExtras().getParcelable("btDevice");
        status = getIntent().getExtras().getString("Status");
        Log.d(TAG,status);
        //a=getIntent().getStringExtra(MainActivity.ID_EXTRA);
        ab = (TextView) findViewById(R.id.editText);
        ab.setText("item is:" + device.getName());


        btnOn = (Button) findViewById(R.id.b1);
        btnOff = (Button) findViewById(R.id.b2);
        chk = (Button) findViewById(R.id.b3);


        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();



        btnOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for(int i=0;i<5;i++)
                    sendData("1");
                Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
                //beginListenForData();
                ab.setText("1");
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for(int i=0;i<5;i++)
                    sendData("0");
                Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
                //beginListenForData();
                ab.setText("0");
            }
        });
        chk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendData("t");
                Toast.makeText(getBaseContext(), "checking temp and humidity", Toast.LENGTH_SHORT).show();
                beginListenForData();
                //ab.setText("t");
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e1) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        try {
            outStream = btSocket.getOutputStream();
            inputStream = btSocket.getInputStream();

        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }

        if(status.equalsIgnoreCase("ON")){
            for(int i=0;i<5;i++)
                sendData("1");
            Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            //beginListenForData();
            ab.setText("1");
        }

        if(status.equalsIgnoreCase("OFF")){
            for(int i=0;i<5;i++)
                sendData("0");
            Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            //beginListenForData();
            ab.setText("0");
        }

        if(status.equalsIgnoreCase("DATA")){
            ab.setText("");
            for(int i=0;i<5;i++) {
                sendData("t");
                beginListenForData();
            }
            //Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            //beginListenForData();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Send data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
            msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        //Log.d(TAG,"while.............");
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            Log.d(TAG,"Bytecount........"+byteCount);
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");

                            handler.post(new Runnable() {
                                public void run() {
                                    Log.d(TAG,"string....."+string);
                                    ab.append(string);
                                }
                            });

                        }
                    } catch (IOException ex) {
                        stopThread = true;
                    }
                }
            }
        });
        thread.start();
    }
}
