package com.monitor.argent.commons;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.api.Code;
import com.monitor.argent.entity.TeacherRegisterRequestBody;
import com.monitor.argent.model.Result;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class TeacherRequestUtil {

    private static final String CITY_API = "/map/nodes.api";
    private static final String SEARCH_SCHOOL_API = "/school/areaschoolrs.api";
    private static final String QR_TOKEN_API = "/qr/token.api";
    private static final String QR_CHECK_API = "/qr/check.api";
    private static final String SIGNUP_TMSIGNSVC_API = "/signup/tmsignsvc.api";
    private static final String SIGNUP_VALIDATE_MOBILE_ONLY_API = "/signup/validatemobileonly.api";
    private static final String SIGNUP_FILTER_SENSITIVE_USERNAME_API = "/signup/filtersensitiveusername.api";
    private static final String SIGNUP_MSIGNUP_API = "/signup/msignup.api";
    private static final String TEACHER_GUIDE_SELECT_SCHOOL_SUBJECT_API = "/teacher/guide/selectschoolsubject.api";

    // 测试环境
    public static final String URL_PATH = "https://ucenter.test.17zuoye.net";

    @Resource
    HttpRequestUtil httpRequestUtil;

    /**
     * 根据省份查询城市
     *
     * @param id
     * @return
     */
    public Result<Object> getCityApi(int id) {
        HashMap<String, String> paramMap = new HashMap<>();
        try {
            paramMap.put("id", String.valueOf(id));
            Map<String, String> resultMap = httpRequestUtil.sendGetData(URL_PATH + CITY_API, null, paramMap);
            if (!resultMap.isEmpty()) {
                if (resultMap.get("code").equals("200")) {
                    String response = resultMap.get("response");
                    if (!StringUtils.isEmpty(response)) {
                        return Result.success(JSONObject.parseArray(response));
                    }
                } else {
                    return Result.failure(Integer.parseInt(resultMap.get("code")), resultMap.get("response"));
                }
            }
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Result.failure(Code.BAD_REQUEST, "请求失败");
    }

    /**
     * 根据城市查询学校
     *
     * @param schoolArray
     * @return
     */
    public Result<Object> getSchoolApi(int[] schoolArray) {
        if (StringUtils.isEmpty(schoolArray)) return Result.failure(Code.BAD_REQUEST, "学校参数异常");
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("level", "SENIOR_SCHOOL");
        paramMap.put("regions", Arrays.toString(schoolArray));
        try {
            Map<String, String> resultMap = httpRequestUtil.sendGetData(URL_PATH + SEARCH_SCHOOL_API, null, paramMap);
            if (!resultMap.isEmpty()) {
                if (resultMap.get("code").equals("200")) {
                    String response = resultMap.get("response");
                    if (!StringUtils.isEmpty(response)) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (!jsonObject.isEmpty()) {
                            return Result.success(jsonObject.get("rows"));
                        }
                    }
                } else {
                    return Result.failure(Integer.parseInt(resultMap.get("code")), resultMap.get("response"));
                }
            }
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Result.failure(Code.BAD_REQUEST, "查询学校信息失败");
    }

    /**
     * 获取QR token
     *
     * @return
     */
    public Result<Object> getQrToken() {
        try {
            Map<String, String> resultMap = httpRequestUtil.sendPostDataByMap(URL_PATH + QR_TOKEN_API, null, null);
            if (!resultMap.isEmpty()) {
                if (resultMap.get("code").equals("200")) {
                    String response = resultMap.get("response");
                    if (!StringUtils.isEmpty(response)) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (!jsonObject.isEmpty()) {
                            if (jsonObject.getBoolean("success")) {
                                return Result.success(jsonObject.get("qrToken"));
                            }
                        }
                    }
                } else {
                    return Result.failure(Integer.parseInt(resultMap.get("code")), resultMap.get("response"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure(Code.BAD_REQUEST, "获取QRToken失败");
    }

    /**
     * 校验qr token
     *
     * @param qrToken
     * @return
     */
    public Result<Object> checkQrToken(String qrToken) {
        if (StringUtils.isEmpty(qrToken)) return Result.failure(Code.PARAMETER_MISSING, "缺少参数");

        Map<String, String> resultMap;
        try {
            resultMap = httpRequestUtil.sendPostDataByMap(URL_PATH + QR_CHECK_API, null, null);
            if (resultMap.isEmpty()) {
                return Result.failure(Code.BAD_REQUEST, "获取QRToken失败");
            }
            if (resultMap.get("code").equals("200")) {
                String response = resultMap.get("response");
                if (!StringUtils.isEmpty(response)) {
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (!jsonObject.isEmpty()) {
                        if (jsonObject.getBoolean("success")) {
                            return Result.success(jsonObject);
                        }
                    }
                }
            } else {
                return Result.failure(Integer.parseInt(resultMap.get("code")), resultMap.get("response"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure(Code.BAD_REQUEST, "获取QRToken失败");
    }

    /**
     * 发送验证码
     *
     * @param mobileNumber
     * @param count
     * @param captchaToken
     * @param cid
     * @return
     */
    public Result<Object> sendSignupSvc(String mobileNumber, int count, String captchaToken, String cid, String captchaCode) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("mobile", mobileNumber);
        paramMap.put("count", String.valueOf(count));
        paramMap.put("captchaToken", captchaToken);
        paramMap.put("cid", cid);
        paramMap.put("captchaCode", captchaCode);
        try {
            Map<String, String> resultMap = httpRequestUtil.sendPostDataByMap(URL_PATH + SIGNUP_TMSIGNSVC_API, null, paramMap);
            if (!resultMap.isEmpty()) {
                if (resultMap.get("code").equals("200")) {
                    String response = resultMap.get("response");
                    if (!StringUtils.isEmpty(response)) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (!jsonObject.isEmpty()) {
                            if (jsonObject.getBoolean("success")) {
                                return Result.success(jsonObject);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure(Code.BAD_REQUEST, "验证码发送失败");
    }

    /**
     * 验证手机验证码
     *
     * @param mobileNumber
     * @return
     */
    public Result<Object> validateSignupMobile(String mobileNumber) {
        Map<String, String> paramMap = new HashMap<>();
        try {
            paramMap.put("mobile", mobileNumber);
            paramMap.put("code", String.valueOf(1234));
            Map<String, String> resultMap = httpRequestUtil.sendPostDataByMap(URL_PATH + SIGNUP_VALIDATE_MOBILE_ONLY_API, null, paramMap);
            if (!resultMap.isEmpty()) {
                if (resultMap.get("code").equals("200")) {
                    String response = resultMap.get("response");
                    if (!StringUtils.isEmpty(response)) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (!jsonObject.isEmpty()) {
                            if (jsonObject.getBoolean("success")) {
                                return Result.success(jsonObject);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure(Code.BAD_REQUEST, "验证码发送失败");
    }

    /**
     * 校验姓名
     *
     * @param userName
     * @return
     */
    public Result<Object> filterSensitiveUserName(String userName) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userName", userName);

        try {
            Map<String, String> resultMap = httpRequestUtil.sendPostDataByMap(URL_PATH + SIGNUP_FILTER_SENSITIVE_USERNAME_API, null, paramMap);
            if (!resultMap.isEmpty()) {
                if (resultMap.get("code").equals("200")) {
                    String response = resultMap.get("response");
                    if (!StringUtils.isEmpty(response)) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (!jsonObject.isEmpty()) {
                            if (jsonObject.getBoolean("success")) {
                                return Result.success(jsonObject);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure(Code.BAD_REQUEST, "老师姓名匹配异常");
    }

    /**
     * 注册老师
     *
     * @param mobile
     * @param realName
     * @param password
     * @param registerType
     * @return
     */
    public Result<Object> mSignup(String mobile, String realName, String password, int registerType) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("code", String.valueOf(1234));
        paramMap.put("userType", String.valueOf(1));
        paramMap.put("mobile", mobile);
        paramMap.put("role", "ROLE_TEACHER");
        paramMap.put("realname", realName);
        paramMap.put("password", password);
        paramMap.put("registerType", String.valueOf(registerType));
        paramMap.put("inviteInfo", "");
        paramMap.put("dataKey", "");
        paramMap.put("webSource", "");
        paramMap.put("_cid", "17-2-0");
        try {
            Map<String, String> resultMap = httpRequestUtil.sendPostDataByMap(URL_PATH + SIGNUP_MSIGNUP_API, null, paramMap);
            if (!resultMap.isEmpty()) {
                if (resultMap.get("code").equals("200")) {
                    String response = resultMap.get("response");
                    if (!StringUtils.isEmpty(response)) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (!jsonObject.isEmpty()) {
                            if (jsonObject.getBoolean("success")) {
                                return Result.success(jsonObject);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure(Code.BAD_REQUEST, "老师注册异常");
    }

    /**
     * 整合一键注册老师
     *
     * @param teacherRegisterRequestBody
     * @return
     */
    public Result<Object> registerTeacher(TeacherRegisterRequestBody teacherRegisterRequestBody) {

        String mobile = teacherRegisterRequestBody.getMobile();
        String passWord = teacherRegisterRequestBody.getPassWord();
        String realName = teacherRegisterRequestBody.getRealName();
        int registerType = teacherRegisterRequestBody.getRegisterType();

        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(passWord) || StringUtils.isEmpty(realName)) {
            return Result.failure(Code.PARAMETER_MISSING, "缺少参数");
        }
        //1. 获取token
        Result<Object> qrTokenResult = getQrToken();
        if (qrTokenResult.isSuccess()) {
            String qrToken = (String) qrTokenResult.getData();
            if (!StringUtils.isEmpty(qrToken)) {//获取响应内容
                //2. 检查token
                Result<Object> checkQrTokenResult = checkQrToken(qrToken);
                if (checkQrTokenResult.isSuccess()) {
                    //3. 发送验证码
                    Result<Object> sendSignupSvcResult = sendSignupSvc(mobile, 1, "ETDJpLQuZXyQh8bw3dCWC81w", "kQG83bpQDj", "1234");
                    if (sendSignupSvcResult.isSuccess()) {
                        //4. 校验手机验证码
                        Result<Object> validateResult = validateSignupMobile(mobile);

                    }
                }
            }
        }

        return Result.failure(Code.BAD_REQUEST, "请求异常");
    }

}
