package com.monitor.argent.socket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.commons.ArthasResultUtil;
import com.monitor.argent.config.ApplicationContextUtil;
import com.monitor.argent.entity.ArthasRequestBody;
import com.monitor.argent.model.Result;
import com.monitor.argent.service.ArthasRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint(value = "/arthas_request")
@EnableScheduling

public class ArthasRequestSocket {

    @Autowired
    public void setStringRedisTemplate(ArthasRequestImpl arthasRequestImpl, ArthasResultUtil arthasResultUtil) {
        ArthasRequestSocket.arthasRequestImpl = arthasRequestImpl;
        ArthasRequestSocket.arthasResultUtil = arthasResultUtil;
    }

    private final Logger logger = LoggerFactory.getLogger(ArthasRequestSocket.class);

    private static CopyOnWriteArraySet<ArthasRequestSocket> user = new CopyOnWriteArraySet<>();
    private static Session session;
    private static ArthasRequestImpl arthasRequestImpl = new ArthasRequestImpl();
    private static ArthasResultUtil arthasResultUtil;
    public static ArthasRequestSocket arthasRequestSocket;

    private static final String EXEC_ACTION = "exec";
    private static final String ASYNC_EXEC_ACTION = "async_exec";
    // RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, NEW, TERMINATED
    private static final String BUSY_THREAD_INFO_COMMAND = "thread -i 1000 -n 5";
    private static final String BLOCK_THREAD_INFO_COMMAND = "thread --state BLOCKED";
    private static final String WAITING_THREAD_STATE_COMMAND = "thread --state WAITING";
    private static final String RUNNABLE_THREAD_STATE_COMMAND = "thread --state RUNNABLE";
    private static final String TIMED_WAIT_THREAD_STATE_COMMAND = "thread –-state TIMED_WAITING";
    private static final String VM_OPTION_PRINT_GC = "vmoption PrintGC";
    // 刷新实时数据的时间间隔 (ms)，默认5000ms   -n  刷新实时数据的次数
    private static final String DASHBOARD_COMMAND = "dashboard -i 1000 -n 1";
    private static final String VERSION = "version";

    private static final String url = "/api";
    private static String ip;
    private static String port;
    private static ArthasRequestBody arthasRequestBody = new ArthasRequestBody();
    public static List<String> commandList = new ArrayList<>();
    public HashMap<String, Boolean> arthasPathMap = new HashMap<>();

    @PostConstruct
    public void init() {
        arthasRequestSocket = this;
    }

    static {
        commandList = loadCommand();
    }

    /**
     * 构造arthas命令集合
     *
     * @return
     */
    // TODO 调试GC信息
    public static List<String> loadCommand() {
        commandList.add(BUSY_THREAD_INFO_COMMAND);
        commandList.add(BLOCK_THREAD_INFO_COMMAND);
        commandList.add(WAITING_THREAD_STATE_COMMAND);
        commandList.add(RUNNABLE_THREAD_STATE_COMMAND);
        commandList.add(TIMED_WAIT_THREAD_STATE_COMMAND);
        commandList.add(DASHBOARD_COMMAND);
        return commandList;
    }

    @OnMessage
    public void onMessage(String message) {
        if (!StringUtils.isEmpty(message)) {
            JSONObject messageJsonObject = JSONObject.parseObject(message);
            if (messageJsonObject != null) {
                ip = messageJsonObject.getString("ip").trim();
                port = messageJsonObject.getString("port").trim();
                arthasRequestBody.setAction("exec");
                arthasRequestBody.setCommand(VERSION);
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        ArthasRequestSocket.session = session;
        user.add(this);
        logger.info("有新连接加入!");
    }

    @OnClose
    public void onClose() {
        user.remove(this);
        logger.info("{} socket is closed", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
        logger.error("{} socket connection error", ArthasRequestSocket.session.getId());
    }

    public void sendMessage(String message) throws IOException {
        if (!StringUtils.isEmpty(message)) {
            //群发消息
            for (ArthasRequestSocket myWebSocket : user) {
                session.getBasicRemote().sendText(message);
            }
        }
    }

    /**
     * 每2秒查一次
     *
     * @throws IOException
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void getTimePercentiles() throws IOException {
        if (arthasPathMap.containsKey(ip + ":" + port)) {
            // 如果为true说明已经成功链接
            if (arthasPathMap.get(ip + ":" + port)) {

                JSONObject resultJson = new JSONObject();
                ArthasRequestImpl arthasRequestImplTemp = (ArthasRequestImpl) ApplicationContextUtil.getBean("arthasRequestImpl");

                for (String command : commandList) {
                    arthasRequestBody.setAction(EXEC_ACTION);
                    arthasRequestBody.setCommand(command);
                    Result<Object> objectResult = arthasRequestImplTemp.sendArthasPostRequest(ip + ":" + port, url, null, arthasRequestBody);

                    //如果请求结果异常跳出循环
                    if (!objectResult.isSuccess()) break;
                    String result = arthasResultUtil.parseResultByCommand(command, objectResult);
                    // 判断是否为线程相关的数据
                    if (command.contains("thread --state")) {
                        String tempCommand = resultJson.getString("thread --state");
                        //还未添加的数据
                        if (StringUtils.isEmpty(tempCommand)) {
                            resultJson.put("thread --state", result);
                        } else {
                            JSONArray temp = JSONArray.parseArray(tempCommand);
                            temp.addAll(JSONArray.parseArray(result));
                            resultJson.put("thread --state", temp.toJSONString());
                        }
                    }
                    resultJson.put(command, result);
                }
                // 修正格式发送
                if (!resultJson.isEmpty()) {
                    session.getBasicRemote().sendText(resultJson.toJSONString());
                }
                //清空
                resultJson.clear();
            }
        }
    }

}
