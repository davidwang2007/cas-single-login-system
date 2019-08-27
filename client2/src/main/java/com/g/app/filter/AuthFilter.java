/**
 * 
 */
package com.g.app.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.g.app.util.TextUtil;
import com.g.app.util.VerifyResultDto;
import com.google.gson.Gson;


/**
 * 客户端使用权限过滤器
 * @author davidwang2006@aliyun.com
 * @date 2018-03-17 13:31:23
 */
public class AuthFilter implements Filter {

	private Logger log = LoggerFactory.getLogger(AuthFilter.class);

	private String loginUrl;
	private String logoutUrl;
	private String verifyUrl;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		//提取cookie u的值
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		req.setAttribute("logoutUrl", logoutUrl);
		String originalUrl = TextUtil.originalUrl(req);

		String ip = TextUtil.remoteIp(req);
		//先看request参数中是否有token_u 参数,有的话，表示是从server重定向过来的
		//那么则要配置cookie而后发送重定向了
		String tokenU = req.getParameter(TextUtil.COOKIE_U_REDIRECT_PARAM_NAME);
		String tokenR = req.getParameter(TextUtil.COOKIE_RANDOM_REDIRECT_PARAM_NAME);
		if(!StringUtils.isEmpty(tokenU) && !StringUtils.isEmpty(tokenR)) {
			//由于是binary search故，参数应是排序后的
			String newUrl = TextUtil.originalUrl(req, TextUtil.COOKIE_RANDOM_REDIRECT_PARAM_NAME,TextUtil.COOKIE_U_REDIRECT_PARAM_NAME);
			//添加cookie，以及发送重定向，目的是在地址栏显示中去除token_u
			Cookie cookie = new Cookie(TextUtil.COOKIE_U_NAME,tokenU);
			cookie.setMaxAge(TextUtil.DEFAULT_SESSION_TIMEOUT);
			resp.addCookie(cookie);
			cookie = new Cookie(TextUtil.COOKIE_RANDOM_NAME,tokenR);
			cookie.setMaxAge(TextUtil.DEFAULT_SESSION_TIMEOUT);
			resp.addCookie(cookie);
			resp.sendRedirect(newUrl);
			return;
		}


		Cookie[] cookies = req.getCookies();
		if(cookies == null) cookies = new Cookie[0];
		//cookie u的值
		String u = null;
		String random = null;
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(TextUtil.COOKIE_U_NAME))
				u = cookie.getValue();
			else if(cookie.getName().equals(TextUtil.COOKIE_RANDOM_NAME))
				random = cookie.getValue();
		}
		//如果u值不为空，则证明浏览器存储了这个cookie
		//下一步，向server端验证这个cookie是否有效
		if(!StringUtils.isEmpty(u) && !StringUtils.isEmpty(random)) {
			try {
				VerifyResultDto dto = verify(u, random);
				if(dto.getCode() == 0){
					req.setAttribute("userno", dto.getUser());
					req.setAttribute("user", dto.getUser());
					chain.doFilter(new AuthHttpServletRequest(req), response);
					return;
				}
			}catch(Exception ex) {
				log.error("verify u:{}, random:{}, got {}",u,random,ex.getMessage(),ex.getCause());
				resp.getWriter().println("WHEN AUTH GOT ERROR:" + ex.getMessage());
				return;
			}
		}

		//其余情况，则全部是重定向至server的登录页面
		resp.sendRedirect(TextUtil.addKv(loginUrl, "redirectUrl", originalUrl));

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		loginUrl = filterConfig.getInitParameter("loginUrl");
		logoutUrl = filterConfig.getInitParameter("logoutUrl");
		verifyUrl = filterConfig.getInitParameter("verifyUrl");
		if(StringUtils.isEmpty(loginUrl) || StringUtils.isEmpty(logoutUrl) || StringUtils.isEmpty(verifyUrl)) throw new ServletException("loginUrl/logoutUrl/verifyUrl must not be empty for AuthFilter");
	}

	/**
	 * verify remote
	 * @param u
	 * @param r
	 * @return
	 * @throws IOException
	 */
	private VerifyResultDto verify(String u, String r) throws IOException{
		URL url = new URL(verifyUrl);
		byte[] raw = String.format("u=%s&r=%s", TextUtil.encode(u),TextUtil.encode(r)).getBytes();
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(raw.length));
		conn.setUseCaches(false);
		conn.connect();
		StringBuilder sb = new StringBuilder();
		try{
			OutputStream os = conn.getOutputStream();
			os.write(raw);
			os.flush();
			os.close();
			if(conn.getResponseCode() - 200 > 100) throw new IOException("statuscode: " + conn.getResponseCode());
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), TextUtil.CHARSET_UTF8));
			String line = null;
			while((line = reader.readLine()) != null) sb.append(line);
		}finally {
			if(conn.getResponseCode() - 200 > 100) conn.getErrorStream().close();
			else conn.getInputStream().close();
		}

		VerifyResultDto jo = new Gson().fromJson(sb.toString(), VerifyResultDto.class);
		return jo;
	}

	@Override
	public void destroy() { }

}
