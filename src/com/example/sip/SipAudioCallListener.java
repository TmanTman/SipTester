package com.example.sip;

import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.siptester.DailActivity;

class SipAudioCallListener extends SipAudioCall.Listener {
		
		DailActivity dailAct;
		
		private final String TAG = "SipAudioCallListener";
		
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
            Message m = dailAct.mHandler.obtainMessage(SipUtilities.CALL_CONNECTED);
        	m.sendToTarget();
        }
    	
    	@Override
        public void onCallEnded(SipAudioCall call) {
            Log.d(TAG, "onCallEnded Listener");
            Message m = dailAct.mHandler.obtainMessage(SipUtilities.IDLE);
        	m.sendToTarget();
        }
    	
    	@Override
    	public void onReadyToCall (SipAudioCall call) {
    		Log.d(TAG, "onReadyToCall Listener");
    	}
    	
    	@Override
    	public void onRinging (SipAudioCall call, SipProfile caller){
    		Log.d(TAG, "onRinging Listener");
    	}
    	
    	@Override
    	public void onCalling (SipAudioCall call) {
    		Log.d(TAG, "onCalling Listener");
    		Message m = dailAct.mHandler.obtainMessage(SipUtilities.RINGING_OUTGOING);
        	m.sendToTarget();
    	}
    	
    	@Override
    	public void onCallBusy (SipAudioCall call){
    		Message m = dailAct.mHandler.obtainMessage(SipUtilities.BUSY);
        	m.sendToTarget();
    		m = dailAct.mHandler.obtainMessage(SipUtilities.IDLE);
        	m.sendToTarget();
    		Log.d(TAG, "onCallBusy Listener");
    	}
    	
    	@Override
    	public void onError (SipAudioCall call, int errorCode, String errorMessage){
    		Message m = dailAct.mHandler.obtainMessage(SipUtilities.ERROR);
    		m.sendToTarget();
    		m = dailAct.mHandler.obtainMessage(SipUtilities.IDLE);
        	m.sendToTarget();
    		//The call is then never established (?)
    		//Return to IDLE state
    		Log.d(TAG, "onError Listener: " + errorMessage + " code: " + errorCode);
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