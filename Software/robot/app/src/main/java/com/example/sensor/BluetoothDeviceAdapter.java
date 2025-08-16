package com.example.sensor;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    private final LayoutInflater inflater;

    public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> devices) {
        super(context, android.R.layout.simple_spinner_item, devices);
        inflater = LayoutInflater.from(context);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        BluetoothDevice device = getItem(position);
        TextView text = view.findViewById(android.R.id.text1);

        if (device == null) {
            text.setText("选择设备");
        } else {
            text.setText(getDeviceDisplayName(device));
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        BluetoothDevice device = getItem(position);
        TextView text = view.findViewById(android.R.id.text1);

        if (device == null) {
            text.setText("选择设备");
        } else {
            text.setText(getDeviceDisplayName(device) + " (" + device.getAddress() + ")");
        }

        return view;
    }

    /**
     * 获取设备显示名称（不需要BLUETOOTH_CONNECT权限）
     */
    private String getDeviceDisplayName(BluetoothDevice device) {
        // 在Android 12+上，getName()需要BLUETOOTH_CONNECT权限
        // 所以我们直接解析广播数据中的设备名称
        String name = parseNameFromAddress(device.getAddress());
        return name != null ? name : "未知设备";
    }

    /**
     * 从MAC地址解析设备名称（示例实现，实际应根据BLE广播数据解析）
     */
    private String parseNameFromAddress(String address) {
        // 这里应该是从你的BLE扫描结果中获取设备名称的逻辑
        // 示例：返回地址后几位作为显示名称
        if (address == null || address.length() < 5) return null;
        return "BLE-" + address.substring(address.length() - 5).replace(":", "");
    }

    /**
     * 添加设备并去重
     */
    public void addDevice(BluetoothDevice device) {
        if (!containsDevice(device)) {
            add(device);
        }
    }

    /**
     * 检查是否已包含该设备
     */
    private boolean containsDevice(BluetoothDevice newDevice) {
        if (newDevice == null) return false;

        String newAddress = newDevice.getAddress();
        for (int i = 0; i < getCount(); i++) {
            BluetoothDevice device = getItem(i);
            if (device != null && newAddress.equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }
}