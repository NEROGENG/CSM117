package test.ucla.simpleclient_server;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.w3c.dom.Text;


public class GameStart extends Activity {
    public final static String PID = "test.ucla.simpleclient_server.PID";
    private static final String TAG = GameStart.class.getSimpleName();
    private Integer pid;
    private boolean assigned;
    private Integer init;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_start);
        Parse.initialize(this, "xJxmXrtjWXGGl3jmHxLmhD5uyG6rv6jSgR9xUwO3", "W0ePKRBPHRHSNVwUTsB2MsW7aJJzhTyGEZ1B4F1c");
        pid = -1;
        assigned = false;
        Button tmpbtn = (Button)findViewById(R.id.button6);
        tmpbtn.setVisibility(View.INVISIBLE);
        tmpbtn.setClickable(false);
        TextView txt = (TextView)findViewById(R.id.textView2);
        txt.setVisibility(View.INVISIBLE);
        Button button2 = (Button)findViewById(R.id.button7);
        button2.setVisibility(View.INVISIBLE);
        button2.setClickable(false);

        init = -1;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_start, menu);
        return true;
    }
    private void updateAssigned(boolean value){
        assigned = value;
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
    public void join(View view) {
        final Intent intent = new Intent(this, HuntTheTrojan.class);


       //Toast.makeText(getApplicationContext(), "Joined game...",
        //        Toast.LENGTH_SHORT).show();
        //startActivity(intent);


        //request the 0 -id item to see if there is a game to match
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
        query.whereMatches("pid", "0");
        Log.d(TAG, "Query Initialized");
        //request it from the DB
        try {

            query.getInBackground(query.getFirst().getObjectId(),new GetCallback<ParseObject>() {

                public void done(ParseObject player, ParseException e) {

                    if (e == null) {
                        Log.d(TAG, "Query successful");
                        if(player.getBoolean("isTaken") == true){
                            //Toast.makeText(getApplicationContext(), "Joined game...",
                              //      Toast.LENGTH_SHORT).show();
                            if (player.getBoolean("isAlive") == true){
                                Toast.makeText(getApplicationContext(), "Game has already started!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Joining the game...", Toast.LENGTH_SHORT).show();
                                for ( pid = 1; pid < 8;pid++) {
                                    if (assigned){
                                        pid--;
                                        break;
                                    }
                                    ParseQuery<ParseObject> query2 = ParseQuery.getQuery("PlayerInfo");
                                    query2.whereMatches("pid", (pid).toString());
                                    try {
                                        ParseObject pbj = query2.get(query2.getFirst().getObjectId());
                                        if (pbj.getBoolean("isTaken") == false){
                                            pbj.put("isTaken", true);
                                            pbj.put("isAlive", true);
                                            pbj.saveInBackground();
                                            assigned = true;
                                        }
                                    }catch (ParseException e2){
                                        e2.printStackTrace();
                                    }
                            }
                            }//if end forloop
                            if (assigned == false){
                                Toast.makeText(getApplicationContext(), "The game is currently full...",
                                           Toast.LENGTH_SHORT).show();
                            }
                            else{

                                intent.putExtra(PID, pid);
                                startActivity(intent);

                            }

                            }

                        }
                        else{
                            Toast.makeText(getApplicationContext(), "There's no game to join.  Make one!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        } catch (ParseException e) {

            e.printStackTrace();
        }


    }

    public void make (View view){


       // Toast.makeText(getApplicationContext(), "Making game...",
               // Toast.LENGTH_SHORT).show();
        //startActivity(intent);
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
        query.whereMatches("pid", "0");
        Log.d(TAG, "Query Initialized");
        //request it from the DB
        try {
            query.getInBackground(query.getFirst().getObjectId(),new GetCallback<ParseObject>() {

                public void done(ParseObject player, ParseException e) {
                    if (e == null) {
                        if(player.getBoolean("isTaken") == true){
                            Toast.makeText(getApplicationContext(), "A game already exists...",
                                    Toast.LENGTH_SHORT).show();


                        }
                        else{

                            Button button = (Button)findViewById(R.id.button6);
                            button.setVisibility(View.VISIBLE);
                            button.setClickable(true);
                            TextView txt = (TextView)findViewById(R.id.textView2);
                            txt.setVisibility(View.VISIBLE);
                            Button button2 = (Button)findViewById(R.id.button7);
                            button2.setVisibility(View.VISIBLE);
                            button2.setClickable(true);
                            Button button3 = (Button)findViewById(R.id.button5);
                            button3.setClickable(false);
                            player.put("isTaken", true);
                            player.saveInBackground();
                            //clean the server
                            for(init=1; init<8;init++){
                                ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
                                query.whereMatches("pid", (Integer.valueOf(init)).toString());
                                try {
                                    query.getInBackground(query.getFirst().getObjectId(),new GetCallback<ParseObject>() {
                                        public void done(ParseObject player, ParseException e) {
                                            if (e == null) {
                                                //ParseGeoPoint point = new ParseGeoPoint(40.0, -30.0);
                                                //gameScore.put("location", point);
                                                player.put("isTaken", false);
                                                player.put("isAlive", false);
                                                player.saveInBackground();
                                            }
                                        }
                                    });
                                } catch (ParseException e2) {
                                    // TODO Auto-generated catch block
                                    e2.printStackTrace();
                                }
                            }
                        }

                    }
                }
            });
        } catch (ParseException e) {

            e.printStackTrace();
        }
    }
    public void start (View view){
        final Intent intent = new Intent(this, HuntTheTrojan.class);
        Toast.makeText(getApplicationContext(), "Game started! Happy Hunting",
                Toast.LENGTH_LONG).show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
        query.whereMatches("pid", "0");
        Log.d(TAG, "Query Initialized");
        //request it from the DB
        try {
            query.getInBackground(query.getFirst().getObjectId(),new GetCallback<ParseObject>() {

                public void done(ParseObject player, ParseException e) {
                    if (e == null) {
                        player.put("isAlive", true);
                        player.saveInBackground();
                        intent.putExtra(PID,0);
                        startActivity(intent);
                    }

            }});}catch (ParseException e){
                e.printStackTrace();
            }
    }
    public void drop (View view){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
        query.whereMatches("pid", "0");
        Log.d(TAG, "Query Initialized");
        //request it from the DB
        try {
            query.getInBackground(query.getFirst().getObjectId(),new GetCallback<ParseObject>() {

                public void done(ParseObject player, ParseException e) {
                    if (e == null) {
                        player.put("isTaken", false);
                        player.saveInBackground();
                        Button button = (Button)findViewById(R.id.button6);
                        button.setVisibility(View.INVISIBLE);
                        button.setClickable(false);
                        Button button1 = (Button) findViewById(R.id.button7);
                        button1.setVisibility(View.INVISIBLE);
                        button1.setClickable(false);
                        Button button5 = (Button) findViewById(R.id.button5);
                        button5.setClickable(true);
                        TextView txt = (TextView)findViewById(R.id.textView2);
                        txt.setVisibility(View.INVISIBLE);
                    }




                }});}catch (ParseException e){
            e.printStackTrace();
        }
    }
}
