package com.wearabledemo;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearService extends WearableListenerService {

    private static final String TAG = "WearService";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Log.v(TAG, "onDataChanged");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.v(TAG, "onMessageReceived");

        if (messageEvent.getPath().equals("/path/message")) {

            final String message = new String(messageEvent.getData());

            Log.d(TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on watch is: " + message);

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);

        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}