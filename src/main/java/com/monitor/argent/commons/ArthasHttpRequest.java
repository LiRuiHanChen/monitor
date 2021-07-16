package com.monitor.argent.commons;

import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.api.ArthasHttpApiRequest;
import com.monitor.argent.entity.ArthasRequestBody;
import com.monitor.argent.entity.ArthasRequestParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 构造arthas基本api请求
 */
@Component
public class ArthasHttpRequest implements ArthasHttpApiRequest {

    @Resource
    HttpRequestUtil httpRequestUtil;

    @Override
    public Map<String, String> sendArthasPostRequest(String host, String url, HashMap<String, String> headerMap, ArthasRequestBody arthasRequestBody) {
        if (arthasRequestBody == null)
            return null;

        JSONObject arthasObject = (JSONObject) JSONObject.toJSON(arthasRequestBody);
        String body = arthasObject.toString();
        try {
            return httpRequestUtil.sendPostDataByJson(host + url, body, headerMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 接收对象（ArthasRequestParam）转为arthas请求对象（ArthasRequestBody）
    public ArthasRequestBody convertArthasRequestParamToArthasRequestBody(ArthasRequestParam arthasRequestParam) {
        if (arthasRequestParam == null)
            return null;
        ArthasRequestBody arthasRequestBody = new ArthasRequestBody();
        arthasRequestBody.setRequestId(arthasRequestParam.getRequestId());
        arthasRequestBody.setAction(arthasRequestParam.getAction());
        arthasRequestBody.setCommand(arthasRequestParam.getCommand());
        arthasRequestBody.setConsumerId(arthasRequestParam.getConsumerId());
        arthasRequestBody.setExecTimeout(arthasRequestParam.getExecTimeout());
        arthasRequestBody.setSessionId(arthasRequestParam.getSessionId());
        return arthasRequestBody;
    }
}
