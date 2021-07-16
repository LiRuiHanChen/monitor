package com.monitor.argent.entity;

import com.alibaba.fastjson.JSONArray;

public class JvmInfoBean {

    JSONArray runtimeList;
    JSONArray classLoadingList;
    JSONArray compilationList;
    JSONArray garbageCollectorsList;
    JSONArray memoryManagersList;
    JSONArray memoryList;
    JSONArray operatingSystemList;
    JSONArray threadInfoList;
    JSONArray fileDescriptorList;

    public JSONArray getRuntimeList() {
        return runtimeList;
    }

    public void setRuntimeList(JSONArray runtimeList) {
        this.runtimeList = runtimeList;
    }

    public JSONArray getClassLoadingList() {
        return classLoadingList;
    }

    public void setClassLoadingList(JSONArray classLoadingList) {
        this.classLoadingList = classLoadingList;
    }

    public JSONArray getCompilationList() {
        return compilationList;
    }

    public void setCompilationList(JSONArray compilationList) {
        this.compilationList = compilationList;
    }

    public JSONArray getGarbageCollectorsList() {
        return garbageCollectorsList;
    }

    public void setGarbageCollectorsList(JSONArray garbageCollectorsList) {
        this.garbageCollectorsList = garbageCollectorsList;
    }

    public JSONArray getMemoryManagersList() {
        return memoryManagersList;
    }

    public void setMemoryManagersList(JSONArray memoryManagersList) {
        this.memoryManagersList = memoryManagersList;
    }

    public JSONArray getMemoryList() {
        return memoryList;
    }

    public void setMemoryList(JSONArray memoryList) {
        this.memoryList = memoryList;
    }

    public JSONArray getOperatingSystemList() {
        return operatingSystemList;
    }

    public void setOperatingSystemList(JSONArray operatingSystemList) {
        this.operatingSystemList = operatingSystemList;
    }

    public JSONArray getThreadInfoList() {
        return threadInfoList;
    }

    public void setThreadInfoList(JSONArray threadInfoList) {
        this.threadInfoList = threadInfoList;
    }

    public JSONArray getFileDescriptorList() {
        return fileDescriptorList;
    }

    public void setFileDescriptorList(JSONArray fileDescriptorList) {
        this.fileDescriptorList = fileDescriptorList;
    }
}
