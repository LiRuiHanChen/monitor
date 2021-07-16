package com.monitor.argent.service;

import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.api.Code;
import com.monitor.argent.commons.ArthasHttpRequest;
import com.monitor.argent.entity.ArthasRequestBody;
import com.monitor.argent.model.Result;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class ArthasRequestImpl {

    @Resource
    ArthasHttpRequest arthasHttpRequest;

    public Result<Object> sendArthasPostRequest(String host, String url, HashMap<String, String> headerMap, ArthasRequestBody arthasRequestBody) {

        if (StringUtils.isEmpty(host) || StringUtils.isEmpty(url)) {
            return Result.failure(Code.BAD_REQUEST, "请求地址异常");
        }

        Map<String, String> resultMap = arthasHttpRequest.sendArthasPostRequest(host, url, headerMap, arthasRequestBody);
        String statusCode = resultMap.get("code");
        String response = resultMap.get("response");
        if (statusCode.equals("200")) {
            return Result.success(JSONObject.parse(response));
        }
        return Result.failure(Code.BAD_REQUEST, response);
    }

}
