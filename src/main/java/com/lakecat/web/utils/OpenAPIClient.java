package com.lakecat.web.utils;

/**
 * Created by slj on 2021/10/20.
 */

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

public class OpenAPIClient {
    public static String getToken(String url, String username, String password, String client, String scope) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String responseInfo = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            // Request parameters and other properties.
            List <NameValuePair> params = new ArrayList <NameValuePair>(10);
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("scope", scope));
            params.add(new BasicNameValuePair("client_id", client));
            params.add(new BasicNameValuePair("grant_type", "password"));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            // response
            CloseableHttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 400) {
                if (null != entity) {
                    responseInfo = EntityUtils.toString(entity);
                }
            } else {
                if (null != entity) {
                    responseInfo = EntityUtils.toString(entity);
                }
                throw new Exception(responseInfo);
            }
        } finally {
            httpclient.close();
        }
        return responseInfo;
    }
}