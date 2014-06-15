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
import android.net.sip.SipSession;
import android.os.Message;
import android.util.Log;

import com.example.siptester.DailActivity;

public class SipUtilities {
	
	public SipManager mSipManager = null;
	private SipProfile mSipProfile;
	private final String TAG = "SipMethods";
	public SipAudioCall.Listener listener;
	public IncomingCallReceiver callReceiver;
	public SipAudioCall call = null;
	public RingtoneManager ringMan = null;
	public Ringtone ringTone = null;
	private DailActivity dailAct;
	
	public SipUtilities (final DailActivity cntx, String pUsername, String pServerIp, String pPassword){
		
		Log.d(TAG, "Starting SipUtilities constructor");
		
		//Set up filter to receive SIP calls:
		// Set up the intent filter. This will be used to fire an
		// IncomingCallReceiver when someone calls the SIP address used by this
		// application.
		//Basically you register the class that will handle the intent INCOMING CALL
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.sip.INCOMING_CALL");
		callReceiver = new IncomingCallReceiver(cntx);
		cntx.registerReceiver(callReceiver, filter);
		
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
				//builder.setAuthUserName("piet");
				//builder.setOutboundProxy("10.110.28.228");
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
			Log.d(TAG, "Making call");
			call = mSipManager.makeAudioCall(mSipProfile.getUriString(), pContact, listener, 30);
		} catch (Exception e){
			Log.d(TAG, "Error: " + e.toString());
		}
	}
	
	public void endCall(){
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
        	dailAct.sendMessage("Registering SIP Server");
        	Message m = dailAct.mHandler.obtainMessage(dailAct.S1);
        	m.sendToTarget();
        }

        public void onRegistrationDone(String localProfileUri, long expiryTime) {
        	Log.d(TAG, "SIP Ready");
        	dailAct.sendMessage("SIP Ready");
        	Message m = dailAct.mHandler.obtainMessage(dailAct.S2);
        	m.sendToTarget();
        }

        public void onRegistrationFailed(String localProfileUri, int errorCode,
                String errorMessage) {
        	Log.d(TAG, "Registration failed: " + errorMessage);
        	dailAct.sendMessage("Registration failed: " + errorMessage);
        	
        }
        
    }
	
	class SipAudioCallListener extends SipAudioCall.Listener {
		
		DailActivity dailAct;
		
		public SipAudioCallListener(DailActivity cntx){
			this.dailAct = cntx;
		}
        
    	@Override
        public void onCallEstablished(SipAudioCall call) {
            Log.d(TAG, "onCallEstablished listener");
            call.startAudio();
            call.setSpeakerMode(true);
            if (call.isMuted())
            {
            	Log.d(TAG, "Call was muted");
            	call.toggleMute();
            }
            //Change utilButton to say "End Call"
            dailAct.sendMessage("End Call");
        }
    	
    	@Override
        public void onCallEnded(SipAudioCall call) {
            Log.d(TAG, "onCallEnded Listener");
            Log.d(TAG, "Sip State: " + SipSession.State.toString(SipSession.getState()));
            dailAct.sendMessage("Dail");
        }
    	
    	@Override
    	public void onReadyToCall (SipAudioCall call) {
    		Log.d(TAG, "onReadyToCall Listener");
    	}
    	
    	@Override
    	public void onRinging (SipAudioCall call, SipProfile caller){
    		Log.d(TAG, "onRinging Listener");
    		try {
				Log.d(TAG, "listener answers call");
				call.answerCall(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    	@Override
    	public void onCalling (SipAudioCall call) {
    		Log.d(TAG, "onCalling Listener");
    	}
    	
    	@Override
    	public void onCallBusy (SipAudioCall call){
    		Log.d(TAG, "onCallBusy Listener");
    	}
    	
    	@Override
    	public void onError (SipAudioCall call, int errorCode, String errorMessage){
    		Log.d(TAG, "onError Listener: " + errorMessage);
    	}
    	
    	@Override
    	public void onRingingBack (SipAudioCall call){
    		Log.d(TAG, "onRingingBack listener");
    	}
    	
    	@Override
    	public void onChanged (SipAudioCall call){
    		Log.d(TAG, "onChanged Listener");
    	}
    	
    }
	
}
