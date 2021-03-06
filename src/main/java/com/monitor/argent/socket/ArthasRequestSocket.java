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
    private static final String TIMED_WAIT_THREAD_STATE_COMMAND = "thread ???-state TIMED_WAITING";
    private static final String VM_OPTION_PRINT_GC = "vmoption PrintGC";
    // ????????????????????????????????? (ms)?????????5000ms   -n  ???????????????????????????
    private static final String DASHBOARD_COMMAND = "dashboard -i 1000 -n 1";
    private static final String VERSION = "version";
    // ??????????????????profiler?????? N ??????????????????????????? --duration ????????????
    private static final String PROFILER_DURATION_COMMAND = "profiler start --duration";

    private static final String url = "/api";
    private static String ip;
    public int durationTime;
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
     * ??????arthas????????????
     *
     * @return
     */
    // TODO ??????GC??????
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
                String key = messageJsonObject.getString("key");
                switch (key) {
                    case "version":
                        JSONObject versionObject = messageJsonObject.getJSONObject("value");
                        ip = versionObject.getString("ip").trim();
                        port = versionObject.getString("port").trim();
                        arthasRequestBody.setAction("exec");
                        arthasRequestBody.setCommand(VERSION);
                        break;
                    case "profiler start --duration":
                        durationTime = (int) messageJsonObject.get("value");
                        // TODO ???????????????
                        if (durationTime > 0) {
                            loadArthasProfiler(arthasRequestBody, key);
                        }
                        break;
                    case "ping":
                        try {
                            session.getBasicRemote().sendText("pong");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "stop":
                        this.onClose();
                        break;
                }
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        ArthasRequestSocket.session = session;
        user.add(this);
        logger.info("??????????????????!");
    }

    @OnClose
    public void onClose() {
        user.remove(this);
        try {
            session.close();
            arthasRequestSocket.arthasPathMap.put(ip + ":" + port, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("{} socket is closed", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
        logger.error("{} socket connection error", ArthasRequestSocket.session.getId());
    }

    public void sendMessage(String message) throws IOException {
        if (!StringUtils.isEmpty(message)) {
            //????????????
            for (ArthasRequestSocket myWebSocket : user) {
                session.getBasicRemote().sendText(message);
            }
        }
    }

    /**
     * ???3????????????
     *
     * @throws IOException
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void getTimePercentiles() throws IOException {
        if (arthasPathMap.containsKey(ip + ":" + port)) {
            // ?????????true????????????????????????
            if (arthasPathMap.get(ip + ":" + port)) {

                JSONObject resultJson = new JSONObject();
                ArthasRequestImpl arthasRequestImplTemp = (ArthasRequestImpl) ApplicationContextUtil.getBean("arthasRequestImpl");

                for (String command : commandList) {
                    arthasRequestBody.setAction(EXEC_ACTION);
                    arthasRequestBody.setCommand(command);
                    Result<Object> objectResult = arthasRequestImplTemp.sendArthasPostRequest(ip + ":" + port, url, null, arthasRequestBody);

                    //????????????????????????????????????
                    if (!objectResult.isSuccess()) break;
                    String result = arthasResultUtil.parseResultByCommand(ip, port, command, objectResult);
                    // ????????????????????????????????????
                    if (command.contains("thread --state")) {
                        String tempCommand = resultJson.getString("thread --state");
                        //?????????????????????
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
                // ??????????????????
                if (!resultJson.isEmpty()) {
                    session.getBasicRemote().sendText(resultJson.toJSONString());
                }
                //??????
                resultJson.clear();
            }
        }
    }

    public void loadArthasProfiler(ArthasRequestBody arthasRequestBody, String key) {
        arthasRequestBody.setAction("exec");
        // command??????: "profiler start --duration 1 --file /xxx/monitor/src/main/resources/static/html/xxx.html"
        //??????????????????
        arthasRequestBody.setCommand(PROFILER_DURATION_COMMAND + " " + durationTime + " " + "--format html");
        ArthasRequestImpl arthasRequestImplTemp = (ArthasRequestImpl) ApplicationContextUtil.getBean("arthasRequestImpl");
        Result<Object> objectResult = arthasRequestImplTemp.sendArthasPostRequest(ip + ":" + port, url, null, arthasRequestBody);
        String arthasOutPutFilePath = arthasResultUtil.parseResultByCommand(ip, port, key, objectResult);
        try {
            session.getBasicRemote().sendText(arthasOutPutFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
