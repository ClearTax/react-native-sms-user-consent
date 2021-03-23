package com.genyaonipko.RNReactNativeSMSUserConsent;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.facebook.react.bridge.ReactApplicationContext;

public class SmsRetrieveBroadcastReceiver extends BroadcastReceiver {

    public static final int SMS_CONSENT_REQUEST = 1244;

    private ReactApplicationContext reactContext;

    public SmsRetrieveBroadcastReceiver(ReactApplicationContext rcontext) {
        super();
        this.reactContext = rcontext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            int statusCode = smsRetrieverStatus.getStatusCode();
            switch (statusCode) {
                case CommonStatusCodes.SUCCESS:
                    // Get consent intent
                    Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    try {
                        reactContext.getCurrentActivity().startActivityForResult(consentIntent, SMS_CONSENT_REQUEST);
                    } catch (ActivityNotFoundException e)  {
                        // Handle the exception here
                    }
                    break;
                case CommonStatusCodes.TIMEOUT:
                    break;
            }
        }
    }
}
