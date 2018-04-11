package com.cqh.magpie.rpc.thrift.spring;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.cqh.magpie.common.Constants;
import com.cqh.magpie.common.URL;
import com.cqh.magpie.common.utils.NetUtils;
import com.cqh.magpie.common.utils.PortUtils;
import com.cqh.magpie.common.utils.StringUtils;
import com.cqh.magpie.registry.Registry;
import com.cqh.magpie.rpc.exception.RpcException;
import com.cqh.magpie.rpc.thrift.config.ProviderConfig;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Data
public class ProviderFactory implements FactoryBean<Object>,InitializingBean,ApplicationContextAware{

	private ApplicationContext applicationContext;
	private int port;
	private String version;
	private Object service;
	private Integer workerThreads = 200;
	
	
	private Class<?> clazz;
	private Registry registry;
	private ProviderConfig providerConfig;
	private URL providerUrl;
    /**
     * serverThread 服务线程
     */
    private ServerThread serverThread;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initRegistry();
		initProviderConfig();
		initAndValidate();
		initProviderUrl();
		registedService();
	}
	
	private void initAndValidate() throws Exception{
		if (port == 0) {
            port = PortUtils.getRandomPort();
        }
		if(StringUtils.isEmpty(version)){
			version = providerConfig.getVersion();
		}
        Class<?> serviceClass = service.getClass();
        // 获取实现类接口
        Class<?>[] interfaces = serviceClass.getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalClassFormatException("service-class should implements Iface");
        }
        TProcessor processor = null;
        String serviceClassName = null;
        for (Class<?> clazz : interfaces) {
            String cname = clazz.getSimpleName();
            if (!"Iface".equals(cname)) {
                continue;
            }
            this.clazz = clazz;
            serviceClassName = clazz.getEnclosingClass().getName();
            String pname = serviceClassName + "$Processor";
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class<?> pclass = classLoader.loadClass(pname);
                if (!TProcessor.class.isAssignableFrom(pclass)) {
                    continue;
                }
                Constructor<?> constructor = pclass.getConstructor(clazz);
                processor = (TProcessor) constructor.newInstance(service);
                break;
            } catch (Exception e) {
                //
            }
        }
        if (processor == null) {
            throw new IllegalClassFormatException("service-class should implements Iface");
        }
        //需要单独的线程,因为serve方法是阻塞的.
        serverThread = new ServerThread(processor, port);
        serverThread.start();
	}

	
	@Override
	public Object getObject() throws Exception {
		return service;
	}

	@Override
	public Class<?> getObjectType() {
		return clazz;
	}

	@Override
	public boolean isSingleton() {
		return true;
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
	
	private void initProviderUrl(){
		Map<String,String> parameter = new HashMap<String,String>();
		parameter.put(Constants.VERSION_KEY, StringUtils.isEmpty(version) ?providerConfig.getVersion() : version);
		parameter.put(Constants.INTERFACE_KEY, clazz.getName().substring(0,clazz.getName().length()-"$Iface".length()));
		parameter.put(Constants.CATEGORY_KEY, Constants.PROVIDERS_CATEGORY);
		Method[] methods = clazz.getDeclaredMethods();
		if(methods.length>0){
			StringBuilder sb  = new StringBuilder();
			for(Method m :methods){
				sb.append(m.getName()).append(Constants.METHODS_SPLIT_KEY);
			}
			String methodString = sb.toString().substring(0, sb.length()-Constants.METHODS_SPLIT_KEY.length());
			parameter.put(Constants.METHODS_KEY, methodString);
		}
		providerUrl = new URL("thrift",NetUtils.getLocalHost(),port,parameter);
		log.debug("register url : {}",providerUrl.toFullString());
	}
	
	private void initProviderConfig(){
		try{
			providerConfig = (ProviderConfig)applicationContext.getBean(Constants.PROVIDER_CONFIG_BEAN_NAME);
		}catch(Exception e){
			try{
				providerConfig = applicationContext.getBean(ProviderConfig.class);
			}catch(Exception e1){
				if(providerConfig == null){
					synchronized(ProviderFactory.class){
						if(null == providerConfig){
							providerConfig = new  ProviderConfig();;
						}
					}
				}
			}
		}
	}
	
	private void registedService(){
		 registry.register(providerUrl);
		 //优雅停机
		 Runtime.getRuntime().addShutdownHook(new Thread(){
			 public void run(){
				 registry.unregister(providerUrl);
			 }
		 });
	}

	class ServerThread extends Thread{
		 private TServer server;

	     public ServerThread(TProcessor processor, int port) throws Exception {
	    	 TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(port);
             TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);
             TProcessorFactory processorFactory = new TProcessorFactory(processor);
             tArgs.processorFactory(processorFactory);
             tArgs.transportFactory(new TFramedTransport.Factory());
             tArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
             tArgs.selectorThreads(8);
             tArgs.workerThreads(workerThreads);
             server = new TThreadedSelectorServer(tArgs);
	        }

	        @Override
	        public void run() {
	            try {
	                //启动服务
	                server.serve();
	            } catch (Exception e) {
	                log.error("thrift server.serve() throws an exception.", e);
	            }
	        }

	        public void stopServer() {
	            log.info("stopping thrift server...");
	            server.stop();
	        }
	}
}
