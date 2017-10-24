package com.myproject.facedetection.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;


import com.opencvlib.ObjectDetector;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.myproject.facedetection.common.myUtils.ByteToBitmap;

/**
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-09-05
 * Time: 10:56
 */
public class CameraManager implements Camera.PreviewCallback {
    private static final String TAG = CameraManager.class.getName();
    private Camera camera;
    private Camera.Parameters parameters;
    private AutoFocusManager autoFocusManager;
    private int requestedCameraId = -1; // 默认打开后置摄像头
    private int cameraPosition;//0代表前置摄像头，1代表后置摄像头
    private int numCameras = Camera.getNumberOfCameras(); // 初始化摄像头数量
    private Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    private Context mContext;

    private boolean initialized;
    private boolean previewing;
    private ArrayList<ObjectDetector> mObjectDetects;
    private CustomImageButton cimbt;
    private SurfaceHolder holder;

    /**
     * 打开摄像头
     *
     * @param cameraId 摄像头id
     * @return Camera
     */
    public Camera open(int cameraId) {
        if (numCameras <= 0) {
            Log.e(TAG, "No cameras!");
            return null;
        }

        // 如果 cameraId 在 [0-numCameras) 之间则打开cameraId 否则默认优先打开后置
        boolean explicitRequest = (cameraId >= 0 && cameraId < numCameras);

        int index = 0;
        if (!explicitRequest) {
            // Select a camera if no explicit camera requested
            while (index < numCameras) {
                Camera.getCameraInfo(index, cameraInfo);
                cameraId = index;
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraPosition = 1;
                    break;
                }
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraPosition = 0;
                    continue;
                }

                index++;
            }
        }

        // 不在 [0-numCameras) 之间 且 没有默认打开的
        Camera camera;
        if (index < numCameras) {
            Log.e(TAG, "Opening camera #" + cameraId);
            camera = Camera.open(cameraId);
        } else {
            Log.e(TAG, "Requested camera does not exist: " + cameraId);
            camera = null;
        }


        int rotation = getDisplayOrientation();
        camera.setDisplayOrientation(rotation);
        camera.setPreviewCallback(this);
        //camera.setOneShotPreviewCallback(this); // 激活 onPreviewFrame 执行一次
        return camera;
    }

    public int getDisplayOrientation() {
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);
        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    public CameraManager(Context context, ArrayList<ObjectDetector> mObjectDetects, CustomImageButton cimbt, SurfaceHolder holder) {
        this.mContext = context;
        this.mObjectDetects = mObjectDetects;
        this.cimbt = cimbt;
        this.holder = holder;
    }


    /**
     * 打开camera
     *
     * @throws IOException IOException
     */
    public synchronized void openDriver()
            throws IOException {
        Log.e(TAG, "openDriver");
        Camera theCamera = camera;
        if (theCamera == null) {
            theCamera = open(requestedCameraId);
            if (theCamera == null) {
                throw new IOException();
            }
            camera = theCamera;
        }
        theCamera.setPreviewDisplay(holder);

        if (!initialized) {
            initialized = true;
            parameters = camera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

            int w = 800;
            int h = 600;
            for (Camera.Size size : previewSizes) {
                Log.e("TAG", "previewSizes width:" + size.width);
                Log.e("TAG", "previewSizes height:" + size.height);
                if (size.width - w <= 200 & size.width >= w) {
                    w = size.width;
                    h = size.height;
                    break;
                }
            }
            Log.e("w*h", String.format("%d*%d", w, h));
            parameters.setPreviewSize(w, h);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);
            parameters.setPictureSize(800, 600);
            theCamera.setParameters(parameters);
        }
    }


    /*
        切换摄像头
     */
    public synchronized void changeCamera() throws IOException {
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            //现在是后置，变更为前置
            if (cameraPosition == 1) {
                //代表摄像头的方位  CAMERA_FACING_FRONT前置 CAMERA_FACING_BACK后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    stopPreview();
                    closeDriver();

                    requestedCameraId = i;
                    initialized = false;

                    openDriver();
                    startPreview();
                    cameraPosition = 0;
                    break;
                }
            } else {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    stopPreview();
                    closeDriver();

                    requestedCameraId = i;
                    initialized = false;

                    openDriver();
                    startPreview();
                    cameraPosition = 1;
                    break;
                }
            }

        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();

        Bitmap bitmap = ByteToBitmap(bytes, previewSize);
        //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);//将data byte型数组转换成bitmap文件

        final Matrix matrix = new Matrix();//转换成矩阵旋转90度
        if (cameraPosition == 1) {
            matrix.setRotate(90);
        } else {
            matrix.setRotate(-90);
        }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);//旋转图片


        Mat grayscaleImage = new Mat(previewSize.height, previewSize.width, CvType.CV_8UC4);
        int absoluteFaceSize = (int) (previewSize.height * 0.2);

        if (bitmap != null) {
            Mat inputFrame = new Mat();
            Utils.bitmapToMat(bitmap, inputFrame);

            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }

            // Create a grayscale image
            Imgproc.cvtColor(inputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);

            MatOfRect mRect = new MatOfRect();


            int maxRectArea = 0 * 0;
            Rect maxRect = null;

            int facenum = 0;

            for (ObjectDetector detector : mObjectDetects) {
                // 检测目标
                Rect[] object = detector.detectObjectImage(inputFrame, mRect);
                Log.e(TAG, object.length + "");

                for (Rect rect : object) {
                    ++facenum;
                    // 找出最大的面积
                    int tmp = rect.width * rect.height;
                    if (tmp >= maxRectArea) {
                        maxRectArea = tmp;
                        maxRect = rect;
                    }
                }
            }

            Bitmap rectBitmap = null;
            if (facenum != 0) {
                // 剪切最大的头像
                //Log.e("剪切的长宽", String.format("高:%s,宽:%s", maxRect.width, maxRect.height));
                Rect rect = new Rect(maxRect.x, maxRect.y, maxRect.width, maxRect.height);
                Mat rectMat = new Mat(inputFrame, rect);  // 从原始图像拿
                rectBitmap = Bitmap.createBitmap(rectMat.cols(), rectMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(rectMat, rectBitmap);

                Bitmap resizeBmp = cimbt.resizeBitmap(rectBitmap, cimbt.getWidth(), cimbt.getHeight());
                cimbt.setBitmap(resizeBmp);
            } else {
                cimbt.clearnImage();
                cimbt.setText("没有检测到人脸");
            }
        }

    }


    public interface OnObjectTrackingInterface {
        // 传输camera的字节流
        void onCameraByteStream(byte[] bytes, Camera camera);

    }

    /**
     * camera是否打开
     *
     * @return camera是否打开
     */

    public synchronized boolean isOpen() {
        return camera != null;
    }

    /**
     * 关闭camera
     */
    public synchronized void closeDriver() {
        Log.e(TAG, "closeDriver");
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * 开始预览
     */
    public synchronized void startPreview() {
        Log.e(TAG, "startPreview");
        Camera theCamera = camera;
        if (theCamera != null && !previewing) {
            theCamera.startPreview();
            previewing = true;
            autoFocusManager = new AutoFocusManager(camera);
        }
    }

    /**
     * 关闭预览
     */
    public synchronized void stopPreview() {
        Log.e(TAG, "stopPreview");
        if (autoFocusManager != null) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }
        if (camera != null && previewing) {
            camera.stopPreview();
            camera.setPreviewCallback(null);  // Camera is being used after Camera.release() was called
            previewing = false;
        }
    }


    /**
     * 打开闪光灯
     */
    public synchronized void openLight() {
        Log.e(TAG, "openLight");
        if (camera != null) {
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
        }
    }

    /**
     * 关闭闪光灯
     */
    public synchronized void offLight() {
        Log.e(TAG, "offLight");
        if (camera != null) {
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
        }
    }

    /**
     * 拍照
     *
     * @param shutter ShutterCallback
     * @param raw     PictureCallback
     * @param jpeg    PictureCallback
     */
    public synchronized void takePicture(final Camera.ShutterCallback shutter, final Camera.PictureCallback raw,
                                         final Camera.PictureCallback jpeg) {
        camera.takePicture(shutter, raw, jpeg);
    }

    private Bitmap reSize(byte[] data) {
        Log.i(TAG, "myJpegCallback:onPictureTaken...");
        Bitmap cutMap = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
        //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。图片竟然不能旋转了，故这里要旋转下
        Matrix matrix = new Matrix();
        matrix.postRotate((float) 90.0);
        Bitmap rotaBitmap = Bitmap.createBitmap(cutMap, 0, 0, cutMap.getWidth(), cutMap.getHeight(), matrix, false);

        //旋转后rotaBitmap是960×1280.预览surfaview的大小是540×800
        //将960×1280缩放到540×800
        Bitmap sizeBitmap = Bitmap.createScaledBitmap(rotaBitmap, 540, 800, true);
        Bitmap rectBitmap = Bitmap.createBitmap(sizeBitmap, 100, 200, 300, 300);//截取
        return rectBitmap;
    }
}
