package com.genyaonipko.RNReactNativeSMSUserConsent;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


import static android.app.Activity.RESULT_OK;

import androidx.annotation.NonNull;


public class RNReactNativeSMSUserConsentModule extends ReactContextBaseJavaModule {
    private static final String LOG_TAG = "RNReactNativeSMSUserConsentModule";

    private final ReactApplicationContext reactContext;
    private Promise currPromise;
    private SmsRetrieveBroadcastReceiver receiver;
    private static final String E_OTP_ERROR = "E_OTP_ERROR";
    private static final String RECEIVED_OTP_PROPERTY = "receivedOtpMessage";
    public static final int SMS_CONSENT_REQUEST = 1244;
    final String SEND_PERMISSION = "com.google.android.gms.auth.api.phone.permission.SEND";


    public RNReactNativeSMSUserConsentModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "RNReactNativeSMSUserConsent";
    }

    @ReactMethod
    public void listenOTP(final Promise promise) {
        unregisterReceiver();

        if (currPromise != null ) {
            currPromise.reject(E_OTP_ERROR, new Error("Rejected to conclude"));
        }

        currPromise = promise;
        Task<Void> task = SmsRetriever.getClient(getCurrentActivity()).startSmsUserConsent(null);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // successfully started an SMS Retriever for one SMS message
                Log.d(LOG_TAG, "SMS retriever OTP listen task started");
                registerReceiver();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG_TAG, "SMS retriever OTP listen task failed");
                promise.reject(E_OTP_ERROR, e);
                currPromise = null;
            }
        });
    }

    @ReactMethod
    public void removeOTPListener(){
        Log.d(LOG_TAG, "Remove OTP listener called");
        unregisterReceiver();
        if (currPromise != null ) {
            currPromise.reject(E_OTP_ERROR, new Error("Rejected to conclude"));
        }
        currPromise = null;
    }

    private void registerReceiver() {
        try{
            receiver = new SmsRetrieveBroadcastReceiver(getCurrentActivity());
            IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
            getCurrentActivity().registerReceiver(receiver, intentFilter, SEND_PERMISSION, null);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unregisterReceiver() {
        if (receiver != null) {
            try{
                getCurrentActivity().unregisterReceiver(receiver);
                receiver = null;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            switch (requestCode) {
                case SMS_CONSENT_REQUEST:
                    unregisterReceiver();
                    try{
                        // User permission granted state
                        if (resultCode == RESULT_OK) {
                            String message = intent.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                            WritableMap map = Arguments.createMap();
                            map.putString(RECEIVED_OTP_PROPERTY, message);
                            currPromise.resolve(map);
                        } else {
                            currPromise.reject(E_OTP_ERROR, new Error("Result code: " + resultCode));
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    currPromise = null;
                    break;
            }
        }
    };
}
