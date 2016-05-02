package com.wearabledemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private GoogleApiClient googleApiClient;
    private Button button;
    private String receivedMessage = null;

    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_main);

        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(messageReceiver, messageFilter);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendMessage = "Test Message Api: " + counter;
                counter++;

                new SendToDataLayerThread("/path/message", sendMessage).start();

                Log.d(TAG, "SendToDataLayerThread()");

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        googleApiClient.connect();
    }

    // data layer connection
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    @Override
    protected void onStop() {
        if (null != googleApiClient && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();

        Log.d(TAG, "onStop()");

    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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
                SendMessageResult result =
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

    public class MessageReceiver extends BroadcastReceiver {
        private static final String TAG = "MessageReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            receivedMessage = intent.getStringExtra("message");
            Log.d("MessageReceiver", "onReceive() receivedMessage = " + receivedMessage);
            String message;
            if (receivedMessage != null) {
                // Display message in UI
                message = receivedMessage;
                Log.d("MessageReceiver", "receivedMessage =" + receivedMessage);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            } else {
                receivedMessage = "No Message";
                Log.d("MessageReceiver", "receivedMessage = No Message");
                Toast.makeText(MainActivity.this, receivedMessage, Toast.LENGTH_SHORT).show();
            }

        }
    }

}