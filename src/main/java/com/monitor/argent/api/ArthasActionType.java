package com.monitor.argent.api;

/**
 * arthas http api请求Action的参数类型
 *
 * @author lilei
 */

public interface ArthasActionType {

    // 同步执行命令，命令正常结束或者超时后中断命令执行后返回命令的执行结果
    String EXEC = "exec";
    // 异步执行命令，立即返回命令的调度结果，命令执行结果通过pull_results获取
    String ASYNC_EXEC = "async_exec";
    // 中断会话当前的命令，类似Telnet Ctrl + c的功能
    String INTERRUPT_JOB = "interrupt_job";
    // 获取异步执行的命令的结果，以http 长轮询（long-polling）方式重复执行
    String PULL_RESULT = "pull_results";
    // 创建会话
    String INIT_SESSION = "init_session";
    // 加入会话，用于支持多人共享同一个Arthas会话
    String JOIN_SESSION = "join_session";
    // 关闭会话
    String CLOSE_SESSION = "close_session";

}
