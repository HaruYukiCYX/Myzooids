package com.example.sensor;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    private LayoutInflater inflater;

    public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> devices) {
        super(context, android.R.layout.simple_spinner_item, devices);
        inflater = LayoutInflater.from(context);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        BluetoothDevice device = getItem(position);
        TextView text = (TextView) view.findViewById(android.R.id.text1);
        
        if (device == null) {
            text.setText("选择设备");
        } else {
            text.setText(device.getName() != null ? device.getName() : device.getAddress());
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
        TextView text = (TextView) view.findViewById(android.R.id.text1);
        
        if (device == null) {
            text.setText("选择设备");
        } else {
            @SuppressLint("MissingPermission") String deviceName = device.getName();
            String deviceAddress = device.getAddress();
            text.setText((deviceName != null ? deviceName : "未知设备") + " (" + deviceAddress + ")");
        }
        
        return view;
    }
}