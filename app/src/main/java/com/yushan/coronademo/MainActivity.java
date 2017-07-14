package com.yushan.coronademo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

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

    private void initData_2(){
        Log.e("yushan","heihei");
    }
}
