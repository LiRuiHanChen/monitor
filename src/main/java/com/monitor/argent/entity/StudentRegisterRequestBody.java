package com.monitor.argent.entity;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class StudentRegisterRequestBody {

    @NotNull
    @Min(1)
    long teacher_id;
    @NotNull
    @Min(1)
    long clazz_id;
    @NotBlank
    @Max(6)
    @Min(1)
    String real_name;
    String app_key;

    public long getTeacher_id() {
        return teacher_id;
    }

    public void setTeacher_id(long teacher_id) {
        this.teacher_id = teacher_id;
    }

    public long getClazz_id() {
        return clazz_id;
    }

    public void setClazz_id(long clazz_id) {
        this.clazz_id = clazz_id;
    }

    public String getReal_name() {
        return real_name;
    }

    public void setReal_name(String real_name) {
        this.real_name = real_name;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }
}
