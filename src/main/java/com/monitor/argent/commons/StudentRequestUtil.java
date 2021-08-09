package com.monitor.argent.commons;

import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.api.Code;
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
    // 测试环境
    public static final String URL_PATH = "https://api.test.17zuoye.net";
    public static final String APP_KEY = "17JuniorStu";
    public static final String SECRET_KEY = "NzVe9rUVkWQt";

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
        return getObjectResult(paramMap, CHECK_CLAZZ_INFO);
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
        return getObjectResult(paramMap, CHECK_KLX_STUDENT_REGISTER_INFO);
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
        return getObjectResult(paramMap, CHECK_DUPLICATE_NAME);
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
        return getObjectResult(paramMap, MOBILE_BINDING_CHECK);
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
        return getObjectResult(paramMap, GET_REGISTER_CODE);
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
        return getObjectResult(paramMap, USER_REGISTER);
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
        return getObjectResult(map, JOIN_SYSTEM_CLAZZ);
    }

    /**
     * 通用请求方法
     *
     * @param paramMap
     * @param requestPathInfo
     * @return
     */
    private Result<Object> getObjectResult(Map<String, String> paramMap, String requestPathInfo) {
        try {
            HashMap<String, String> headerMap = new HashMap<>();
            headerMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
            Map<String, String> resultMap = httpRequestUtil.sendPostDataByMap(URL_PATH + requestPathInfo, headerMap, paramMap);
            if (!resultMap.isEmpty()) {
                if (resultMap.get("code").equals("200")) {
                    return Result.success(JSONObject.parseObject(resultMap.get("response")));
                } else {
                    return Result.failure(Integer.parseInt(resultMap.get("code")), resultMap.get("response"));
                }
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
    public ArrayList<String> loadMobileList(int count) {
        if (count == 0 || count >= 10) return null;
        ArrayList<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            StringBuilder phoneLastNum = new StringBuilder();
            phoneLastNum.append("179");//手机号标准格式
            for (int j = 0; j < 8; j++) {
                //每次循环都从0~9挑选一个随机数
                phoneLastNum.append((int) (Math.random() * 10));
            }
            list.add(phoneLastNum.toString());
        }
        return list;
    }
}
