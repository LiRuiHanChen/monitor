package com.monitor.argent.controller;

import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.api.Code;
import com.monitor.argent.commons.StudentRequestUtil;
import com.monitor.argent.entity.RegisterStudentResponse;
import com.monitor.argent.entity.StudentRegisterBody;
import com.monitor.argent.entity.StudentRegisterRequestBody;
import com.monitor.argent.model.Result;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class StudentController {

    @Resource
    StudentRequestUtil studentRequestUtil;

    String imei = "EA22A997-9E02-4380-8783-D0FFCDCDCA3E";
    String passwd = "test1234";
    private static final String[] numChars = {"壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖", "拾"};

    /**
     * 中学端学生注册--检查老师ID号有效性
     *
     * @param id 老师号
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/check/teacher", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> checkTeacher(@RequestParam String id) {
        if (StringUtils.isEmpty(id)) return Result.failure(Code.PARAMETER_MISSING, "参数异常");

        return studentRequestUtil.requestCheckClazzInfo(id);
    }

    /**
     * 中学端学生注册--手机号179xxxxxxxx
     *
     * @param studentRegisterBody
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    @CrossOrigin
    @RequestMapping(value = "/register/student", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> register(@RequestBody StudentRegisterBody studentRegisterBody) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (studentRegisterBody == null) return Result.failure(Code.PARAMETER_MISSING, "参数异常");

        int count = studentRegisterBody.getCount();
        if (count <= 0 || count > 10) return Result.failure(Code.BAD_REQUEST, "生成账号数量异常");

        //定长list，来源于前端指定长度 <=10
        List<RegisterStudentResponse> registerStudentResponseList = new ArrayList<>(count);
        String sessionKey = null;

        //根据需要生成的账号（count数量）来遍历姓名：（我是学生一）
        String realName = studentRegisterBody.getReal_name();
        //获取随机手机号
        ArrayList<String> mobileList = studentRequestUtil.loadMobileList(count, "179");
        for (int i = 0; i < count; i++) {
            //修改学生名字
            studentRegisterBody.setReal_name(realName + numChars[i]);
            // 取手机号
            String mobileNumber = mobileList.get(i);
            // 去除count属性
            Map<String, String> map = BeanUtils.describe(studentRegisterBody);
            map.remove("count");
            StudentRegisterRequestBody registerBody = new StudentRegisterRequestBody();
            registerBody = studentRequestUtil.mapToStudentRegisterBody(registerBody, map);
            /**
             * 需要统一判断调用接口中result是否为"400"
             */
            // 检查学生姓名、填涂号和选择的班级group是否匹配;判断result是否为success
            Result<Object> checkKlsResponse = studentRequestUtil.requestCheckKlsRegisterInfo(registerBody);
            if (checkKlsResponse != null) {
                if (checkKlsResponse.isSuccess()) {
                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(checkKlsResponse.getData());
                    String result = jsonObject.getString("result");
                    if (!result.equals("success")) {
                        return Result.failure(Code.BAD_REQUEST, "学生班级group不匹配");
                    }
                }
            }

            // 检查学生姓名是否重复,判断user_id是否为0
            Result<Object> checkDuplicateResponse = studentRequestUtil.requestCheckDuplicateName(registerBody);
            if (checkDuplicateResponse != null) {
                if (checkDuplicateResponse.isSuccess()) {
                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(checkDuplicateResponse.getData());
                    String result = jsonObject.getString("result");

                    if (!result.equals("success")) {
                        return Result.failure(Code.BAD_REQUEST, jsonObject.getString("message"));
                    }
                    int userId = jsonObject.getInteger("user_id");
                    if (userId != 0) return Result.failure(Code.BAD_REQUEST, "学生重复");
                }
            }

            // 判断手机号是否有效 "student_mobile_binded": true  "parent_mobile_binded": false,
            Result<Object> checkMobileResponse = studentRequestUtil.requestMobileCheck(registerBody, mobileNumber);
            if (checkMobileResponse != null) {
                if (checkMobileResponse.isSuccess()) {
                    // 判断请求状态
                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(checkMobileResponse.getData());
                    String result = jsonObject.getString("result");

                    if (!result.equals("success")) {
                        return Result.failure(Code.BAD_REQUEST, jsonObject.getString("message"));
                    }
                    Boolean binded = jsonObject.getBoolean("student_mobile_binded");
                    if (binded) return Result.failure(Code.BAD_REQUEST, "手机号绑定异常");
                }
            }

            //发送验证码
            Map<String, String> registerCodeParamMap = studentRequestUtil.parseBeanToMap(registerBody);
            registerCodeParamMap.remove("clazz_id");
            registerCodeParamMap.remove("real_name");
            registerCodeParamMap.put("user_type", "3");
            registerCodeParamMap.put("user_code", mobileNumber);
            registerCodeParamMap.put("imei", imei);
            Result<Object> getRegisterCodeResponse = studentRequestUtil.requestGetRegisterApi(registerCodeParamMap);

            if (getRegisterCodeResponse != null) {
                if (getRegisterCodeResponse.isSuccess()) {
                    // 判断请求状态
                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(getRegisterCodeResponse.getData());
                    String result = jsonObject.getString("result");
                    if (!result.equals("success")) {
                        String message = jsonObject.getString("message");
                        if (StringUtils.isEmpty(message)) {
                            return Result.failure(Code.BAD_REQUEST, "注册码异常");
                        }
                        return Result.failure(Code.BAD_REQUEST, message);
                    }
                }
            }
            // 注册
            Map<String, String> paramMap = studentRequestUtil.parseBeanToMap(registerBody);
            paramMap = studentRequestUtil.registerParamToMap(paramMap, mobileNumber, passwd);

            RegisterStudentResponse registerStudentResponse = new RegisterStudentResponse();
            Result<Object> registerResult = studentRequestUtil.requestRegisterApi(paramMap);

            if (registerResult != null) {
                if (registerResult.isSuccess()) {
                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(registerResult.getData());
                    String result = jsonObject.getString("result");
                    if (!result.equals("success")) {
                        return Result.failure(Code.BAD_REQUEST, jsonObject.getString("message"));
                    }
                    sessionKey = jsonObject.getString("session_key");
                    long user_id = jsonObject.getLong("user_id");
                    registerStudentResponse.setUserId(user_id);
                    registerStudentResponse.setMobile(mobileNumber);
                    registerStudentResponse.setRealName(registerBody.getReal_name());
                    registerStudentResponse.setPassword(passwd);
                    registerStudentResponseList.add(registerStudentResponse);
                }
            }

            //判断是否超过加入班级
            Map<String, String> paramJoin = studentRequestUtil.parseBeanToMap(registerBody);
            paramJoin.put("session_key", sessionKey);
            paramJoin.remove("sig");
            paramJoin.remove("real_name");
            Result<Object> joinClazz = studentRequestUtil.requestJoinSystemClazz(paramJoin);
            if (joinClazz != null) {
                if (joinClazz.isSuccess()) {
                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(joinClazz.getData());
                    String result = jsonObject.getString("result");
                    if (!result.equals("success")) {
                        return Result.failure(Code.BAD_REQUEST, jsonObject.getString("message"));
                    }
                }
            }
        }
        return Result.success(registerStudentResponseList);
    }

    /**
     * 小学端学生注册，检查老师ID有效性
     *
     * @param id 老师ID
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/signup/check/teacher/info", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> checkClazzInfo(@RequestParam String id) {
        if (StringUtils.isEmpty(id)) return Result.failure(Code.PARAMETER_MISSING, "参数异常");
        //检查并判断老师ID
        return studentRequestUtil.signupCheckClazzInfo(id);
    }

    /**
     * 小学注册---手机号：171xxxxxx
     *
     * @param studentRegisterBody
     * @return
     */
    @CrossOrigin
    @RequestMapping(value = "/signup/register", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> signupRegister(@RequestBody StudentRegisterBody studentRegisterBody) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (studentRegisterBody == null) return Result.failure(Code.PARAMETER_MISSING, "参数异常");
        long clazzId = studentRegisterBody.getClazz_id();
        long teacherId = studentRegisterBody.getTeacher_id();
        if (clazzId == 0 || teacherId == 0)
            return Result.failure(Code.PARAMETER_MISSING, "参数异常");

        if (StringUtils.isEmpty(studentRegisterBody.getReal_name()))
            return Result.failure(Code.PARAMETER_MISSING, "姓名不能为空");

        int count = studentRegisterBody.getCount();
        if (count <= 0 || count > 10) return Result.failure(Code.BAD_REQUEST, "生成账号数量异常");

        //定长list，来源于前端指定长度 <=10
        List<RegisterStudentResponse> registerStudentResponseList = new ArrayList<>(count);

        /*
         根据需要生成的账号（count数量）来遍历姓名：（我是学生一）
         */
        //获取随机手机号
        ArrayList<String> mobileList = studentRequestUtil.loadMobileList(count, "171");
        for (int i = 0; i < mobileList.size(); i++) {
            String realName = studentRegisterBody.getReal_name();
            // 检查学生姓名
            realName += numChars[i];
            Result<Object> checkSignupObject = studentRequestUtil.checkSignupRealName(realName, Long.toString(clazzId), Long.toString(teacherId));
            if (checkSignupObject.isSuccess()) {
                // 如果姓名校验结果为false
                if (!JSONObject.parseObject(String.valueOf(checkSignupObject.getData())).getString("result").equals("success"))
                    break;
                // 判断手机号是否有效 "student_mobile_binded": true  "parent_mobile_binded": false,
                // 检查手机号是否绑定
//                Result<Object> mobileBind = studentRequestUtil.requestSignupMobileCheck(Long.toString(teacherId), mobileList.get(i));
//                if (mobileBind != null) {
//                    if (mobileBind.isSuccess()) {
//                        // 判断请求状态
//                        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(mobileBind.getData());
//                        String result = jsonObject.getString("result");
//                        if (!result.equals("success")) {
//                            break;
//                        }
//                        Boolean binded = jsonObject.getBoolean("student_mobile_binded");
//                        if (binded) break;
//                    }
//                }
                // 发送验证码
                Result<Object> smsignResult = studentRequestUtil.signupSmsignSvc(mobileList.get(i), Long.toString(teacherId));
                if (smsignResult.isSuccess()) {
                    if (!JSONObject.parseObject(String.valueOf(smsignResult.getData())).getString("result").equals("success"))
                        break;
                }
                // 注册
                Map<String, String> paramMap = BeanUtils.describe(studentRegisterBody);
                paramMap = studentRequestUtil.signupRegisterParamToMap(paramMap, realName, mobileList.get(i), passwd);
                Result<Object> registerResult = studentRequestUtil.signupUser(paramMap);

                // 判断注册结果，组装返回
                if (registerResult.isSuccess()) {
                    JSONObject response = JSONObject.parseObject(String.valueOf(registerResult.getData()));
                    if (response == null || !response.getString("result").equals("success")) break;
//                    sessionKey = jsonObject.getString("session_key");
                    System.out.println(response.getString("session_key"));
                    RegisterStudentResponse registerStudentResponse = new RegisterStudentResponse();
                    registerStudentResponse.setPassword(passwd);
                    registerStudentResponse.setMobile(mobileList.get(i));
                    registerStudentResponse.setRealName(realName);
                    registerStudentResponse.setUserId(response.getLong("user_id"));
                    registerStudentResponseList.add(registerStudentResponse);
                    // 检查是否允许加入班级
//                Result<Object> joinSysResult = studentRequestUtil.requestSignupJoinSystemClazz(Long.toString(clazzId), Long.toString(teacherId));
                }
            }
        }
        return Result.success(registerStudentResponseList);
    }
}
