/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sip;

import com.example.siptester.DailActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.*;
import android.os.Message;
import android.util.Log;

/**
 * Listens for incoming SIP calls, intercepts and hands them off to
 * WalkieTalkieActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {
	/**
	 * Processes the incoming call, answers it, and hands it over to the
	 * WalkieTalkieActivity.
	 * 
	 * @param context
	 *            The context under which the receiver is running.
	 * @param intent
	 *            The intent being received.
	 */
	
	private DailActivity dailAct;
	
	public IncomingCallReceiver (DailActivity cntx) {
		this.dailAct = cntx;
	}
	
	private String TAG = "IncomingCallReceiver";
	
	//This is called when the intent is received
	//The intent is called when the manager reports that the phone is ringing
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received intent");
		SipAudioCall incomingCall = null;
		Log.d(TAG, "Entering try clause");
		try {

			//Creates a listener object that is attached to the SipAudioCall incomingCall object
//			Why does both the listener and the call object call answerCall?
			//Does this answerCall simply acknowledge the ringing?
			
			//Change the utilityButton to say "Answer"
			dailAct.sendMessage("Answer");
			Log.d(TAG, "Create listener object");
			SipAudioCall.Listener listener = dailAct.sipUtil.listener;
			Log.d(TAG, "Provide listener to mSipManager");
			//Here you register the listener declared above to answer when ringing
			incomingCall = dailAct.sipUtil.mSipManager.takeAudioCall(intent, listener);	
			//Attach to SipSession
			incomingCall.attachCall(dailAct.sipUtil.mSipSession, "IncomingCall");
			//Makes the phone ring
			dailAct.sipUtil.ringTone.play();
			//Gives the call to sipUtil so that it can be answered from there
			Log.d(TAG, "IncomingCallReceiver provides call to sipUtil.call");	
			dailAct.sipUtil.call = incomingCall;
			

		} catch (Exception e) {
			if (incomingCall != null) {
				incomingCall.close();
				Log.d(TAG, "Exception caught while answering");
			}
		}
	}
	
}
