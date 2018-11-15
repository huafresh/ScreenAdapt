package com.hua.screenadapt;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.hua.screen_adapt_core.ScreenAdaptManager;

/**
 * @author hua
 * @version V1.0
 * @date 2018/11/15 15:03
 */

public class SecondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenAdaptManager.get().adapt(this);
        setContentView(R.layout.activity_second);
        Button app = (Button) findViewById(R.id.app);
        Button activity = (Button) findViewById(R.id.activity);

        app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater.from(SecondActivity.this.getApplicationContext()).inflate(R.layout.layout_temp, null);
            }
        });

        activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater.from(SecondActivity.this).inflate(R.layout.layout_temp, null);
            }
        });
    }
}
