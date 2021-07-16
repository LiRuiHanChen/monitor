package com.monitor.argent.api;

import com.monitor.argent.entity.ArthasRequestBody;

import java.util.HashMap;
import java.util.Map;

public interface ArthasHttpApiRequest {

    Map<String, String> sendArthasPostRequest(String host, String url, HashMap<String, String> headerMap, ArthasRequestBody arthasRequestBody);

}
