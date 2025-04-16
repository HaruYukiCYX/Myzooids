package com.example.sensor;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {
    private static final String TAG = "BluetoothManager";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice connectedDevice;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Context context;
    private Handler handler;

    private Thread readThread;
    private boolean isConnected = false;
    private boolean stopReading = false;

    public interface BluetoothDataListener {
        void onDataReceived(String data);
    }

    private BluetoothDataListener dataListener;

    public BluetoothManager(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setBluetoothDataListener(BluetoothDataListener listener) {
        this.dataListener = listener;
    }

    public boolean isBluetoothAvailable() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public List<BluetoothDevice> getPairedDevices() {
        List<BluetoothDevice> deviceList = new ArrayList<>();
        if (bluetoothAdapter != null) {
            @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                deviceList.addAll(pairedDevices);
            }
        }
        return deviceList;
    }

    @SuppressLint("MissingPermission")
    public boolean connectToDevice(BluetoothDevice device) {
        if (device == null) {
            return false;
        }

        if (isConnected) {
            disconnect();
        }

        try {
            Log.d(TAG, "Attempting to connect to: " + device.getName());
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            connectedDevice = device;
            isConnected = true;

            startReadingData();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Connection failed: " + e.getMessage());
            try {
                bluetoothSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close socket", closeException);
            }
            return false;
        }
    }

    public void disconnect() {
        stopReading = true;
        if (readThread != null) {
            readThread.interrupt();
            readThread = null;
        }

        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing connection", e);
        }

        connectedDevice = null;
        isConnected = false;
    }

    private void startReadingData() {
        stopReading = false;
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;

                while (!stopReading) {
                    try {
                        // Read from the InputStream
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            final String data = new String(buffer, 0, bytes);
                            // Send the data to the listener on the main thread
                            if (dataListener != null) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        dataListener.onDataReceived(data);
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading data", e);
                        break;
                    }
                }
            }
        });
        readThread.start();
    }

    // 添加发送数据的方法
    public void sendData(String data) throws IOException {
        if (!isConnected || outputStream == null) {
            throw new IOException("蓝牙未连接，无法发送数据");
        }

        try {
            // 将字符串转换为字节数组并发送
            byte[] bytes = data.getBytes();
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error sending data: " + e.getMessage());
            throw e;
        }
    }

    public void sendData(byte[] data) throws IOException {
        if (!isConnected || outputStream == null) {
            throw new IOException("蓝牙未连接，无法发送数据");
        }

        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error sending data: " + e.getMessage());
            throw e;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    @SuppressLint("MissingPermission")
    public String getConnectedDeviceName() {
        if (connectedDevice != null) {
            return connectedDevice.getName();
        }
        return null;
    }

    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }
}