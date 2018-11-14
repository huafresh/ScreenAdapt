package com.hua.screenadapt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.hua.screen_adapt_core.ScreenAdaptManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenAdaptManager.get().adapt(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDelegate().installViewFactory();

//        LinearLayout linearLayout = findViewById(R.id.ll_2);
//        ScreenAdaptManager.get().adapt(linearLayout);
    }
}
