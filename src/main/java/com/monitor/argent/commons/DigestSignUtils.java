package com.monitor.argent.commons;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class DigestSignUtils {

    // 对 key 进行排序，然后拼接成 K1=V1&K2=V2secret 的方式进行 MD5
    public String signMd5(Map<String, String> m, String secret) {
        List<String> keys = new ArrayList<>(m.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append('=').append(m.get(key)).append('&');
        }
        sb.setLength(sb.length() - 1);

        try {
            return DigestUtils.md5Hex((sb + secret).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("failed to signMd5", e);
        }
    }
}
