package yin.deng.dyrequestutils.okhttplib.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import okhttp3.Call;
import okhttp3.Response;
import yin.deng.dyrequestutils.okhttplib.HttpInfo;
import yin.deng.dyrequestutils.okhttplib.bean.CallbackMessage;
import yin.deng.dyrequestutils.okhttplib.bean.DownloadMessage;
import yin.deng.dyrequestutils.okhttplib.bean.ProgressMessage;
import yin.deng.dyrequestutils.okhttplib.bean.UploadMessage;
import yin.deng.dyrequestutils.okhttplib.callback.BaseActivityLifecycleCallbacks;
import yin.deng.dyrequestutils.okhttplib.callback.BaseCallback;
import yin.deng.dyrequestutils.okhttplib.callback.CallbackOk;
import yin.deng.dyrequestutils.okhttplib.callback.Callback;
/**
 * 主线程Handler
 * @author zhousf
 */
public class OkMainHandler extends Handler {

    private static OkMainHandler singleton;

    public static OkMainHandler getInstance(){
        if(null == singleton){
            synchronized (OkMainHandler.class){
                if(null == singleton)
                    singleton = new OkMainHandler();
            }
        }
        return singleton;
    }

    private OkMainHandler() {
        super(Looper.getMainLooper());
    }

    /**
     * 网络请求回调标识
     */
    public static final int RESPONSE_CALLBACK = 0x01;

    /**
     * 进度回调标识
     */
    public static final int PROGRESS_CALLBACK = 0x02;

    /**
     * 上传结果回调标识
     */
    public static final int RESPONSE_UPLOAD_CALLBACK = 0x03;

    /**
     * 下载结果回调标识
     */
    public static final int RESPONSE_DOWNLOAD_CALLBACK = 0x04;


    @Override
    public void handleMessage(Message msg) {
        final int what = msg.what;
        String requestTag = "";
        try {
            switch (what){
                case RESPONSE_CALLBACK://网络请求
                    CallbackMessage callMsg = (CallbackMessage) msg.obj;
                    if(null != callMsg.callback){
                        //开始回调
                        requestTag = callMsg.requestTag;
                        if(!BaseActivityLifecycleCallbacks.isActivityDestroyed(callMsg.requestTag)){
                            BaseCallback callback = callMsg.callback;
                            if(callback instanceof CallbackOk){
                                ((CallbackOk)callback).onResponse(callMsg.info);
                            } else if(callback instanceof yin.deng.dyrequestutils.okhttplib.callback.Callback){
                                HttpInfo info = callMsg.info;
                                if(info.isSuccessful()){
                                    ((yin.deng.dyrequestutils.okhttplib.callback.Callback)callback).onSuccess(info);
                                }else{
                                    ((yin.deng.dyrequestutils.okhttplib.callback.Callback)callback).onFailure(info);
                                }
                                //当返回结果是Response时自动关闭ResponseBody
                                if(info.isNeedResponse()){
                                    Response res = info.getResponse();
                                    if(null != res){
                                        res.close();
                                    }
                                }
                            }
                        }
                    }
                    Call call = callMsg.call;
                    if (call != null) {
                        if(!call.isCanceled()){
                            call.cancel();
                        }
                        BaseActivityLifecycleCallbacks.cancel(requestTag,call);
                    }
                    break;
                case PROGRESS_CALLBACK://进度回调
                    ProgressMessage proMsg = (ProgressMessage) msg.obj;
                    if(null != proMsg.progressCallback){
                        requestTag = proMsg.requestTag;
                        if(!BaseActivityLifecycleCallbacks.isActivityDestroyed(proMsg.requestTag)){
                            proMsg.progressCallback.onProgressMain(proMsg.percent,proMsg.bytesWritten,proMsg.contentLength,proMsg.done);
                        }
                    }
                    break;
                case RESPONSE_UPLOAD_CALLBACK://上传结果回调
                    UploadMessage uploadMsg = (UploadMessage) msg.obj;
                    if(null != uploadMsg.progressCallback){
                        requestTag = uploadMsg.requestTag;
                        if(!BaseActivityLifecycleCallbacks.isActivityDestroyed(requestTag)){
                            uploadMsg.progressCallback.onResponseMain(uploadMsg.filePath,uploadMsg.info);
                            BaseActivityLifecycleCallbacks.cancel(requestTag);
                        }
                    }
                    break;
                case RESPONSE_DOWNLOAD_CALLBACK://下载结果回调
                    DownloadMessage downloadMsg = (DownloadMessage) msg.obj;
                    if(null != downloadMsg){
                        requestTag = downloadMsg.requestTag;
                        if(!BaseActivityLifecycleCallbacks.isActivityDestroyed(requestTag)){
                            downloadMsg.progressCallback.onResponseMain(downloadMsg.filePath,downloadMsg.info);
                            BaseActivityLifecycleCallbacks.cancel(requestTag);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            BaseActivityLifecycleCallbacks.cancel(requestTag);
        }
    }



}
