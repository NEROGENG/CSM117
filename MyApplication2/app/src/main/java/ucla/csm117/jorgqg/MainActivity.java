package ucla.csm117.jorgqg;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.parse.*;

import java.util.Collections;
import java.util.List;

public class MainActivity extends FragmentActivity {
    //Google Map variables
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private Location myLoc;
    //Parse variables
    private Integer pid = 1;
    private Button button;

    //private List<Marker> markerList = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpParse();
        setUpGoogleMap();

        button = (Button) findViewById(R.id.button_update);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                ParseQuery query = ParseQuery.getQuery("PlayerInfo");
                query.whereEqualTo("isTaken", true);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> playerList, ParseException e) {
                        if (e == null) {
                            googleMap.clear();
                            for(ParseObject obj : playerList) {
                                ParseGeoPoint g = obj.getParseGeoPoint("location");
                                ParseGeoPoint ml = new ParseGeoPoint(myLoc.getLatitude(),myLoc.getLongitude());
                                if(g.distanceInKilometersTo(ml) < 0.1) {
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
            }
        });
    }

    private void setUpParse() {
        Parse.initialize(this, "xJxmXrtjWXGGl3jmHxLmhD5uyG6rv6jSgR9xUwO3", "W0ePKRBPHRHSNVwUTsB2MsW7aJJzhTyGEZ1B4F1c");
    }

    private void setUpGoogleMap() {
        if (googleMap == null) {
            googleMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (googleMap != null) {
                setUpMap();
            }
        }
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    private void drawMarker(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(20));
    }
}