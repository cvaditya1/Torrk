package com.example.ave.torrk;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by AVE on 3/21/2016.
 */
public class HttpCalls {

    private HttpCalls(){

    }

    private static HttpURLConnection mHttpURLConnection;
    private static InputStream inputStream;

    public static boolean openHttpConnection(String site){
        mHttpURLConnection = null;
        inputStream = null;
        int resCode = -1;
        try {
            URL url = new URL(site);
            mHttpURLConnection = (HttpURLConnection)url.openConnection();
            mHttpURLConnection.setReadTimeout(15 * 1000);
            mHttpURLConnection.connect();
            resCode = mHttpURLConnection.getResponseCode();
            if(resCode == HttpURLConnection.HTTP_OK){
                inputStream = mHttpURLConnection.getInputStream();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getContent(){
        String returnString = null;
        try {
            if(inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                returnString = sb.toString();
            } else {
                returnString = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnString = null;
        } finally {
            returnString = null;
        }
        return returnString;
    }

    public static void closeHttpConnection(){
        try{
            if(inputStream != null){
                inputStream.close();
                inputStream = null;
            }
            if(mHttpURLConnection != null){
                mHttpURLConnection.disconnect();
                mHttpURLConnection = null;
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private static String mURL = null;
    public static Connection jsoupConnect(String siteUrl){
        Connection jsoupConnection = null;
        try{
            if(jsoupConnection == null){
                mURL = siteUrl;
                jsoupConnection = Jsoup.connect(siteUrl);
                jsoupConnection.timeout(15 * 1000);
                Connection.Response resp = jsoupConnection.execute();
                Log.i("HTTPCalls","JsoupConnect(): connection statusCode: " + resp.statusCode());
                if(resp.statusCode() == HttpURLConnection.HTTP_OK){
                    Log.i("HTTPCalls","JsoupConnect(): connection successful with " + siteUrl);
                    return jsoupConnection;
                } else {
                    return null;
                }
            } else {
                if(mURL.equals(siteUrl)){
                    Log.i("HTTPCalls","JsoupConnect(): previous successful connection retrieved: " + siteUrl);
                    return jsoupConnection;
                } else {
                    jsoupConnection = null;
                    return jsoupConnect(siteUrl);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            Log.e("HTTPCalls","JsoupConnect(): Error in connecting with " + siteUrl);
            return null;
        }
    }

    public static Document jsoupGetContent(Connection connection){
        Document content_Document = null;
        try{
            if(connection != null){
                content_Document = connection.get();
                Log.i("HTTPCalls","JsoupConnect(): Success in getting webpage ");
                return content_Document;
            } else {
                return null;
            }
        } catch (Exception e){
            e.printStackTrace();
            Log.e("HTTPCalls","JsoupConnect(): Error in getting document ");
            return null;
        }
    }
}
