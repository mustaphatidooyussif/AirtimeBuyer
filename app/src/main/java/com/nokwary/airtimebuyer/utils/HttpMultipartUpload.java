package com.nokwary.airtimebuyer.utils;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;


public class HttpMultipartUpload {

    public static String uploadFile(String filename, String url, String request_code) {
        String responseString = null;
        Log.d("Log", "File path" + filename);
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        try {
            MultipartEntity entity = new MultipartEntity();
            File file = new File(filename);
            entity.addPart("file", new FileBody(file));
            entity.addPart("request_code", new StringBody(request_code, Charset.forName("UTF-8")));
            //long totalSize = entity.getContentLength();
            httppost.setEntity(entity);

            // Making server call
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();


            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Server response
                responseString = EntityUtils.toString(r_entity);
                Log.d("Log", responseString);
            } else {
                responseString = "Error occurred! Http Status Code: "
                        + statusCode + " -> " + response.getStatusLine().getReasonPhrase();
                Log.d("Log", responseString);
            }

        } catch (ClientProtocolException e) {
            responseString = e.toString();
        } catch (IOException e) {
            responseString = e.toString();
        }finally{
            httpclient.getConnectionManager().shutdown();
        }

        return responseString;
    }
}
