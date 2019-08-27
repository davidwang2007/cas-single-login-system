/**
 * 
 */
package com.g.app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.StringUtils;

/**
 * add more util method: 2016-8-5 14:57:54
 * @author davidwang2006@aliyun.com
 */
public abstract class TextUtil {
	
	/**
	 * hide the implicit public one
	 */
	private TextUtil(){}

	/**
	 * name
	 */
	public static final String REDIS_CAS_KEYNAME_PREFIX = "cas:user:";
	public static final String COOKIE_U_NAME = "u";
	public static final String COOKIE_RANDOM_NAME = "r";
	public static final String COOKIE_U_REDIRECT_PARAM_NAME = "token_u";
	public static final String COOKIE_RANDOM_REDIRECT_PARAM_NAME = "token_r";

	public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
	//默认2小时后无效
	public static final int DEFAULT_SESSION_TIMEOUT = 60 * 120;
	
	/**
	 * add kv param to original url
	 * @param originalUrl
	 * @param key
	 * @param value not url encoded already
	 * @author davidwang2006@aliyun.com
	 * @return
	 */
	public static String addKv(String originalUrl, String key,String value) {
		StringBuilder sb = new StringBuilder(originalUrl);
		if(originalUrl.indexOf("?") > 0) sb.append("&");
		else sb.append("?");
		try {
			sb.append(URLEncoder.encode(key, "UTF-8"));
			sb.append("=");
			sb.append(URLEncoder.encode(value, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
		
		return sb.toString();
	}
	
	/**
	 * get remove client ip
	 * @param req
	 * @author davidwang2006@aliyun.com
	 * @return
	 */
	public static String remoteIp(HttpServletRequest req) {
		String ip = req.getHeader("X-Forwarded-For");
		if(StringUtils.isEmpty(ip)) {
			ip = req.getRemoteAddr();
			int index = ip.lastIndexOf(":");
			if(index > 0) ip = ip.substring(0, index);
		}
		return ip;
	}
	
	/**
	 * 简单加密
	 * @param msg
	 * @author davidwang2006@aliyun.com
	 * @return
	 */
	public static String sha1(String msg) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			byte[] result = digest.digest(msg.getBytes(CHARSET_UTF8));
			return Base64.encodeBase64URLSafeString(result);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * generate random
	 * @param msg
	 * @return
	 */
	public static String random(String msg) {
		
		SecretKeySpec sk = new SecretKeySpec(String.valueOf(System.currentTimeMillis()).getBytes(), "HmacSHA1");
		Mac mac;
		try {
			mac = Mac.getInstance("HmacSHA1");
			mac.init(sk);
			byte[] raw = mac.doFinal(msg.getBytes(CHARSET_UTF8));
			return Base64.encodeBase64URLSafeString(raw);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(e.getMessage());
		}
		
	}
	
	/**
	 * encode url
	 * @param url
	 * @author davidwang2006@aliyun.com
	 * @return
	 */
	public static String encode(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * get httpservletrequest original url , including the request parameters
	 * <p>
	 * 请保证 excludeKeys 是排序过的，从小至大
	 * @param req
	 * @return
	 */
	public static String originalUrl(HttpServletRequest req,String... excludeKeys) {
		StringBuilder sb = new StringBuilder(req.getRequestURL());
		sb.append("?");
		Enumeration<String> em = req.getParameterNames();
		while(em.hasMoreElements()) {
			String n = em.nextElement();
			if(Arrays.binarySearch(excludeKeys, 0, excludeKeys.length, n) >= 0) continue;
			sb.append(String.format("%s=%s&", encode(n),req.getParameter(n)));
		}
		char lastChar = sb.charAt(sb.length() - 1);
		switch(lastChar) {
		case '?':
		case '&':
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	
}