package com.monitor.argent.commons;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.entity.EchartsLineStackSeriesBean;
import com.monitor.argent.entity.JvmInfoBean;
import com.monitor.argent.entity.ThreadBlockingBean;
import com.monitor.argent.entity.ThreadStateBean;
import com.monitor.argent.model.Result;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ArthasResultUtil {

    public static ThreadStateBean threadStateBean = new ThreadStateBean();
    public static JvmInfoBean jvmInfoBean = new JvmInfoBean();
    public static ThreadBlockingBean threadBlockingBean = new ThreadBlockingBean();
    public static LinkedHashSet<String> legendData = new LinkedHashSet<>();

    public SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
    public static List<String> jvmInfoType = new ArrayList<>();

    static {
        jvmInfoType = getJvmInfoTypeList();
    }

    private static List<String> getJvmInfoTypeList() {
        ArthasResultUtil.jvmInfoType.add("RUNTIME");
        ArthasResultUtil.jvmInfoType.add("CLASS-LOADING");
        ArthasResultUtil.jvmInfoType.add("COMPILATION");
        ArthasResultUtil.jvmInfoType.add("GARBAGE-COLLECTORS");
        ArthasResultUtil.jvmInfoType.add("MEMORY-MANAGERS");
        ArthasResultUtil.jvmInfoType.add("MEMORY");
        ArthasResultUtil.jvmInfoType.add("OPERATING-SYSTEM");
        ArthasResultUtil.jvmInfoType.add("THREAD");
        ArthasResultUtil.jvmInfoType.add("FILE-DESCRIPTOR");
        return ArthasResultUtil.jvmInfoType;
    }

    /**
     * 解析命令和结果
     */
    public String parseResultByCommand(String command, Result<Object> objectResult, Long now) {
        if (command.isEmpty() || objectResult == null) return null;

        String result = null;
        ThreadStateBean threadStateBean;
        //根据不同命令提取信息
        switch (command) {
            case "thread -i 1000 -n 5":
                result = this.convertToBusyThread(objectResult).toJSONString();
                break;
            case "jvm":
                JvmInfoBean jvmInfoBean = this.convertToJvmInfoBean(objectResult);
                result = JSON.toJSONString(jvmInfoBean);
                break;
            case "thread --state BLOCKED":
                threadStateBean = this.convertToThreadStateBean(objectResult);
                threadStateBean.getxAxis().add(time.format(new Date(now)));
                result = JSON.toJSONString(threadStateBean);
                break;
            case "thread --state WAITING":
                threadStateBean = this.convertToThreadStateBean(objectResult);
                threadStateBean.getxAxis().add(time.format(new Date(now)));
                result = JSON.toJSONString(threadStateBean);
            case "thread --state RUNNABLE":
                threadStateBean = this.convertToThreadStateBean(objectResult);
                threadStateBean.getxAxis().add(time.format(new Date(now)));
                result = JSON.toJSONString(threadStateBean);
                break;
            case "thread –-state TIMED_WAITING":
                threadStateBean = this.convertToThreadStateBean(objectResult);
                threadStateBean.getxAxis().add(time.format(new Date(now)));
                result = JSON.toJSONString(threadStateBean);
                break;
            default:
                break;
        }
        return result;
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
    public ThreadStateBean convertToThreadStateBean(Result<Object> objectResult) {
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
                JSONArray busyArray = tempJSON.getJSONArray("threadStats");
                for (Object object : busyArray) {
                    JSONObject temp = (JSONObject) JSONObject.toJSON(object);
                    String threadName = temp.getString("name");
                    int time = temp.getIntValue("time");
                    System.out.println("time= " + time);
                    // 如果是新的线程名，添加
                    if (!legendData.contains(threadName)) {
                        legendData.add(threadName);
                        EchartsLineStackSeriesBean echartsLineStackSeriesBean = new EchartsLineStackSeriesBean();
                        echartsLineStackSeriesBean.setName(threadName);
                        echartsLineStackSeriesBean.setData(time);
                        LinkedList<EchartsLineStackSeriesBean> tempList = threadStateBean.getSeries();
                        tempList.add(echartsLineStackSeriesBean);
                        threadStateBean.setSeries(tempList);
                    } else {
                        LinkedList<EchartsLineStackSeriesBean> tempList = threadStateBean.getSeries();
                        Optional<EchartsLineStackSeriesBean> beanObject = tempList.stream().filter(e -> e.getName().equals(threadName)).findFirst();
                        beanObject.ifPresent(nameBean -> nameBean.setData(time));
                    }
                }
            }
        }
        threadStateBean.setLegendData(legendData);
        return threadStateBean;
    }

    /**
     * 获取jvm信息
     */
    public JvmInfoBean convertToJvmInfoBean(Result<Object> objectResult) {
        if (objectResult == null || !objectResult.isSuccess()) {
            return null;
        }

        JSONArray jsonArray = getArthasResponseResults(objectResult);
        if (jsonArray.isEmpty()) {
            return null;
        }
        for (Object o : jsonArray) {
            JSONObject tempJSON = (JSONObject) JSONObject.toJSON(o);
            if (tempJSON.getString("type").equals("jvm")) {
                JSONObject jvmInfoObject = (JSONObject) tempJSON.get("jvmInfo");
                for (String jvmType : jvmInfoType) {
                    switch (jvmType) {
                        case "RUNTIME":
                            jvmInfoBean.setRuntimeList(jvmInfoObject.getJSONArray(jvmType));
                        case "CLASS-LOADING":
                            jvmInfoBean.setClassLoadingList(jvmInfoObject.getJSONArray(jvmType));
                        case "COMPILATION":
                            jvmInfoBean.setCompilationList(jvmInfoObject.getJSONArray(jvmType));
                        case "GARBAGE-COLLECTORS":
                            jvmInfoBean.setGarbageCollectorsList(jvmInfoObject.getJSONArray(jvmType));
                        case "MEMORY-MANAGERS":
                            jvmInfoBean.setMemoryManagersList(jvmInfoObject.getJSONArray(jvmType));
                        case "MEMORY":
                            jvmInfoBean.setMemoryList(jvmInfoObject.getJSONArray(jvmType));
                        case "OPERATING-SYSTEM":
                            jvmInfoBean.setOperatingSystemList(jvmInfoObject.getJSONArray(jvmType));
                        case "THREAD":
                            jvmInfoBean.setThreadInfoList(jvmInfoObject.getJSONArray(jvmType));
                        case "FILE-DESCRIPTOR":
                            jvmInfoBean.setFileDescriptorList(jvmInfoObject.getJSONArray(jvmType));
                    }
                }
            }
        }
        return jvmInfoBean;
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
