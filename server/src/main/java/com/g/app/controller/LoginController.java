/**
 * 
 */
package com.g.app.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.g.app.util.CasUserSessionDto;
import com.g.app.util.TextUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 * redis里面存储的session/cookie结构为
 * 
 *	cas:user:k1: v1
 *	cas:user:k2: v2
 *  ...
 *	cas:user:kn: vn
 * 	...
 * 
 * k1 为加密后的username -> 即cookie u的值
 * v1 为当前登录的客户端ip 
 * 
 * @author davidwang2006@aliyun.com
 * @date 2018-03-17 11:19:17
 */
@RestController
public class LoginController {
	
	
	@Autowired
	private JedisPool jedisPool;
	
	
	/**
	 * login page
	 * @param redirectUrl
	 * @param u
	 * @author davidwang2006@aliyun.com
	 * @return
	 */
	@RequestMapping(value="/login",method=RequestMethod.GET)
	public ModelAndView loginPage(@RequestParam(required=false,value="redirectUrl")String redirectUrl,
			@CookieValue(required=false,value=TextUtil.COOKIE_U_NAME, defaultValue="") String u,
			@CookieValue(required=false,value=TextUtil.COOKIE_RANDOM_NAME,defaultValue="") String random,
			Model model,HttpServletRequest req) {
		
		//校验参数redirectUrl, 正常情况下，此参数必存在，但此处考虑一下这个情况
		if(StringUtils.isEmpty(redirectUrl)) {
			return new ModelAndView("error","msg","redirectUrl 必需");
		}
		
		//客户端ip地址
		String ip = TextUtil.remoteIp(req);
		
		//1. 如果有此cookie表示已经登录成功
		//此时需要向redis校验
		try(Jedis jedis = jedisPool.getResource();){
			if(!StringUtils.isEmpty(u) && jedis.exists(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u)) {
				CasUserSessionDto dto = CasUserSessionDto.parse(jedis.get(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u));
				//如果ip等于redis里面存储的ip, 则证明是同一个人同一个浏览器客户端,直接重定向至client页面
				if(dto.sameRandom(random)) {
					String url = TextUtil.addKv(redirectUrl, TextUtil.COOKIE_U_REDIRECT_PARAM_NAME, u);
					url = TextUtil.addKv(url, TextUtil.COOKIE_RANDOM_REDIRECT_PARAM_NAME, dto.getRandom());
					return new ModelAndView(String.format("redirect:%s", url));
				}
			}
		}
		
		//其余情况，则直接显示登录页面
		return new ModelAndView("login");
		
	}
	
	/**
	 * login page
	 * @param redirectUrl
	 * @param u
	 * @author davidwang2006@aliyun.com
	 * @return
	 */
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public ModelAndView login(@RequestParam(required=false,value="redirectUrl")String redirectUrl,
			@RequestParam(required=false,value="username")String username,
			@RequestParam(required=false,value="password")String password,
			@RequestParam(required=false,value="kick")String kick,
			@CookieValue(required=false,value=TextUtil.COOKIE_RANDOM_NAME,defaultValue="") String random,
			Model model,
			HttpServletRequest req,
			HttpServletResponse resp) {
		
		//校验参数redirectUrl, 正常情况下，此参数必存在，但此处考虑一下这个情况
		if(StringUtils.isEmpty(redirectUrl)) {
			model.addAttribute("msg","redirectUrl 必需");
			return new ModelAndView("error");
		}
		
		//客户端ip地址
		String ip = TextUtil.remoteIp(req);
		
		//1. 判断用户名与密码是否正确
		if(!"admin".equals(username) || !"admin".equals(password)) {//真实时请使用service/dao 替换
			model.addAttribute("msg","用户名密码错误");
			return new ModelAndView("login");
		}
		
		
		//加密用户名
		String u = TextUtil.sha1(username);
		//2. 如果用户名密码正确，则要判断是否已经在另一台登录
		try(Jedis jedis = jedisPool.getResource();){
			if(jedis.exists(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u)) {
				CasUserSessionDto dto = CasUserSessionDto.parse(jedis.get(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u));
				if(!dto.sameRandom(random) && StringUtils.isEmpty(kick)) {
					//2.0 如果不同，则提示踢掉它
					model.addAttribute("ip", dto.getIp());
					model.addAttribute("kick", true);
					return new ModelAndView("login");
				}//如果相同，则进行下一步登录成功操作
			}
		}

		//3. 登录成功
		random = TextUtil.random(String.format("%s:%s", username,ip));
		CasUserSessionDto dto = new CasUserSessionDto();
		dto.setIp(ip);
		dto.setLoginTime(System.currentTimeMillis());
		dto.setUsername(username);
		dto.setRedirectUrl(redirectUrl);
		dto.setRandom(random);
		//2.0 存入jedis
		try(Jedis jedis = jedisPool.getResource();){
			jedis.set(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u, dto.json());
			//2.1 设置一个超时时间
			jedis.expire(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u, TextUtil.DEFAULT_SESSION_TIMEOUT);
		}
		
		//组装cookie
		Cookie cookie = new Cookie(TextUtil.COOKIE_U_NAME,u);
		cookie.setMaxAge(TextUtil.DEFAULT_SESSION_TIMEOUT);
		resp.addCookie(cookie);
		cookie = new Cookie(TextUtil.COOKIE_RANDOM_NAME, random);
		cookie.setMaxAge(TextUtil.DEFAULT_SESSION_TIMEOUT);
		resp.addCookie(cookie);
		//必须先发送重定向至自己[server],这样才能将这个cookie存起来
		return new ModelAndView("redirect:/login?redirectUrl="+TextUtil.encode(redirectUrl));

		
	}

}
