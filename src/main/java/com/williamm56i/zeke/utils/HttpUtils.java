package com.williamm56i.zeke.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public class HttpUtils {

    /**
     * HTTP GET
     *
     * @param url URL
     * @return json string
     */
    public static String get(String url) {
        return get(url, null);
    }

    /**
     * HTTP GET with custom header
     *
     * @param url       URL
     * @param headerMap custom header
     * @return json string
     */
    public static String get(String url, Map<String, String> headerMap) {
        WebClient webClient = WebClient.create();
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headerMap != null) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            for (String key: headerMap.keySet()) {
                httpHeaders.set(key, headerMap.get(key));
            }
        }
        return webClient.get().uri(url).headers(headers -> headers.addAll(httpHeaders)).retrieve().bodyToMono(String.class).block();
    }

    /**
     * HTTP POST
     *
     * @param url  URL
     * @param body request boy
     * @return json string
     * @throws JsonProcessingException e
     */
    public static String post(String url, Object body) throws JsonProcessingException {
        return post(url, body, null);
    }

    /**
     * HTTP POST with custom header
     *
     * @param url       URL
     * @param body      request boy
     * @param headerMap custom header
     * @return json string
     * @throws JsonProcessingException e
     */
    public static String post(String url, Object body, Map<String, String> headerMap) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(body);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (headerMap != null) {
            for (String key: headerMap.keySet()) {
                httpHeaders.set(key, headerMap.get(key));
            }
        }

        WebClient webClient = WebClient.create();
        return webClient.post().uri(url).headers(headers -> headers.addAll(httpHeaders)).bodyValue(jsonString).retrieve().bodyToMono(String.class).block();
    }
}
