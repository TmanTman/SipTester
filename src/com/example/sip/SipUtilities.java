package com.example.sip;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.siptester.DailActivity;

public class SipUtilities {
	
	public SipManager mSipManager = null;
	private SipProfile mSipProfile;
	private final String TAG = "SipUtilities";
	public SipAudioCall.Listener listener;
	public IncomingCallReceiver callReceiver;
	public SipAudioCall call = null;
	public RingtoneManager ringMan = null;
	public Ringtone ringTone = null;
	private DailActivity dailAct;
	
	//STATES MESSAGE CODES
	//States related to SIP
	public static final int SIP_REGISTERING = 11;
	public static final int SIP_READY = 12;
	public static final int SIP_REGISTRATION_FAILED = 13;
	//States related to calls
	public static final int CALL_CONNECTED = 21; 
	public static final int IDLE = 22;
	public static final int RINGING_INCOMING = 23; 
	public static final int RINGING_OUTGOING = 24;
	public static final int BUSY = 25;
	public static final int ERROR = 26;
	
	//SIP state
	public StateSip stateSip = StateSip.UNINIT_STATE;
	//Call state
	public StateCall stateCall = StateCall.UNINIT_STATE;
	
	
	public SipUtilities (final DailActivity cntx, String pUsername, String pServerIp, String pPassword){
		
		Log.d(TAG, "Starting SipUtilities constructor");
		
		//Set up filter to receive SIP calls:
		//Register the class that will handle the intent INCOMING CALL
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.sip.INCOMING_CALL");
		callReceiver = new IncomingCallReceiver(cntx);
		cntx.registerReceiver(callReceiver, filter);
		
		this.dailAct = cntx;
		
		//Ringtone operations
		ringMan = new RingtoneManager(cntx);
		ringTone = RingtoneManager.getRingtone(cntx, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
		
		if (mSipManager == null) {
			this.mSipManager = SipManager.newInstance(cntx);
			SipProfile.Builder builder = null;
			Log.d(TAG, "Attempting to build with: ");
			Log.d(TAG, "Username: ." + pUsername + ". Password: ." + pPassword + ". ServerIP: ." + pServerIp + ".");
			try {
				//Set the SIP username and serverdomain

				//Phone 1: User Account
				builder = new SipProfile.Builder(pUsername, pServerIp);
				} catch (java.text.ParseException e) {
					Log.d(TAG, "Error: " + e.getMessage());
					e.printStackTrace();
				}
				//Set the SIP password
				builder.setPassword(pPassword);
				builder.setPort(5060);
				builder.setProtocol("UDP");
				this.mSipProfile=builder.build();
				
		}
		
		//Send Pending Intent to the SIPManager
		//This registers the actions that will be called when the mSipProfile SIP Profile receives a call
		Intent intent = new Intent();
        intent.setAction("com.example.sip.INCOMING_CALL");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(cntx, 0,intent, Intent.FILL_IN_DATA);
        
        //This registers a listener for when the registration of the SIP service fails or succeeds
        try {
        	Log.d(TAG, "Opening SIP profile");
            mSipManager.open(mSipProfile, pendingIntent, null);        
            mSipManager.setRegistrationListener(mSipProfile.getUriString(), new SipRegistrationListener(cntx));
        } catch (SipException e) {      
            e.printStackTrace();
        }    
	}
	
	public void makeCall(String pContact){	
		//Configure the listener that responds when you dial another SIP account
		//Make a listener from my own SipAudioCallListener class
        listener = new SipAudioCallListener(this.dailAct);
		try {
			Log.d(TAG, "Making call to: " + pContact);
			call = mSipManager.makeAudioCall(mSipProfile.getUriString(), pContact, listener, 30);
		} catch (Exception e){
			Log.d(TAG, "Error: " + e.toString());
		}
	}
	
	public void endCall(){
		if (this.ringTone.isPlaying())
			ringTone.stop();
		try{
			Log.d(TAG, "Ending call");
			call.endCall();
		} catch (Exception e){
			Log.d(TAG, "Error: " + e.toString());
		}
	}
	
	public void answer(){
		this.ringTone.stop();
		try {
			call.answerCall(30);
		} catch (SipException e) {
			Log.d(TAG, "Error: " + e.toString());
			return;
		}
		call.startAudio();
		call.setSpeakerMode(true);
		if (call.isMuted()) {
			call.toggleMute();
		}
	}
 
	//Handles SIP registration to server
	//Changes the String on the DailActivity screen to reflect current status of registration
	class SipRegistrationListener implements android.net.sip.SipRegistrationListener {

		DailActivity dailAct;
		
		public SipRegistrationListener (DailActivity cntx) {
			dailAct = cntx;
		}
		
        public void onRegistering(String localProfileUri) {
        	Log.d(TAG, "Registering with SIP server");
        	Message m = dailAct.mHandler.obtainMessage(SipUtilities.SIP_REGISTERING);
        	m.sendToTarget();
        }

        public void onRegistrationDone(String localProfileUri, long expiryTime) {
        	Log.d(TAG, "SIP Ready");
        	Message m = dailAct.mHandler.obtainMessage(SipUtilities.SIP_READY);
        	m.sendToTarget();
        }

        public void onRegistrationFailed(String localProfileUri, int errorCode,
                String errorMessage) {
        	Log.d(TAG, "Registration failed: " + errorMessage);
        	Message m = dailAct.mHandler.obtainMessage(SipUtilities.SIP_REGISTRATION_FAILED);
        	m.sendToTarget();
        }
        
    }
	
}
