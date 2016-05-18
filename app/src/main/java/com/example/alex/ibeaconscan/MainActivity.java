package com.example.alex.ibeaconscan;

import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import org.altbeacon.beacon.*;
import java.util.Collection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements BeaconConsumer{
    private BeaconManager beaconManager;

    private TextView RssiText,UuidText,MajorText,MinorText;
    private Handler mHandler;
    private int Rssi, Major, Minor;
    private String Uuid;

    private String address = "140.134.226.182";
    private int port = 8765;
    Socket clientSocket = new Socket();
    DataOutputStream outToServer;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thread = new Thread(sendtoServer);
        thread.start();

        RssiText = (TextView) findViewById(R.id.RssiText);
        UuidText = (TextView) findViewById(R.id.UuidText);
        MajorText = (TextView) findViewById(R.id.MajorText);
        MinorText = (TextView) findViewById(R.id.MinorText);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        mHandler = new Handler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect() {
        try {
            //beaconManager.startMonitoringBeaconsInRegion(new Region("all-beacons-region", null, null, null ));
            beaconManager.startRangingBeaconsInRegion(new Region("com.example.alex.ibeaconscan", null, null, null ));
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        //beaconManager.setMonitorNotifier(this);
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Beacon beacon = beacons.iterator().next();

                    Rssi = beacon.getRssi();
                    Uuid = beacon.getId1().toUuidString();
                    Major = beacon.getId2().toInt();
                    Minor = beacon.getId3().toInt();
                    mHandler.post(scanRunnable);
                }

            }
        });

    }

    public Runnable sendtoServer = new Runnable() {
        @Override
        public void run() {
            try {
                clientSocket = new Socket(InetAddress.getByName(address), port);
                outToServer = new DataOutputStream( clientSocket.getOutputStream() );
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    public Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            RssiText.setText(Rssi + "");
            UuidText.setText(Uuid);
            MajorText.setText(Major + "");
            MinorText.setText(Minor + "");

            if(clientSocket.isConnected()) {
                try {
                    outToServer.writeUTF(Rssi + " " + Uuid + " " + Major + " " + Minor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    };

}
