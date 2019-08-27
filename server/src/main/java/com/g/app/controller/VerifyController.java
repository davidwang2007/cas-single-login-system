/**
 * 
 */
package com.g.app.controller;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g.app.util.CasUserSessionDto;
import com.g.app.util.TextUtil;
import com.g.app.util.VerifyResultDto;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * verify controller
 * @author davidwang2006@aliyun.com
 * @date 2018-03-19 17:12:24
 */
@RestController
public class VerifyController implements Serializable {
	private static final long serialVersionUID = 7639723570004264856L;
	@Autowired
	private JedisPool jedisPool;
	
	/**
	 * 校验cookie是否okay
	 * <p>
	 * u & r
	 * @return
	 */
	@RequestMapping(value="/verify",method=RequestMethod.POST)
	public VerifyResultDto verify(
			@RequestParam(required=false,value="u") String u,
			@RequestParam(required=false,value="r") String random,
			HttpServletRequest req) {
		VerifyResultDto dto = new VerifyResultDto();
		dto.setCode(1);
		dto.setMsg("not matched");
		if(StringUtils.isEmpty(u)) {
			u = req.getParameter("u");
		}
		if(StringUtils.isEmpty(random)) {
			random = req.getParameter("r");
		}
		
		if(!StringUtils.isEmpty(u) && !StringUtils.isEmpty(random))
			try(Jedis jedis = jedisPool.getResource();){
				if(jedis.exists(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u)) {
					CasUserSessionDto cdo = CasUserSessionDto.parse(jedis.get(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u));
					if(cdo.sameRandom(random)) {//jedis中存在 ，且random一致，则证明已登录过
						dto.setCode(0);
						dto.setMsg("matched");
						dto.setUser(cdo.getUsername());
						//重置一下超时时间
						jedis.expire(TextUtil.REDIS_CAS_KEYNAME_PREFIX+u, TextUtil.DEFAULT_SESSION_TIMEOUT);
					}
				}
			}
		
		return dto;
	}
	
}
