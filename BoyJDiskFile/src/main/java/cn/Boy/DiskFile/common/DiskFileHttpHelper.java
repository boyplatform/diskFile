package cn.Boy.DiskFile.common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;


public class DiskFileHttpHelper {

    private static DiskFileHttpHelper instance = null;
    public static DiskFileHttpHelper getInstance()
    {
            if (instance == null) {
                instance = new DiskFileHttpHelper();
            }

        return instance;
    }
    public enum postQuestMode{
        json,textBody,binaryBody
    }
    public enum getQuestMode{
        json,textBody,binaryBody
    }

    public String postRequest(String PostUrl, Map<String,Object> parameterMap,String mode) throws JSONException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            if (mode.equals("json")) {

                JSONObject jsob = new JSONObject();
                for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
                    jsob.put(entry.getKey(), entry.getValue());
                }
                StringEntity data = new StringEntity(jsob.toString(), "utf-8");
                data.setContentEncoding("utf-8");
                data.setContentType("application/json");

                HttpUriRequest request = RequestBuilder
                        .post(PostUrl)
                        .setEntity(data)
                        .build();

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        System.out.println(status);
                        if (status == 200) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status + response.toString());
                        }
                    }
                };
                String responseBody = httpClient.execute(request, responseHandler);
                return responseBody;
            }else if(mode.equals("textBody")){

                MultipartEntityBuilder multipartEntityBuilder= MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                for (Map.Entry<String, Object> entry : parameterMap.entrySet())
                {
                    multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString());
                }
                HttpEntity data=multipartEntityBuilder.build();

                HttpUriRequest request = RequestBuilder
                        .post(PostUrl)
                        .setEntity(data)
                        .build();

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        System.out.println(status);
                        if (status == 200) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }
                };
                String responseBody = httpClient.execute(request, responseHandler);
                return responseBody;

            }else if(mode.equals("binaryBody")){

                MultipartEntityBuilder multipartEntityBuilder= MultipartEntityBuilder.create()
                        .setMode(HttpMultipartMode.RFC6532);

                for (Map.Entry<String, Object> entry : parameterMap.entrySet())
                {
                    if(entry.getValue() instanceof String)
                    {
                        multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString(), ContentType.DEFAULT_BINARY);

                    }
                    else if(entry.getValue() instanceof List)
                    {
                        for(File f: (List<File>)entry.getValue())
                        {
                            multipartEntityBuilder.addBinaryBody(entry.getKey(), f, ContentType.DEFAULT_BINARY, f.getName());
                        }
                    }
                    else{
                        multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString(), ContentType.DEFAULT_BINARY);
                    }


                }

                HttpEntity data=multipartEntityBuilder.build();

                HttpUriRequest request = RequestBuilder
                        .post(PostUrl)
                        .setEntity(data)
                        .build();

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        System.out.println(status);
                        if (status == 200) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }
                };
                String responseBody = httpClient.execute(request, responseHandler);
                return responseBody;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return  "";
    }

    public String getRequest(String getUrl,Map<String,Object> parameterMap,String mode) throws JSONException{
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            if (mode.equals("json")) {

                JSONObject jsob = new JSONObject();
                for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
                    jsob.put(entry.getKey(), entry.getValue());
                }
                StringEntity data = new StringEntity(jsob.toString(), "utf-8");
                data.setContentEncoding("utf-8");
                data.setContentType("application/json");

                HttpUriRequest request = RequestBuilder
                        .get(getUrl)
                        .setEntity(data)
                        .build();

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        System.out.println(status);
                        if (status == 200) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status + response.toString());
                        }
                    }
                };
                String responseBody = httpClient.execute(request, responseHandler);
                return responseBody;
            }else if(mode.equals("textBody")){

                MultipartEntityBuilder multipartEntityBuilder= MultipartEntityBuilder.create().setCharset(Charset.forName("UTF-8"))
                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                for (Map.Entry<String, Object> entry : parameterMap.entrySet())
                {
                    multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString());
                }
                HttpEntity data=multipartEntityBuilder.build();

                HttpUriRequest request = RequestBuilder
                        .get(getUrl)
                        .setEntity(data)
                        .build();

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        System.out.println(status);
                        if (status == 200) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }
                };
                String responseBody = httpClient.execute(request, responseHandler);
                return responseBody;

            }else if(mode.equals("binaryBody")){

                MultipartEntityBuilder multipartEntityBuilder= MultipartEntityBuilder.create()
                        .setMode(HttpMultipartMode.RFC6532);

                for (Map.Entry<String, Object> entry : parameterMap.entrySet())
                {
                    if(entry.getValue() instanceof String)
                    {
                        multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString(), ContentType.DEFAULT_BINARY);

                    }
                    else if(entry.getValue() instanceof List)
                    {
                        for(File f: (List<File>)entry.getValue())
                        {
                            multipartEntityBuilder.addBinaryBody(entry.getKey(), f, ContentType.DEFAULT_BINARY, f.getName());
                        }
                    }
                    else{
                        multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString(), ContentType.DEFAULT_BINARY);
                    }


                }

                HttpEntity data=multipartEntityBuilder.build();

                HttpUriRequest request = RequestBuilder
                        .get(getUrl)
                        .setEntity(data)
                        .build();

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        System.out.println(status);
                        if (status == 200) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }
                };
                String responseBody = httpClient.execute(request, responseHandler);
                return responseBody;
            }else if(mode.equals("queryCephApiUrl")){

                HttpUriRequest request = RequestBuilder
                        .get(getUrl)
                        .build();
                request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                request.addHeader("Accept-Encoding","gzip, deflate, sdch");
                request.addHeader("Accept-Language","zh-CN,zh;q=0.8");
                request.addHeader("Connection","keep-alive");
                request.addHeader("DNT","1");

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        System.out.println(status);
                        if (status == 200) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }
                };
                String responseBody = httpClient.execute(request, responseHandler);
                return responseBody;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return  "";
    }
}
