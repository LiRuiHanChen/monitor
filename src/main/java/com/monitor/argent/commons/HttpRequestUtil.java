package com.monitor.argent.commons;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

@Component
public class HttpRequestUtil {

    /**
     * post请求传输map数据
     *
     * @param url
     * @param parameterMap
     * @param headerMap
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public Map<String, String> sendPostDataByMap(String url, HashMap<String, String> headerMap, Map<String, String> parameterMap) throws ClientProtocolException, IOException {
        if (url.isEmpty()) {
            return null;
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        if (!parameterMap.isEmpty()) {
            List<NameValuePair> nameValuePairs = new ArrayList<>(parameterMap.size());
            for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
        }
        return getIntegerStringMap(headerMap, httpClient, httpPost);
    }

    /**
     * 处理response
     *
     * @param headerMap
     * @param httpClient
     * @param httpPost
     * @return
     * @throws IOException
     */
    private Map<String, String> getIntegerStringMap(HashMap<String, String> headerMap, CloseableHttpClient httpClient, HttpPost httpPost) throws IOException {
        CloseableHttpResponse response = null;
        HashMap<String, String> resultMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(headerMap)) {
            for (Map.Entry<String, String> tempEntry : headerMap.entrySet()) {
                httpPost.addHeader(tempEntry.getKey(), tempEntry.getValue());
            }
        }
        try {
            response = httpClient.execute(httpPost);
            if (response.getEntity() != null) {
                resultMap.put("code", Integer.toString(response.getStatusLine().getStatusCode()));
                resultMap.put("response", EntityUtils.toString(response.getEntity(), "utf-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
            httpClient.close();
        }
        return resultMap;
    }

    /**
     * post请求json提交参数
     *
     * @param url
     * @param parameterJsonData
     * @param headerMap
     * @return
     * @throws IOException
     */
    public Map<String, String> sendPostDataByJson(String url, String parameterJsonData, HashMap<String, String> headerMap) throws IOException {
        if (url.isEmpty()) {
            return null;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        if (!parameterJsonData.isEmpty()) {
            StringEntity stringEntity = new StringEntity(parameterJsonData, ContentType.APPLICATION_JSON);
            stringEntity.setContentEncoding("utf-8");
            httpPost.setEntity(stringEntity);
        }

        return getIntegerStringMap(headerMap, httpClient, httpPost);
    }

    /**
     * Get请求
     *
     * @param url
     * @param headerMap
     * @param parameterMap
     * @return
     * @throws URISyntaxException
     */
    public Map<String, String> sendGetData(String url, HashMap<String, String> headerMap, HashMap<String, String> parameterMap) throws URISyntaxException, UnsupportedEncodingException {
        if (url.isEmpty()) {
            return null;
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        URIBuilder uriBuilder = new URIBuilder(url);

        if (!CollectionUtils.isEmpty(parameterMap)) {
            for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                uriBuilder.setParameter(entry.getKey(), URLDecoder.decode(entry.getValue(),"UTF-8"));
            }
        }

        HttpGet httpGet = new HttpGet(uriBuilder.build());

        if (!CollectionUtils.isEmpty(headerMap)) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }
        HashMap<String, String> resultMap = new HashMap<>();
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            if (response.getEntity() != null) {
                resultMap.put("code", Integer.toString(response.getStatusLine().getStatusCode()));
                resultMap.put("response", EntityUtils.toString(response.getEntity(), "utf-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Objects.requireNonNull(response).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    // 启用HTTPS携带证书GET请求
    public static String HttpsForGet(String url, String keystore_PathFile, String keyPwd) throws IOException,
            KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient httpClient = null;
        if (url.startsWith("https")) {
            File cert = new File(keystore_PathFile);
            String keystore = keyPwd;
            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(cert, keystore.toCharArray(), new TrustSelfSignedStrategy()).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"},
                    null, NoopHostnameVerifier.INSTANCE);
            httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } else {
            httpClient = HttpClients.createDefault();
        }
        try (CloseableHttpClient _httpClient = httpClient; CloseableHttpResponse res = _httpClient.execute(httpGet);) {
            StatusLine sl = res.getStatusLine();
            return sl.toString().split(" ")[1];
        }
    }

    // 启用HTTPS携带证书post请求
    public static String HttpsForPost(String url, String keystore_PathFile, String keyPwd, String json)
            throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException {

        String returnValue = "这是默认返回值，接口调用失败";
        HttpPost httppost = new HttpPost(url);
        CloseableHttpClient httpClient = null;
        if (url.startsWith("https")) {
            File cert = new File(keystore_PathFile);
            String keystore = keyPwd;
            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(cert, keystore.toCharArray(), new TrustSelfSignedStrategy()).build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"},
                    null, NoopHostnameVerifier.INSTANCE);
            httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } else {
            httpClient = HttpClients.createDefault();
        }
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try (CloseableHttpClient _httpClient = httpClient; CloseableHttpResponse res = _httpClient.execute(httppost);) {
            StringEntity requestEntity = new StringEntity(json, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httppost.setHeader("Content-type", "application/json");
            httppost.setEntity(requestEntity);
            // 发送HttpPost请求，获取返回值
            returnValue = httpClient.execute(httppost, responseHandler);
        }
        return returnValue;
    }

}
