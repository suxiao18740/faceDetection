package com.myproject.facedetection.ui;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
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

import java.io.File;

public class FaceDetectionAndroidActivity extends AppCompatActivity {

    private static String CAMERAIMAGENAME = "image.jpg";
    private CustomImageButton imageButton;
    private TextView textView;
    private Bitmap bitmap;
    private Bitmap resizeBitmap;
    private int numberOfFaceDetected;
    private FaceDetector.Face[] myFace;
    private Bitmap bitmap565;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection_android);
        textView = (TextView) findViewById(R.id.tv_face);
        imageButton = (CustomImageButton) findViewById(R.id.iv_face);
        imageButton.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);


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
            toast = Toast.makeText(FaceDetectionAndroidActivity.this, "未选择图像", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // 转换 释放
        bitmap565 = bitmap.copy(Bitmap.Config.RGB_565, true);

        if (!bitmap.isRecycled())
            bitmap.recycle();


        // 识别图片
        detectFace();

        // 画框
        drawFace();


        // 将照片剪裁 bitmap将被释放重新赋值
        int ibWidth = imageButton.getWidth();
        int ibHeight = imageButton.getHeight();
        resizeBitmap = imageButton.resizeBitmap(bitmap565, ibWidth, ibHeight);

        imageButton.setBitmap(resizeBitmap);


    }

    private void detectFace() {
        int numberOfFace = 12;
        FaceDetector myFaceDetect;

        int imageWidth = bitmap565.getWidth();
        int imageHeight = bitmap565.getHeight();
        myFace = new FaceDetector.Face[numberOfFace];
        myFaceDetect = new FaceDetector(imageWidth, imageHeight, numberOfFace);
        numberOfFaceDetected = myFaceDetect.findFaces(bitmap565, myFace);

        textView.setText(String.format("检测到%1$d个人脸", numberOfFaceDetected));

    }

    private void drawFace() {
        Canvas canvas = new Canvas(bitmap565);
        // canvas.drawBitmap(bitmap565, 0, 0, null);
        Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(5);
        for (int i = 0; i < numberOfFaceDetected; i++) {
            FaceDetector.Face face = myFace[i];
            PointF myMidPoint = new PointF();
            face.getMidPoint(myMidPoint);
            float myEyesDistance = face.eyesDistance();
            canvas.drawRect((int) (myMidPoint.x - myEyesDistance * 1.5),
                    (int) (myMidPoint.y - myEyesDistance * 1.5),
                    (int) (myMidPoint.x + myEyesDistance * 1.5),
                    (int) (myMidPoint.y + myEyesDistance * 1.8), myPaint);
        }

    }


}
