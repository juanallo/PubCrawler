package com.jalloro.android.pubcrawler.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class FirebaseAuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private FirebaseAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new FirebaseAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}