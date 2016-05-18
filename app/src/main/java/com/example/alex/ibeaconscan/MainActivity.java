package com.example.alex.ibeaconscan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import org.altbeacon.beacon.*;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer{
    protected static final String TAG = "MainActivity";
    private BeaconManager beaconManager;

    private BluetoothManager BTManager;
    private BluetoothAdapter BTAdapter = null;
    private TextView RssiText,UuidText,MajorText,MinorText;
    private Handler mHandler;
    private int Rssi, Major, Minor;
    private String Uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RssiText = (TextView) findViewById(R.id.RssiText);
        UuidText = (TextView) findViewById(R.id.UuidText);
        MajorText = (TextView) findViewById(R.id.MajorText);
        MinorText = (TextView) findViewById(R.id.MinorText);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        mHandler = new Handler();
        //BTAdapter = BluetoothAdapter.getDefaultAdapter();
        //BTManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        //mHandler.post(scanRunnable);

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



    public Runnable scanRunnable = new Runnable()
    {
        @Override
        public void run() {
            RssiText.setText(Rssi + "");
            UuidText.setText(Uuid);
            MajorText.setText(Major + "");
            MinorText.setText(Minor + "");

            //BTAdapter.startLeScan(leScanCallback);
            //mHandler.postDelayed(this, 2000);
            //BTAdapter.stopLeScan(leScanCallback);
        }
    };
/*
    public BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, int Rssi, byte[] scanRecord)
        {
            int startByte = 2;
            boolean patternFound = false;
            while (startByte <= 5)
            {
                if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 &&
                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15)
                {
                    patternFound = true;
                    break;
                }
                startByte++;
            }

            if (patternFound)
            {
                //Convert to hex String
                byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                String hexString = bytesToHex(uuidBytes);

                //UUID detection
                String Uuid =  hexString.substring(0,8) + "-" +
                        hexString.substring(8,12) + "-" +
                        hexString.substring(12,16) + "-" +
                        hexString.substring(16,20) + "-" +
                        hexString.substring(20,32);

                int Major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);
                int Minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);
                RssiText.setText(Rssi + "");
                UuidText.setText(Uuid);
                MajorText.setText(Major + "");
                MinorText.setText(Minor + "");
            }
            BTAdapter.stopLeScan(leScanCallback);
        }

    };
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    */
}
