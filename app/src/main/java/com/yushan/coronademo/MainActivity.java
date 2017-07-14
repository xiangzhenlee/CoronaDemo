package com.yushan.coronademo;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CoronaView corona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView(){
        corona = (CoronaView) findViewById(R.id.corona);
        corona.setViewOnClickListener(new CoronaView.ViewOnClickListener() {
            @Override
            public void onClicked(int clickZone) {
                Toast.makeText(MainActivity.this, "click:" + clickZone, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData(){
        Log.e("yushan","hahah");
    }
}
