/**
 * 
 */
package com.g.app.filter;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StringUtils;

/**
 * 包装一下原始的HttpServletRequestWrapper, 以支持UserPricipal
 * @author davidwang2006@aliyun.com
 * @date 2018-03-17 14:08:10
 */
public class AuthHttpServletRequest extends HttpServletRequestWrapper {
	private HttpServletRequest req;
	public AuthHttpServletRequest(HttpServletRequest request) {
		super(request);
		this.req = request;
	}

	@Override
	public Principal getUserPrincipal() {
		String user = (String) req.getAttribute("userno");
		if(StringUtils.isEmpty(user))
			user = (String) req.getAttribute("user");
		if(StringUtils.hasText(user))
			return new AuthPrincipal(user);
		return super.getUserPrincipal();
	}
	
	/**
	 * wrap the principal
	 * TODO
	 * @author davidwang2006@aliyun.com
	 * @date 2018-03-17 14:12:31
	 *
	 */
	static class AuthPrincipal implements Principal{
		private String name;
		public AuthPrincipal(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
