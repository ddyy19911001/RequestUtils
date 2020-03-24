package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import java.io.IOException;
import java.util.HashMap;

import okhttp3.Headers;
import yin.deng.dyrequestutils.http.LogUtils;
import yin.deng.dyrequestutils.http.MyHttpUtils;
import yin.deng.dyrequestutils.okhttplib.HttpInfo;
import yin.deng.dyrequestutils.okhttplib.callback.Callback;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyHttpUtils httpUtils=new MyHttpUtils(getApplication());
        httpUtils.sendMsgGet("https://pan.lanzou.com/i0ltl2d", new HashMap<String, String>(), new Callback() {
            @Override
            public void onSuccess(HttpInfo info) throws IOException {
               Headers headers=info.getResponseHeaders();
            }

            @Override
            public void onFailure(HttpInfo info) throws IOException {

            }
        });
    }
}
