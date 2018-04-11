

	magpie-bridge  
	#跨语言RPC中间件  最新版本：0.0.1-SNAPSHOT

  一、功能描述
  
    1.thrift服务自动注册和发现；
    
    2.负载均衡：目前支持权重随机和轮循；
    
    3.支持熔断机制；
    
    4.目前支持:
    
         (1)版本分组：即conumser只会调用到相同版本的provider;
         
         (2)方法分组:同一接口，培加新方法时,新旧版本provider同时存在，调用新方法的consumer只会调用到实现了新方法的provider; 
         
         (3)当前版本不支持thrift接口方法重载；
         
    5.支持优雅停机；
    
    6.服务依赖关系存储在注册中心，可供生成服务依赖图；
  
  
  二、Demo用例
  
  1.服务提供者端配置：
	      
	<!-- 注册中心 provider和consumer都需要 -->
      <bean id="magpieRegistry" class="com.cqh.magpie.rpc.thrift.spring.RegistryFactory">
      		<property name="url" value="zookeeper://127.0.0.1:2181"/>
      		<property name="domainName" value="testMagpie"></property>
      </bean>

	   <!-- 提供者bean实现 -->
      <bean id="demoServiceImpl" class="com.cqh.magpie.rpc.thrift.DemoServiceImpl"></bean>
      
      <!-- 提供者bean服务注册 -->
      <bean id="provider" class="com.cqh.magpie.rpc.thrift.spring.ProviderFactory">
      		<property name="version" value="1.0"/>
      		<property name="service" ref="demoServiceImpl"/>
      </bean>
      
   2.消费者端配置：
   
   	<!-- 注册中心 provider和consumer都需要 -->
      <bean id="magpieRegistry" class="com.cqh.magpie.rpc.thrift.spring.RegistryFactory">
      		<property name="url" value="zookeeper://127.0.0.1:2181"/>
      		<property name="domainName" value="testMagpie"></property>
      </bean>
  		<!-- 消费端默认配置，如果消费者bean没有配置下列属性，则默认使用本配置-->
      <bean  class="com.cqh.magpie.rpc.thrift.config.ConsumerConfig">
      		<property name="version" value="1.0"/>
      		<property name="timeout" value="5000"/>
      		<property name="retry" value="0"/>
      </bean>
    	<!-- 消费者bean A -->
      <bean id="echoService" class="com.cqh.magpie.rpc.thrift.spring.ConsumerProxyFactory">
      	   <property name="version" value="1.0"/>
      	   <property name="serviceName" value="com.cqh.magpie.rpc.thrift.EchoService"/>
      </bean> 
       <!-- 消费者bean B -->
       <bean id="demoService" class="com.cqh.magpie.rpc.thrift.spring.ConsumerProxyFactory">
      	   <property name="version" value="1.0"/>
      	   <property name="serviceName" value="com.cqh.magpie.rpc.thrift.DemoService"/>
      </bean> 
   
   三、设计参考
   
    1.registry部分代码取自dubbo，去掉了configurators和routers，并作了少量修改；
    
    2.RPC实现参考其他中间件；

      
      
     
      
     