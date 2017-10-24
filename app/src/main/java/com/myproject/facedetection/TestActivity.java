package com.myproject.facedetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.myproject.facedetection.entity.CustomImageButton;

import java.io.IOException;
import java.io.InputStream;

public class TestActivity extends AppCompatActivity {

    private CustomImageButton imageButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //Resources r = this.getResources();
        //Bitmap bm = BitmapFactory.decodeResource(r,R.raw.threepoint_shooters_1990);
        //textView.setText(String.valueOf(bm.getWidth()));
        //imageButton.setBitmap(bm);

        // assets目录下为原图
        String path = "aaa.jpg"; //图片存放的路径

        //InputStream is = getClassLoader().getResourceAsStream(path); //得到图片流


        InputStream is = null;
        try {
            is = this.getResources().getAssets().open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(is);
        int[] pixels = new int[bm.getHeight() * bm.getWidth()]; // 1维数组

        bm.getPixels(pixels, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
        for (int i = 0; i < 100; i++) {
            Log.e(String.format("line %d", i), String.format("red: %s | green: %s | blue: %s ",
                    Color.red(pixels[i]),
                    Color.green(pixels[i]),
                    Color.blue(pixels[i])));
        }

        imageButton = (CustomImageButton) findViewById(R.id.iv_face);
        imageButton.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        imageButton.setBitmap(bm);

    }
}
