package com.myproject.facedetection.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.myproject.facedetection.R;

public class MainActivity extends AppCompatActivity {

    private static String strLibraryName = "opencv_java3"; // 不需要添加前缀 libopencv_java3

    static {
        try {
            Log.e("loadLibrary", strLibraryName);
            System.loadLibrary(strLibraryName);
            //System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // couldn't find "libopencv_java320.so"
        } catch (UnsatisfiedLinkError e) {
            Log.e("loadLibrary", "Native code library failed to load.\n" + e);
        } catch (Exception e) {
            Log.e("loadLibrary", "Exception: " + e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /**
     * 按钮点击事件
     *
     * @param view
     */
    public void click(View view) {
        int bid = view.getId();
        try {
            switch (bid) {

                // 人脸检测1
                case R.id.iv11:
                    Intent intent11 = new Intent(MainActivity.this, FaceDetectionAndroidActivity.class);
                    this.startActivity(intent11);
                    break;

                // 人脸检测2
                case R.id.iv12:
                    Intent intent12 = new Intent(MainActivity.this, FaceDetectionOpenCVActivity.class);
                    this.startActivity(intent12);
                    break;


                // 测试
                case R.id.iv21:
                    Intent intent21 = new Intent(MainActivity.this, FaceTrackingActivity.class);
                    this.startActivity(intent21);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            Log.e("异常", "click异常!");
            e.printStackTrace();
        }

    }

}
