package com.monitor.argent.controller;

import com.monitor.argent.api.Code;
import com.monitor.argent.commons.TeacherRequestUtil;
import com.monitor.argent.entity.TeacherRegisterRequestBody;
import com.monitor.argent.model.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class TeacherController {
    @Resource
    TeacherRequestUtil teacherRequestUtil;

    @RequestMapping(value = "/load/city", method = RequestMethod.GET)
    @ResponseBody
    public Result<Object> checkTeacher(@RequestParam int id) {
        if (id == 0) return Result.failure(Code.PARAMETER_MISSING, "参数异常");
        return teacherRequestUtil.getCityApi(id);
    }

    @RequestMapping(value = "/load/school", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> loadSchool(@RequestParam("cityIdList") int[] cityIdList) {
        if (StringUtils.isEmpty(cityIdList)) return Result.failure(Code.PARAMETER_MISSING, "参数异常");

        return teacherRequestUtil.getSchoolApi(cityIdList);
    }

    @RequestMapping(value = "/teacher/register", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> loadSchool(@RequestBody TeacherRegisterRequestBody teacherRegisterRequestBody) {
        if (teacherRegisterRequestBody == null) return Result.failure(Code.PARAMETER_MISSING, "参数异常");

        return teacherRequestUtil.registerTeacher(teacherRegisterRequestBody);
    }
}
