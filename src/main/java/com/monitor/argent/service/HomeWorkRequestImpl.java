package com.monitor.argent.service;

import com.monitor.argent.commons.HttpRequestUtil;
import com.monitor.argent.dao.HomeWorkMapper;
import com.monitor.argent.entity.HomeWorkRequestBean;
import com.monitor.argent.entity.HomeWorkResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HomeWorkRequestImpl {

    @Autowired
    HomeWorkMapper homeWorkMapper;
    @Resource
    HttpRequestUtil httpRequestUtil;

    public List<HomeWorkResponseBody> getHomeWorkTestCase(String caseName, int stage, int subject, int flag) {
        return homeWorkMapper.getHomeWorkTestCase(caseName, stage, subject, flag);
    }

    public int addHomeWorkTestCase(HomeWorkRequestBean homeWorkRequestBean) {
        return homeWorkMapper.addHomeWorkTestCase(homeWorkRequestBean);
    }

    public int editHomeWorkTestCase(HomeWorkResponseBody homeWorkResponseBody) {
        return homeWorkMapper.editHomeWorkTestCase(homeWorkResponseBody);
    }

    public Map<String, String> runHomeWorkTestCase(String host, String url, String paramData, HashMap<String, String> headerMap) {
        if (StringUtils.isEmpty(paramData)) return null;

        try {
            return httpRequestUtil.sendPostDataByJson(host.trim() + url.trim(), paramData, headerMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
