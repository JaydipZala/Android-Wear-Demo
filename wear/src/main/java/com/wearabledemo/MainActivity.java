package com.wearabledemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "WearMainActivity";

    private TextView textView;
    private String receivedMessage = null;
    private String message = "";

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "onCreate()");

        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(messageReceiver, messageFilter);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    private void setScreen() {
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                textView = (TextView) stub.findViewById(R.id.text_view);
                textView.setText(message);
                new SendToDataLayerThread("/path/message", receivedMessage + " has been received by wear").start();
            }
        });
    }

    public class MessageReceiver extends BroadcastReceiver {
        private static final String TAG = "MessageReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            receivedMessage = intent.getStringExtra("message");
            Log.d("MessageReceiver", "onReceive() receivedMessage = " + receivedMessage);

            if (receivedMessage != null) {
                // Display message in UI
                message = receivedMessage;
                Log.d("MessageReceiver", "receivedMessage =" + receivedMessage);
            } else {
                receivedMessage = "No Message";
                Log.d("MessageReceiver", "receivedMessage = No Message");
            }

            setScreen();

        }
    }

    class SendToDataLayerThread extends Thread {
        String path;
        String handheldMessage;

        SendToDataLayerThread(String pth, String message) {
            path = pth;
            handheldMessage = message;
        }

        public void run() {
            Log.d(TAG, "SendToDataLayerThread()");

            NodeApi.GetConnectedNodesResult nodeResult = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            for (Node node : nodeResult.getNodes()) {
                MessageApi.SendMessageResult result =
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, handheldMessage.getBytes()).await();

                if (result.getStatus().isSuccess()) {
                    Log.d(TAG, "To: " + node.getDisplayName());
                    Log.d(TAG, "Message = " + handheldMessage);
                } else {
                    Log.d(TAG, "Send error");
                }
            }
        }
    }

    // data layer connection
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
