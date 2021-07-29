package com.monitor.argent.entity;

import org.springframework.util.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

public class ArthasRequestBody {

    @NotNull
    @Max(value = 30, message = "action参数大于30")
    String action;
    String requestId;
    String sessionId;
    String consumerId;
    String command;
    //命令同步执行的超时时间(ms)，默认为30000
    String execTimeout;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        if (StringUtils.isEmpty(action)) {
            this.action = "";
        }
        this.action = action;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            this.requestId = "";
        }
        this.requestId = requestId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        if (StringUtils.isEmpty(sessionId)) {
            this.sessionId = "";
        }
        this.sessionId = sessionId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        if (StringUtils.isEmpty(consumerId)) {
            this.consumerId = "";
        }
        this.consumerId = consumerId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        if (StringUtils.isEmpty(command)) {
            this.command = "";
        }
        this.command = command;
    }

    public String getExecTimeout() {
        return execTimeout;
    }

    public void setExecTimeout(String execTimeout) {
        if (StringUtils.isEmpty(execTimeout)) {
            this.execTimeout = "";
        }
        this.execTimeout = execTimeout;
    }
}
