package test.ucla.simpleclient_server;

import android.app.Activity;
import android.location.Criteria;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
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
import android.widget.*;
import android.util.Log;
import android.location.Location;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import java.util.List;
import java.util.Set;
import java.util.UUID;


import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.*;


public class HuntTheTrojan extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String TAG = HuntTheTrojan.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private ListView lv;
    int REQUEST_ENABLE_BT;

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
    private BluetoothRunner BR;
    private ClientRunner CR;
    private backThread BT;
    private BackRunner BRunn;

    private class backThread extends Thread{
        private Set<BluetoothDevice> pairedDevices;
        backThread(){
             pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    Log.d("backThread", device.getName());
                    // Add the name and address to an array adapter to show in a ListView
                   ClientRunner cr = new ClientRunner();
                   cr.execute(device);
                }
            }
        }
        public void run(){

        }
    }

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
    private class BluetoothRunner extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if(mBluetoothAdapter.isEnabled())
            AT = new AcceptThread();
            Log.d("BluetoothRunner", "thread created");
            while(true) {
                AT.run();
                if (!AT.isAlive())
                    break;
            }


            return null;

        }

    }
    private class BackRunner extends AsyncTask<Void , Void, Void>{
        @Override
        protected Void doInBackground (Void... params){

            BT = new backThread();
            Log.d("BackRunner", "Backthreadcreated");
            while(true){
                BT.run();
                if (!BT.isAlive()) {
                    break;
                }
            }

            return null;
        }
    }

    //async task for mouse
    private class ClientRunner extends AsyncTask<BluetoothDevice , Void, Void>{
        @Override
        protected Void doInBackground (BluetoothDevice... params){
            CT = new ConnectThread(params[0]);
            Log.d("clientRunner", "created a thread with device" +params[0].getName());
            while(true) {
                CT.run();
                if (!CT.isAlive())
                    break;
            }
            Log.d("ClientRunner", "threadcreated");
            return null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_text_client_actvity);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);
        REQUEST_ENABLE_BT = 1;
        BT = new backThread();
        //bT.run();

        //make ourselves discoverable
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
        startActivity(discoverableIntent);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        minutePing_cd = true;
        superPing_cd = true;
        Intent intent = getIntent();

        pid =  intent.getIntExtra(GameStart.PID, -1);

        Parse.initialize(this, "xJxmXrtjWXGGl3jmHxLmhD5uyG6rv6jSgR9xUwO3", "W0ePKRBPHRHSNVwUTsB2MsW7aJJzhTyGEZ1B4F1c");
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (pid != 0){                                              //mouse
            mBluetoothAdapter.startDiscovery();
            Button superb = (Button)findViewById(R.id.button3);
            superb.setClickable(false);
            superb.setVisibility(View.INVISIBLE);
            BRunn = new BackRunner();
            BRunn.execute();


        }
        else {
            mBluetoothAdapter.startDiscovery();
            //AT = new AcceptThread();                        //creates for cat
            Toast.makeText(this, "Parent: Server thread created for cat",
                    Toast.LENGTH_SHORT).show();
            BR = new BluetoothRunner();
            Log.d("BluetoothRunner", "started");
            BR.execute();

        }
        if (googleMap == null){
            googleMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            if (googleMap != null) {
                setUpMap();
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
                    //we create a CR to send to that device.
                   CR = new ClientRunner();
                  CR.execute(device);
                }

            }
        }
    };
    private void setUpMap() {
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //googleMap.clear();
                //drawMarker(location);
                myLoc = location;
                drawMarker(myLoc);
                pushLocation();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        Criteria criteria = new Criteria();
        String provider= locationManager.getBestProvider(criteria, true);

        myLoc = locationManager.getLastKnownLocation(provider);

        if (myLoc != null) {
            drawMarker(myLoc);
            double latitude = myLoc.getLatitude();
            double longitude = myLoc.getLongitude();

            LatLng latLng = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
        else
            Toast.makeText(this, "No location found.",
                    Toast.LENGTH_SHORT).show();
         if (myLoc == null){
             myLoc = new Location(locationManager.NETWORK_PROVIDER);

         }


        locationManager.requestLocationUpdates(provider, 10000, 20, locationListener);
    }
    private void drawMarker(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        googleMap.addMarker(new MarkerOptions().position(latLng));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(20));
    }
    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }
    private void pushLocation() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
        query.whereMatches("pid", pid.toString());
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
        Log.d("MouseConnect-Inside", "sending pid");
        try {
            outStream.write(this.pid);
            gameEnd(1);
            Log.d("Mouse", "writing " + this.pid + " to outstream");
        }catch (IOException e){}
        //if our pid is not -1 then it isn't initialized


    }

    //uncomment for client aka mouse device
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        private final OutputStream mmOutStream;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            Log.d("ConnectThread-Inside", "inconstructor");
            BluetoothSocket tmp = null;

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
            Log.d("ConnectThread-Inside", "run function");

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out

            }

            // Do work to manage the connection (in a separate thread)
            MouseConnect(mmOutStream);
            Log.d("ConnectThread-Inside", "Sent");
            //cancel();
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
        private InputStream mmInstream;


        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            Log.d("accept thread", "in constructor");
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
            Log.d("accept thread", "server socket found");

            }

        public void run() {
            Log.d("thread run", "servr is running");
            BluetoothSocket socket;
                socket = null;
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    mmInstream = null;

                }
                // If a connection was accepted
                if (socket != null) {
                    InputStream tempin = null;
                    // Do work to manage the connection (in a separate thread)
                    try{
                        tempin = socket.getInputStream();

                    }catch (IOException e){}
                    mmInstream = tempin;
                }
                else {
                    mmInstream = null;
                }

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
                ParseObject pobj= null;
                //request it from the DB
                try {
                    pobj = query.get(query.getFirst().getObjectId());

                } catch (ParseException e) {

                    e.printStackTrace();
                }
                if (pobj != null){
                    pobj.put("isTaken", false);
                    pobj.put("isAlive", false);
                    pobj.saveInBackground();
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
        if (minutePing_cd) {
            Log.d("minute ping", "start ping");
            ParseQuery query = ParseQuery.getQuery("PlayerInfo");
            query.whereEqualTo("isAlive", true);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> playerList, ParseException e) {
                    Log.d("minute ping", "middle ping 1");
                    if (e == null) {
                        googleMap.clear();
                        for(ParseObject obj : playerList) {
                            Log.d("minute ping", "in ping");
                            ParseGeoPoint g = obj.getParseGeoPoint("location");
                            ParseGeoPoint ml = new ParseGeoPoint(myLoc.getLatitude(),myLoc.getLongitude());
                            Log.d("minute ping", "geopoint created");
                            //if(g.distanceInKilometersTo(ml) < 0.1) {
                            Location l = new Location(myLoc);
                            l.setLatitude(g.getLatitude());
                            l.setLongitude(g.getLongitude());
                            Log.d("minute ping", "in ping 2");
                            drawMarker(l);
                            Log.d("minute ping", "in ping 3");
                            //}
                        }
                    }
                }
            });
            new CountDownTimer(30000, 1000) {
                Button button = (Button) findViewById(R.id.button2);

                public void onTick(long millisUntilFinished) {
                        minutePing_cd = false;
                        button.setText(" " + millisUntilFinished / 1000 + " ");
                    Log.d("minute ping", "countdown");
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

            if (superPing_cd ) {
                ParseQuery query = ParseQuery.getQuery("PlayerInfo");
                query.whereEqualTo("isAlive", true);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> playerList, ParseException e) {
                        if (e == null) {
                            googleMap.clear();
                            for(ParseObject obj : playerList) {
                                ParseGeoPoint g = obj.getParseGeoPoint("location");

                                ParseGeoPoint ml = new ParseGeoPoint(myLoc.getLatitude(),myLoc.getLongitude());
                                if(g.distanceInKilometersTo(ml) < 0.2) {
                                    Location l = new Location(myLoc);
                                    l.setLatitude(g.getLatitude());
                                    l.setLongitude(g.getLongitude());
                                    drawMarker(l);
                                }
                            }
                        } else {
                            //Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });

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
