/**
 * 接收数据格式示例:
 * {
 * "host":"http://localhost:8563",
 * "url":"api",
 * "action": "exec", // 请求的动作/行为，可选值请参考”请求Action”小节。
 * "requestId": "req112", // 可选请求ID，由客户端生成
 * "sessionId": "94766d3c-8b39-42d3-8596-98aee3ccbefb", // Arthas会话ID，一次性命令不需要设置会话ID
 * "consumerId": "955dbd1325334a84972b0f3ac19de4f7_2", //  Arthas消费者ID，用于多人共享会话
 * "command": "version", // Arthas command line
 * "execTimeout": "10000" //命令同步执行的超时时间(ms)，默认为30000
 * }
 */

package com.monitor.argent.entity;

import com.sun.istack.internal.NotNull;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class ArthasRequestParam {

    @NotNull
    @Min(value = 5, message = "host异常")
    @Max(value = 50, message = "host超长")
    String host;
    //默认value = "api"
    String url;
    @NotNull
    @Max(value = 30, message = "action参数大于30")
    String action;
    String requestId;
    String sessionId;
    String consumerId;
    String command;
    //命令同步执行的超时时间(ms)，默认为30000
    String execTimeout;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

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
