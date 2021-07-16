package com.monitor.argent.controller;

import com.monitor.argent.api.Code;
import com.monitor.argent.commons.ArthasHttpRequest;
import com.monitor.argent.entity.ArthasRequestBody;
import com.monitor.argent.entity.ArthasRequestParam;
import com.monitor.argent.model.Result;
import com.monitor.argent.service.ArthasRequestImpl;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class ArthasRequestController {

    @Resource
    ArthasHttpRequest arthasHttpRequest;
    @Resource
    ArthasRequestImpl arthasRequestImpl;

    @RequestMapping(value = "/action", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> arthasActionRequest(@RequestBody ArthasRequestParam arthasRequestParam) {
        if (arthasRequestParam == null)
            return Result.failure(Code.PARAMETER_MISSING);

        String host = arthasRequestParam.getHost().trim();
        String url = arthasRequestParam.getUrl().trim();

        if (StringUtils.isEmpty(host) || StringUtils.isEmpty(url))
            return Result.failure(Code.PARAMETER_MISSING);

        //对象转换
        ArthasRequestBody arthasRequestBody = arthasHttpRequest.convertArthasRequestParamToArthasRequestBody(arthasRequestParam);

        if (arthasRequestBody == null)
            return Result.failure(Code.PARAMETER_MISSING);

        return arthasRequestImpl.sendArthasPostRequest(host, url, null, arthasRequestBody);
    }

}
