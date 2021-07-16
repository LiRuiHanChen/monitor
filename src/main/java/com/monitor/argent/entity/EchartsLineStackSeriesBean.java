package com.monitor.argent.entity;

import java.util.LinkedList;

/**
 * echarts折线图中Series属性
 */
public class EchartsLineStackSeriesBean {
    String name;
    String type = "line";
    String stack = "总量";
    LinkedList data = new LinkedList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public LinkedList getData() {
        return data;
    }

    public void setData(int data) {
        this.data.add(data);
    }
}
