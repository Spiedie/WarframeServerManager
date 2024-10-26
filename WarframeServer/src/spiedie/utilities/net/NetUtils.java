package spiedie.utilities.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.util.Collections;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import spiedie.utilities.data.StringUtils;

import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.utilities.util.persistentInfo.DataCache;
import spiedie.utilities.util.persistentInfo.Info;

public class NetUtils {
	public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";
	//Mozilla/5.0 (Windows NT 6.1; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0
	
	public static final String KEY_NET_CACHE = "net";
	
	public static final String KEY_NET_CACHE_DELAY = "netCacheDelay";
	public static final String KEY_SET_EMPTY_CACHE_DATA_IF_NOT_FOUND = "setEmptyIfUrlNotFound";
	public static final String KEY_SET_USER_AGENT = "URLConnectionSetUserAgent";
	public static final String KEY_USER_AGENT = "URLConnectionUserAgent";
	
	public static boolean wGetUseCache = true;
	
	/**
	 * 
	 */
	private NetUtils(){}
	
	public static String localIp(){
		String ip = null;
		try {
			for(NetworkInterface i : Collections.list(NetworkInterface.getNetworkInterfaces())){
				List<InetAddress> addresses = Collections.list(i.getInetAddresses());
				for(InetAddress a : addresses){
					if(ip == null && a.isSiteLocalAddress()) ip = a.getHostAddress();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		try {
			if(ip == null) ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}
	
	public static String hash(String s){
		String postfix = StringUtils.keep(s, StringUtils.DIGITS+StringUtils.LOWER+StringUtils.UPPER);
		return Integer.toHexString(s.hashCode())+postfix;
	}
	
	public static String getHTMLCached(String url, ISettings options, long allowedOutOfDate) throws IOException{
		byte[] buf = wGetCached(url, options, allowedOutOfDate);
		return buf == null ? null : new String(buf, Stream.charset);
	}
	
	/**
	 * get data from url. If within time limit, return cached data. If not, try and get new data from the url. In the end cached data is returned.
	 * @param url
	 * @param options
	 * @param allowedOutOfDate
	 * @return the data
	 * @throws IOException
	 */
	public static byte[] wGetCached(String url, ISettings options, long allowedOutOfDate) throws IOException{
		String key = KEY_NET_CACHE+File.separator+hash(url);
		String timeKey = KEY_NET_CACHE+File.separator+hash(url+"-time");
		boolean isKeySet = DataCache.isSet(key, null, options);
		boolean isTimeKeySet = Info.isSet(timeKey, null, options);
		long timeSinceCache = isTimeKeySet ? Time.millis() - Long.parseLong(Info.getProperty(timeKey, null, options)) : 0;
		if(!wGetUseCache || !isKeySet || !isTimeKeySet || timeSinceCache > allowedOutOfDate){
			if(allowedOutOfDate != 0){
				if(!wGetUseCache){
					Log.err(NetUtils.class, "Cache disabled, fetch from net: "+url);
				} else if(!isKeySet || !isTimeKeySet){
					Log.err(NetUtils.class, "No cache, fetch from net: "+url);
				} else if(timeSinceCache > allowedOutOfDate){
					Log.err(NetUtils.class, "Out of date by "+Time.toTimeString(timeSinceCache)+" ms, fetch from net: "+url);
				}
			}
			byte[] buf = null;
			try{
				buf = wGet(url);
			} catch(IOException e){
				Log.err(NetUtils.class, "wGetCached: "+url+" "+e);
			}
			if(options != null && options.isSet(KEY_NET_CACHE_DELAY)){
				long delay = Long.parseLong("0"+options.getProperty(KEY_NET_CACHE_DELAY));
				Time.sleep(delay);
			}
			if(buf == null && options != null && options.isSet(KEY_SET_EMPTY_CACHE_DATA_IF_NOT_FOUND)){
				buf = new byte[0];
			}
			if(buf != null){
				DataCache.setProperty(key, buf, null, options);
				Info.setProperty(timeKey, String.valueOf(Time.millis()), null, options);
			}
		}
		return DataCache.getProperty(key, null, options);
	}
	
	public static byte[] wGet(String url) throws IOException{
		return wGet(new URL(url));
	}
	
	public static byte[] wGet(URL url) throws IOException{
		InputStream in = getConnectionStream(url);
		return Stream.read(in);
	}
	
	private static InputStream getConnectionStream(URL url) throws IOException{
		URLConnection con = url.openConnection();
		if(Constants.getSettings().isSet(KEY_SET_USER_AGENT)){
			String userAgent = DEFAULT_USER_AGENT;
			if(Constants.isSet(KEY_USER_AGENT)) userAgent = Constants.getProperty(KEY_USER_AGENT);
			con.addRequestProperty("User-Agent", userAgent);
		}
		addHeader(con, "Accept");
		addHeader(con, "Accept-Encoding");
		addHeader(con, "Accept-Language");
		addHeader(con, "Host");
		return con.getInputStream();
	}
	
	private static void addHeader(URLConnection con, String key) {
		if(Constants.getSettings().isSet(key)){
			con.addRequestProperty(key, Constants.getSettings().getProperty(key));
		}
	}
	
	/**
	 * Disable all SSL security checks. Use with caution.
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static void removeSSLSecurity() throws NoSuchAlgorithmException, KeyManagementException{
//		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public void checkServerTrusted(X509Certificate[] cert, String authType) throws CertificateException {}
					public void checkClientTrusted(X509Certificate[] cert, String authType) throws CertificateException {}
					
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				}
		};
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}
}
