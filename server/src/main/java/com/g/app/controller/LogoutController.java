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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.g.app.util.CasUserSessionDto;
import com.g.app.util.TextUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 退出登录controller
 * @author davidwang2006@aliyun.com
 * @date 2018-03-17 12:41:33
 */
@RestController
public class LogoutController {
	
	@Autowired
	private JedisPool jedisPool;
	

	@RequestMapping("/logout")
	public ModelAndView logout(@RequestParam(required=false,value="redirectUrl")String redirectUrl,
			@CookieValue(required=false,value=TextUtil.COOKIE_U_NAME, defaultValue="") String u,
			@CookieValue(required=false,value=TextUtil.COOKIE_RANDOM_NAME,defaultValue="") String random,
			Model model,
			HttpServletRequest req,
			HttpServletResponse resp) {
		//客户端ip地址
		String ip = TextUtil.remoteIp(req);
		
		if(StringUtils.isEmpty(redirectUrl))
			redirectUrl = req.getHeader("Referer");

		//如果cookie u不存在 ，则我们不必删除它
		if(!StringUtils.isEmpty(u)) {
			try(Jedis jedis = jedisPool.getResource();){
				if(jedis.exists(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u)) {
					CasUserSessionDto dto = CasUserSessionDto.parse(jedis.get(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u));
					if(dto.sameRandom(random)) {//只有存储的值中的ip与此客户端ip是一致的，才能移除
						jedis.del(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u);
						if(StringUtils.isEmpty(redirectUrl)) redirectUrl = dto.getRedirectUrl();
					}
				}
			}
			//从浏览器中删除此cookie
			Cookie cookie = new Cookie(TextUtil.COOKIE_U_NAME,u);
			cookie.setMaxAge(0);
			resp.addCookie(cookie);
			cookie = new Cookie(TextUtil.COOKIE_RANDOM_NAME,"nonce");
			cookie.setMaxAge(0);
			resp.addCookie(cookie);
		}
		
		//校验参数redirectUrl, 正常情况下，此参数必存在，但此处考虑一下这个情况
		if(StringUtils.isEmpty(redirectUrl)) {
			model.addAttribute("msg","redirectUrl 必需");
			return new ModelAndView("error");
		}
		//其余情况，则直接显示登录页面
		return new ModelAndView("redirect:/login?redirectUrl="+TextUtil.encode(redirectUrl));
		
	}
	
}
