package com.myproject.facedetection.common;

import android.hardware.Camera;

/**
 * Created by think-hxr on 17-10-17.
 */

public interface OnObjectTrackingInterface {
    // 传输camera的字节流
    void onCameraByteStream(byte[] bytes, Camera camera);
}
