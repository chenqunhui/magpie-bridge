

Magpie-bridge  
#跨语言RPC中间件  最新版本：0.0.1-SNAPSHOT

#引用Jar包：

eq:
	  <!-- 注册中心 provider和consumer都需要 -->
      <bean id="magpieRegistry" class="com.cqh.magpie.rpc.thrift.spring.RegistryFactory">
      		<property name="url" value="zookeeper://127.0.0.1:2181"/>
      		<property name="domainName" value="testMagpie"></property>
      </bean>
      <!-- 消费端默配置 -->
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
      
      <!-- 提供者bean实现 -->
      <bean id="demoServiceImpl" class="com.cqh.magpie.rpc.thrift.DemoServiceImpl"></bean>
      
      <!-- 提供者bean服务注册 -->
      <bean id="provider" class="com.cqh.magpie.rpc.thrift.spring.ProviderFactory">
      		<property name="version" value="1.0"/>
      		<property name="service" ref="demoServiceImpl"/>
      </bean>