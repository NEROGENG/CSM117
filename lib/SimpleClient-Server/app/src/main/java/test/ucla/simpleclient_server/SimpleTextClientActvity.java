package test.ucla.simpleclient_server;


import android.app.Activity;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.*;
import android.util.Log;
import android.location.Location;
import android.text.style.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import com.parse.*;


public class SimpleTextClientActvity extends Activity {
    private static final String TAG = SimpleTextClientActvity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private ListView lv;
    int REQUEST_ENABLE_BT;
    private ArrayAdapter mArrayAdapter;
    private String NAME = "HungryChicken";
    private UUID MY_UUID = UUID.fromString("6655653c6-8767-44a4-a928-9a950c17cb23");

    public void exit(View view){
        System.exit(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_text_client_actvity);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);
        REQUEST_ENABLE_BT = 1;
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        lv = (ListView) findViewById(R.id.listView);
        Parse.initialize(this, "xJxmXrtjWXGGl3jmHxLmhD5uyG6rv6jSgR9xUwO3", "W0ePKRBPHRHSNVwUTsB2MsW7aJJzhTyGEZ1B4F1c");
    }
    /* uncomment for client aka mouse device
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }


        public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        // Will cancel an in-progress connection, and close the socket
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    */

    //uncomment for server aka cat device
    private class AcceptThread extends RecursiveTask<>{
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
                //find name and UUID
            }catch (IOException e) { }
            mmServerSocket = tmp;
        }
        public void run(){
            //once we have created the socket, we need to connect
            BluetoothSocket socket = null;
            while (true){ //constantly check for clients
                try{
                    //blocking call that waits for client
                    socket = mmServerSocket.accept();
                }catch (IOException e) {
                    break;
                }

                if(socket != null) {
                    //do something with the connection
                    //TODO: tell the server that there is something different with the state of the game
                    //if there is 0 "left" then there should be a push notification to all other

                    try {
                        mmServerSocket.close();
                    }catch (IOException e){}
                    break;
                }

            }
        }
        public void cancel(){
            try {
                mmServerSocket.close();
            }catch(IOException e){}

        }


    }

    //discovering devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mArrayAdapter.add(device.getName() + '\n' + device.getAddress());
            }

        }
    };

    public void changeText(View view){
        Button changeButton;
        changeButton = (Button) findViewById(R.id.button4); // reference to the send button

        // Button press event listener
        changeButton.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
            // Perform action on click
            Log.d(TAG, "BUTTON REGISTERED!");
            TextView temp = (TextView) findViewById(R.id.textView);
            if ((temp.getText().toString()).equals("You are a cat") ) {
                temp.setText("You are a mouse");
            }
            else if(temp.getText().toString().equals("You are a mouse")){
                temp.setText("You are a cat");
            }
            else {
                Log.d(TAG, "YOUFUCKEDUP");
                temp.setText("YOUFUCKEDUP");
            }
        }
    });
}

    public void button_Connect (View view)
    {
         //turn on bluetooth if not active already
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //show already connected devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() >0){
            for (BluetoothDevice device: pairedDevices){
                mArrayAdapter.add(device.getName() + '\n' + device.getAddress());
            }
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);


    }

    public void listD (View view){
        Set<BluetoothDevice> pairedDevices;
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "There are no devices nearby", Toast.LENGTH_SHORT).show();
        }
        else {
            ArrayList list = new ArrayList();
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName());
            }
            Toast.makeText(getApplicationContext(), "Showing Paired Devices",
                    Toast.LENGTH_SHORT).show();
            final ArrayAdapter adapter = new ArrayAdapter
                    (this, android.R.layout.simple_list_item_1, list);
            lv.setAdapter(adapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_simple_text_client_actvity, menu);
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
}
