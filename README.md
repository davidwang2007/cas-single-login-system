##单点登录系统示例

####基础准备
1. 本地安装redis[或连接远程redis]
2. 假设本地地址为 192.168.1.101


####本地启动
1. 客户端1:  cd client && mvn jetty:run
2. 客户端2:  cd client2 && mvn jetty:run
3. 服务端:   cd server && mvn jetty:run
4. 浏览器打开 http://192.168.1.101:8080/client 
5. 重定向至 http://192.168.1.101:8088/server/login
6. 输入用户名密码admin/admin 登录成功后，重定向至server
7. 此时打开client2: http://192.168.1.101:8081/client2  打开成功
8. 此时在另一电脑登录[请将client,client2中的web.xml中的192.168.1.101配置改为机器实际ip地址并重启项目]，会提示另一客户端已登录，是否踢出
9. ....


####配置处
1. client工程通过AuthFilter进行登录鉴权，请在web.xml中自由配置
2. server工程通过LoginController, LogoutController进行登录、退出。
3. client 向 server 端验证cookie信息是否有效
4. server 端 redis/jedis 配置文件为 applicationContext.xml中 JedisPool, 如果是集群，请更换为JedisCluster, 并在使用JedisPool处改为JedisCluster 即可


####bug list
1. 当前版本存在，如果同一局域网访问外部server时，其出口ip是一致的，故同一局域网中的A与B可以同时使用同一个用户名登录成功了，就达不到只有一个可登录的目的
	当前[通过添加另一随机cookie的方式`s`]解决此问题 2018-03-19 15:01:00 

	*即以 ip+random 作为惟一标识*
	当前实际只以random作为惟一标识即可

2. the client should not care about the redis, etc. We just add verify api for client verify the cookie


`author: davidwang2006@aliyun.com

`wechat: davidwang2006
