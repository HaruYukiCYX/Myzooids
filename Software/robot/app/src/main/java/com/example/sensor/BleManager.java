package com.example.sensor;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleManager {
    private static final String TAG = "BleManager";

    // UUIDs for your BLE service and characteristics (replace with your actual UUIDs)
    private static final UUID SERVICE_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    private static final UUID CHARACTERISTIC_UUID_RX = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
    private static final UUID CHARACTERISTIC_UUID_TX = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");

    private Context context;
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic txCharacteristic;
    private BluetoothGattCharacteristic rxCharacteristic;

    private boolean isScanning = false;
    private boolean isConnected = false;

    public interface BleDataListener {
        void onDataReceived(byte[] data);
        void onDeviceFound(BluetoothDevice device);
        void onConnectionStateChanged(boolean connected);
    }

    private BleDataListener dataListener;

    public BleManager(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    public void setBleDataListener(BleDataListener listener) {
        this.dataListener = listener;
    }

    public boolean isBluetoothAvailable() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    public void startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not enabled or not available");
            return;
        }

        if (bleScanner == null) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (bleScanner == null) {
            Log.e(TAG, "Failed to get BLE scanner");
            return;
        }

        if (isScanning) {
            stopScan();
        }

        // Optional: Add scan filters if you know the service UUID
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bleScanner.startScan(filters, settings, scanCallback);
        isScanning = true;
        Log.d(TAG, "BLE scan started");
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (bleScanner != null && isScanning) {
            bleScanner.stopScan(scanCallback);
            isScanning = false;
            Log.d(TAG, "BLE scan stopped");
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (device != null && dataListener != null) {
                handler.post(() -> dataListener.onDeviceFound(device));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "BLE scan failed with error: " + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    public boolean connectToDevice(BluetoothDevice device) {
        if (device == null) {
            Log.e(TAG, "Device is null");
            return false;
        }

        if (isConnected) {
            disconnect();
        }

        Log.d(TAG, "Connecting to device: " + device.getName());
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
        return true;
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server");
                isConnected = true;
                // Discover services
                bluetoothGatt.discoverServices();

                if (dataListener != null) {
                    handler.post(() -> dataListener.onConnectionStateChanged(true));
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server");
                isConnected = false;
                closeGatt();

                if (dataListener != null) {
                    handler.post(() -> dataListener.onConnectionStateChanged(false));
                }
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "服务发现完成");
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    txCharacteristic = service.getCharacteristic(CHARACTERISTIC_UUID_TX);
                    rxCharacteristic = service.getCharacteristic(CHARACTERISTIC_UUID_RX);

                    if (rxCharacteristic != null) {
                        // 为RX特征启用通知
                        gatt.setCharacteristicNotification(rxCharacteristic, true);

                        // 写入描述符以启用通知
                        BluetoothGattDescriptor descriptor = rxCharacteristic.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                } else {
                    Log.e(TAG, "未找到服务");
                }
            } else {
                Log.e(TAG, "服务发现失败: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if (characteristic.getUuid().equals(CHARACTERISTIC_UUID_RX)) {
                byte[] data = characteristic.getValue();
                if (data != null && data.length > 0 && dataListener != null) {
                    handler.post(() -> dataListener.onDataReceived(data));
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic written successfully");
            } else {
                Log.e(TAG, "Characteristic write failed: " + status);
            }
        }
    };

    @SuppressLint("MissingPermission")
    public void sendData(byte[] data) {
        if (!isConnected || txCharacteristic == null || bluetoothGatt == null) {
            Log.e(TAG, "Not connected or characteristic not found");
            return;
        }

        txCharacteristic.setValue(data);
        bluetoothGatt.writeCharacteristic(txCharacteristic);
    }

    @SuppressLint("MissingPermission")
    private void closeGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    @SuppressLint("MissingPermission")
    public String getConnectedDeviceName() {
        if (bluetoothGatt != null && bluetoothGatt.getDevice() != null) {
            return bluetoothGatt.getDevice().getName();
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    public BluetoothDevice getConnectedDevice() {
        return bluetoothGatt != null ? bluetoothGatt.getDevice() : null;
    }
}