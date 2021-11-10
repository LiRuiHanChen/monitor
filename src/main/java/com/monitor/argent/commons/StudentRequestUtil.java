package com.monitor.argent.commons;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.api.Code;
import com.monitor.argent.entity.StudentRegisterBody;
import com.monitor.argent.entity.StudentRegisterRequestBody;
import com.monitor.argent.model.Result;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 中学端学生注册
 */
@Component
public class StudentRequestUtil {

    private static final String CHECK_CLAZZ_INFO = "/v1/student/clazz/checkclazzinfo.vpage";
    private static final String CHECK_KLX_STUDENT_REGISTER_INFO = "/v1/student/clazz/checkklxstudentregisterinfo.vpage";
    private static final String CHECK_DUPLICATE_NAME = "/v1/student/clazz/checkduplicatename.vpage";
    private static final String JOIN_SYSTEM_CLAZZ = "/v1/student/clazz/joinsystemclazz.vpage";
    private static final String MOBILE_BINDING_CHECK = "/v1/student/mobilebindingcheck.vpage";
    private static final String USER_REGISTER = "/v1/user/register.api";
    private static final String GET_REGISTER_CODE = "/v1/user/register/verifycode/get.api";
    //    private static final String SIGNUP_CHECK_CLAZZ_INFO = "/signup/checkclazzinfo.api";
//    private static final String SIGNUP_FILTER_NAME = "/signup/filtersensitiveusername.api";
    private static final String SIGNUP_SVC_API = "/signup/smsignsvc.api";
    private static final String SIGNUP_SIGNUP_API = "/signup/signup.api";
    // 测试环境
    public static final String URL_PATH = "https://api.test.17zuoye.net";
    public static final String SIGNUP_URL_PATH = "https://ucenter.test.17zuoye.net";
    public static final String APP_KEY = "17JuniorStu";
    // 专用小学key
    public static final String SIGNUP_APP_KEY = "17Student";
    public static final String SECRET_KEY = "NzVe9rUVkWQt";
    // 专用小学key
    public static final String SIGNUP_SECRET_KEY = "kuLwGZMJBcQj";

    @Resource
    HttpRequestUtil httpRequestUtil;
    @Resource
    DigestSignUtils digestSignUtils;

    /**
     * 计算sig
     */
    public String loadSigValue(String secretKey, Map<String, String> paramMap) {
        if (secretKey.isEmpty() || paramMap.isEmpty()) return null;

        return digestSignUtils.signMd5(paramMap, secretKey);
    }

    /**
     * 根据老师号查找可加入的班级列表
     */
    public Result<Object> requestCheckClazzInfo(String id) {
        //计算sig
        Map<String, String> paramMap = new HashMap();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("id", id);
        String sig = loadSigValue(SECRET_KEY, paramMap);
        if (StringUtils.isEmpty(sig)) {
            return Result.failure(Code.BAD_REQUEST, "sig计算失败");
        }
        paramMap.put("sig", sig);
        // 请求
        return getObjectResult(paramMap, URL_PATH, CHECK_CLAZZ_INFO);
    }

