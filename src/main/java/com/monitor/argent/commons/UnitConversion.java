package com.monitor.argent.commons;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class UnitConversion {

    /**
     * byte转换为MB
     */
    public double byteToGB(long byteValue) {
        if (byteValue >= 1024 * 1024) {
            DecimalFormat format = new DecimalFormat("###.00");
            String value = format.format(byteValue / (1024.0 * 1024.0));
            return Double.parseDouble(value);
        }
        return 0;
    }

}
