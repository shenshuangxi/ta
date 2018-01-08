package com.sundy.ta.datasearch.downloader;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sundy.ta.datasearch.model.Site;

public class HttpClientGenerator {

	private final static Logger logger = LoggerFactory.getLogger(HttpClientGenerator.class);
	
	private static PoolingHttpClientConnectionManager connectionManager;
	
	public static HttpClientGenerator build(){
		return new HttpClientGenerator();
	}
	
	public HttpClientGenerator setPoolSize(int poolSize) {
		connectionManager.setMaxTotal(poolSize);
		return this;
	}
	
	public CloseableHttpClient getClient(Site site) {
		return generateClient(site);
	}
	
	private HttpClientGenerator() {
		Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", buildSSLConnectionSocketFactory())
				.build();
		connectionManager = new PoolingHttpClientConnectionManager(reg);
		connectionManager.setDefaultMaxPerRoute(Integer.MAX_VALUE);
	}
	
	private static ConnectionSocketFactory buildSSLConnectionSocketFactory() {
		try {
			return new SSLConnectionSocketFactory(createIgnoreVerifySSL(), new String[]{"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null, new DefaultHostnameVerifier());
		} catch (KeyManagementException e) {
            logger.error("ssl connection fail", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("ssl connection fail", e);
        }
		return SSLConnectionSocketFactory.getSocketFactory();
	}

	private static SSLContext createIgnoreVerifySSL() throws KeyManagementException, NoSuchAlgorithmException {
		X509TrustManager trustManager = new X509TrustManager() {
			
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			
			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}
			
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}
		};
		SSLContext sc = SSLContext.getInstance("SSLv3");
		sc.init(null, new TrustManager[]{trustManager}, null);
		return sc;
	}
	
	

	private CloseableHttpClient generateClient(Site site) {
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		httpClientBuilder.setConnectionManager(connectionManager);
		if(site.getUserAgent() != null) {
			httpClientBuilder.setUserAgent(site.getUserAgent());
		} else {
			httpClientBuilder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		}
		
		if (site.isUseGzip()) {
			httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptor() {
				public void process(HttpRequest request, HttpContext context)
						throws HttpException, IOException {
					if(!request.containsHeader("Accept-Encoding")) {
						request.addHeader("Accept-Encoding", "gzip");
					}
				}
			});
		}
		
		httpClientBuilder.setRedirectStrategy(new CustomRedirectStrtegy());
		
		SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
		socketConfigBuilder.setSoKeepAlive(true).setTcpNoDelay(true);
		socketConfigBuilder.setSoTimeout(site.getTimeOut());
		SocketConfig socketConfig = socketConfigBuilder.build();
		connectionManager.setDefaultSocketConfig(socketConfig);
		httpClientBuilder.setDefaultSocketConfig(socketConfig);
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(site.getRetryTimes(), true));
		generateCookie(httpClientBuilder, site);
		return httpClientBuilder.build();
		
	}

	private void generateCookie(HttpClientBuilder httpClientBuilder, Site site) {
		if (site.isDisableCookieManagement()) {
            httpClientBuilder.disableCookieManagement();
            return;
        }
        CookieStore cookieStore = new BasicCookieStore();
        for (Map.Entry<String, String> cookieEntry : site.getDefaultCookies().entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setDomain(site.getDomain());
            cookieStore.addCookie(cookie);
        }
        for (Map.Entry<String, Map<String, String>> domainEntry : site.getCookies().entrySet()) {
            for (Map.Entry<String, String> cookieEntry : domainEntry.getValue().entrySet()) {
                BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
                cookie.setDomain(domainEntry.getKey());
                cookieStore.addCookie(cookie);
            }
        }
        httpClientBuilder.setDefaultCookieStore(cookieStore);
	}
	
}
