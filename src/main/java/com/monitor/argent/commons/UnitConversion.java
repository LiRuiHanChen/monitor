package com.monitor.argent.commons;

import com.alibaba.fastjson.JSONObject;
import com.monitor.argent.entity.MemoryBean;
import org.springframework.stereotype.Component;

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
}
