package com.android.factorytest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

public class CameraActivity extends TestItemBaseActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCamera;
    private SurfaceView surfaceView;
    private int mCamId = Camera.getNumberOfCameras() - 1; // default camera
    private final static int MSG_CHANGE_CAMERA = 0x00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.camera_layout);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                finish();
            }
        }

        surfaceView = (SurfaceView) findViewById(R.id.preview);
        surfaceView.getHolder().addCallback(this);
    }

    private void startCamera() {
        if (mCamera != null) {
            Log.d("llx", "start camera, already started. return");
            return;
        }
        if (mCamId > (Camera.getNumberOfCameras() - 1) || mCamId < 0) {
            Log.e("llx", "####### start camera failed, inviald params, camera No.="+ mCamId);
            return;
        }

        mCamera = Camera.open(mCamId);
        mCamera.setPreviewCallbackWithBuffer(this);
        try {
            mCamera.setPreviewDisplay(surfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    private void stopCamera() {
        if (mCamera != null) {
            // need to SET NULL CB before stop preview!!!
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
        mHandler.sendEmptyMessageDelayed(MSG_CHANGE_CAMERA,3000);
    }

    @Override
    protected void onPause() {
        mHandler.removeMessages(MSG_CHANGE_CAMERA);
        stopCamera();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("llx", "onBackPressed");
        finish();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(surfaceView.getHolder());
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHANGE_CAMERA:
                    if(Camera.getNumberOfCameras()>0) {
                        mCamId = (mCamId + 1) % Camera.getNumberOfCameras();
                        stopCamera();
                        startCamera();
                        mHandler.sendEmptyMessageDelayed(MSG_CHANGE_CAMERA, 3000);
                    }
                    break;
            }
        }
    };
}
