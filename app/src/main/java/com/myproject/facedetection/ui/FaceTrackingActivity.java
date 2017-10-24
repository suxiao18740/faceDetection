package com.myproject.facedetection.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.myproject.facedetection.entity.CameraManager;
import com.myproject.facedetection.R;
import com.myproject.facedetection.entity.CustomImageButton;
import com.opencvlib.ObjectDetector;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.io.IOException;
import java.util.ArrayList;

public class FaceTrackingActivity extends AppCompatActivity {
    private static final String TAG = CameraManager.class.getName();
    private ArrayList<ObjectDetector> mObjectDetects;
    private ObjectDetector mFaceDetector;
    private Mat grayscaleImage;
    private float absoluteFaceSize = 2.2F;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CameraManager cameraManager;
    private CustomImageButton cimbt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_tracking);
        cimbt = (CustomImageButton) findViewById(R.id.imbt_bitmap);
        cimbt.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        mObjectDetects = new ArrayList<>();
        mFaceDetector = new ObjectDetector(this, R.raw.haarcascade_frontalface_alt, 6, absoluteFaceSize, absoluteFaceSize, new Scalar(255, 0, 0, 255));
        mObjectDetects.add(mFaceDetector);


        //windowManager = (WindowManager) this.getSystemService(this.WINDOW_SERVICE);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //JavaCameraView mOpenCvCameraView = new JavaCameraView(this, -1);
        //setContentView(mOpenCvCameraView);
        //mOpenCvCameraView.setCvCameraViewListener(this);

        //mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
        //mOpenCvCameraView.setCvCameraViewListener(this);

        // And we are ready to go
        //mOpenCvCameraView.enableView();
        //mSurfaceView


        mSurfaceView = (SurfaceView) findViewById(R.id.java_surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();

        // mSurfaceView 不需要自己的缓冲区
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // mSurfaceView添加回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) { //SurfaceView创建
                try {
                    cameraManager = new CameraManager(FaceTrackingActivity.this, mObjectDetects, cimbt, mSurfaceHolder);
                    cameraManager.openDriver();
                    cameraManager.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { //SurfaceView销毁
                holder.removeCallback(this); // Camera is being used after Camera.release() was called
                cameraManager.stopPreview();
                cameraManager.closeDriver();

            }
        });

    }


    /**
     * 点击事件
     *
     * @param v
     */
    public void changeClick(View v) throws IOException {

        int bt_id = v.getId();
        switch (bt_id) {
            case R.id.changeText:
                cameraManager.changeCamera();
                break;

            default:
                break;
        }


    }


}
