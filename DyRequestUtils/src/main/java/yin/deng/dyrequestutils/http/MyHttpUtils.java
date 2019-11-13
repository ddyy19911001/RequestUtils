package yin.deng.dyrequestutils.http;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.okhttplib.HttpInfo;
import com.okhttplib.OkHttpUtil;
import com.okhttplib.annotation.CacheType;
import com.okhttplib.annotation.DownloadStatus;
import com.okhttplib.annotation.Encoding;
import com.okhttplib.bean.DownloadFileInfo;
import com.okhttplib.callback.Callback;
import com.okhttplib.callback.ProgressCallback;
import com.okhttplib.cookie.PersistentCookieJar;
import com.okhttplib.cookie.cache.SetCookieCache;
import com.okhttplib.cookie.persistence.SharedPrefsCookiePersistor;


import java.io.File;
import java.io.IOException;
import java.util.List;


public class MyHttpUtils {
    private final Gson mGson;
    OnNoNetRequestListener noNetRequestListener;
    Context context;
    public void setNoNetRequestListener(OnNoNetRequestListener noNetRequestListener) {
        this.noNetRequestListener = noNetRequestListener;
    }

    public interface OnNoNetRequestListener{
        void onNoNet(String requestUrl);
    }
    public interface OnGetInfoListener{
        void onInfoGet(String requestUrl,Object info);
    }

    public MyHttpUtils(Application context,OnOkHttpInitListener listener) {
        this.context=context;
        mGson=new Gson();
        if(listener==null){
            throw new IllegalArgumentException("请初始化OkHttpUtil,自定义设置参数！");
        }else{
            listener.onOkHttpInit();
        }
    }

    public interface OnOkHttpInitListener{
        void onOkHttpInit();
    }

