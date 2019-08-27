/**
 * 
 */
package com.g.app.util;

import java.io.Serializable;

/**
 * TODO
 * @author davidwang2006@aliyun.com
 * @date 2018-03-19 17:29:56
 *
 */
public class VerifyResultDto implements Serializable {
	private static final long serialVersionUID = -4025908449225780551L;
	private int code;
	private String msg;
	private String user;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

}
