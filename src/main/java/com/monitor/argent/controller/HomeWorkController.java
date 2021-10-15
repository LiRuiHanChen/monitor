package com.monitor.argent.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.api.Code;
import com.monitor.argent.commons.UnitConversion;
import com.monitor.argent.entity.HomeWorkRequestBean;
import com.monitor.argent.entity.HomeWorkResponseBody;
import com.monitor.argent.entity.HomeWorkTestCaseBean;
import com.monitor.argent.model.Result;
import com.monitor.argent.service.HomeWorkRequestImpl;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeWorkController {

    @Autowired
    HomeWorkRequestImpl homeWorkRequestImpl;
    @Resource
    UnitConversion unitConversion;

    private static final String HOME_WORK_HOST = "http://ai-image.staging.17zuoye.net";
    private static final String HOME_WORK_URL = "/DotMatrix/infer";
    private static HashMap HOME_WORK_HEADER_MAP = new HashMap();

    public static void createHeaderMap() {
        HOME_WORK_HEADER_MAP.put("Content-Type", "application/json");
    }

    @RequestMapping(value = "/getHomeWork", method = RequestMethod.GET)
    @ResponseBody
    public Result<Object> getHomeWorkRequest(@RequestParam("caseName") String caseName, @RequestParam("stage") int stage,
                                             @RequestParam("subject") int subject, @NotNull @RequestParam("flag") int flag) {

        List<HomeWorkResponseBody> homeWorkResponseBodyList = homeWorkRequestImpl.getHomeWorkTestCase(caseName, stage, subject, flag);
        if (homeWorkResponseBodyList.isEmpty()) {
            return Result.failure(Code.BAD_REQUEST, "查询结果为空");
        }
        return Result.success(homeWorkResponseBodyList);
    }

    @RequestMapping(value = "/addHomeWork", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> addHomeWorkRequest(@RequestBody HomeWorkRequestBean homeWorkRequestBean) {
        return Result.success("");
    }

    @RequestMapping(value = "/editHomeWork", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> editHomeWorkRequest(@RequestBody HomeWorkResponseBody homeWorkResponseBody) {
        return Result.success("");
    }

    @RequestMapping(value = "/runHomeWorkTestCase", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> runHomeWorkTestCase(@RequestBody HomeWorkTestCaseBean homeWorkTestCaseBean) {

        if (homeWorkTestCaseBean != null) {

            String jsonData = JSON.toJSONString(homeWorkTestCaseBean);
            if (!jsonData.isEmpty()) {
                Map<String, String> resultMap = homeWorkRequestImpl.runHomeWorkTestCase(HOME_WORK_HOST, HOME_WORK_URL, jsonData, HOME_WORK_HEADER_MAP);
                String response = null;

                if (!resultMap.isEmpty()) {
                    String statusCode;
                    statusCode = resultMap.get("code");
                    response = resultMap.get("response");
                    if (statusCode.equals("200")) {
                        return Result.success(JSONObject.parse(response));
                    }
                }
                return Result.failure(Code.BAD_REQUEST, response);
            }
        }
        return Result.failure(Code.PARAMETER_MISSING, "请求参数异常");
    }
}
