package in.rbofficial.durotto;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SwitchCompat enable, bt, gps;
    private BluetoothAdapter btAdapter;
    public static final int BT_RE_CODE = 101010;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        enable = findViewById(R.id.enable_service_switch);
        bt     = findViewById(R.id.bt_switch);
        gps    = findViewById(R.id.gps_switch);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        bt.setChecked(btAdapter.isEnabled());



        bt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                if(!btAdapter.isEnabled()){
                    enableBluetooth();
                    btAdapter.enable();
                }else {
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter);
                }
            }else {
                if(btAdapter.isEnabled())
                    btAdapter.disable();
            }
        });

        gps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                turnGPSOn();
            }else {
                turnGPSOff();
            }
        });
        
    }



    private void enableBluetooth() {
        if(btAdapter==null){
            Toast.makeText(getApplicationContext(), "This Device Does not support bluetooth", Toast.LENGTH_SHORT).show();
        }else {
            Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btIntent,BT_RE_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==BT_RE_CODE){
            if(requestCode==RESULT_OK){
                Toast.makeText(getApplicationContext(), "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
            }else if(requestCode==RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), "Bluetooth Enabled Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void log(String msg){
        Log.d("LOGMANAGER",msg);
    }


    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    private void turnGPSOff(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                log(deviceName);
                log(deviceHardwareAddress);
            }
        }
    };
}