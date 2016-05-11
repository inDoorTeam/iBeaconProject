package com.example.alex.ibeaconscan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private BluetoothManager BTManager;
    private BluetoothAdapter BTAdapter = null;
    private TextView RssiText,UuidText,MajorText,MinorText;
    private Handler mHandler;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        RssiText = (TextView) findViewById(R.id.RssiText);
        UuidText = (TextView) findViewById(R.id.UuidText);
        MajorText = (TextView) findViewById(R.id.MajorText);
        MinorText = (TextView) findViewById(R.id.MinorText);
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        BTManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mHandler.post(scanRunnable);
    }
    private Runnable scanRunnable = new Runnable()
    {
        @Override
        public void run() {
            BTAdapter.startLeScan(leScanCallback);
            mHandler.postDelayed(this, 1000);
            //BTAdapter.stopLeScan(leScanCallback);
        }
    };
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback()
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
    private static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
