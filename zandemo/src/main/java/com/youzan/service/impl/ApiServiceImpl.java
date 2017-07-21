package com.youzan.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

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
import org.springframework.stereotype.Service;

import com.youzan.service.ApiService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("apiService")
public class ApiServiceImpl implements ApiService{
	
	protected Logger logger = Logger.getLogger(ApiServiceImpl.class);
	
	private static final String deals_url = "/v1/dealgroup";
	
	private static final String accessToken_url = "/security/accesstoken";
	
	private static final String searchCustomer_url = "/v1/customeridentities";
	
	private static final String mergeCustomer_url = "/v1/customerService/migrateIdentity";
	
	private static final String getCustomer_url = "/v1/customers";
	private String appid = "";
	private String secret = "";
	private String path = "";

	@Override
	public String dealGroup(JSONArray params,String appid,String secret,String path) {
		this.appid = appid;
		this.secret = secret;
		this.path = path;
		String resMsg = "";
		try {
			String accessToken = getAccessToken();
			if(accessToken == null || "".equals(accessToken)){
				logger.info("accessToken is blank");
				return "error";
			}
			String url = path+deals_url;
			String result = postHttps(url+"?access_token="+accessToken, params.toString());
			if(result!= null){
				logger.info("deal group result:"+result);
				JSONObject jsonobj = JSONObject.fromObject(result);
				if(jsonobj.containsKey("ErrorCode") 
						&& "0".equals(jsonobj.getString("ErrorCode"))){
					JSONObject success = jsonobj.getJSONObject("Message").getJSONArray("success").getJSONObject(0);
					String customerId = success.getString("id");
					logger.info("deal group customerId:"+customerId);
					resMsg =  customerId;
				}else{
					resMsg = "error";
				}
			}
		} catch (Exception e) {
			logger.error("send dealGroup error -- ");
			logger.error(e.getMessage(),e);
			return "error";
		}
		return resMsg;
	
	}
	
	 /**
     * 客户身份信息查询接口
     * @param idenType msr-id jpush-android-id
     * @param idenValue 
     * @param valueKey identityValue customerId
     * @return
     */
	@Override
    public String searchCustomer(String idenValue){
    	logger.info("search Customer identityType:wechat identityValue:"+idenValue);
    	String url = path+searchCustomer_url;
    	String accessToken = getAccessToken();
    	logger.info("searchCustomer acccessToken:"+accessToken);
    	String result = sendGet(url,
    			"access_token="+accessToken+"&identityType=wechat&identityValue="+idenValue);
    	logger.info("searchCustomer Result:"+result);
    	
    	if(result ==null || "".equals(result)){
    		return "";
    	}
    	JSONArray arr = JSONArray.fromObject(result);
    	if(arr.isEmpty()){
    		return "";
    	}
    	
    	return arr.toString();
    }
	
	public  String addIdentity(String identityValue,String customerId){
		JSONObject params = new JSONObject();
   		params.put("identityType", "wechat");
   		params.put("identityValue", identityValue);
   		params.put("customerId", customerId);
    	String accessToken = getAccessToken();
    	String url = path + searchCustomer_url;
		return post(url+"?access_token="+accessToken, params);
    }
	
	 /**
     * 身份迁移
     * @param idenType
     * @param idenValue
     * @param toCusId
     * @return
     */
    public String migrateIdentity(String idenValue,String toCusId){
    	String url = path+mergeCustomer_url;
    	String accessToken = getAccessToken();	
    	JSONObject params = new JSONObject();
    	JSONObject json = new JSONObject();
    	json.put("type", "wechat");
    	json.put("value", idenValue);
    	params.put("identity", json);
    	params.put("toCustomerId", toCusId);
    	return post(url+"?access_token="+accessToken, params);
    }
    
    
    public String getCustomer(String mobile){
    	try {
			String url = path + getCustomer_url;
			String accessToken = getAccessToken();	
			String requestparam = "access_token="+accessToken+"&mobile="+mobile+"&select=name,email,telephone,mobile,birthday";
			String cusArr = sendGet(url, requestparam);
			
			JSONArray arr = JSONObject.fromObject(cusArr).getJSONArray("rows");
			return arr.getString(0);
		} catch (Exception e) {
			logger.error("getCustomer  mobile:"+mobile);
			logger.error(e.getMessage(),e);
		}
    	return null;
    }
	
	private String getAccessToken(){
		try {
			String url = path+accessToken_url;
			//发送get请求,通过appid和sercet获取accesstoken.
			String access = sendGet(url,"grant_type=client_credentials&appid=" + appid + "&secret=" + secret);
			Map<String,String> res = JSONObject.fromObject(access);
			String access_token = res.get("access_token").toString();
			logger.info("get access token success: "+access_token);
			return access_token;
		} catch (Exception e) {
			logger.error("get Accesstoken error");
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	 public String post(String URL,JSONObject json) {
	        logger.info("post url:" + URL);
	        logger.info("post params:" + json);
	        HttpClient client = new DefaultHttpClient();
	        HttpPost post = new HttpPost(URL);

	        post.setHeader("Content-Type", "application/json");
	        post.addHeader("Authorization", "Basic YWRtaW46");
	        String result = "";

	        try {

	            StringEntity s = new StringEntity(json.toString(), "utf-8");
	            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
	                    "application/json"));
	            post.setEntity(s);

	            // 发送请求
	            HttpResponse httpResponse = client.execute(post);
	            
	            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            	 logger.info("post success");
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
	                 logger.info("post result:"+result);
	            }
	        } catch (Exception e) {
	           logger.error("post error");
	           logger.error(e.getMessage(),e);
	           return "";
	        }

	        return result;
	    }
	
	 public String sendGet(String url, String param) {
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
	                // 获取所有响应头字段
	                Map<String, List<String>> map = connection.getHeaderFields();
	                // 遍历所有的响应头字段
	                for (String key : map.keySet()) {
	                    logger.info(key + "--->" + map.get(key));
	                }
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
	
	 private void trustAllHosts() {  
	  	  
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
	 private  String  postHttps(String url,String params){
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

}
