package com.myproject.facedetection.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * 通用函数
 */
public class myUtils {

    /**
     * byte转为bitmap
     *
     * @param data 字节流
     * @return bm
     */
    public static Bitmap ByteToBitmap(byte[] data, Camera.Size previewSize) {

        Bitmap bm = null;
        ByteArrayOutputStream baos = null;
        try {
            YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
            baos = new ByteArrayOutputStream();
            //这里 80 是图片质量，取值范围 0-100，100为品质最高
            yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
            byte[] jdata = baos.toByteArray();
            bm = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);//将data byte型数组转换成bitmap文件

            // 旋转90度

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bm;
    }


    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap Base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    /*
    缩放 指定高度
     */
    public static Bitmap resizeBitmap(Bitmap bm, int ivbWidth, int ivbHeight) {
        Bitmap resizeBmp = null;
        try {
            int width = bm.getWidth();
            int height = bm.getHeight();

            Matrix matrix = new Matrix();

            float scaleWidth = ((float) ivbWidth) / width;
            float scaleHeight = ((float) ivbHeight) / height;

            matrix.postScale(scaleWidth, scaleHeight); //长和宽放大缩小的比例
            resizeBmp = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);

        } catch (Exception e) {
            e.printStackTrace();

        }
        return resizeBmp;
    }


    /**
     * 裁剪图片
     *
     * @param bitmap 传入需要裁剪的bitmap
     * @return 返回裁剪后的bitmap
     */
    public static Bitmap cropBitmap(Bitmap bitmap, int x1, int y1, int x2, int y2) {
        if (x1 + x2 + y2 + y1 == 0) {
            return null;
        }
        int cropWidth = x2 - x1;
        int cropHeight = y2 - y1;
        int crop_x = x1;
        int crop_y = y1;
        return Bitmap.createBitmap(bitmap, crop_x, crop_y, cropWidth, cropHeight, null, false);
    }

}
