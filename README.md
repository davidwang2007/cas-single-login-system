##�����¼ϵͳʾ��

####����׼��
1. ���ذ�װredis[������Զ��redis]
2. ���豾�ص�ַΪ 192.168.1.101


####��������
1. �ͻ���1:  cd client && mvn jetty:run
2. �ͻ���2:  cd client2 && mvn jetty:run
3. �����:   cd server && mvn jetty:run
4. ������� http://192.168.1.101:8080/client 
5. �ض����� http://192.168.1.101:8088/server/login
6. �����û�������admin/admin ��¼�ɹ����ض�����server
7. ��ʱ��client2: http://192.168.1.101:8081/client2  �򿪳ɹ�
8. ��ʱ����һ���Ե�¼[�뽫client,client2�е�web.xml�е�192.168.1.101���ø�Ϊ����ʵ��ip��ַ��������Ŀ]������ʾ��һ�ͻ����ѵ�¼���Ƿ��߳�
9. ....


####���ô�
1. client����ͨ��AuthFilter���е�¼��Ȩ������web.xml����������
2. server����ͨ��LoginController, LogoutController���е�¼���˳���
3. client �� server ����֤cookie��Ϣ�Ƿ���Ч
4. server �� redis/jedis �����ļ�Ϊ applicationContext.xml�� JedisPool, ����Ǽ�Ⱥ�������ΪJedisCluster, ����ʹ��JedisPool����ΪJedisCluster ����


####bug list
1. ��ǰ�汾���ڣ����ͬһ�����������ⲿserverʱ�������ip��һ�µģ���ͬһ�������е�A��B����ͬʱʹ��ͬһ���û�����¼�ɹ��ˣ��ʹﲻ��ֻ��һ���ɵ�¼��Ŀ��
	��ǰ[ͨ�������һ���cookie�ķ�ʽ`s`]��������� 2018-03-19 15:01:00 

	*���� ip+random ��ΪΩһ��ʶ*
	��ǰʵ��ֻ��random��ΪΩһ��ʶ����

2. the client should not care about the redis, etc. We just add verify api for client verify the cookie


`author: davidwang2006@aliyun.com

`wechat: davidwang2006
