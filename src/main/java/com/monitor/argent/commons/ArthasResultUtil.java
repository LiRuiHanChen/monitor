package com.monitor.argent.commons;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.entity.MemoryBean;
import com.monitor.argent.entity.ThreadBlockingBean;
import com.monitor.argent.model.Result;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;

@Component
public class ArthasResultUtil {

    public static ThreadBlockingBean threadBlockingBean = new ThreadBlockingBean();
    @Resource
    UnitConversion unitConversion;

    /**
     * 解析命令和结果
     */
    public String parseResultByCommand(String command, Result<Object> objectResult) {
        if (command.isEmpty() || objectResult == null) return null;

        String result;
        //根据不同命令提取信息
        switch (command) {
            case "dashboard -i 1000 -n 1":
                result = convertToMemoryBean(this.loadMemoryInfo(objectResult)).toJSONString();
                break;
            case "thread -i 1000 -n 5":
                result = this.convertToBusyThread(objectResult).toJSONString();
                break;
            case "thread --state BLOCKED":
            case "thread –-state TIMED_WAITING":
            case "thread --state WAITING":
            case "thread --state RUNNABLE":
                JSONArray jsonArray = this.convertToThreadStateBean(objectResult);
                result = JSON.toJSONString(jsonArray);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
        return result;
    }

    /**
     * 获取dashboard命令中返回的内存信息
     */
    public JSONObject loadMemoryInfo(Result<Object> objectResult) {
        if (objectResult == null || !objectResult.isSuccess()) {
            return null;
        }

        JSONArray jsonArray = getArthasResponseResults(objectResult);
        if (!jsonArray.isEmpty()) {
            for (Object object : jsonArray) {
                JSONObject tempJSON = (JSONObject) JSONObject.toJSON(object);

                if (tempJSON.isEmpty()) break;
                if (tempJSON.getString("type").equals("dashboard")) {
                    return tempJSON.getJSONObject("memoryInfo");
                }
            }
        }
        return null;
    }

    /**
     * 转换内存信息
     * 1.单位值由byte转为MB
     * 2.增加占用比例
     */
    public JSONArray convertToMemoryBean(JSONObject memoryObject) {
        if (memoryObject.isEmpty()) return null;

        JSONArray heapArray = memoryObject.getJSONArray("heap");
        JSONArray noHeapArray = memoryObject.getJSONArray("nonheap");
        ArrayList<JSONObject> memoryBeanList = new ArrayList<>(heapArray.size() + noHeapArray.size());

        if (!heapArray.isEmpty()) {
            for (Object object : heapArray) {
                JSONObject tempJSON = (JSONObject) JSONObject.toJSON(object);
                MemoryBean memoryBean = tempJSON.toJavaObject(MemoryBean.class);
                memoryBeanList.add(unitConversion.memoryBeanAttributeUnitConversion(memoryBean));
            }
        }
        if (!noHeapArray.isEmpty()) {
            for (Object object : noHeapArray) {
                JSONObject tempJSON = (JSONObject) JSONObject.toJSON(object);
                MemoryBean memoryBean = tempJSON.toJavaObject(MemoryBean.class);
                memoryBeanList.add(unitConversion.memoryBeanAttributeUnitConversion(memoryBean));
            }
        }
        return JSONArray.parseArray(memoryBeanList.toString());
    }

    /**
     * 处理单位时间内最繁忙的N条信息
     */
    public JSONArray convertToBusyThread(Result<Object> objectResult) {
        if (objectResult == null || !objectResult.isSuccess()) {
            return null;
        }

        JSONArray jsonArray = getArthasResponseResults(objectResult);
        if (jsonArray.isEmpty()) {
            return null;
        }
        for (Object o : jsonArray) {
            JSONObject tempJSON = (JSONObject) JSONObject.toJSON(o);
            if (tempJSON.getString("type").equals("thread")) {
                return tempJSON.getJSONArray("busyThreads");
            }
        }
        return null;
    }

    /**
     * 线程状态查询结果转换
     */
    public JSONArray convertToThreadStateBean(Result<Object> objectResult) {
        if (objectResult == null || !objectResult.isSuccess()) {
            return null;
        }

        JSONArray jsonArray = getArthasResponseResults(objectResult);
        if (jsonArray.isEmpty()) {
            return null;
        }
        JSONArray threadStats = null;
        for (Object o : jsonArray) {
            JSONObject tempJSON = (JSONObject) JSONObject.toJSON(o);
            if (tempJSON.getString("type").equals("thread")) {
                threadStats = tempJSON.getJSONArray("threadStats");
            }
        }
        return threadStats;
    }

    /**
     * 阻塞线程结果
     * 目前只支持找出synchronized关键字阻塞住的线程， 如果是java.util.concurrent.Lock， 目前还不支持
     */
    public ThreadBlockingBean convertToThreadBlockBean(Result<Object> objectResult) {
        if (objectResult == null || !objectResult.isSuccess()) {
            return null;
        }

        JSONArray jsonArray = getArthasResponseResults(objectResult);
        if (jsonArray.isEmpty()) {
            return null;
        }

        for (Object o : jsonArray) {
            JSONObject tempJSON = (JSONObject) JSONObject.toJSON(o);
            if (tempJSON.getString("message").equals("No most blocking thread found!")) {
                return null;
            }
        }
        return threadBlockingBean;
    }

    /**
     * 获取response中的results
     *
     * @param objectResult
     * @return
     */
    public JSONArray getArthasResponseResults(Result<Object> objectResult) {
        if (objectResult == null || !objectResult.isSuccess()) {
            return null;
        }
        Object data = objectResult.getData();
        if (data == null) {
            return null;
        }
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(data);
        JSONObject body = (JSONObject) jsonObject.get("body");
        return body.getJSONArray("results");
    }

}
