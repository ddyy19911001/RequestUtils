package yin.deng.dyrequestutils.okhttplib.interceptor;


import yin.deng.dyrequestutils.okhttplib.HttpInfo;

/**
 * 请求结果拦截器
 * @author zhousf
 */
public interface ResultInterceptor {

    HttpInfo intercept(HttpInfo info) throws Exception;

}
