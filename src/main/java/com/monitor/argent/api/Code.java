package com.monitor.argent.api;

public interface Code {

    int SUCCESS = 200;
    int SYSTEM_ERROR = 500;
    // influxDB请求失败
    int BAD_REQUEST = 401;
    // influxDB token 失效
    int TOKEN_OVER_QUOTA = 429;

    int PARAMETER_MISSING = 502;

}
