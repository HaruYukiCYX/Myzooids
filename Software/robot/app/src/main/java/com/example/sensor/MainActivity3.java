package com.example.sensor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import androidx.camera.view.PreviewView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;

public class MainActivity3 extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final List<String> REQUIRED_PERMISSIONS = Arrays.asList(Manifest.permission.CAMERA);
    private static final String TAG = "MainActivity";
    private static final long THRESHOLD_ALERT_INTERVAL = 30000;

    private Spinner spinnerBluetoothDevices;
    private Button btnConnect;
    private TextView tvBluetoothStatus;
    private ImageView imgBluetoothStatus;

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private Handler handler = new Handler();

    private com.example.sensor.BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceAdapter deviceAdapter;

    // 通信协议常量
    private static final byte HEADER = (byte) 0xAA;
    private static final byte MOVE_COMMAND = 0x01;
    private static final byte DIRECTION_FORWARD = 0x01;
    private static final byte DIRECTION_BACKWARD = 0x02;
    private static final byte DIRECTION_LEFT = 0x03;
    private static final byte DIRECTION_RIGHT = 0x04;
    private static final byte DIRECTION_STOP = 0x05;

    private static final int PERMISSION_REQUEST_BLUETOOTH = 1;
    private static final String[] BLUETOOTH_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final String[] BLUETOOTH_PERMISSIONS_S = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    };

    private ActivityResultLauncher<Intent> enableBluetoothLauncher;

    private LinearLayout llFetch, llCamera, llNotification;
    private ImageButton btnUp, btnDown, btnLeft, btnRight;
    private View viewCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initViews();

        cameraExecutor = Executors.newSingleThreadExecutor();

        initBluetooth();

        registerActivityResultLaunchers();

        checkAndRequestPermissions();
    }

    private void initViews() {
        LinearLayout mainLayout = findViewById(R.id.main_container);

        View bluetoothHeader = LayoutInflater.from(this.peekAvailableContext()).inflate(R.layout.bluetooth_header, mainLayout, false);
        mainLayout.addView(bluetoothHeader, 0);

        spinnerBluetoothDevices = bluetoothHeader.findViewById(R.id.spinner_bluetooth_devices);
        btnConnect = bluetoothHeader.findViewById(R.id.btn_connect);
        tvBluetoothStatus = bluetoothHeader.findViewById(R.id.tv_bluetooth_status);
        imgBluetoothStatus = bluetoothHeader.findViewById(R.id.img_bluetooth_status);

        // 初始化方向控制和功能区控件
        llFetch = findViewById(R.id.ll_fetch);
        llCamera = findViewById(R.id.ll_camera);
        llNotification = findViewById(R.id.ll_notification);
        btnUp = findViewById(R.id.btn_up);
        btnDown = findViewById(R.id.btn_down);
        btnLeft = findViewById(R.id.btn_left);
        btnRight = findViewById(R.id.btn_right);
        viewCenter = findViewById(R.id.view_center);

        // 初始化相机预览控件和关闭按钮
        previewView = findViewById(R.id.previewView);
        ImageButton closeCameraBtn = findViewById(R.id.btn_close_camera);
        ScrollView mainScrollView = findViewById(R.id.main_scroll_view);

        // 关闭相机按钮点击事件
        closeCameraBtn.setOnClickListener(v -> {
            // 停止相机预览
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }
            // 隐藏相机预览，显示控制界面
            previewView.setVisibility(View.GONE);
            closeCameraBtn.setVisibility(View.GONE);
            mainScrollView.setVisibility(View.VISIBLE);
            llCamera.setVisibility(View.VISIBLE); // 重新显示"打开摄像头"按钮
        });

        // 修改打开相机的点击事件
        llCamera.setOnClickListener(v -> {
            openCamera();
            // 隐藏主滚动视图，显示关闭按钮
            mainScrollView.setVisibility(View.GONE);
            closeCameraBtn.setVisibility(View.VISIBLE);
        });


        // 设置功能区点击事件
        llFetch.setOnClickListener(v -> {
            updateFeatureSelection(llFetch);
            sendFeatureCommand((byte) 0x10); // 假设0x10是智能取物命令
        });

        llNotification.setOnClickListener(v -> {
            updateFeatureSelection(llNotification);
            sendFeatureCommand((byte) 0x12); // 假设0x12是智能提醒命令
        });

        // 设置方向控制按钮触摸事件
        setupDirectionControls();

    }

    private void openCamera() {
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS.toArray(new String[0]),
                    REQUEST_CODE_PERMISSIONS
            );
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // 配置预览
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 选择后置摄像头
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 解绑之前的所有绑定
                cameraProvider.unbindAll();

                // 绑定相机生命周期
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

                // 显示预览，隐藏按钮
                previewView.setVisibility(android.view.View.VISIBLE);
                findViewById(R.id.ll_camera).setVisibility(android.view.View.GONE);

            } catch (Exception e) {
                Toast.makeText(this, "相机启动失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void sendFeatureCommand(byte featureCommand) {
        if (!bluetoothManager.isConnected()) {
            Toast.makeText(this, "未连接蓝牙设备，无法发送设置", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建数据包 (假设使用不同的命令类型0x02表示功能命令)
        byte[] packet = new byte[4];
        packet[0] = HEADER;
        packet[1] = 0x02; // 功能命令类型
        packet[2] = featureCommand;
        packet[3] = (byte) (packet[0] ^ packet[1] ^ packet[2]); // 简单的XOR校验和
        try {
            // 发送数据包
            sendBluetoothData(packet);
        } catch (IOException e) {
            Log.d(TAG, "sendDirectionCommand: " + e.getMessage());
            return;
        }

        // 日志输出
        String featureString;
        switch (featureCommand) {
            case 0x10:
                featureString = "智能取物";
                break;
            case 0x11:
                featureString = "打开摄像头";
                break;
            case 0x12:
                featureString = "智能提醒";
                break;
            default:
                featureString = "未知功能";
                break;
        }
        Log.d(TAG, "发送功能命令: " + featureString);
    }

    private void updateFeatureSelection(LinearLayout selectedLayout) {
        // 重置所有功能区的背景
        llFetch.setBackgroundResource(R.drawable.bg_feature_item);
        llCamera.setBackgroundResource(R.drawable.bg_feature_item);
        llNotification.setBackgroundResource(R.drawable.bg_feature_item);

        // 设置被选中的功能区背景
        selectedLayout.setBackgroundResource(R.drawable.bg_feature_item_selected);
    }

    private void setupDirectionControls() {
        // 上按钮（前进）
        btnUp.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 按下按钮，发送前进命令
                sendDirectionCommand(DIRECTION_FORWARD);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // 释放按钮，发送停止命令
                sendDirectionCommand(DIRECTION_STOP);
            }
            return false;
        });

        // 下按钮（后退）
        btnDown.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 按下按钮，发送后退命令
                sendDirectionCommand(DIRECTION_BACKWARD);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // 释放按钮，发送停止命令
                sendDirectionCommand(DIRECTION_STOP);
            }
            return false;
        });

        // 左按钮（左转）
        btnLeft.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 按下按钮，发送左转命令
                sendDirectionCommand(DIRECTION_LEFT);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // 释放按钮，发送停止命令
                sendDirectionCommand(DIRECTION_STOP);
            }
            return false;
        });

        // 右按钮（右转）
        btnRight.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 按下按钮，发送右转命令
                sendDirectionCommand(DIRECTION_RIGHT);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // 释放按钮，发送停止命令
                sendDirectionCommand(DIRECTION_STOP);
            }
            return false;
        });

        // 中心按钮（停止）
        viewCenter.setOnClickListener(v -> sendDirectionCommand(DIRECTION_STOP));
    }

    private void sendDirectionCommand(byte direction) {
        if (!bluetoothManager.isConnected()) {
            Toast.makeText(this, "未连接蓝牙设备，无法发送设置", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建数据包
        byte[] packet = new byte[4];
        packet[0] = HEADER;
        packet[1] = MOVE_COMMAND;
        packet[2] = direction;
        packet[3] = (byte) (packet[0] ^ packet[1] ^ packet[2]); // 简单的XOR校验和

        // 发送数据包
        try {
            sendBluetoothData(packet);
        } catch (IOException e) {
            Log.d(TAG, "sendDirectionCommand: " + e.getMessage());
            return;
        }

        // 日志输出
        String directionString;
        switch (direction) {
            case DIRECTION_FORWARD:
                directionString = "前进";
                break;
            case DIRECTION_BACKWARD:
                directionString = "后退";
                break;
            case DIRECTION_LEFT:
                directionString = "左转";
                break;
            case DIRECTION_RIGHT:
                directionString = "右转";
                break;
            case DIRECTION_STOP:
                directionString = "停止";
                break;
            default:
                directionString = "未知";
                break;
        }
        Log.d(TAG, "发送方向命令: " + directionString);
    }

    private void registerActivityResultLaunchers() {
        enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        initBluetoothDevices();
                    } else {
                        Toast.makeText(this, "蓝牙功能未启用", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        for (String permission : BLUETOOTH_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            for (String permission : BLUETOOTH_PERMISSIONS_S) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_BLUETOOTH);
        } else {
            checkBluetoothEnabled();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                checkBluetoothEnabled();
            } else {
                Toast.makeText(this, "需要蓝牙权限才能使用此功能", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "权限被拒绝，无法使用相机",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkBluetoothEnabled() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBtIntent);
        } else if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            initBluetoothDevices();
        }
    }

    private void initBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.bluetooth.BluetoothManager bluetoothManager =
                    (android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        this.bluetoothManager = new com.example.sensor.BluetoothManager(this, handler);

        this.bluetoothManager.setBluetoothDataListener(new com.example.sensor.BluetoothManager.BluetoothDataListener() {
            @Override
            public void onDataReceived(String data) {
                processBluetoothData(data);
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToSelectedDevice();
            }
        });

        spinnerBluetoothDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initBluetoothDevices() {
        List<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();

        deviceAdapter = new BluetoothDeviceAdapter(this, pairedDevices);
        spinnerBluetoothDevices.setAdapter(deviceAdapter);

        updateBluetoothUI();
    }

    private void connectToSelectedDevice() {
        BluetoothDevice selectedDevice = (BluetoothDevice) spinnerBluetoothDevices.getSelectedItem();
        if (selectedDevice != null) {
            tvBluetoothStatus.setText("正在连接...");
            imgBluetoothStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_dark));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean success = bluetoothManager.connectToDevice(selectedDevice);

                    runOnUiThread(new Runnable() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void run() {
                            if (success) {
                                Toast.makeText(MainActivity3.this,
                                        "已连接到 " + selectedDevice.getName(),
                                        Toast.LENGTH_SHORT).show();

                                updateBluetoothUI();
                            } else {
                                Toast.makeText(MainActivity3.this,
                                        "连接失败，请重试",
                                        Toast.LENGTH_SHORT).show();

                                tvBluetoothStatus.setText("未连接");
                                imgBluetoothStatus.setColorFilter(ContextCompat.getColor(MainActivity3.this, android.R.color.darker_gray));
                                btnConnect.setText("连接");
                            }
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "请选择一个蓝牙设备", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBluetoothUI() {
        if (bluetoothManager.isConnected()) {
            tvBluetoothStatus.setText("已连接: " + bluetoothManager.getConnectedDeviceName());
            imgBluetoothStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            btnConnect.setText("断开");

            btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothManager.disconnect();

                    tvBluetoothStatus.setText("未连接");
                    imgBluetoothStatus.setColorFilter(ContextCompat.getColor(MainActivity3.this, android.R.color.darker_gray));
                    btnConnect.setText("连接");

                    btnConnect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connectToSelectedDevice();
                        }
                    });


                    Toast.makeText(MainActivity3.this, "已断开连接", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            tvBluetoothStatus.setText("未连接");
            imgBluetoothStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
            btnConnect.setText("连接");
        }
    }

    private void processBluetoothData(String data) {

    }

    // 发送蓝牙数据的辅助方法
    private void sendBluetoothData(byte[] data) throws IOException {
        if (bluetoothManager != null) {
            bluetoothManager.sendData(data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothManager != null && bluetoothManager.isConnected()) {
            bluetoothManager.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBluetoothUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (bluetoothManager != null) {
            bluetoothManager.disconnect();
        }
    }
}