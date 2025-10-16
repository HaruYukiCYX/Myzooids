package com.example.sensor;

import static java.lang.System.loadLibrary;

import android.Manifest;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import org.opencv.core.Point;

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
    private static final byte POSITIONING_COMMAND = 0x02;
    private static final byte DIRECTION_FORWARD = 0x01;
    private static final byte DIRECTION_BACKWARD = 0x02;
    private static final byte DIRECTION_LEFT = 0x03;
    private static final byte DIRECTION_RIGHT = 0x04;
    private static final byte DIRECTION_STOP = 0x05;

    private double targetX = 0;
    private double targetY = 0;

    private float longPressRawX = 0;
    private float longPressRawY = 0;
    // UI组件（保持不变）

    private PositionControlManager positionControlManager;
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

        positionControlManager = new PositionControlManager(bleManager);
        positionControlManager.setControlInterval(50); // 50ms控制周期
        positionControlManager.setErrorTolerance(0.5); // 0.5cm误差容忍度
        positionControlManager.setPidParameters(1.2f, 0.1f, 0.2f); // PID参数

        registerActivityResultLaunchers();
        checkAndRequestPermissions();
    }
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

        javaCameraView.setOnTouchListener((v, event) -> {
            // 仅在手指按下时记录屏幕绝对坐标（长按的起始点）
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                longPressRawX = event.getRawX(); // 屏幕绝对X坐标（左上角为0）
                longPressRawY = event.getRawY(); // 屏幕绝对Y坐标
                Log.d(TAG, "长按屏幕坐标：RawX=" + longPressRawX + ", RawY=" + longPressRawY);
            }
            return false; // 不拦截触摸事件，确保长按事件能正常触发
        });

        coordinateCatcher = new TouchCoordinateCatcher(this, (opencvImageX, opencvImageY) -> {
            Log.d(TAG, "统一后的OpenCV坐标：X=" + opencvImageX + ", Y=" + opencvImageY);
            targetX = opencvImageX;
            targetY = opencvImageY;
        });

        // 关闭相机按钮点击事件
        closeCameraBtn.setOnClickListener(v -> {
            if (javaCameraView != null) {
                javaCameraView.disableView();
                javaCameraView.setVisibility(View.GONE);
            }
            coordinateCatcher.clearCircle();
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
            //sendFeatureCommand((byte) 0x10);
        });
        llNotification.setOnClickListener(v -> {
            updateFeatureSelection(llNotification);
            //sendFeatureCommand((byte) 0x12);
        });
        setupDirectionControls();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        coordinateCatcher.setupForCameraView(javaCameraView, width, height);
        Log.d(TAG, "OpenCV图像分辨率：" + width + "x" + height);
        Log.d(TAG, "相机视图显示尺寸：" + javaCameraView.getWidth() + "x" + javaCameraView.getHeight());
        javaCameraView.setOnLongClickListener(v -> {
            // 第一步：调用坐标捕捉器，传递长按的屏幕坐标，触发转换
            coordinateCatcher.setLongPressCoordinate(longPressRawX, longPressRawY);

            // 第二步：等待坐标更新（短暂延迟，确保转换完成）
            handler.postDelayed(() -> {
                // 检查坐标是否有效（非0，排除初始值）
                if (targetX == 0 && targetY == 0) {
                    Toast.makeText(MainActivity3.this, "坐标获取失败，请重新长按", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 第三步：停止当前控制，设置新目标，启动PID
                if (positionControlManager.isControlling()) {
                    positionControlManager.stopControl();
                }
                positionControlManager.setTargetPosition(targetX, targetY);
                Toast.makeText(MainActivity3.this,
                        "目标位置已设置: (" + (int) targetX + ", " + (int) targetY + ")",
                        Toast.LENGTH_SHORT).show();

                // 第四步：蓝牙连接正常则启动控制
                if (bleManager != null && bleManager.isConnected()) {
                    positionControlManager.startControl();
                } else {
                    Toast.makeText(this, "请先连接蓝牙设备", Toast.LENGTH_SHORT).show();
                }
            }, 50); // 50ms延迟，确保坐标转换完成（可根据实际情况调整）

            return true; // 消费长按事件，避免重复触发
        });
    }

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

//    private void sendFeatureCommand(byte featureCommand) {
//        if (!bleManager.isConnected()) {
//            Toast.makeText(this, "未连接蓝牙设备，无法发送设置", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        byte[] packet = new byte[4];
//        packet[0] = HEADER;
//        packet[1] = 0x02;
//        packet[2] = featureCommand;
//        packet[3] = (byte) (packet[0] ^ packet[1] ^ packet[2]);
//        try {
//            sendBluetoothData(packet);
//        } catch (IOException e) {
//            Log.d(TAG, "sendDirectionCommand: " + e.getMessage());
//            return;
//        }
//        String featureString;
//        switch (featureCommand) {
//            case 0x10:
//                featureString = "智能取物";
//                break;
//            case 0x11:
//                featureString = "打开摄像头";
//                break;
//            case 0x12:
//                featureString = "智能提醒";
//                break;
//            default:
//                featureString = "未知功能";
//                break;
//        }
//        Log.d(TAG, "发送功能命令: " + featureString);
//    }

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
        // 1. 获取相机原始帧（RGBA格式）
        Mat frame = inputFrame.rgba();
        if (frame == null || frame.empty()) {
            Log.e(TAG, "onCameraFrame: 相机帧为空，跳过处理");
            return frame;
        }

        // 2. 调用ArucoProcessor处理帧（内部会检测标记并更新中心点坐标）
        Mat processedFrame = arucoProcessor.processFrame(frame);

        if (arucoProcessor != null) {
            // 3.1 判断是否检测到Aruco标记
            if (arucoProcessor.isMarkerDetected()) {
                // 3.2 获取标记中心点坐标（OpenCV图像坐标系，与目标位置坐标系一致）
                Point currentMarkerCenter = arucoProcessor.getLatestMarkerCenter();
                double currentX = currentMarkerCenter.x;
                double currentY = currentMarkerCenter.y;

                // 打印日志，方便调试坐标是否正常
                Log.d(TAG, "onCameraFrame: 检测到Aruco标记，当前位置 → X=" + currentX + ", Y=" + currentY);

                // 3.3 将当前位置传递给PositionControlManager（PID控制核心）
                if (positionControlManager != null) {
                    positionControlManager.updateCurrentPosition(currentX, currentY);
                } else {
                    Log.w(TAG, "onCameraFrame: PositionControlManager未初始化，无法传递当前位置");
                }
            } else {
                // 3.4 未检测到标记时，停止PID控制（避免无效计算导致设备失控）
                Log.w(TAG, "onCameraFrame: 未检测到Aruco标记");
                if (positionControlManager != null && positionControlManager.isControlling()) {
                    positionControlManager.stopControl();
                    // 在UI线程显示提示（避免跨线程操作UI）
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity3.this,
                                "未检测到Aruco标记，已停止电机控制",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        } else {
            Log.e(TAG, "onCameraFrame: ArucoProcessor未初始化，无法获取当前位置");
        }

        // 4. 返回处理后的帧（含Aruco标记绘制效果），用于相机预览显示
        return processedFrame;
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