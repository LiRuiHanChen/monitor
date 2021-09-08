package com.monitor.argent.entity;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class TeacherRegisterRequestBody {

    @NotBlank
    @Min(1)
    @Max(6)
    String realName;

    @NotBlank
    @Min(8)
    @Max(16)
    String passWord;

    @NotBlank
    @Min(11)
    @Max(11)
    String mobile;

    @NotNull
    int registerType;

    @NotNull
    int[] SubjectTypeList;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getRegisterType() {
        return registerType;
    }

    public void setRegisterType(int registerType) {
        this.registerType = registerType;
    }

    public int[] getSubjectTypeList() {
        return SubjectTypeList;
    }

    public void setSubjectTypeList(int[] subjectTypeList) {
        SubjectTypeList = subjectTypeList;
    }
}
