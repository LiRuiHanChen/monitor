package com.monitor.argent.entity;

/**
 * 学科枚举
 */

public enum SubjectEnum {

    ENGLISH("英语",1),CHINESE("语文",2),MATHEMATICS("数学",3);

    //成员变量
    private String name;
    private int index;

    //构造方法
    private SubjectEnum(String name,int index)
    {
        this.name=name;
        this.index=index;
    }

    //覆盖方法
    @Override
    public String toString()
    {
        return this.index+"-"+this.name;
    }

}
