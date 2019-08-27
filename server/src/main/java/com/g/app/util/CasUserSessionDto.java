/**
 * 
 */
package com.g.app.util;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import com.google.gson.Gson;

/**
 * 存储在jedis里面的值, key为 cas:user:$u
 * <p>
 * 存储时，值为此类的json字符串
 * @author davidwang2006@aliyun.com
 * @date 2018-03-17 12:12:54
 */
public class CasUserSessionDto implements Serializable {
	private static final long serialVersionUID = -492289162544727390L;
	private String username;
	private String ip;
	private String random;
	//上次登录时间
	private long loginTime;
	//存储一个初始的重定向链接
	private String redirectUrl;
	public CasUserSessionDto() {
	}
	public static CasUserSessionDto parse(String json) {
		return new Gson().fromJson(json, CasUserSessionDto.class);
	}
	public String json() {
		return new Gson().toJson(this);
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getIp() {
		return ip;
	}
	public boolean sameIp(String ip2) {
		if(StringUtils.isEmpty(ip)) return false;
		return ip.equals(ip2);
	}
	
	public boolean sameRandom(String random2) {
		if(StringUtils.isEmpty(random)) return false;
		return random.equals(random2);
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	public long getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	public String getRedirectUrl() {
		return redirectUrl;
	}
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	public String getRandom() {
		return random;
	}
	public void setRandom(String random) {
		this.random = random;
	}
}
