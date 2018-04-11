package com.cqh.magpie.rpc.thrift.spring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.cqh.magpie.common.Constants;
import com.cqh.magpie.common.URL;
import com.cqh.magpie.common.utils.NetUtils;
import com.cqh.magpie.common.utils.StringUtils;
import com.cqh.magpie.common.utils.UrlUtils;
import com.cqh.magpie.registry.Registry;
import com.cqh.magpie.rpc.consumer.ConsumerProxy;
import com.cqh.magpie.rpc.exception.RpcException;
import com.cqh.magpie.rpc.hyxtrix.HystrixContext;
import com.cqh.magpie.rpc.loadbalance.LoadBalancer;
import com.cqh.magpie.rpc.loadbalance.RandomLoadBalancer;
import com.cqh.magpie.rpc.loadbalance.RoundRobinLoadBalancer;
import com.cqh.magpie.rpc.thrift.config.ConsumerConfig;
import com.cqh.magpie.rpc.thrift.consumer.RegistryInvokerFactory;
import com.cqh.magpie.rpc.thrift.consumer.ThriftClientFactory;
import com.cqh.magpie.rpc.thrift.consumer.ThriftConsumerInvoker;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 消费者端serviceBean工厂
 * @author chenqunhui
 *
 */
@Data
@Slf4j
public class ConsumerProxyFactory implements FactoryBean<Object>,InitializingBean,ApplicationContextAware{
	
	
	private ApplicationContext applicationContext;
	private String serviceName;
	private Class<?> clazz;
	private Object target;
	private String version;
	private int timeout =0;
	private String serviceLoadBalancer;
	/**
     * 服务降级的伪装者类对象
     */
    private Object fallbackObject;
    private int errorThresholdPercentage=50;
	
	private static Registry registry;
	private static ConsumerConfig consumerConfig;
	private URL subscribeUrl;
	private LoadBalancer loadBalancer;
	//注册发现服务，持有url和连接池
	RegistryInvokerFactory invokerFactory;
	
	@Override
	public Object getObject() throws Exception {
		return target;
	}

	@Override
	public Class<?> getObjectType() {
		return clazz;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(StringUtils.isEmpty(serviceName)){
			throw new RpcException("Property 'serviceName' is required for ConsumerProxyFactory,please set it!");
		}
		initConsumerConfig();
		initSubscribeUrl();
		initRegistry();
		initLoadBalance();
		subscribeService();
		
	}
	private void initRegistry(){
		try{
			registry = (Registry)applicationContext.getBean(Constants.REGISTER_BEAN_NAME);
		}catch(Exception e){
			try{
				registry = applicationContext.getBean(Registry.class);
			}catch(Exception e1){
				if(registry == null){
					throw new RpcException("No register bean cant be found,please set it!");
				}
			}
			
		}
	}
	
	private void initConsumerConfig(){
		try{
			consumerConfig = (ConsumerConfig)applicationContext.getBean(Constants.CONSUMER_CONFIG_BEAN_NAME);
		}catch(Exception e){
			try{
				consumerConfig = applicationContext.getBean(ConsumerConfig.class);
			}catch(Exception e1){
				if(consumerConfig == null){
					synchronized(ConsumerProxyFactory.class){
						if(null == consumerConfig){
							consumerConfig = new  ConsumerConfig();;
						}
					}
				}
			}
		}
	}
	
	private void initSubscribeUrl(){
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.VERSION_KEY, serviceName);
		parameters.put(Constants.VERSION_KEY,version == null? consumerConfig.getVersion():version);
		parameters.put(Constants.TIMEOUT_KEY, String.valueOf(timeout == 0 ? consumerConfig.getTimeout() : timeout));
		parameters.put(Constants.CATEGORY_KEY, Constants.CONSUMERS_CATEGORY);
		subscribeUrl = new URL("thrift",NetUtils.getLocalHost(),0,parameters);
		log.debug("init subscribe url : {}",subscribeUrl.toFullString());
	}
	
	private void initLoadBalance(){
		if(StringUtils.isEmpty(serviceLoadBalancer)){
			serviceLoadBalancer = consumerConfig.getLoadbalance();
		}
		if(RoundRobinLoadBalancer.NAME.equals(serviceLoadBalancer)){
			loadBalancer = new RoundRobinLoadBalancer();
		}else{
			loadBalancer = new RandomLoadBalancer();
		}
		
	}

	@SuppressWarnings("unchecked")
	private InvocationHandler getInvocationHandler(){
        // 加载Client.Factory类
        Class<TServiceClientFactory<TServiceClient>> fi;
		try {
			fi = (Class<TServiceClientFactory<TServiceClient>>)
			this.getClass().getClassLoader().loadClass(serviceName + "$Client$Factory");
		} catch (ClassNotFoundException e) {
			throw new RpcException(e);
		}
		TServiceClientFactory<TServiceClient> clientFactory ;
		try {
			clientFactory = fi.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new RpcException(e);
		} 
		//thrift连接池Factory类
		ThriftClientFactory thriftClientFactory = new ThriftClientFactory(clientFactory,null,Constants.DEFAULT_CONNECT_TIMEOUT,subscribeUrl.getParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT));
		invokerFactory = new RegistryInvokerFactory(subscribeUrl,thriftClientFactory);
		//负载均衡，获取执行对象
		ThriftConsumerInvoker invoker = new ThriftConsumerInvoker(clazz, invokerFactory, loadBalancer);
		//熔断
		ConsumerProxy proxy = new ConsumerProxy(invoker,new HystrixContext(fallbackObject,errorThresholdPercentage));
		return proxy;
	}

	//订阅服务
	private void subscribeService() throws Exception{
		registry.subscribe(subscribeUrl, invokerFactory);
		clazz = this.getClass().getClassLoader().loadClass(serviceName+"$Iface");
		target = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{clazz}, getInvocationHandler());
		//优雅停机
		 Runtime.getRuntime().addShutdownHook(new Thread(){
			 public void run(){
				 registry.unsubscribe(subscribeUrl, invokerFactory);;
			 }
		 });
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
}
