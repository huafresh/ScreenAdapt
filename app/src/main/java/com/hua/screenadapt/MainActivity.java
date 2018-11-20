package com.hua.screenadapt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.hua.screen_adapt_core.ScreenAdaptManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenAdaptManager.get().adapt(this);
        setContentView(R.layout.activity_main);
        findViewById(R.id.second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });

        findViewById(R.id.activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_temp, null);
            }
        });
        findViewById(R.id.app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater.from(MainActivity.this.getApplicationContext()).inflate(R.layout.layout_temp, null);
            }
        });

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll_2);
        findViewById(R.id.adapt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScreenAdaptManager.get().adapt(linearLayout);
            }
        });

    }
}