    /**
     * 学生注册时，检查输入的姓名 填涂号和选择的班级group是否匹配
     *
     * @param studentRegisterBody
     * @return
     */
    public Result<Object> requestCheckKlsRegisterInfo(StudentRegisterRequestBody studentRegisterBody) {
        //解析参数并计算sig
        Map<String, String> paramMap = null;
        try {
            paramMap = parseBeanToMap(studentRegisterBody);
            paramMap.put("scan_number", "");
            String sig = loadSigValue(SECRET_KEY, paramMap);
            if (!StringUtils.isEmpty(sig)) {
                paramMap.put("sig", sig);
            } else {
                return Result.failure(Code.BAD_REQUEST, "sig计算失败");
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        // 请求
        return getObjectResult(paramMap, URL_PATH, CHECK_KLX_STUDENT_REGISTER_INFO);
    }

    /**
     * 注册的时候检查学生姓名是否重复，如果重复则返回已有学生的id
     *
     * @param studentRegisterBody
     * @return
     */
    public Result<Object> requestCheckDuplicateName(StudentRegisterRequestBody studentRegisterBody) {
        //解析参数并计算sig
        Map<String, String> paramMap = null;
        try {
            paramMap = parseBeanToMap(studentRegisterBody);
            String sig = loadSigValue(SECRET_KEY, paramMap);
            if (!StringUtils.isEmpty(sig)) {
                paramMap.put("sig", sig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 请求
        return getObjectResult(paramMap, URL_PATH, CHECK_DUPLICATE_NAME);
    }

    /**
     * 检查手机号是否被注册
     *
     * @param studentRegisterBody
     * @return
     */
    public Result<Object> requestMobileCheck(StudentRegisterRequestBody studentRegisterBody, String userCode) {
        Map<String, String> paramMap = new HashMap<>();
        try {
            paramMap = parseBeanToMap(studentRegisterBody);
            //增加user_code
            paramMap.put("user_code", userCode);
            paramMap.remove("clazz_id");
            paramMap.remove("real_name");
            String sig = loadSigValue(SECRET_KEY, paramMap);
            if (!StringUtils.isEmpty(sig)) {
                paramMap.put("sig", sig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 请求
        return getObjectResult(paramMap, URL_PATH, MOBILE_BINDING_CHECK);
    }

    /**
     * 发送验证码
     *
     * @param paramMap
     * @return
     */
    public Result<Object> requestGetRegisterApi(Map<String, String> paramMap) {
        try {
            String sig = loadSigValue(SECRET_KEY, paramMap);
            if (!StringUtils.isEmpty(sig)) {
                paramMap.put("sig", sig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 请求
        return getObjectResult(paramMap, URL_PATH, GET_REGISTER_CODE);
    }

    /**
     * 注册用户
     *
     * @param paramMap <String, String>
     * @return
     */
    public Result<Object> requestRegisterApi(Map<String, String> paramMap) {
        try {
            String sig = loadSigValue(SECRET_KEY, paramMap);
            if (!StringUtils.isEmpty(sig)) {
                paramMap.put("sig", sig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 请求
        return getObjectResult(paramMap, URL_PATH, USER_REGISTER);
    }

    /**
     * 加入系统班级
     *
     * @return
     */
    public Result<Object> requestJoinSystemClazz(Map<String, String> map) {
        /*
            1.去除real_name属性
            2.解析参数并计算sig
        */
        try {
            String sig = loadSigValue(SECRET_KEY, map);
            if (!StringUtils.isEmpty(sig)) {
                map.put("sig", sig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 请求
        return getObjectResult(map, URL_PATH, JOIN_SYSTEM_CLAZZ);
    }

    /**
     * 通用请求方法
     *
     * @param paramMap
     * @param requestPathInfo
     * @return
     */
    private Result<Object> getObjectResult(Map<String, String> paramMap, String urlPath, String requestPathInfo) {
        try {
            HashMap<String, String> headerMap = new HashMap<>();
            headerMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
            Map<String, String> resultMap = httpRequestUtil.sendPostDataByMap(urlPath + requestPathInfo, headerMap, paramMap);
            if (!resultMap.isEmpty()) {
                if (resultMap.get("code").equals("500")||resultMap.get("code").equals("502")) {
                    return Result.success(resultMap.get("response"));
                }
                return Result.success(JSONObject.parseObject(resultMap.get("response")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure(Code.BAD_REQUEST, "请求失败");
    }

    /**
     * bean转map
     *
     * @param studentRegisterBody
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    public Map<String, String> parseBeanToMap(StudentRegisterRequestBody studentRegisterBody) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return BeanUtils.describe(studentRegisterBody);
    }

    public Map<String, String> parseSignupBeanToMap(StudentRegisterBody studentRegisterBody) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return BeanUtils.describe(studentRegisterBody);
    }

    public StudentRegisterRequestBody mapToStudentRegisterBody(StudentRegisterRequestBody studentRegisterBody, Map<String, String> map) {
        try {
            BeanUtils.populate(studentRegisterBody, map);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return studentRegisterBody;
    }

    /**
     * 构建注册的请求参数
     *
     * @param paramMap
     * @param mobileNumber
     * @param passwd
     * @return
     */
    public Map<String, String> registerParamToMap(Map<String, String> paramMap, String mobileNumber, String passwd) {
        paramMap.remove("teacher_id");//去除teacher_id
        paramMap.put("passwd", passwd);
        paramMap.put("verify_code", "1234");
        paramMap.put("user_type", "3");
        paramMap.put("nick_name", "");
        paramMap.put("avatar_dat", "");
        paramMap.put("_cid", "18-1-0");
        paramMap.put("invitor_id", "");
        paramMap.put("user_code", mobileNumber);
        return paramMap;
    }

    /**
     * 随机生成手机号
     *
     * @return
     */
    public ArrayList<String> loadMobileList(int count, String phoneNumber) {
        if (count == 0 || count >= 10) return null;
        ArrayList<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            StringBuilder phoneLastNum = new StringBuilder();
            phoneLastNum.append(phoneNumber);//手机号标准格式
            for (int j = 0; j < 8; j++) {
                //每次循环都从0~9挑选一个随机数
                phoneLastNum.append((int) (Math.random() * 10));
            }
            list.add(phoneLastNum.toString());
        }
        return list;
    }

    /**
     * 小学学生注册--检查老师ID
     *
     * @param id
     * @return
     */
    public Result<Object> signupCheckClazzInfo(String id) {
        //计算sig
        Map<String, String> paramMap = new HashMap();
        paramMap.put("app_key", SIGNUP_APP_KEY);
        paramMap.put("id", id);
        String sig = loadSigValue(SIGNUP_SECRET_KEY, paramMap);
        if (StringUtils.isEmpty(sig)) {
            return Result.failure(Code.BAD_REQUEST, "sig计算失败");
        }
        paramMap.put("sig", sig);
        // 请求
        return getObjectResult(paramMap, URL_PATH, CHECK_CLAZZ_INFO);
    }

    /**
     * 小学端注册--检查姓名有效性
     *
     * @param name
     * @return
     */
    public Result<Object> checkSignupRealName(String name, String clazzId, String teacherId) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("real_name", name);
        paramMap.put("clazz_id", clazzId);
        paramMap.put("teacher_id", teacherId);
        paramMap.put("app_key", SIGNUP_APP_KEY);
        try {
            String sig = loadSigValue(SIGNUP_SECRET_KEY, paramMap);
            if (!StringUtils.isEmpty(sig)) {
                paramMap.put("sig", sig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getObjectResult(paramMap, URL_PATH, CHECK_DUPLICATE_NAME);
    }

    /**
     * 小学学生--判断是否可以加入班级
     *
     * @return
     */
    public Result<Object> requestSignupJoinSystemClazz(String clazzId, String teacherId) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("clazz_id", clazzId);
        paramMap.put("teacher_id", teacherId);
        paramMap.put("app_key", SIGNUP_APP_KEY);
        try {
            String sig = loadSigValue(SIGNUP_SECRET_KEY, paramMap);
            if (!StringUtils.isEmpty(sig)) {
                paramMap.put("sig", sig);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getObjectResult(paramMap, URL_PATH, JOIN_SYSTEM_CLAZZ);
    }

    /**
     * 小学学生注册--检查手机号是否绑定
     *
     * @param phoneNumber
     * @return
     */
    public Result<Object> requestSignupMobileCheck(String teacherId, String phoneNumber) {
        Map<String, String> paramMap = new HashMap<>();
        //增加user_code
        paramMap.put("user_code", phoneNumber);
        paramMap.put("teacher_id", teacherId);
        String sig = loadSigValue(SIGNUP_SECRET_KEY, paramMap);
        if (!StringUtils.isEmpty(sig)) {
            paramMap.put("sig", sig);
        }
        // 请求
        return getObjectResult(paramMap, URL_PATH, MOBILE_BINDING_CHECK);
    }

    /**
     * 小学端注册--手机号发送验证码
     */
    public Result<Object> signupSmsignSvc(String mobile, String teacher_id) {
        Map<String, String> paramMap = new HashMap();
        paramMap.put("user_type", "3");
        paramMap.put("user_code", mobile);
        paramMap.put("teacher_id", teacher_id);
        paramMap.put("imei", "ed942adf-047f-4101-a542-d4dfcf1cac9d");
        paramMap.put("app_key", SIGNUP_APP_KEY);
        String sig = loadSigValue(SIGNUP_SECRET_KEY, paramMap);
        if (!StringUtils.isEmpty(sig)) {
            paramMap.put("sig", sig);
        }
        // 请求
        return getObjectResult(paramMap, URL_PATH, GET_REGISTER_CODE);
    }

    /**
     * 小学注册---构造请求参数
     *
     * @param paramMap
     * @param mobileNumber
     * @param passwd
     * @return
     */
    public Map<String, String>
    signupRegisterParamToMap(Map<String, String> paramMap, String realName, String mobileNumber, String passwd) {
        paramMap.remove("count");
        paramMap.remove("teacher_id");
        paramMap.put("passwd", passwd);
        paramMap.put("verify_code", "1234");
        paramMap.put("user_type", "3");
        paramMap.put("nick_name", "");
        paramMap.put("avatar_dat", "");
        paramMap.put("_cid", "15-1-0");
        paramMap.put("invitor_id", "");
        paramMap.put("user_code", mobileNumber);
        paramMap.put("app_key", SIGNUP_APP_KEY);
        String sig = loadSigValue(SIGNUP_SECRET_KEY, paramMap);
        if (!StringUtils.isEmpty(sig)) {
            paramMap.put("sig", sig);
        }
        return paramMap;
    }

    public Result<Object> signupUser(Map<String, String> paramMap) {
        return getObjectResult(paramMap, URL_PATH, USER_REGISTER);
    }


}
