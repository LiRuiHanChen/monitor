package com.monitor.argent.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.api.Code;
import com.monitor.argent.commons.UnitConversion;
import com.monitor.argent.entity.HomeWorkRequestBean;
import com.monitor.argent.entity.HomeWorkResponseBody;
import com.monitor.argent.model.Result;
import com.monitor.argent.service.HomeWorkRequestImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    private static final String KIBANA_WORK_HOST = "http://10.8.14.85:5601/api/console/proxy?path=_search&method=POST";
    private static final String PAPER_WIDTH_WORK_HOST = "http://192.168.100.79:1889/?service=com.voxlearning.pour-point.service.task&method=getPicture&version=1.0.0&mode=legacy&group=alps-hydra-test";
    private static final String HOME_WORK_URL = "/DotMatrix/infer";
    private static HashMap HOME_WORK_HEADER_MAP = new HashMap();
    private static HashMap KINABA_DATA_HEADER_MAP = new HashMap();

    public void createKinHeaderMap() {
        KINABA_DATA_HEADER_MAP.put("Content-Type", "application/json");
        KINABA_DATA_HEADER_MAP.put("Accept", "text/plain, */*; q=0.01");
        KINABA_DATA_HEADER_MAP.put("Accept-Encoding", "gzip, deflate");
        KINABA_DATA_HEADER_MAP.put("Accept-Language", "zh-CN,zh;q=0.9");
        KINABA_DATA_HEADER_MAP.put("Connection", "keep-alive");
        KINABA_DATA_HEADER_MAP.put("kbn-version", "6.3.0");
        KINABA_DATA_HEADER_MAP.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36");
    }

    public void createHeaderMap() {
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

    @RequestMapping(value = "/getKibanaWork", method = RequestMethod.GET)
    @ResponseBody
    public Result<Object> getKibanaWork(@RequestParam("dzbId") String dzbId) {
        if (StringUtils.isEmpty(dzbId)) return Result.failure(Code.PARAMETER_MISSING, "参数为空");
        JSONObject data = unitConversion.createQueryJSON(dzbId);
        JSONObject paperWidthData = unitConversion.createPaperWidthData(dzbId);
        String paperWidth = null;

        if (data == null) return Result.failure(Code.BAD_REQUEST, "参数异常");
        createKinHeaderMap();
        createHeaderMap();

        Map<String, String> kibanaResult = homeWorkRequestImpl.getKinabaData(KIBANA_WORK_HOST, data.toJSONString(), KINABA_DATA_HEADER_MAP);
        // 获取paperWidth值
        Map<String, String> paperWidthResult = homeWorkRequestImpl.getKinabaData(PAPER_WIDTH_WORK_HOST, paperWidthData.toJSONString(), HOME_WORK_HEADER_MAP);
        if (paperWidthResult.get("code").equals("200")) {
            String response = paperWidthResult.get("response");
            JSONObject object = JSONObject.parseObject(response);
            if (object.getIntValue("code") == 0) {
                paperWidth = JSONObject.parseObject(object.get("data").toString()).getString("paperWidth");
            }
        }
        kibanaResult.put("paperWidth", paperWidth);

        return Result.success(kibanaResult);
    }
}
