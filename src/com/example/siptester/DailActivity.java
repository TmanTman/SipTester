package com.example.siptester;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sip.SipUtilities;
import com.example.sip.StateCall;
import com.example.sip.StateSip;

public class DailActivity extends ActionBarActivity {
	
	//For Debugging purposes
	private String TAG = "DailActivity";
	
	//Sip class
	public SipUtilities sipUtil = null;
	
	//SharedPreferences file
	private static final String PREFS_NAME = "MyPrefsFile";
	
	//SIP account details
	private String username;
	private String password;
	private String serverip;
	
	//Class handle to TextView for SIP call status
	TextView tViewSipState;
	TextView tViewCallState;
	Button utilityButton;
	Button declineButton;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dail);
        tViewSipState = (TextView)this.findViewById(R.id.textView1);
        tViewCallState = (TextView)this.findViewById(R.id.textView4);
        utilityButton = (Button)this.findViewById(R.id.button1);
        declineButton = (Button)this.findViewById(R.id.button3);
    }
    
	 @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//Initialize the SharedPreferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		//Get username from Prefs
		username = settings.getString("username", "");
		//Get server address from Prefs
		serverip = settings.getString("serverip", "");
		//Get password from Prefs
		password = settings.getString("password", "");	
		//Register SIP Service
		if (username != "" && password != "" && serverip != "")
		{
			sipUtil = new SipUtilities(this, username, serverip, password);
		}
	}
	 
	 @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//Destroy SIP Service
		if (sipUtil != null){
			if (sipUtil.call != null) {
				sipUtil.call.close();
			}
	
			if (sipUtil.callReceiver != null) {
				this.unregisterReceiver(sipUtil.callReceiver);
			}
		}
	}
	 
	 @Override
		public void onDestroy() {
			super.onDestroy();
			if (sipUtil != null) {
				if (sipUtil.call != null) {
					sipUtil.call.close();
				}
	
				if (sipUtil.callReceiver != null) {
					this.unregisterReceiver(sipUtil.callReceiver);
				}
			}
		}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dail, menu);
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
    
  //Create Handler to update screen from SipUtilities
    public Handler mHandler = new Handler(Looper.getMainLooper()){
  	
    	@Override 
    	public void handleMessage(Message inputMessage){
    		Log.d(TAG, "inputMessage what value: " + inputMessage.what);
    		int stateCode = -2; 
    		if (inputMessage.what != 0) {
    			stateCode = (int) inputMessage.what;
    			Log.d(TAG, "Received state: " + stateCode);
    		}
    		//Switch statement for SIP Registration status
    		if (stateCode < 20){
	    		switch (stateCode) {
	    		case SipUtilities.SIP_REGISTERING:
	    			Log.d(TAG, "Handler SipUtilities.SIP_REGISTERING");
	    			tViewSipState.setText("Registering SIP account...");
	    			sipUtil.stateSip = StateSip.SIP_REGISTERING;
	    			break;
	    		case SipUtilities.SIP_READY:
	    			Log.d(TAG, "Handler SipUtilities.SIP_READY");
	    			tViewSipState.setText("SIP Ready");
	    			sipUtil.stateSip = StateSip.SIP_READY;
	    			break;
	    		case SipUtilities.SIP_REGISTRATION_FAILED:
	    			Log.d(TAG, "Handler SipUtilities.SIP_REG_FAILED");
	    			tViewSipState.setText("SIP Failed");
	    			sipUtil.stateSip = StateSip.SIP_REGISTRARION_FAILED;
	    		default:
	    			Log.d(TAG, "Reached default statement");
	    			break;
	    		}
    		} else if (stateCode < 30) {
    			switch (stateCode){
    			case SipUtilities.IDLE:
    				Log.d(TAG, "Handler SipUtilities.IDLE");
    				tViewCallState.setText("Ready to call");
    				utilityButton.setText("Dail");
    				declineButton.setVisibility(View.INVISIBLE);
    				sipUtil.stateCall = StateCall.IDLE;
    				break;
    			case SipUtilities.CALL_CONNECTED:
    				Log.d(TAG, "Handler SipUtilities.CALL_CONNECTED");
    				tViewCallState.setText("Call Underway");
    				declineButton.setVisibility(View.INVISIBLE);
    				sipUtil.stateCall = StateCall.CALL_CONNECTED;
    				break;
    			case SipUtilities.RINGING_OUTGOING:
    				Log.d(TAG, "Handler SipUtilities.RINGING_OUTGOING");
    				tViewCallState.setText("Calling...");
    				utilityButton.setText("End Call");
    				sipUtil.stateCall = StateCall.RINGING_OUTGOING;
    				break;
    			case SipUtilities.RINGING_INCOMING:
    				Log.d(TAG, "Handler SipUtilities.RINGING_INCOMING");
    				tViewCallState.setText("Ringing...");
    				utilityButton.setText("Answer");
    				declineButton.setVisibility(View.VISIBLE);
    				sipUtil.stateCall = StateCall.RINGING_INCOMING;
    				break;
    			default:
    				Log.d(TAG, "Reached default statement");
    				break;
    			}
    		}
    	}
    };
    
    //Function that handled the button push
    public void utilButtonPush (View v){
    	EditText contactField = (EditText)this.findViewById(R.id.editText1);
    	String contactNum = contactField.getText().toString();
    	if (sipUtil != null) {
	    	if (!contactNum.equals("")) {
	    		//Check if SIP is ready to place a call
	    		if (sipUtil.call == null || sipUtil.call.getState() == SipSession.State.READY_TO_CALL ) {
	    			Log.d(TAG, "Contact Number..." + contactNum + "...");
	    			sipUtil.makeCall(contactNum);
	    		}
	    		//If there is an incoming call, answer it
	    		else if (sipUtil.call.getState() == SipSession.State.INCOMING_CALL) {
	    			sipUtil.call.setListener(sipUtil.listener);
	    			sipUtil.answer();
	    		}
	    		//If a call is already established, answer it
	    		else if (sipUtil.call.getState() == SipSession.State.IN_CALL){
	    			sipUtil.endCall();
	    		}
	    	} else {
	    		Toast.makeText(this, "No contact number entered", Toast.LENGTH_LONG).show();
	    	}
		} else {
    		Toast.makeText(this, "No SIP account registered yet", Toast.LENGTH_LONG).show();
    	}
    }
    
    public void setupAccount (View v) {
    	Intent intent = new Intent(DailActivity.this, AccountRegActivity.class);
		startActivity(intent);
    }

}
