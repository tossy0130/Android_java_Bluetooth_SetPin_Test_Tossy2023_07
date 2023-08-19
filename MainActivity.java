package com.example.bluetooth_con_test_01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //  プリンター　アドレス　：：　E47FB2FDF13B

    // "00:01:95:98:D0:3B";  アダプター01
    //  00:01:95:43:98:27  アダプター 02
    private static final String DEVICE_ADDRESS = "00:01:95:43:98:27"; // Bluetoothアドレス
    private static final String GET_ADDRESS = "000195439827"; // Bluetoothアドレス
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPPプロファイルのUUID
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;

    private boolean Pair_Flg = false;

    // === 追加 230731
    private final BroadcastReceiver pairingRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            System.out.println("BroadcastReceiver 01　action :::" + action);

            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // === Android のペアリング接続　アクションを取得
                int variant = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

                System.out.println("BroadcastReceiver 02 variant :::" + variant);

                if (variant == BluetoothDevice.PAIRING_VARIANT_PIN) {
                    String pin = "1234";
                    byte[] pinBytes = pin.getBytes(); // バイトへ変換

                    System.out.println("pin  :::" + pin);

                    // === パーミッション　許可
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                            return;
                        }
                    }
                        // ===
                    System.out.println("setPin:::値:::" + pinBytes);
                    device.setPin(pinBytes);
                    abortBroadcast();

                }
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println(" onCreate ********* APP スタート **********");


        // Bluetoothペアリングリクエストを監視
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(pairingRequestReceiver, filter);


        Button paer_connectButton = findViewById(R.id.paer_connectButton);
        paer_connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // === 230731 追加

                // ==============================================================
                // ================================ Bluetooth ペアリング処理 START
                // Bluetoothペアリングリクエストを監視
                //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
                //registerReceiver(pairingRequestReceiver, filter);

                // ペアリングを開始するBluetoothデバイスを取得してください
                // BluetoothDevice bluetoothDevice = getBluetoothDevice();

                String Get_Mac_Addres = Cteate_Deviceaddress(DEVICE_ADDRESS);
                System.out.println("Get_Mac_Addres ::: 値 :::" + Get_Mac_Addres);


                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
                System.out.println("device:::値:::" + device);

                // === パーミッション　許可
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }

                // デバイスとのペアリングを開始
                if (device != null) {
                    device.createBond();
                }
                // ================================ Bluetooth ペアリング処理 END
                // ==============================================================

            }
        });


        Button connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("=========== 接続ボタン:::クリック start ==============");
                connectToDevice();
            }
        });

        /**
         *  コネクション 切断ボタン
         */
        Button dis_connectButton = findViewById(R.id.dis_connectButton);
        dis_connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(socket != null) {
                        socket.close();
                        System.out.println(" 切断ボタン ********* 切断OK **********");
                    }
                } catch (IOException e11) {
                    e11.printStackTrace();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (bluetoothAdapter == null) {
            // デバイスがBluetoothをサポートしていない場合
            showToast("デバイスBluetoothサポート外");
            finish();
        }


    }

    // === 230731 追加
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pairingRequestReceiver);
    }


    /**
     *  socketへ接続する
     */
    private void connectToDevice() {

        System.out.println("connectToDevice === 開始");


     //   if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        // === パーミッション　許可
 //       if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
        }

        // === Cteate_Deviceaddress で macアドレスへ :を追加
        String get_mac_str = Cteate_Deviceaddress(GET_ADDRESS);
        System.out.println("get_mac_str ::: 値 :::" + get_mac_str);

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

        System.out.println("device ::: con　中 ::: " + device);
        // デバイスとのペアリングを開始
        if (device != null) {

            if (Pair_Flg == false) {
                device.createBond();
                Pair_Flg = true;
                return;
            } else {

            }

        }

                try {
                     device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
                 //   BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
                 //   device.setPin(new byte[]{1, 2, 3, 4});


                 //   BluetoothSocket A_socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    BluetoothSocket A_socket = device.createRfcommSocketToServiceRecord(MY_UUID);

                    A_socket.connect();

                    System.out.println("接続 A_socket :::" + A_socket);

                    this.socket = A_socket;
                    // 接続成功時の処理
                    showToast("接続成功");
                    System.out.println("******************* 接続成功 ********************");

                } catch (IOException  e) {
                    // 接続失敗時の処理
                    showToast("接続に失敗：" + e.getMessage());
                    System.out.println("接続に失敗：" + e.getMessage());

                    /*
                    try {
                        if(this.socket != null) {
                            this.socket.close();
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }

                     */
                }

                System.out.println("======= if パーミッション 内 return; ::: END ======");
                return;


    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // === PINコード　挿入
    private BluetoothDevice getBluetoothDevice(){

        // === DEVICE_ADDRESS
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
    //    BluetoothDevice device = bluetoothAdapter.getRemoteDevice("00:01:95:98:D0:3B");
        return device;
    }

    /**
     *    ===  mac アドレスを：区切りで返す
     */
    private String Cteate_Deviceaddress(String str) {

        StringBuffer Mc_str = new StringBuffer();
        int idx_count = 2;
        for(int i = 0; i <= 4; i++) {
            Mc_str.insert(idx_count, ":");
            idx_count += 3;
        }
        str = Mc_str.toString();
        return str;

    }


}