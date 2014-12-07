package test.ucla.simpleclient_server;


import android.app.Activity;
import android.location.Criteria;
import android.os.AsyncTask;


import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.*;


public class HuntTheTrojan extends Activity {
    private static final String TAG = HuntTheTrojan.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private ListView lv;
    int REQUEST_ENABLE_BT;
    private ArrayAdapter mArrayAdapter;
    private String NAME = "HungryChicken";
    private UUID MY_UUID = UUID.fromString("6655653c6-8767-44a4-a928-9a950c17cb23");
    private Integer pid;                    //integer for client to identify in the server

    boolean superPing_cd;
    boolean minutePing_cd;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private Location myLoc;

    private ConnectThread CT;
    private AcceptThread AT;
    private backThread bT;


    public void exit(View view){
        if (pid == 0) {
            Toast.makeText(getApplicationContext(), "Quitting game...",
                    Toast.LENGTH_SHORT).show();
            gameEnd(0);
        }
        else {
            Toast.makeText(getApplicationContext(), "Quitting game...",
                    Toast.LENGTH_SHORT).show();
            gameEnd(1);
        }


        Intent intent = new Intent(this, GameStart.class);
        startActivity(intent);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_text_client_actvity);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);
        REQUEST_ENABLE_BT = 1;
        //bT = new backThread();
        //bT.run();
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        minutePing_cd = true;
        superPing_cd = true;
        Intent intent = getIntent();
        Integer fromPast = intent.getIntExtra(GameStart.PID, -1);
        pid = fromPast;
        Log.d(TAG, "123412314" + pid.toString());
        Parse.initialize(this, "xJxmXrtjWXGGl3jmHxLmhD5uyG6rv6jSgR9xUwO3", "W0ePKRBPHRHSNVwUTsB2MsW7aJJzhTyGEZ1B4F1c");
        if (pid != 0){
            Button superb = (Button)findViewById(R.id.button3);
            superb.setClickable(false);
            superb.setVisibility(View.INVISIBLE);

        }
        else {
            mBluetoothAdapter.startDiscovery();
        }
        if (googleMap == null){
            googleMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            if (googleMap != null) {
                setUpMap();
            }
        }

    }
    public class backThread extends Thread{
        backThread(){

        }
        public void run(){
            //checked through all the paired devices
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Add the name and address to an array adapter to show in a ListView
                    if (pid == 0){
                        AT = new AcceptThread();
                        AT.run();
                    }
                    else{
                        CT = new ConnectThread(device);
                        CT.run();
                    }
                }
            }
        }
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (pid != 0){
                    //we are a mouse so we connected to the cat
                    //we create a CT to send to that device.
                    CT = new ConnectThread(device);
                    CT.run();
                }
                else {
                    //we are a cat so we
                    AT = new AcceptThread();
                    AT.run();
                }
            }
        }
    };
    private void setUpMap() {
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);

        Location location = locationManager.getLastKnownLocation(provider);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //googleMap.clear();
                //drawMarker(location);
                myLoc = location;
                pushLocation();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(location != null){
            //PLACE THE INITIAL MARKER
            myLoc = location;
            drawMarker(location);
        }
        locationManager.requestLocationUpdates(provider, 10000, 20, locationListener);
    }
    private void drawMarker(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(20));
    }
    private void pushLocation() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
        query.whereMatches("pid", (pid++).toString());
        try {
            query.getInBackground(query.getFirst().getObjectId(),new GetCallback<ParseObject>() {
                public void done(ParseObject player, ParseException e) {
                    if (e == null && myLoc != null) {
                        double latitude = myLoc.getLatitude();
                        double longitude = myLoc.getLongitude();
                        ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);
                        player.put("location", point);
                        player.saveInBackground();
                    }
                }
            });
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void MouseConnect(OutputStream outStream){
        //once we have connected we should create a  kill our game
        OutputStream mmOutStream = outStream;

        try {
            mmOutStream.write(this.pid);

        }catch (IOException e){}
        //if our pid is not -1 then it isn't initialized


    }
    private class MouseBackground extends AsyncTask<Void, Void, Void>{
        private BluetoothSocket mmServerSocket;

        MouseBackground(BluetoothSocket passed_Socket) {
            mmServerSocket = passed_Socket;
        }
        @Override
        protected  Void doInBackground(Void... params){
            BluetoothSocket socket = null;
            try{
                //blocking call that waits for client
                mmServerSocket.connect();
            }catch (IOException e) {

            }

            return null;
        }
    }
    //uncomment for client aka mouse device
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
            InputStream tempstream = null;
           OutputStream tempout = null;


            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
            try {
                   tempstream = mmSocket.getInputStream();
                   tempout = mmSocket.getOutputStream();
            }
            catch(IOException e) {}

            mmInStream = tempstream;
            mmOutStream = tempout;
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
            MouseConnect(mmOutStream);
            cancel();
            mBluetoothAdapter.startDiscovery();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    public void CatConnect(BluetoothSocket socket){
       BluetoothSocket mSocket2 = socket;
        //get the input stream
        InputStream mmStream = null;
        try {
            if (mSocket2 != null)
                mmStream = mSocket2.getInputStream();
        }catch (IOException e){}

        Integer temp = -1;

        //read from the mouse's input
        try {
            temp = mmStream.read();
        }catch (IOException e){}

        if (temp != -1) {
            //if we get a valid value from mice
            ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
            query.whereMatches("pid", temp.toString());

            //request it from the DB
            try {
                query.getInBackground(query.getFirst().getObjectId(),new GetCallback<ParseObject>() {
                    public void done(ParseObject player, ParseException e) {
                        if (e == null) {
                            //the player is taken and the number is received again, so we kill it
                            if (player.getBoolean("isTaken")== true) {
                                if(player.getBoolean("isAlive")== true) {
                                    player.put("isAlive", false);
                                    player.saveInBackground();
                                }
                            }



                        }
                    }
                });
            } catch (ParseException e) {

                e.printStackTrace();
            }
        }
          }


    //Server
    //uncomment for server aka cat device
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private final InputStream mmInstream;


        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;

            BluetoothSocket socket;

            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    mmInstream = null;
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    InputStream tempin = null;
                    // Do work to manage the connection (in a separate thread)
                    try{
                        tempin = socket.getInputStream();

                    }catch (IOException e){}
                        mmInstream = tempin;

                    break;
                }
                else {
                    mmInstream = null;
                    break;
                }
            }

        }

        public void run() {
            Integer pid = -1;

            try {
                pid = mmInstream.read();
            }
            catch(IOException e)
            {

            }
            if (pid != -1){
                //if we have a valid number from the in stream, kill it if its alive
                ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
                query.whereMatches("pid", String.valueOf(pid));
                try{
                    ParseObject pobj = query.get(query.getFirst().getObjectId());
                    if (pobj.getBoolean("isAlive") == true){
                        pobj.put("isAlive", false);
                        pobj.saveInBackground();
                    }
                    //else this object is already dead, we do nothing
                }catch (ParseException e ){
                    e.printStackTrace();
                }
            }

        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }


    //what the server does in the background, bluetooth connections
    private class ServerBackground extends AsyncTask<Void, Void, Void>{
        private BluetoothServerSocket mmServerSocket;

        ServerBackground(BluetoothServerSocket passed_Ssocket) {
            mmServerSocket = passed_Ssocket;
        }
        @Override
        protected  Void doInBackground(Void... params){
            BluetoothSocket socket = null;
            try{
                //blocking call that waits for client
                socket = mmServerSocket.accept();
            }catch (IOException e) {

            }
            if (socket != null)
            {
                CatConnect(socket);
            }
            return null;
        }
    }
    //what the server does in the background, bluetooth connections





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

    public void gameEnd( int end) {
        //if cat has won and not timed out, all mice lose
        if (end == 1) {
            if (pid == 0) {     //cat
                Toast.makeText(getApplicationContext(), "Game is over! Congratulations!",
                        Toast.LENGTH_SHORT).show();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
                query.whereMatches("pid", String.valueOf(0));
                try{
                    ParseObject pobj = query.get(query.getFirst().getObjectId());
                    pobj.put("isTaken", false);
                    pobj.put("isAlive", false);
                    pobj.saveInBackground();
                }catch (ParseException e ){
                    e.printStackTrace();
                }

                //now we have to clean up the DB
            } else {            //mouse
                Toast.makeText(getApplicationContext(), "Game over!  Thank you for playing!",
                        Toast.LENGTH_SHORT).show();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
                query.whereMatches("pid", pid.toString());

                //request it from the DB
                try {
                    ParseObject pobj = query.get(query.getFirst().getObjectId());
                    pobj.put("isTaken", false);
                    pobj.put("isAlive", false);
                    pobj.saveInBackground();


                } catch (ParseException e) {

                    e.printStackTrace();
                }
            }
        }

        //game has timed out
        else {
            Log.d(TAG,"inside game(1)");
            if (pid == 0){      //cat
                Toast.makeText(getApplicationContext(), "You lost! Thank you for playing!",
                        Toast.LENGTH_SHORT).show();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
                query.whereMatches("pid", String.valueOf(0));
                try{
                    ParseObject pobj = query.get(query.getFirst().getObjectId());
                    pobj.put("isTaken", false);
                    pobj.put("isAlive", false);
                    pobj.saveInBackground();



                //now we have to clean up the data base

                }catch (ParseException e ){
                    e.printStackTrace();
                }
            }
            else{               //mouse
                ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
                query.whereMatches("pid", pid.toString());

                //request it from the DB
                try {
                    ParseObject pobj = query.get(query.getFirst().getObjectId());
                    pobj.put("isTaken", false);
                    pobj.put("isAlive", false);
                    pobj.saveInBackground();
                    /*query.get(query.getFirst().getObjectId());,new GetCallback<ParseObject>() {
                        public void done(ParseObject player, ParseException e) {
                            if (e == null) {
                                                                        //reset data
                                    player.put("isTaken", false);
                                    player.put("isAlive", false);
                                    player.saveInBackground();

                            }
                        }
                    });*/
                } catch (ParseException e) {

                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Game is over! Congratulations!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void minutePing(View view) {
        if (minutePing_cd == true) {
            new CountDownTimer(30000, 1000) {
                Button button = (Button) findViewById(R.id.button2);

                public void onTick(long millisUntilFinished) {
                        minutePing_cd = false;
                        button.setText(" " + millisUntilFinished / 1000 + " ");
                }

                public void onFinish() {

                    button.setText("Minute ping");
                    minutePing_cd = true;
                }
            }.start();
        }
    }
    public void SuperPing (View view){
        if (pid != 0) {//do  nothing for mouse
            Toast.makeText(getApplicationContext(), "Super ping is only available to cats!",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {

            if (superPing_cd == true) {
                new CountDownTimer(10000, 1000) {
                    Button button = (Button) findViewById(R.id.button3);

                    public void onTick(long millisUntilFinished) {
                        superPing_cd = false;
                        button.setText(" " + millisUntilFinished / 1000 + " ");
                    }

                    public void onFinish() {

                        button.setText("Super ping");
                        superPing_cd = true;
                    }
                }.start();
            }
        }
    }
}
