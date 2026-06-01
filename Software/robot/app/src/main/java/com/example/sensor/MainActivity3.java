package com.example.sensor;

import static java.lang.System.loadLibrary;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
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

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;

public class MainActivity3 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity3";
    private static final int REQUEST_CODE_PERMISSIONS = 10;

    private static final byte HEADER = (byte) 0xAA;
    private static final byte MOVE_COMMAND = 0x01;
    private static final byte DIRECTION_FORWARD = 0x01;
    private static final byte DIRECTION_BACKWARD = 0x02;
    private static final byte DIRECTION_LEFT = 0x03;
    private static final byte DIRECTION_RIGHT = 0x04;
    private static final byte DIRECTION_STOP = 0x05;
    private static final byte FRAME_END = (byte) 0x55;

    public enum ControlMode { MANUAL, AUTO }
    private ControlMode currentMode = ControlMode.MANUAL;

    private double targetX = 0;
    private double targetY = 0;
    private float longPressRawX = 0;
    private float longPressRawY = 0;
    private boolean isControlActive = false;

    private int frameCounter = 0;
    private int lostFramesCounter = 0;

    // 记录最后一次成功识别目标时的距离
    private double lastKnownDistance = 9999.0;

    private PositionControlManager positionControlManager;
    private ArucoProcessor arucoProcessor;
    private BleManager bleManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceAdapter deviceAdapter;
    private TouchCoordinateCatcher coordinateCatcher;
    private Handler handler = new Handler();

    private Spinner spinnerBluetoothDevices;
    private Button btnConnect;
    private TextView tvBluetoothStatus, tvDistanceError;
    private ImageView imgBluetoothStatus;

    private JavaCameraView javaCameraView;

    private LinearLayout llCamera; // 移除了已经没有的 llFetch 和 llNotification 声明
    private ImageButton btnUp, btnDown, btnLeft, btnRight, closeCameraBtn;
    private View viewCenter;
    private ScrollView mainScrollView;

    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private static final int PERMISSION_REQUEST_BLUETOOTH = 1;

    static {
        try {
            System.loadLibrary("opencv_java3");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "OpenCV lib not found", e);
        }
    }

    private static final String[] REQUIRED_PERMISSIONS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
            new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA} :
            new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main2);

        arucoProcessor = new ArucoProcessor();
        initViews();
        initBluetooth();

        positionControlManager = new PositionControlManager(bleManager, this::getCurrentMode);
        positionControlManager.setControlInterval(50);

        registerActivityResultLaunchers();
        checkAndRequestPermissions();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && currentMode == ControlMode.AUTO) {
            hideSystemUI();
        }
    }

    private void initViews() {
        LinearLayout mainLayout = findViewById(R.id.main_container);
        View bluetoothHeader = LayoutInflater.from(this).inflate(R.layout.bluetooth_header, mainLayout, false);
        mainLayout.addView(bluetoothHeader, 0);

        spinnerBluetoothDevices = bluetoothHeader.findViewById(R.id.spinner_bluetooth_devices);
        btnConnect = bluetoothHeader.findViewById(R.id.btn_connect);
        tvBluetoothStatus = bluetoothHeader.findViewById(R.id.tv_bluetooth_status);
        imgBluetoothStatus = bluetoothHeader.findViewById(R.id.img_bluetooth_status);

        llCamera = findViewById(R.id.ll_camera);

        btnUp = findViewById(R.id.btn_up);
        btnDown = findViewById(R.id.btn_down);
        btnLeft = findViewById(R.id.btn_left);
        btnRight = findViewById(R.id.btn_right);
        viewCenter = findViewById(R.id.view_center);

        mainScrollView = findViewById(R.id.main_scroll_view);
        tvDistanceError = findViewById(R.id.tv_distance_error);
        closeCameraBtn = findViewById(R.id.btn_close_camera);

        javaCameraView = findViewById(R.id.camera_view);
        javaCameraView.setVisibility(SurfaceView.GONE);
        javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        javaCameraView.setMaxFrameSize(640, 480);
        javaCameraView.setCvCameraViewListener(this);

        javaCameraView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                longPressRawX = event.getRawX();
                longPressRawY = event.getRawY();
            }
            return false;
        });

        isControlActive = false;
        tvDistanceError.setVisibility(View.GONE);

        coordinateCatcher = new TouchCoordinateCatcher(this, (x, y) -> {
            targetX = x; targetY = y;
        });

        closeCameraBtn.setOnClickListener(v -> closeCameraLogic());
        llCamera.setOnClickListener(v -> startCamera());

        // 移除了多余 View 的监听器以及初始化默认选中方法调用
        if (llCamera != null) {
            llCamera.setBackgroundResource(R.drawable.bg_feature_item);
        }

        setupDirectionControls();
    }

    private void startCamera() {
        if (allPermissionsGranted()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            hideSystemUI();

            mainScrollView.setVisibility(View.GONE);
            closeCameraBtn.setVisibility(View.VISIBLE);
            javaCameraView.setVisibility(View.VISIBLE);
            javaCameraView.enableView();

            setCurrentMode(ControlMode.AUTO);
            tvDistanceError.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Auto Tracking Mode Enabled", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void closeCameraLogic() {
        if (positionControlManager != null) positionControlManager.stopControl();
        isControlActive = false;

        if (javaCameraView != null) {
            javaCameraView.disableView();
            javaCameraView.setVisibility(View.GONE);
        }

        closeCameraBtn.setVisibility(View.GONE);
        mainScrollView.setVisibility(View.VISIBLE);
        if (llCamera != null) llCamera.setVisibility(View.VISIBLE);
        tvDistanceError.setVisibility(View.GONE);

        setCurrentMode(ControlMode.MANUAL);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        showSystemUI();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        coordinateCatcher.setupForCameraView(javaCameraView, width, height);

        javaCameraView.setOnLongClickListener(v -> {
            coordinateCatcher.setLongPressCoordinate(longPressRawX, longPressRawY);
            handler.postDelayed(() -> {
                if (targetX == 0 && targetY == 0) return;
                if (positionControlManager != null) {
                    positionControlManager.stopControl();
                    positionControlManager.setTargetPosition(targetX, targetY);
                    isControlActive = true;
                    runOnUiThread(() -> {
                        tvDistanceError.setVisibility(View.VISIBLE);
                        if (bleManager != null && bleManager.isConnected()) {
                            positionControlManager.startControl();
                            Toast.makeText(this, "Tracking Started", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }, 100);
            return true;
        });
    }

    @Override
    public void onCameraViewStopped() {
        coordinateCatcher.clearCircle();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        if (frame == null || frame.empty()) return frame;

        if (arucoProcessor != null) {
            frameCounter++;

            // 每一帧都处理
            arucoProcessor.detect(frame);

            if (arucoProcessor.isDetected()) {
                lostFramesCounter = 0;

                Point center = arucoProcessor.getCenter();
                double angle = arucoProcessor.getAngleRad();

                if (positionControlManager != null) {
                    positionControlManager.updateCurrentPosition(center.x, center.y, angle);
                }

                if (isControlActive && currentMode == ControlMode.AUTO) {
                    // 记录最后一次看到的纯像素距离
                    lastKnownDistance = Math.sqrt(Math.pow(targetX - center.x, 2) + Math.pow(targetY - center.y, 2));
                    String info = String.format("Dist: %.1f px", lastKnownDistance);
                    runOnUiThread(() -> tvDistanceError.setText(info));
                }
            } else {
                lostFramesCounter++;

                if (isControlActive && currentMode == ControlMode.AUTO) {
                    // 盲刹机制：保留有效的 80 像素阈值
                    if (lastKnownDistance < 20.0) {
                        Log.i(TAG, "Near target and went blind! Executing BLIND BRAKE!");
                        if (positionControlManager != null) {
                            positionControlManager.stopControl();
                        }
                        runOnUiThread(() -> tvDistanceError.setText("Reached (Blind Brake)"));
                    }
                    else if (lostFramesCounter > 3) {
                        if (positionControlManager != null) {
                            positionControlManager.suspendControl();
                        }
                    }
                }
            }

            arucoProcessor.draw(frame);
        }

        return frame;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null) javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentMode == ControlMode.AUTO && javaCameraView != null) {
            javaCameraView.enableView();
            hideSystemUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleManager != null) bleManager.disconnect();
        if (javaCameraView != null) javaCameraView.disableView();
        if (arucoProcessor != null) arucoProcessor.release();
    }

    private ControlMode getCurrentMode() { return currentMode; }
    private void setCurrentMode(ControlMode mode) { this.currentMode = mode; }

    private boolean allPermissionsGranted() {
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    // 彻底重构了该方法，只保留了 llCamera 的状态防御，杜绝 NPE 崩溃
    private void updateFeatureSelection() {
        if (llCamera != null) {
            llCamera.setBackgroundResource(R.drawable.bg_feature_item_selected);
        }
    }

    private void setupDirectionControls() {
        btnUp.setOnTouchListener((v, e) -> handleDirectionTouch(e, DIRECTION_FORWARD));
        btnDown.setOnTouchListener((v, e) -> handleDirectionTouch(e, DIRECTION_BACKWARD));
        btnLeft.setOnTouchListener((v, e) -> handleDirectionTouch(e, DIRECTION_LEFT));
        btnRight.setOnTouchListener((v, e) -> handleDirectionTouch(e, DIRECTION_RIGHT));
        viewCenter.setOnClickListener(v -> { if (currentMode == ControlMode.MANUAL) sendDirectionCommand(DIRECTION_STOP); });
    }

    private boolean handleDirectionTouch(MotionEvent event, byte direction) {
        if (currentMode != ControlMode.MANUAL) return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) sendDirectionCommand(direction);
        else if (event.getAction() == MotionEvent.ACTION_UP) sendDirectionCommand(DIRECTION_STOP);
        return false;
    }

    private void sendDirectionCommand(byte direction) {
        if (!bleManager.isConnected()) return;
        byte[] packet = new byte[6];
        packet[0] = HEADER; packet[1] = MOVE_COMMAND; packet[2] = direction; packet[3] = 0x00;
        packet[4] = (byte) (packet[0] ^ packet[1] ^ packet[2] ^ packet[3]); packet[5] = FRAME_END;
        try {
            bleManager.sendData(packet);
        } catch (Exception e) {
            Log.e(TAG, "Cmd failed", e);
        }
    }

    private void registerActivityResultLaunchers() {
        enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { if (result.getResultCode() == RESULT_OK) startBleScan(); }
        );
    }

    private void checkAndRequestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_BLUETOOTH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH && grantResults.length > 0) checkBluetoothEnabled();
    }

    private void checkBluetoothEnabled() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            enableBluetoothLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        } else {
            startBleScan();
        }
    }

    private void initBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleManager = new BleManager(this, handler);
        bleManager.setBleDataListener(new BleManager.BleDataListener() {
            @Override public void onDataReceived(byte[] data) {}
            @Override public void onDeviceFound(BluetoothDevice device) {
                runOnUiThread(() -> { if (deviceAdapter != null) { deviceAdapter.addDevice(device); deviceAdapter.notifyDataSetChanged(); }});
            }
            @Override public void onConnectionStateChanged(boolean connected) {
                runOnUiThread(() -> { updateBluetoothUI(); if (!connected) { if (positionControlManager != null) positionControlManager.stopControl(); } });
            }
        });
        btnConnect.setOnClickListener(v -> connectToSelectedDevice());
        deviceAdapter = new BluetoothDeviceAdapter(this, new ArrayList<>());
        spinnerBluetoothDevices.setAdapter(deviceAdapter);
    }

    private void startBleScan() {
        if (allPermissionsGranted()) { bleManager.startScan(); handler.postDelayed(() -> bleManager.stopScan(), 10000); }
    }

    private void connectToSelectedDevice() {
        BluetoothDevice device = (BluetoothDevice) spinnerBluetoothDevices.getSelectedItem();
        if (device == null) return;
        new Thread(() -> {
            boolean success = bleManager.connectToDevice(device);
            runOnUiThread(() -> { if (success) updateBluetoothUI(); else Toast.makeText(this, "Connect Failed", Toast.LENGTH_SHORT).show(); });
        }).start();
    }

    private void updateBluetoothUI() {
        if (bleManager.isConnected()) {
            tvBluetoothStatus.setText("Connected");
            imgBluetoothStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            btnConnect.setText("Disconnect");
            btnConnect.setOnClickListener(v -> { bleManager.disconnect(); updateBluetoothUI(); });
        } else {
            tvBluetoothStatus.setText("Disconnected");
            imgBluetoothStatus.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
            btnConnect.setText("Connect");
            btnConnect.setOnClickListener(v -> connectToSelectedDevice());
        }
    }
}