package yin.deng.dyutils.http;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/29 0029.
 */
public class BaseHttpInfo  implements Serializable {
    private String _baseRequestUrl;
    private Map<String,String> _baseMapParams=new HashMap<>();


    public Map<String, String> get_baseMapParams() {
        return _baseMapParams;
    }

    public void set_baseMapParams(Map<String, String> _baseMapParams) {
        this._baseMapParams = _baseMapParams;
    }


    public String get_baseRequestUrl() {
        return _baseRequestUrl;
    }

    public void set_baseRequestUrl(String _baseRequestUrl) {
        this._baseRequestUrl = _baseRequestUrl;
    }

    @Override
    public String toString() {
        return "BaseHttpInfo{" +
                "_baseRequestUrl='" + _baseRequestUrl + '\'' +
                ", _baseMapParams=" + _baseMapParams +
                '}';
    }
}
