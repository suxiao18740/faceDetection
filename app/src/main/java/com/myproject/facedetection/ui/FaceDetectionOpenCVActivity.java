package com.myproject.facedetection.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.myproject.facedetection.R;
import com.myproject.facedetection.entity.CustomImageButton;
import com.opencvlib.ObjectDetector;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class FaceDetectionOpenCVActivity extends AppCompatActivity {
    private ObjectDetector mFaceDetector;

    private static String CAMERAIMAGENAME = "image.jpg";
    private CustomImageButton imageButton;
    private CustomImageButton imageButton2;
    private TextView textView;
    private Bitmap bitmap;
    private Bitmap rectBitmap;
    private Bitmap resizeBitmap;
    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection_opencv);
        textView = (TextView) findViewById(R.id.tv_face);
        imageButton = (CustomImageButton) findViewById(R.id.iv_face);
        imageButton.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        imageButton2 = (CustomImageButton) findViewById(R.id.iv_face2);
        imageButton2.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        mFaceDetector = new ObjectDetector(this, R.raw.haarcascade_frontalface_alt, 6, 0.2F, 0.2F, new Scalar(255, 0, 0, 255));
    }


    /**
     * 点击添加照片事件
     *
     * @param v
     */
    public void onClick(View v) {

        int bt_id = v.getId();
        switch (bt_id) {
            case R.id.addPic:
                // 添加照片
                // 打开本地相册
                Intent intent1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent1, 101);
                break;

            case R.id.takePhoto:
                // 拍照
                // 打开本地相机
                Intent intent2 = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), CAMERAIMAGENAME));
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent2, 102);

                break;

            case R.id.back:
                this.finish();
                break;

            default:
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 加判断 不选择照片或者不拍照时不闪退
        //Log.e("data", String.valueOf(data));
        //if (data == null)
        //return;

        bitmap = null;
        switch (requestCode) {
            // 选择图片库的图片
            case 101:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri uri = data.getData();
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            // 表示调用本地照相机拍照
            case 102:
                if (resultCode == RESULT_OK) {
                    //Bundle bundle = data.getExtras();
                    //bm = (Bitmap) bundle.get("data");
                    bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + CAMERAIMAGENAME);

                }
                break;
            default:
                break;
        }

        Log.e("bitmap", String.valueOf(bitmap));

        if (bitmap == null) {
            toast = Toast.makeText(FaceDetectionOpenCVActivity.this, "未选择图像", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }


        // 识别图片 并画框
        detectFace();


        // 将照片剪裁 bitmap将被释放重新赋值
        int ibWidth = imageButton.getWidth();
        int ibHeight = imageButton.getHeight();
        resizeBitmap = imageButton.resizeBitmap(bitmap, ibWidth, ibHeight);

        imageButton.setBitmap(resizeBitmap);
        imageButton2.setBitmap(rectBitmap);


    }

    private void detectFace() {
        try {
            // bitmapToMat
            Mat toMat = new Mat();
            Utils.bitmapToMat(bitmap, toMat);
            Mat copyMat = new Mat();
            toMat.copyTo(copyMat); // 复制

            // togray
            Mat gray = new Mat();
            Imgproc.cvtColor(toMat, gray, Imgproc.COLOR_RGBA2GRAY);

            MatOfRect mRect = new MatOfRect();
            Rect[] object = mFaceDetector.detectObjectImage(gray, mRect);

            Log.e("objectLength", object.length + "");


            int maxRectArea = 0 * 0;
            Rect maxRect = null;

            int facenum = 0;
            // Draw a bounding box around each face.
            for (Rect rect : object) {
                Imgproc.rectangle(
                        toMat,
                        new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(255, 0, 0), 3);
                ++facenum;
                // 找出最大的面积
                int tmp = rect.width * rect.height;
                if (tmp >= maxRectArea) {
                    maxRectArea = tmp;
                    maxRect = rect;
                }
            }

            rectBitmap = null;
            if (facenum != 0) {
                // 剪切最大的头像
                Log.e("剪切的长宽", String.format("高:%s,宽:%s", maxRect.width, maxRect.height));
                Rect rect = new Rect(maxRect.x, maxRect.y, maxRect.width, maxRect.height);
                Mat rectMat = new Mat(copyMat, rect);  // 从原始图像拿
                rectBitmap = Bitmap.createBitmap(rectMat.cols(), rectMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(rectMat, rectBitmap);
            }

            textView.setText(String.format("检测到%1$d个人脸", facenum));
            Utils.matToBitmap(toMat, bitmap);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
