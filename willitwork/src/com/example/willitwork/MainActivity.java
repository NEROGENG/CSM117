package com.example.willitwork;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/*import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;*/
import java.util.List;
 
//import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import com.parse.*;

public class MainActivity extends ActionBarActivity {

	//private static final String tag = "helloworld";
	
	private Integer pid = 1;
	private Boolean myType;
	private EditText textField;
	private Button button;
	private Button button02;
	private String messsage;
	
	private Integer init = 0;
	
	private String retmsg;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        
        myType = false;
        
        Parse.initialize(this, "xJxmXrtjWXGGl3jmHxLmhD5uyG6rv6jSgR9xUwO3", "W0ePKRBPHRHSNVwUTsB2MsW7aJJzhTyGEZ1B4F1c");
        
        textField = (EditText) findViewById(R.id.editText1); // reference to the text field
		button = (Button) findViewById(R.id.button1); // reference to the send button
 
		/*
		for(int i=0; i<8;i++){
			ParseObject gameScore = new ParseObject("PlayerInfo");
			gameScore.put("pid",Integer.valueOf(i).toString());
			gameScore.put("deviceType", myType);
			ParseGeoPoint point = new ParseGeoPoint(40.0+i*i, -30.0-i*i);
			gameScore.put("location", point);
			gameScore.put("isTaken", false);
			//gameScore.put("msg", messsage);
			gameScore.saveInBackground();
		}*/
		
		
		for(init=0; init<8;init++){
			ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
			query.whereMatches("pid", (Integer.valueOf(init)).toString());
			try {
				query.getInBackground(query.getFirst().getObjectId(),new GetCallback<ParseObject>() {
					public void done(ParseObject player, ParseException e) {
						if (e == null) {
							//ParseGeoPoint point = new ParseGeoPoint(40.0, -30.0);
							//gameScore.put("location", point);
							player.put("isTaken", true);
							player.saveInBackground();
						}
					}
				});
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
			
	
		init = 0;
		// Button press event listener
		button.setOnClickListener(new View.OnClickListener() {
 
			public void onClick(View v) {
				messsage = textField.getText().toString(); // get the text message on the text field
				textField.setText(""); // Reset the text field to blank
				SendMessage sendMessageTask = new SendMessage();
				sendMessageTask.execute();
			}
		});
		
		
		
		
		ParsePush.subscribeInBackground("", new SaveCallback() {
			  @Override
			  public void done(ParseException e) {
			    if (e == null) {
			      Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
			    } else {
			      Log.e("com.parse.push", "failed to subscribe for push", e);
			    }
			  }
			});
		
		
		button02 = (Button) findViewById(R.id.button2); // reference to the send button
		 
		// Button press event listener
		button02.setOnClickListener(new View.OnClickListener() {
 
			public void onClick(View v) {
				
				ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
				query.whereMatches("pid", pid.toString());
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> playerList, ParseException e) {
				        if (e == null) {
				            Log.d("player", "Retrieved " + playerList.size() + " players");
				            ParseObject obj = playerList.get(playerList.size()-1);
				            //messsage = obj.getString("msg: ");
				            retmsg = messsage + ": " + "(" + String.valueOf(obj.getParseGeoPoint("location").getLatitude());
				            retmsg += ","+ String.valueOf(obj.getParseGeoPoint("location").getLongitude()) + ")";
				        } else {
				            Log.d("score", "Error: " + e.getMessage());
				        }
				        TextView temp = (TextView) findViewById(R.id.textView);
						temp.setText(retmsg);
				    }
				});
				
				//messsage = ;//push something // get the text message on the text field
				//TextView temp = (TextView) findViewById(R.id.textView);
				//temp.setText(retmsg);
			}
		});
		
    }
    
    
    private class SendMessage extends AsyncTask<Void, Void, Void> {
    	 
		@Override
		protected Void doInBackground(Void... params) {
			
			ParseQuery<ParseObject> query = ParseQuery.getQuery("PlayerInfo");
			query.whereMatches("pid", (pid++).toString());
			try {
				query.getInBackground(query.getFirst().getObjectId(),new GetCallback<ParseObject>() {
				public void done(ParseObject player, ParseException e) {
					if (e == null) {
					    	//ParseGeoPoint point = new ParseGeoPoint(40.0, -30.0);
							//gameScore.put("location", point);
							player.put("msg", messsage);
							player.saveInBackground();
					}
				}
				});
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			ParseObject gameScore = new ParseObject("PlayerInfo");
			gameScore.put("pid",pid);
			gameScore.put("deviceType", myType);
			ParseGeoPoint point = new ParseGeoPoint(40.0, -30.0);
			gameScore.put("location", point);
			gameScore.put("msg", messsage);
			gameScore.saveInBackground();
			*/
			return null;
			
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
