package com.example.sensor;

import static java.lang.System.loadLibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class MainActivity3 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "MainActivity3";
    private static final String[] REQUIRED_PERMISSIONS = getRequiredPermissions();
    private static final int REQUEST_CODE_PERMISSIONS = 10;

    // BLE相关常量（保持不变）
    private static final UUID SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB");
    private static final UUID CHARACTERISTIC_UUID_TX = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB");
    private static final UUID CHARACTERISTIC_UUID_RX = UUID.fromString("00002A38-0000-1000-8000-00805F9B34FB");
    private static final byte HEADER = (byte) 0xAA;
    private static final byte MOVE_COMMAND = 0x01;
    private static final byte DIRECTION_FORWARD = 0x01;
    private static final byte DIRECTION_BACKWARD = 0x02;
    private static final byte DIRECTION_LEFT = 0x03;
    private static final byte DIRECTION_RIGHT = 0x04;
    private static final byte DIRECTION_STOP = 0x05;

    // UI组件（保持不变）
    private Spinner spinnerBluetoothDevices;
    private Button btnConnect;
    private TextView tvBluetoothStatus;
    private ImageView imgBluetoothStatus;
    private JavaCameraView javaCameraView;
    private LinearLayout llFetch, llCamera, llNotification;
    private ImageButton btnUp, btnDown, btnLeft, btnRight, closeCameraBtn;
    private View viewCenter;
    private ScrollView mainScrollView;

    // 核心对象（新增/修改）
    private BleManager bleManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceAdapter deviceAdapter;
    private TouchCoordinateCatcher coordinateCatcher; // 坐标捕捉器（关键）
    private Handler handler = new Handler();
    private ArucoProcessor arucoProcessor;
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
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

    static {
        loadLibrary("opencv_java3");
    }

    private static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA
            };
        } else {
            return new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA
            };
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initViews(); // 初始化UI时创建坐标捕捉器
        arucoProcessor = new ArucoProcessor();
        initBluetooth();
        registerActivityResultLaunchers();
        checkAndRequestPermissions();
    }

    // --------------------------
    // 关键修改1：初始化UI时创建TouchCoordinateCatcher
    // --------------------------
    private void initViews() {
        LinearLayout mainLayout = findViewById(R.id.main_container);

        // 加载蓝牙头部布局（保持不变）
        View bluetoothHeader = LayoutInflater.from(this.peekAvailableContext()).inflate(R.layout.bluetooth_header, mainLayout, false);
        mainLayout.addView(bluetoothHeader, 0);
        spinnerBluetoothDevices = bluetoothHeader.findViewById(R.id.spinner_bluetooth_devices);
        btnConnect = bluetoothHeader.findViewById(R.id.btn_connect);
        tvBluetoothStatus = bluetoothHeader.findViewById(R.id.tv_bluetooth_status);
        imgBluetoothStatus = bluetoothHeader.findViewById(R.id.img_bluetooth_status);

        // 初始化相机相关UI（关键：绑定关闭按钮和滚动视图）
        llFetch = findViewById(R.id.ll_fetch);
        llCamera = findViewById(R.id.ll_camera);
        llNotification = findViewById(R.id.ll_notification);
        btnUp = findViewById(R.id.btn_up);
        btnDown = findViewById(R.id.btn_down);
        btnLeft = findViewById(R.id.btn_left);
        btnRight = findViewById(R.id.btn_right);
        viewCenter = findViewById(R.id.view_center);
        javaCameraView = findViewById(R.id.camera_view);
        closeCameraBtn = findViewById(R.id.btn_close_camera);
        mainScrollView = findViewById(R.id.main_scroll_view);

        // 相机初始状态：隐藏
        javaCameraView.setVisibility(SurfaceView.GONE);
        javaCameraView.setCvCameraViewListener(this);

        // --------------------------
        // 核心：创建坐标捕捉器 + 设置回调（获取OpenCV坐标）
        // --------------------------
        coordinateCatcher = new TouchCoordinateCatcher(this, (opencvImageX, opencvImageY) -> {
            // 这里获取的是与Aruco检测统一的OpenCV图像坐标系坐标
            Log.d(TAG, "统一后的OpenCV坐标：X=" + opencvImageX + ", Y=" + opencvImageY);
            // 后续业务逻辑：如根据坐标控制机器人、匹配Aruco标记等
            // 示例：sendBluetoothCommandByCoordinate(opencvImageX, opencvImageY);
        });

        // 关闭相机按钮点击事件（保持不变，新增清除圆圈逻辑）
        closeCameraBtn.setOnClickListener(v -> {
            if (javaCameraView != null) {
                javaCameraView.disableView();
                javaCameraView.setVisibility(View.GONE);
            }
            coordinateCatcher.clearCircle(); // 关闭相机时清除绘制的圆圈
            javaCameraView.setVisibility(View.GONE);
            closeCameraBtn.setVisibility(View.GONE);
            mainScrollView.setVisibility(View.VISIBLE);
            llCamera.setVisibility(View.VISIBLE);
        });

        // 打开相机按钮点击事件（保持不变）
        llCamera.setOnClickListener(v -> {
            startCamera();
            mainScrollView.setVisibility(View.GONE);
            closeCameraBtn.setVisibility(View.VISIBLE);
        });

        // 功能区和方向控制（保持不变）
        updateFeatureSelection(llFetch);
        llFetch.setOnClickListener(v -> {
            updateFeatureSelection(llFetch);
            sendFeatureCommand((byte) 0x10);
        });
        llNotification.setOnClickListener(v -> {
            updateFeatureSelection(llNotification);
            sendFeatureCommand((byte) 0x12);
        });
        setupDirectionControls();
    }

    // --------------------------
    // 关键修改2：修复onCameraViewStarted参数（传入OpenCV图像分辨率）
    // --------------------------
    @Override
    public void onCameraViewStarted(int width, int height) {
        // 关键：width/height 是OpenCV图像分辨率（相机输出帧尺寸，与ArucoProcessor.processFrame的Mat尺寸一致）
        // 之前错误：传入javaCameraView.getWidth()（显示尺寸），现在改为OpenCV图像分辨率
        coordinateCatcher.setupForCameraView(javaCameraView, width, height);
        Log.d(TAG, "OpenCV图像分辨率：" + width + "x" + height);
        Log.d(TAG, "相机视图显示尺寸：" + javaCameraView.getWidth() + "x" + javaCameraView.getHeight());
    }

    // --------------------------
    // 其他方法保持不变，仅补充相机生命周期的坐标捕捉器管理
    // --------------------------
    private void startCamera() {
        if (allPermissionsGranted()) {
            javaCameraView.setVisibility(View.VISIBLE);
            javaCameraView.enableView();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void sendFeatureCommand(byte featureCommand) {
        if (!bleManager.isConnected()) {
            Toast.makeText(this, "未连接蓝牙设备，无法发送设置", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] packet = new byte[4];
        packet[0] = HEADER;
        packet[1] = 0x02;
        packet[2] = featureCommand;
        packet[3] = (byte) (packet[0] ^ packet[1] ^ packet[2]);
        try {
            sendBluetoothData(packet);
        } catch (IOException e) {
            Log.d(TAG, "sendDirectionCommand: " + e.getMessage());
            return;
        }
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
        llFetch.setBackgroundResource(R.drawable.bg_feature_item);
        llCamera.setBackgroundResource(R.drawable.bg_feature_item);
        llNotification.setBackgroundResource(R.drawable.bg_feature_item);
        selectedLayout.setBackgroundResource(R.drawable.bg_feature_item_selected);
    }

    private void setupDirectionControls() {
        btnUp.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) sendDirectionCommand(DIRECTION_FORWARD);
            else if (event.getAction() == MotionEvent.ACTION_UP) sendDirectionCommand(DIRECTION_STOP);
            return false;
        });
        btnDown.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) sendDirectionCommand(DIRECTION_BACKWARD);
            else if (event.getAction() == MotionEvent.ACTION_UP) sendDirectionCommand(DIRECTION_STOP);
            return false;
        });
        btnLeft.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) sendDirectionCommand(DIRECTION_LEFT);
            else if (event.getAction() == MotionEvent.ACTION_UP) sendDirectionCommand(DIRECTION_STOP);
            return false;
        });
        btnRight.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) sendDirectionCommand(DIRECTION_RIGHT);
            else if (event.getAction() == MotionEvent.ACTION_UP) sendDirectionCommand(DIRECTION_STOP);
            return false;
        });
        viewCenter.setOnClickListener(v -> sendDirectionCommand(DIRECTION_STOP));
    }

    private void sendDirectionCommand(byte direction) {
        if (!bleManager.isConnected()) {
            Toast.makeText(this, "未连接蓝牙设备，无法发送设置", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] packet = new byte[4];
        packet[0] = HEADER;
        packet[1] = MOVE_COMMAND;
        packet[2] = direction;
        packet[3] = (byte) (packet[0] ^ packet[1] ^ packet[2]);
        try {
            sendBluetoothData(packet);
        } catch (IOException e) {
            Log.d(TAG, "sendDirectionCommand: " + e.getMessage());
            return;
        }
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
                    if (result.getResultCode() == RESULT_OK) startBleScan();
                    else Toast.makeText(this, "蓝牙功能未启用", Toast.LENGTH_SHORT).show();
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
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_BLUETOOTH);
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
            if (allGranted) checkBluetoothEnabled();
            else Toast.makeText(this, "需要蓝牙权限才能使用此功能", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) startCamera();
            else Toast.makeText(this, "权限被拒绝，无法使用相机", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkBluetoothEnabled() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBtIntent);
        } else if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            startBleScan();
        }
    }

    private void initBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleManager = new BleManager(this, handler);
        bleManager.setBleDataListener(new BleManager.BleDataListener() {
            @Override
            public void onDataReceived(byte[] data) {
                processBluetoothData(Arrays.toString(data));
            }

            @Override
            public void onDeviceFound(BluetoothDevice device) {
                runOnUiThread(() -> {
                    if (deviceAdapter != null) {
                        deviceAdapter.addDevice(device);
                        deviceAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onConnectionStateChanged(boolean connected) {
                runOnUiThread(MainActivity3.this::updateBluetoothUI);
            }
        });
        btnConnect.setOnClickListener(v -> connectToSelectedDevice());
        deviceAdapter = new BluetoothDeviceAdapter(this, new ArrayList<>());
        spinnerBluetoothDevices.setAdapter(deviceAdapter);
    }

    private void startBleScan() {
        if (allPermissionsGranted()) {
            bleManager.startScan();
            handler.postDelayed(() -> bleManager.stopScan(), 10000);
        }
    }

    private void connectToSelectedDevice() {
        BluetoothDevice selectedDevice = (BluetoothDevice) spinnerBluetoothDevices.getSelectedItem();
        if (selectedDevice != null) {
            tvBluetoothStatus.setText("正在连接...");
            imgBluetoothStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            new Thread(() -> {
                boolean success = bleManager.connectToDevice(selectedDevice);
                runOnUiThread(() -> {
                    if (success) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Toast.makeText(MainActivity3.this, "已连接到 " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();
                        updateBluetoothUI();
                    } else {
                        Toast.makeText(MainActivity3.this, "连接失败，请重试", Toast.LENGTH_SHORT).show();
                        tvBluetoothStatus.setText("未连接");
                        imgBluetoothStatus.setColorFilter(ContextCompat.getColor(MainActivity3.this, android.R.color.darker_gray));
                        btnConnect.setText("连接");
                    }
                });
            }).start();
        } else {
            Toast.makeText(this, "请选择一个蓝牙设备", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBluetoothUI() {
        if (bleManager.isConnected()) {
            tvBluetoothStatus.setText("已连接: " + bleManager.getConnectedDeviceName());
            imgBluetoothStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            btnConnect.setText("断开");
            btnConnect.setOnClickListener(v -> {
                bleManager.disconnect();
                tvBluetoothStatus.setText("未连接");
                imgBluetoothStatus.setColorFilter(ContextCompat.getColor(MainActivity3.this, android.R.color.darker_gray));
                btnConnect.setText("连接");
                btnConnect.setOnClickListener(v1 -> connectToSelectedDevice());
                Toast.makeText(MainActivity3.this, "已断开连接", Toast.LENGTH_SHORT).show();
            });
        } else {
            tvBluetoothStatus.setText("未连接");
            imgBluetoothStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
            btnConnect.setText("连接");
        }
    }

    private void processBluetoothData(String data) {}

    private void sendBluetoothData(byte[] data) throws IOException {
        if (bleManager != null) bleManager.sendData(data);
    }




    @Override
    public void onCameraViewStopped() {
        coordinateCatcher.clearCircle(); // 相机停止时清除圆圈
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        return arucoProcessor.processFrame(frame); // Aruco检测（与OpenCV坐标统一）
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bleManager != null && bleManager.isConnected()) bleManager.disconnect();
        if (javaCameraView != null) javaCameraView.disableView();
        coordinateCatcher.clearCircle(); // 暂停时清除圆圈
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBluetoothUI();
        if (javaCameraView != null && javaCameraView.getVisibility() == View.VISIBLE) {
            javaCameraView.enableView(); // 恢复相机时重新启用
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleManager != null) bleManager.disconnect();
        if (javaCameraView != null) javaCameraView.disableView();
    }
}