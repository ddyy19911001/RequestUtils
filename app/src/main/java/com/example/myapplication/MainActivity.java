package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.okhttplib.HttpInfo;
import com.okhttplib.callback.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yin.deng.dyrequestutils.http.BaseHttpInfo;
import yin.deng.dyrequestutils.http.HeaderParam;
import yin.deng.dyrequestutils.http.LogUtils;
import yin.deng.dyrequestutils.http.MyHttpUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyHttpUtils httpUtils=new MyHttpUtils(getApplication());
        httpUtils.sendMsgGet("url", new JsonObject(), new Callback() {
            @Override
            public void onSuccess(HttpInfo info) throws IOException {

            }

            @Override
            public void onFailure(HttpInfo info) throws IOException {

            }
        });
    }
}
