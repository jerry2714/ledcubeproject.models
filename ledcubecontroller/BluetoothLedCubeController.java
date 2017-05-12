package ledcubeproject.models.ledcubecontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * 繼承自DeviceController，具有藍芽通訊和跟父類別相同的指令能力，擔任和LED立方體溝通的功能
 * BluetoothDeviceController的使用盡量遵守以下順序:
 * 先用isDeviceSupportBluetooth()和open()確定此裝置支援藍芽並且藍芽已經開啟
 * clean()清除和之前的連線相關的所有屬性
 * establishSocket()建立與指定裝置連線用的BluetoothSocket
 * connect()建立連線
 * 最後即可用send()傳遞資料
 * Created by Jerry on 2017/3/23.
 */

public class BluetoothLedCubeController extends LedCubeController {

    private final static int REQUEST_ENABLE_BT = 100;
    private boolean isDeviceSupportBluetooth = false;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;

    public BluetoothLedCubeController() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)// Device does not support Bluetooth
            isDeviceSupportBluetooth = false;
        else isDeviceSupportBluetooth = true;
    }

    /**
     * 確認這台裝置是否支援藍芽
     *
     * @return
     */
    public boolean isDeviceSupportBluetooth() {
        return isDeviceSupportBluetooth;
    }

    /**
     * 清除和前一次連線相關的屬性
     */
    public void clean()
    {
        bluetoothSocket = null;
    }

    /**
     * 啟動手機中的藍芽
     *
     * @param act 呼叫者本身需要是一個Activity
     */
    public void open(Activity act) {
        if (isDeviceSupportBluetooth && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            act.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * 取得所有已配對的裝置的名稱
     *
     * @return 所有已配對的裝置的名稱組成的陣列
     */
    public String[] listBoundedDevicesName() {
        if (!isDeviceSupportBluetooth) return null;
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {     // There are paired devices. Get the name and address of each paired device.
            String[] names = new String[pairedDevices.size()];
            int i = 0;
            for (BluetoothDevice device : pairedDevices) {
                names[i] = device.getName();
                i++;
            }
            return names;
        }
        return null;
    }

    /**
     * 建立一個可以和目標裝置進行連線溝通的BluetoothSocket
     *
     * @param deviceName 目標裝置的名稱
     * @return 若成功則為true，失敗為false
     */
    public boolean establishSocket(String deviceName) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(deviceName)) {
                Log.d("mytest", "device found");
                if (bluetoothSocket != null)
                {
                    if(bluetoothSocket.getRemoteDevice().equals(device))
                        return true;
                    else
                        disconnect();
                }
                bluetoothSocket = establishSocket(device);
                break;
            }
        }
        if (bluetoothSocket == null) return false;
        else return true;
    }

    /**
     * 建立一個可以和目標裝置進行連線溝通的BluetoothSocket
     *
     * @param btd 目標裝置
     * @return 可以和目標裝置進行連線溝通的BluetoothSocket，如果建立失敗則回傳null
     */
    public BluetoothSocket establishSocket(BluetoothDevice btd) {
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//        UUID uuid = btd.getUuids()[0].getUuid();
        try {
            BluetoothSocket bts = btd.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            //BluetoothSocket bts = btd.createRfcommSocketToServiceRecord(uuid);
            return bts;
        } catch (Exception e) {
            Log.d("exception", "establishSocket error");
        }
        return null;
    }

    /**
     * 以目前此物件所持有的BluetoothSocket來建立連線
     *
     * @return 建立連線成功則true，失敗則false
     */
    public boolean connect() {
        if (bluetoothSocket != null) {
            if (bluetoothSocket.isConnected())
                return true;
            bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
                setOutputStream(bluetoothSocket.getOutputStream());
                setInputStream(bluetoothSocket.getInputStream());
                if (bluetoothSocket.isConnected())
                    return true;
            } catch (java.io.IOException e) {
                e.printStackTrace();
                Log.d("exeption", "connect failed");
            }
        }
        return false;
    }

    public void disconnect()
    {
        if(bluetoothSocket != null)
            try {
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public boolean isConnected()
    {
        try{
            return bluetoothSocket.isConnected();
        }catch (Exception e){
            return false;
        }
    }






}
