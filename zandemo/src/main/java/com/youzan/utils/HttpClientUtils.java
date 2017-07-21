package com.youzan.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

public class HttpClientUtils {
	
	protected static Logger logger = Logger.getLogger(HttpClientUtils.class);

	//发送get请求
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader inpo = null;
        boolean isInt = false;
        int i = 1;
        while(isInt==false) {
        	if(i>=5){
        		break;
        	}
            try {
                Thread.sleep(100);
                isInt = true;
                String urlNameString = url + "?" + param;
                logger.info(urlNameString);
                trustAllHosts();
                URL realUrl = new URL(urlNameString);
                // 打开和URL之间的连接
                URLConnection connection = realUrl.openConnection();
                // 设置通用的请求属性
                connection.setRequestProperty("accept", "*/*");
                connection.setRequestProperty("connection", "Keep-Alive");
                connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                // 建立实际的连接
                connection.connect();
                // 定义 BufferedReader输入流来读取URL的响应
                inpo = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String line;
                while ((line = inpo.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                isInt=false;
                logger.info("发送GET请求出现异常！" + e + "结尾");
                e.printStackTrace();
                result = "";
            }
            // 使用finally块来关闭输入流
            finally {
                try {
                    if (inpo != null) {
                        inpo.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            i++;
        }
        return result;
    }
    
    private static void trustAllHosts() {  
    	  
        // Create a trust manager that does not validate certificate chains  
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {  
      
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
                return new java.security.cert.X509Certificate[] {};  
            }  
      
            public void checkClientTrusted(X509Certificate[] chain, String authType)  {  
                  
            }  
      
            public void checkServerTrusted(X509Certificate[] chain, String authType) {  
                  
            }  
        } };  
      
        // Install the all-trusting trust manager  
        try {  
            SSLContext sc = SSLContext.getInstance("TLS");  
            sc.init(null, trustAllCerts, new java.security.SecureRandom());  
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    //
    
    public static String postForYouZan(String URL,String json){
    	HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(URL);

        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addHeader("Authorization", "Basic YWRtaW46");
        String result = "error";

        try {

            StringEntity s = new StringEntity(json, "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/x-www-form-urlencoded"));
            post.setEntity(s);

            // 发送请求
            HttpResponse httpResponse = client.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                logger.info("请求服务器成功，做相应处理");
            } else {
                logger.info("请求服务端失败");
            }
            // 获取响应输入流
            InputStream inStream = httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inStream, "utf-8"));
            StringBuilder strber = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                strber.append(line + "\n");
            inStream.close();

            result = strber.toString();
            logger.info(result);

        } catch (Exception e) {
            logger.error("请求异常");
            logger.error(e.getMessage(),e);
        }

        return result;
    }
	
    public static String post(String URL,String json) {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(URL);

        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
//        post.setHeader("Content-Type", "application/json");
//        post.addHeader("Authorization", "Basic YWRtaW46");
        String result = "error";

        try {

            StringEntity s = new StringEntity(json, "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                  "application/x-www-form-urlencoded"));
//            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
//                    "application/json"));
            post.setEntity(s);

            // 发送请求
            HttpResponse httpResponse = client.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                logger.info("请求服务器成功，做相应处理");
            } else {
                logger.info("请求服务端失败");
            }
            // 获取响应输入流
            InputStream inStream = httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inStream, "utf-8"));
            StringBuilder strber = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                strber.append(line + "\n");
            inStream.close();

