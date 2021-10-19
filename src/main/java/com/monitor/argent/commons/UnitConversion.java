package com.monitor.argent.commons;

import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.entity.MemoryBean;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UnitConversion {

    /**
     * byte转换为MB
     */
    public int byteToMB(int byteValue) {
        if (byteValue >= 1024 * 1024) {
            return (int) (byteValue / (1024.0 * 1024.0));
        }
        return 0;
    }

    /**
     * 计算内存使用率
     */
    public String getUsage(int total, int used) {
        return null;
    }

    /**
     * 转换对象中属性的存储单位为MB
     *
     * @param memoryBean
     * @return
     */
    public JSONObject memoryBeanAttributeUnitConversion(MemoryBean memoryBean) {
        int max = memoryBean.getMax();
        int total = memoryBean.getTotal();
        int used = memoryBean.getUsed();
        if (max > 0) {
            memoryBean.setMax(byteToMB(max));
        }
        if (total > 0) {
            memoryBean.setTotal(byteToMB(total));
        }
        if (used > 0) {
            memoryBean.setUsed(byteToMB(used));
        }
        return (JSONObject) JSONObject.toJSON(memoryBean);
    }

    /**
     * 将对象属性转化为map结合
     */
    public <T> Map<String, String> beanToMap(T bean) {
        Map<String, String> map = new HashMap<>();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", (String) beanMap.get(key));
            }
        }
        return map;
    }

    public JSONObject createQueryJSON(String dzbId) {
        JSONObject query = new JSONObject();
        JSONObject match = new JSONObject();
        JSONObject dzbJSON = new JSONObject();
        dzbJSON.put("dzb_id", dzbId);
        match.put("match", dzbJSON);
        query.put("query", match);
        return query;
    }
}
