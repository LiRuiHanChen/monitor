package com.monitor.argent.entity;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class ThreadBlockingBean {

    // 存储前端x时间轴数据
    LinkedList<String> xAxis = new LinkedList<>();
    // 存储前端图表中 series属性
    LinkedList<EchartsLineStackSeriesBean> series = new LinkedList<>();
    // 存储前端过滤标签
    LinkedHashSet<String> legendData = new LinkedHashSet<>();

    public LinkedList<String> getxAxis() {
        return xAxis;
    }

    public void setxAxis(LinkedList<String> xAxis) {
        this.xAxis = xAxis;
    }

    public LinkedList<EchartsLineStackSeriesBean> getSeries() {
        return series;
    }

    public void setSeries(LinkedList<EchartsLineStackSeriesBean> series) {
        this.series = series;
    }

    public Set<String> getLegendData() {
        return legendData;
    }

    public void setLegendData(LinkedHashSet<String> legendData) {
        this.legendData = legendData;
    }

}
