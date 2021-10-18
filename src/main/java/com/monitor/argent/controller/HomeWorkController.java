package com.monitor.argent.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.api.Code;
import com.monitor.argent.entity.HomeWorkRequestBean;
import com.monitor.argent.entity.HomeWorkResponseBody;
import com.monitor.argent.model.Result;
import com.monitor.argent.service.HomeWorkRequestImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeWorkController {

    @Autowired
    HomeWorkRequestImpl homeWorkRequestImpl;

    private static final String HOME_WORK_HOST = "http://ai-image.staging.17zuoye.net";
    private static final String HOME_WORK_URL = "/DotMatrix/infer";
    private static HashMap HOME_WORK_HEADER_MAP = new HashMap();

    public static void createHeaderMap() {
        HOME_WORK_HEADER_MAP.put("Content-Type", "application/json");
    }

    @RequestMapping(value = "/getHomeWork", method = RequestMethod.GET)
    @ResponseBody
    public Result<Object> getHomeWorkRequest(@RequestParam("caseName") String caseName, @RequestParam(required = false, name = "stage") Integer stage,
                                             @RequestParam(required = false, name = "subject") Integer subject, @RequestParam(name = "flag") int flag) {

        List<HomeWorkResponseBody> homeWorkResponseBodyList = homeWorkRequestImpl.getHomeWorkTestCase(caseName, stage, subject, flag);
        if (homeWorkResponseBodyList.isEmpty()) {
            return Result.failure(Code.BAD_REQUEST, "查询结果为空");
        }
        return Result.success(homeWorkResponseBodyList);
    }

    /**
     * 有ID更新，无ID新增
     *
     * @param homeWorkRequestBean
     * @return
     */
    @RequestMapping(value = "/saveHomeWork", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> addHomeWorkRequest(@RequestBody HomeWorkRequestBean homeWorkRequestBean) {
        if (homeWorkRequestBean == null) return Result.failure(Code.PARAMETER_MISSING, "参数异常");
        int id = homeWorkRequestBean.getId();
        int saveResult;
        // id 大于0 更新
        if (id > 0) {
            saveResult = homeWorkRequestImpl.editHomeWorkTestCase(homeWorkRequestBean);
        } else {
            saveResult = homeWorkRequestImpl.addHomeWorkTestCase(homeWorkRequestBean);
        }

        if (saveResult <= 0) return Result.failure(402, "保存失败");
        return Result.success(saveResult);
    }

    @RequestMapping(value = "/runHomeWorkTestCase", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> runHomeWorkTestCase(@RequestBody List<HomeWorkRequestBean> homeWorkTestCaseBeanList) {
        Map<String, Boolean> result = new HashMap<>();

        if (homeWorkTestCaseBeanList.isEmpty()) return Result.failure(Code.PARAMETER_MISSING, "请求参数异常");
        for (HomeWorkRequestBean item : homeWorkTestCaseBeanList) {
            if (item == null) break;
            String tempRequestData = item.getRequestBody();
            if (StringUtils.isEmpty(tempRequestData)) break;

            Map<String, String> resultMap = homeWorkRequestImpl.runHomeWorkTestCase(HOME_WORK_HOST, HOME_WORK_URL, tempRequestData, HOME_WORK_HEADER_MAP);
            if (resultMap.isEmpty()) break;

            // 预期结果
            JSONObject expectJSON = JSONObject.parseObject(item.getExpectValue());
            String response = null;
            String statusCode;
            statusCode = resultMap.get("code");
            response = resultMap.get("response");
            if (statusCode.equals("200")) {
                // response中tjudge属性，根据expectValue对比
                JSONObject jsonObject = JSONObject.parseObject(response);
                // froms请求结果
                JSONArray forms = (JSONArray) jsonObject.get("forms");
                if (forms == null) break;
                // 遍历forms中subject_qid与预期结果对比
                for (Object tempJson : forms) {
                    JSONObject tempJsonObject = (JSONObject) JSONObject.toJSON(tempJson);
                    String subject_qid = tempJsonObject.getString("subject_qid");
                    int tjudge = tempJsonObject.getIntValue("tjudge");
                    // 对比预期结果，如果不为空说明有结果
                    Integer actualValue = expectJSON.getInteger(subject_qid);
                    if (actualValue != null) {
                        if (actualValue != tjudge) {
                            result.put(subject_qid, false);
                        } else {
                            result.put(subject_qid, true);
                        }
                    }
                }
            } else {
                result.put(item.getCaseName(), false);
            }
        }
        return Result.success(result);
    }
}