            result = strber.toString();
            logger.info(result);

        } catch (Exception e) {
            logger.info("请求异常");
            throw new RuntimeException(e);
        }

        return result;
    }
    
    public static String sendPost(String reqUrl,String param){
        try {
        	logger.info("request post url:"+reqUrl);
        	logger.info("request post param: "+param);
        	
            // 创建url资源
            URL url = new URL(reqUrl);
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);

            conn.setDoInput(true);

            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("POST");
            // 设置维持长连接
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", "UTF-8");
            //转换为字节数组
            byte[] data = param.getBytes();
            // 设置文件长度
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));

            // 设置文件类型:
            conn.setRequestProperty("Content-Type", "application/json");
            // 开始连接请求
            conn.connect();
            OutputStream  out = conn.getOutputStream();
            // 写入请求的字符串
            out.write(param.getBytes());
            out.flush();
            out.close();

            logger.info("responseCode："+conn.getResponseCode());
            // 请求返回的状态
            if (conn.getResponseCode() == 200) {
            	logger.info("请求成功");
                // 请求返回的数据
                InputStream inof = conn.getInputStream();
                String a = null;
                try {
                    byte[] data1 = new byte[inof.available()];
                    inof.read(data1);
                    // 转成字符串
                    String result = new String(data1);
                    logger.info("request post responseMessage:"+result);
                   return result;
                } catch (Exception e1) {
                   logger.error("获取response error");
                   logger.error(e1.getMessage(),e1);
                }
            } else {
               logger.info("send post 请求失败");
               logger.info("send post "+conn.getResponseMessage());
            }

        } catch (Exception e) {
            logger.error("发送Post请求错误");
            logger.error(e.getMessage(),e);
        }
        return "error";
    }
    
    
    public static String  postHttps(String url,String params){
    	DefaultHttpClient client = new DefaultHttpClient();
    	enableSSL(client);
    	client.getParams().setParameter("http.protocol.content-charset",
    			HTTP.UTF_8);
    	client.getParams().setParameter(HTTP.CONTENT_ENCODING, HTTP.UTF_8);
    	client.getParams().setParameter(HTTP.CHARSET_PARAM, HTTP.UTF_8);
    	client.getParams().setParameter(HTTP.DEFAULT_PROTOCOL_CHARSET,
    			HTTP.UTF_8);
    	
        HttpPost post = new HttpPost(url);

        post.setHeader("Content-Type", "application/json");
        post.addHeader("Authorization", "Basic YWRtaW46");
        String result = "error";
        try {

            StringEntity s = new StringEntity(params, "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            post.setEntity(s);

            // 发送请求
            HttpResponse httpResponse = client.execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                logger.info("请求服务器成功，做相应处理");
            } else {
                logger.info("请求服务端失败");
            }
            // 获取响应输入流
            InputStream inStream = httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inStream, "utf-8"));
            StringBuilder strber = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                strber.append(line + "\n");
            inStream.close();

            result = strber.toString();
            logger.info(result);
        } catch (Exception e) {
            logger.error("发送 https 请求异常");
            logger.error(e.getMessage(),e);
            return "error";
        }
    	
    	return result;
    }
    
    /** 
     * 访问https的网站 
     * @param httpclient 
     */  
    private static void enableSSL(DefaultHttpClient httpclient){  
        //调用ssl  
         try {  
                SSLContext sslcontext = SSLContext.getInstance("TLS");  
                sslcontext.init(null, new TrustManager[] { truseAllManager }, null);  
                SSLSocketFactory sf = new SSLSocketFactory(sslcontext);  
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
                Scheme https = new Scheme("https", sf, 443);  
                httpclient.getConnectionManager().getSchemeRegistry().register(https);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
    }  
    /** 
     * 重写验证方法，取消检测ssl 
     */  
    private static TrustManager truseAllManager = new X509TrustManager(){  
  
        public void checkClientTrusted(  
                java.security.cert.X509Certificate[] arg0, String arg1)  
                throws CertificateException {  
            // TODO Auto-generated method stub  
              
        }  
  
        public void checkServerTrusted(  
                java.security.cert.X509Certificate[] arg0, String arg1)  
                throws CertificateException {  
            // TODO Auto-generated method stub  
              
        }  
  
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
            // TODO Auto-generated method stub  
            return null;  
        }  
          
    }; 
    
    
    public static void getUploadInformation(String path,String obj) throws IOException{
        //创建连接
        URL url = new URL(path);
        HttpURLConnection connection ;
        StringBuffer sbuffer=null;
        try {
            //添加 请求内容
            connection= (HttpURLConnection) url.openConnection();
            //设置http连接属性
            connection.setDoOutput(true);// http正文内，因此需要设为true, 默认情况下是false;
            connection.setDoInput(true);// 设置是否从httpUrlConnection读入，默认情况下是true;
            connection.setRequestMethod("PUT"); // 可以根据需要 提交 GET、POST、DELETE、PUT等http提供的功能
            //connection.setUseCaches(false);//设置缓存，注意设置请求方法为post不能用缓存
            // connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Host", "*******");  //设置请 求的服务器网址，域名，例如***.**.***.***
            connection.setRequestProperty("Content-Type", " application/json");//设定 请求格式 json，也可以设定xml格式的
            connection.setRequestProperty("Accept-Charset", "utf-8");  //设置编码语言
            connection.setRequestProperty("X-Auth-Token", "token");  //设置请求的token
            connection.setRequestProperty("Connection", "keep-alive");  //设置连接的状态
            connection.setRequestProperty("Transfer-Encoding", "chunked");//设置传输编码
            connection.setRequestProperty("Content-Length", obj.toString().getBytes().length + "");  //设置文件请求的长度
            connection.setReadTimeout(10000);//设置读取超时时间
            connection.setConnectTimeout(10000);//设置连接超时时间
            connection.connect();
            OutputStream out = connection.getOutputStream();//向对象输出流写出数据，这些数据将存到内存缓冲区中
            out.write(obj.toString().getBytes());            //刷新对象输出流，将任何字节都写入潜在的流中
            out.flush();
            // 关闭流对象,此时，不能再向对象输出流写入任何数据，先前写入的数据存在于内存缓冲区中
            out.close();
            //读取响应
            if (connection.getResponseCode()==200){
                // 从服务器获得一个输入流
                InputStreamReader   inputStream =new InputStreamReader(connection.getInputStream());//调用HttpURLConnection连接对象的getInputStream()函数, 将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
                BufferedReader reader = new BufferedReader(inputStream);
                String lines;
                sbuffer= new StringBuffer("");
                while ((lines = reader.readLine()) != null){
                    lines = new String(lines.getBytes(), "utf-8");
                    sbuffer.append(lines);
                }
                reader.close();
                logger.info("modify customer result:"+sbuffer.toString());
            }else{
                System.out.println("请求失败"+connection.getResponseCode());
            }
            //断开连接
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