    public MyHttpUtils(Application context) {
        this.context=context;
        mGson=new Gson();
        String downloadFileDir = Environment.getExternalStorageDirectory().getPath()+"/my_okHttp_download/";
        String cacheDir = Environment.getExternalStorageDirectory().getPath()+"/my_okHttp_cache";
        OkHttpUtil.init(context)
                .setConnectTimeout(15)//连接超时时间
                .setWriteTimeout(15)//写超时时间
                .setReadTimeout(15)//读超时时间
                .setMaxCacheSize(10 * 1024 * 1024)//缓存空间大小
                .setCacheType(CacheType.FORCE_NETWORK)//缓存类型  可设置仅网络、先网络再缓存、先缓存再网络
                .setHttpLogTAG("MyHttpLog")//设置请求日志标识
                .setIsGzip(false)//Gzip压缩，需要服务端支持
                .setShowHttpLog(true)//显示请求日志
                .setShowLifecycleLog(false)//显示Activity销毁日志
                .setRetryOnConnectionFailure(true)//失败后不自动重连
                .setCachedDir(new File(cacheDir))//设置缓存目录
                .setDownloadFileDir(downloadFileDir)//文件下载保存目录
                .setResponseEncoding(Encoding.UTF_8)//设置全局的服务器响应编码
                .setRequestEncoding(Encoding.UTF_8)//设置全局的请求参数编码
//                .setHttpsCertificate("12306.cer")//设置全局Https证书
                .addResultInterceptor(HttpInterceptor.ResultInterceptor)//请求结果拦截器
//                .addExceptionInterceptor(HttpInterceptor.ExceptionInterceptor)//请求链路异常拦截器
                .setCookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)))//持久化cookie
                .build();
    }

    public MyHttpUtils(Application context,int timeoutSecond,boolean needLog,String logTag,String downLoadDir,String cacheDir) {
        this.context=context;
        mGson=new Gson();
//        String downloadFileDir = Environment.getExternalStorageDirectory().getPath()+"/my_okHttp_download/";
//        String cacheDir = Environment.getExternalStorageDirectory().getPath()+"/my_okHttp_cache";
        OkHttpUtil.init(context)
                .setConnectTimeout(timeoutSecond)//连接超时时间
                .setWriteTimeout(timeoutSecond)//写超时时间
                .setReadTimeout(timeoutSecond)//读超时时间
                .setMaxCacheSize(10 * 1024 * 1024)//缓存空间大小
                .setCacheType(CacheType.FORCE_NETWORK)//缓存类型  可设置仅网络、先网络再缓存、先缓存再网络
                .setHttpLogTAG(logTag)//设置请求日志标识
                .setIsGzip(false)//Gzip压缩，需要服务端支持
                .setShowHttpLog(needLog)//显示请求日志
                .setShowLifecycleLog(false)//显示Activity销毁日志
                .setRetryOnConnectionFailure(true)//失败后不自动重连
                .setCachedDir(new File(cacheDir))//设置缓存目录
                .setDownloadFileDir(downLoadDir)//文件下载保存目录
                .setResponseEncoding(Encoding.UTF_8)//设置全局的服务器响应编码
                .setRequestEncoding(Encoding.UTF_8)//设置全局的请求参数编码
//                .setHttpsCertificate("12306.cer")//设置全局Https证书
                .addResultInterceptor(HttpInterceptor.ResultInterceptor)//请求结果拦截器
//                .addExceptionInterceptor(HttpInterceptor.ExceptionInterceptor)//请求链路异常拦截器
                .setCookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context)))//持久化cookie
                .build();
    }

    public HttpInfo.Builder getHttpInfoBuilder(String requestUrl, JsonObject jsonObject, List<HeaderParam> params){
        HttpInfo.Builder builder= HttpInfo.Builder();
        builder .setUrl(requestUrl)
                .addParamJson(mGson.toJson(jsonObject))
                .setNeedResponse(false);//设置返回结果为Response
        if(params!=null){
            for(int i=0;i<params.size();i++){
                if(params.get(i).getValue()==null){
                    continue;
                }
                builder.addHead(params.get(i).getKey(),params.get(i).getValue());
            }
        }
        return builder;
    }

    public HttpInfo.Builder getHttpInfoBuilder(String requestUrl, JsonObject jsonObject){
        HttpInfo.Builder builder= HttpInfo.Builder();
        builder .setUrl(requestUrl)
                .addParamJson(mGson.toJson(jsonObject))
                .setNeedResponse(false);//设置返回结果为Response
        return builder;
    }


    public void sendMsgGet(final String requestUrl, JsonObject object, final Class x, final OnGetInfoListener onGetInfoListener){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        if(object==null){
            object=new JsonObject();
        }
        HttpInfo requestInfo= getHttpInfoBuilder(requestUrl,object).build();
        OkHttpUtil.getDefault(this)//绑定生命周期
                .doGetAsync(requestInfo, new Callback() {
                    @Override
                    public void onSuccess(HttpInfo info) throws IOException {
                        String data=info.getRetDetail();
                        initSucessLog(info,true);
                        Object obj= mGson.fromJson(data,x);
                        if(onGetInfoListener!=null){
                            onGetInfoListener.onInfoGet(requestUrl,obj);
                        }
                    }

                    @Override
                    public void onFailure(HttpInfo info) throws IOException {
                        initSucessLog(info,false);
                    }
                });
    }

    public void sendMsgGet(final String requestUrl, List<HeaderParam> params, JsonObject object, final Class x, final OnGetInfoListener onGetInfoListener){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        if(object==null){
            object=new JsonObject();
        }
        HttpInfo requestInfo= getHttpInfoBuilder(requestUrl,object,params).build();
        OkHttpUtil.getDefault(this)//绑定生命周期
                .doGetAsync(requestInfo, new Callback() {
                    @Override
                    public void onSuccess(HttpInfo info) throws IOException {
                        String data=info.getRetDetail();
                        initSucessLog(info,true);
                        Object obj= mGson.fromJson(data,x);
                        if(onGetInfoListener!=null){
                            onGetInfoListener.onInfoGet(requestUrl,obj);
                        }
                    }

                    @Override
                    public void onFailure(HttpInfo info) throws IOException {
                        initSucessLog(info,false);
                    }
                });
    }

    public void sendMsgGet(String requestUrl, JsonObject object,Callback callback){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        if(object==null){
            object=new JsonObject();
        }
        HttpInfo requestInfo= getHttpInfoBuilder(requestUrl,object).build();
        OkHttpUtil.getDefault(this)//绑定生命周期
                .doGetAsync(requestInfo,callback);
    }

    public void sendMsgGet(String requestUrl,List<HeaderParam> params, JsonObject object,Callback callback){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        if(object==null){
            object=new JsonObject();
        }
        HttpInfo requestInfo= getHttpInfoBuilder(requestUrl,object,params).build();
        OkHttpUtil.getDefault(this)//绑定生命周期
                .doGetAsync(requestInfo,callback);
    }


    //"Content-Type","application/json"
    public void sendMsgPost(final String requestUrl, JsonObject object, final Class x, final OnGetInfoListener onGetInfoListener){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        if(object==null){
            object=new JsonObject();
        }
        HttpInfo requestInfo= getHttpInfoBuilder(requestUrl,object).build();
        OkHttpUtil.getDefault(this)//绑定生命周期
                .doPostAsync(requestInfo, new Callback() {
                    @Override
                    public void onSuccess(HttpInfo info) throws IOException {
                        String data=info.getRetDetail();
                        initSucessLog(info,true);
                        Object obj= mGson.fromJson(data,x);
                        if(onGetInfoListener!=null){
                            onGetInfoListener.onInfoGet(requestUrl,obj);
                        }
                    }

                    @Override
                    public void onFailure(HttpInfo info) throws IOException {
                        initSucessLog(info,false);
                    }
                });
    }

    public void sendMsgPost(final String requestUrl, List<HeaderParam> params, JsonObject object, final Class x, final OnGetInfoListener onGetInfoListener){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        if(object==null){
            object=new JsonObject();
        }
        HttpInfo requestInfo= getHttpInfoBuilder(requestUrl,object,params).build();
        OkHttpUtil.getDefault(this)//绑定生命周期
                .doPostAsync(requestInfo, new Callback() {
                    @Override
                    public void onSuccess(HttpInfo info) throws IOException {
                        String data=info.getRetDetail();
                        initSucessLog(info,true);
                        Object obj= mGson.fromJson(data,x);
                        if(onGetInfoListener!=null){
                            onGetInfoListener.onInfoGet(requestUrl,obj);
                        }
                    }

                    @Override
                    public void onFailure(HttpInfo info) throws IOException {
                        initSucessLog(info,false);
                    }
                });
    }

    public void sendMsgPost(String requestUrl, JsonObject object,Callback callback){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        if(object==null){
            object=new JsonObject();
        }
        HttpInfo requestInfo= getHttpInfoBuilder(requestUrl,object).build();
        OkHttpUtil.getDefault(this)//绑定生命周期
                .doPostAsync(requestInfo,callback);
    }


    public void sendMsgPost(String requestUrl,List<HeaderParam> params, JsonObject object,Callback callback){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        if(object==null){
            object=new JsonObject();
        }
        HttpInfo requestInfo= getHttpInfoBuilder(requestUrl,object,params).build();
        OkHttpUtil.getDefault(this)//绑定生命周期
                .doPostAsync(requestInfo,callback);
    }

    /**
     * 异步上传单个文件：显示上传进度
     */
    public void doUploadSingleFile(final String requestUrl, String fileName, File file, final Class x, final OnGetInfoListener onGetInfoListener) {
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo.Builder builder = HttpInfo.Builder();
        builder.setUrl(requestUrl)
                .addUploadFile(fileName, file, new ProgressCallback(){
                    @Override
                    //结果回调，Ui线程中
                    public void onResponseMain(String filePath, HttpInfo info) {
                        String data=info.getRetDetail();
                        initSucessLog(info,true);
                        Object obj= mGson.fromJson(data,x);
                        if(onGetInfoListener!=null){
                            onGetInfoListener.onInfoGet(requestUrl,obj);
                        }
                    }

                    @Override
                    //进度回调，ui线程中
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        LogUtils.i("上传中："+percent+"%\n已上传："+bytesWritten+"\n总大小："+contentLength);
                    }
                });
                HttpInfo info=builder.build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info);
    }


    /**
     * 异步上传单个文件：显示上传进度
     */
    public void doUploadSingleFile(final String requestUrl, List<HeaderParam> params, String fileName, File file, final Class x, final OnGetInfoListener onGetInfoListener) {
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo.Builder builder = HttpInfo.Builder();
        builder.setUrl(requestUrl)
                .addUploadFile(fileName, file, new ProgressCallback(){
                    @Override
                    //结果回调，Ui线程中
                    public void onResponseMain(String filePath, HttpInfo info) {
                        String data=info.getRetDetail();
                        initSucessLog(info,true);
                        Object obj= mGson.fromJson(data,x);
                        if(onGetInfoListener!=null){
                            onGetInfoListener.onInfoGet(requestUrl,obj);
                        }
                    }

                    @Override
                    //进度回调，ui线程中
                    public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                        LogUtils.i("上传中："+percent+"%\n已上传："+bytesWritten+"\n总大小："+contentLength);
                    }
                });
        for(int i=0;i<params.size();i++){
            builder.addHead(params.get(i).getKey(),params.get(i).getValue());
        }
        HttpInfo info=builder.build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info);
    }


    /**
     * 异步上传图片：显示上传进度
     */
    public void doUploadSingleFile(String requestUrl,String fileName,File file,ProgressCallback callback) {
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo.Builder builder = HttpInfo.Builder();
        builder.setUrl(requestUrl)
                .addUploadFile(fileName, file, callback);
        HttpInfo info = builder.build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info);
    }

    /**
     * 异步上传图片：显示上传进度
     */
    public void doUploadSingleFile(String requestUrl,List<HeaderParam> params,String fileName,File file,ProgressCallback callback) {
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo.Builder builder = HttpInfo.Builder();
        builder.setUrl(requestUrl)
                .addUploadFile(fileName, file, callback);
        if(params!=null){
            for(int i=0;i<params.size();i++){
                builder.addHead(params.get(i).getKey(),params.get(i).getKey());
            }
        }
        HttpInfo info = builder.build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info);
    }


    /**
     * 异步上传多个文件：显示上传进度
     */
    public void doUploadMuiltFiles(final String requestUrl, FileListParams fileListParams, final Class x, final OnGetInfoListener onGetInfoListener) {
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo.Builder builder = HttpInfo.Builder().setUrl(requestUrl);
        for(int i=0;i<fileListParams.getFiles().size();i++){
            builder.addUploadFile(fileListParams.getFileNames().get(i),fileListParams.getFiles().get(i).getAbsolutePath());
        }
        HttpInfo info=builder.build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info,new ProgressCallback(){
            @Override
            //结果回调，Ui线程中
            public void onResponseMain(String filePath, HttpInfo info) {
                String data=info.getRetDetail();
                initSucessLog(info,true);
                Object obj= mGson.fromJson(data,x);
                if(onGetInfoListener!=null){
                    onGetInfoListener.onInfoGet(requestUrl,obj);
                }
            }

            @Override
            //进度回调，ui线程中
            public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                LogUtils.i("上传中："+percent+"%\n已上传："+bytesWritten+"\n总大小："+contentLength);
            }
        });
    }

    /**
     * 异步上传多个文件：显示上传进度
     */
    public void doUploadMuiltFiles(final String requestUrl, List<HeaderParam> params, FileListParams fileListParams, final Class x, final OnGetInfoListener onGetInfoListener) {
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo.Builder builder = HttpInfo.Builder().setUrl(requestUrl);
        for(int i=0;i<fileListParams.getFiles().size();i++){
            builder.addUploadFile(fileListParams.getFileNames().get(i),fileListParams.getFiles().get(i).getAbsolutePath());
        }
        if(params!=null){
            for(int i=0;i<params.size();i++){
                builder.addHead(params.get(i).getKey(),params.get(i).getValue());
            }
        }
        HttpInfo info=builder.build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info,new ProgressCallback(){
            @Override
            //结果回调，Ui线程中
            public void onResponseMain(String filePath, HttpInfo info) {
                String data=info.getRetDetail();
                initSucessLog(info,true);
                Object obj= mGson.fromJson(data,x);
                if(onGetInfoListener!=null){
                    onGetInfoListener.onInfoGet(requestUrl,obj);
                }
            }

            @Override
            //进度回调，ui线程中
            public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                LogUtils.i("上传中："+percent+"%\n已上传："+bytesWritten+"\n总大小："+contentLength);
            }
        });
    }



    /**
     * 异步上传多个文件：显示上传进度
     */
    public void doUploadMuiltFiles(String requestUrl,FileListParams fileListParams, ProgressCallback callback) {
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo.Builder builder = HttpInfo.Builder().setUrl(requestUrl);
        for(int i=0;i<fileListParams.getFiles().size();i++){
            builder.addUploadFile(fileListParams.getFileNames().get(i),fileListParams.getFiles().get(i).getAbsolutePath());
        }
        HttpInfo info=builder.build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info,callback);
    }

    /**
     * 异步上传多个文件：显示上传进度
     */
    public void doUploadMuiltFiles(String requestUrl,List<HeaderParam> params,FileListParams fileListParams, ProgressCallback callback) {
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo.Builder builder = HttpInfo.Builder().setUrl(requestUrl);
        for(int i=0;i<fileListParams.getFiles().size();i++){
            builder.addUploadFile(fileListParams.getFileNames().get(i),fileListParams.getFiles().get(i).getAbsolutePath());
        }
        if(params!=null){
            for(int i=0;i<params.size();i++){
                builder.addHead(params.get(i).getKey(),params.get(i).getValue());
            }
        }
        HttpInfo info=builder.build();
        OkHttpUtil.getDefault(this).doUploadFileAsync(info,callback);
    }


    public DownloadFileInfo download(String requestUrl, String saveFileName){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return null;
        }
        DownloadFileInfo fileInfo = new DownloadFileInfo(requestUrl, saveFileName, new ProgressCallback() {
            @Override
            public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                LogUtils.d("下载进度：" + percent);
            }

            @Override
            public void onResponseMain(String filePath, HttpInfo info) {
                if (info.isSuccessful()) {
                    initSucessLog(info, true);
                    LogUtils.i(info.getRetDetail());
                } else {
                    initSucessLog(info, false);
                }
            }
        });
        HttpInfo.Builder builder = HttpInfo.Builder().addDownloadFile(fileInfo);
        HttpInfo info = builder.build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);
        return fileInfo;
    }


    private DownloadFileInfo download(String requestUrl,List<HeaderParam> params, String saveFileName){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return null;
        }
        DownloadFileInfo fileInfo = new DownloadFileInfo(requestUrl, saveFileName, new ProgressCallback() {
            @Override
            public void onProgressMain(int percent, long bytesWritten, long contentLength, boolean done) {
                LogUtils.d("下载进度：" + percent);
            }

            @Override
            public void onResponseMain(String filePath, HttpInfo info) {
                if (info.isSuccessful()) {
                    initSucessLog(info, true);
                    LogUtils.i(info.getRetDetail());
                } else {
                    initSucessLog(info, false);
                }
            }
        });
        HttpInfo.Builder builder = HttpInfo.Builder().addDownloadFile(fileInfo);
        if(params!=null){
            for(int i=0;i<params.size();i++){
                builder.addHead(params.get(i).getKey(),params.get(i).getValue());
            }
        }
        HttpInfo info = builder.build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);
        return fileInfo;
    }




    public DownloadFileInfo download(String requestUrl, String saveFileName, ProgressCallback callback){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return null;
        }
        DownloadFileInfo fileInfo = new DownloadFileInfo(requestUrl, saveFileName, callback);
        HttpInfo info = HttpInfo.Builder().addDownloadFile(fileInfo).build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);
        return fileInfo;
    }

    public DownloadFileInfo download(String requestUrl,List<HeaderParam> params, String saveFileName, ProgressCallback callback){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(requestUrl);
                LogUtils.e("网络中断，无法请求数据");
            }
            return null;
        }
        DownloadFileInfo fileInfo = new DownloadFileInfo(requestUrl, saveFileName, callback);
        HttpInfo.Builder builder = HttpInfo.Builder().addDownloadFile(fileInfo);
        if(params!=null){
            for(int i=0;i<params.size();i++){
                builder.addHead(params.get(i).getKey(),params.get(i).getValue());
            }
        }
        HttpInfo info = builder.build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);
        return fileInfo;
    }


    public void goOnDownLoad( DownloadFileInfo fileInfo){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(fileInfo.getUrl());
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo info = HttpInfo.Builder().addDownloadFile(fileInfo).build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);
    }

    public void goOnDownLoad( DownloadFileInfo fileInfo,List<HeaderParam> params){
        if(!NetUtils.isNetworkConnected(context)){
            if(noNetRequestListener!=null){
                noNetRequestListener.onNoNet(fileInfo.getUrl());
                LogUtils.e("网络中断，无法请求数据");
            }
            return;
        }
        HttpInfo.Builder builder = HttpInfo.Builder().addDownloadFile(fileInfo);
        if(params!=null){
            for(int i=0;i<params.size();i++){
                builder.addHead(params.get(i).getKey(),params.get(i).getValue());
            }
        }
        HttpInfo info = builder.build();
        OkHttpUtil.Builder().setReadTimeout(120).build(this).doDownloadFileAsync(info);
    }

    public String getDownLoadStatu(DownloadFileInfo downloadFileInfo){
        if (null != downloadFileInfo){
            String status = downloadFileInfo.getDownloadStatus();
            return status;
        }else{
            return null;
        }
    }

    public void pauseDownLoad(DownloadFileInfo downloadFileInfo) {
        if (null != downloadFileInfo){
            downloadFileInfo.setDownloadStatus(DownloadStatus.PAUSE);
        }
    }


    public void initSucessLog(HttpInfo info,boolean isSucess) {
        if(isSucess) {
            LogUtils.i("请求结果（成功）：\n请求地址："+info.getUrl()+"\n请求参数："+info.getParamJson()+"\n响应结果:"+info.getRetDetail());
        }else{
            LogUtils.e("请求结果（失败）：\n请求地址："+info.getUrl()+"\n请求参数："+info.getParamJson()+"\n错误原因："+info.getRetDetail());
        }
    }



}
