package com.cqh.magpie.rpc.thrift.consumer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.TServiceClient;

import com.cqh.magpie.common.Constants;
import com.cqh.magpie.common.URL;
import com.cqh.magpie.registry.NotifyListener;
import com.cqh.magpie.registry.Registry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryInvokerFactory implements NotifyListener{

	private List<URL> cacheUrls = new CopyOnWriteArrayList<URL>();
	private volatile Map<String,List<URL>> methodUrlsMap;
	private GenericKeyedObjectPool<String,TServiceClient> pool;
	//private Registry registry;
	private URL subscribeUrl;
	
	public RegistryInvokerFactory(URL subscribeUrl,ThriftClientFactory factory){
		this.pool = new  GenericKeyedObjectPool<String,TServiceClient>(factory,makePoolConfig());
		///this.registry = registry;
		this.subscribeUrl = subscribeUrl;
		
	}
	
	@Override
	public void notify(List<URL> urls) {
		List<URL> newUrls = new CopyOnWriteArrayList<URL>();
		newUrls.addAll(urls);
		cacheUrls = newUrls;
		methodUrlsMap = toMethodUrlMap(newUrls);
	}
	
	public Map<String,List<URL>> toMethodUrlMap(List<URL> urls){
		Map<String,List<URL>> newMethodUrlMap = new HashMap<String,List<URL>>();
		if(null != urls && urls.size() >0){
			for(URL url:urls){
				String parameter = url.getParameter(Constants.METHODS_KEY);
				if(parameter != null && parameter.length() >0){
					String[] methods = Constants.COMMA_SPLIT_PATTERN.split(parameter);
					if(methods != null && methods.length >0){
						for(String method :methods){
							if(method != null && method.length() >0){
								List<URL> methodUrls = newMethodUrlMap.get(method);
								if(methodUrls == null){
									methodUrls = new ArrayList<URL>();
									newMethodUrlMap.put(method, methodUrls);
								}
								methodUrls.add(url);
							}
						}
					}
				}
			}
		}
		for(String method :new HashSet<String>(newMethodUrlMap.keySet())){
			List<URL> methodUrls = newMethodUrlMap.get(method);
			newMethodUrlMap.put(method, Collections.unmodifiableList(methodUrls));
		}
		return Collections.unmodifiableMap(newMethodUrlMap);
		
	}

	public TServiceClient borrowObject(String key) throws Exception{
		return pool.borrowObject(key);
	}
	
	public void returnObject(String key,TServiceClient client){
		try {
			pool.returnObject(key, client);
		} catch (Exception e) {
			log.error("return object {} error ",client,e);
		}
	}
	
	/**
	 * 按方法名来过滤可用url
	 * @param method
	 * @return
	 */
	public List<URL> getAvailableUrls(Method method){
//		List<URL> urls = new ArrayList<URL>(cacheUrls.size());
//		if(cacheUrls.isEmpty()){
//			return urls;
//		}
//		for(URL url :cacheUrls){
//			if(UrlUtils.isMathMethod(url, method.getName())){
//				urls.add(url);
//			}
//		}
		if(null == methodUrlsMap){
			methodUrlsMap = toMethodUrlMap(cacheUrls);
		}
		if(methodUrlsMap.isEmpty()){
			return new  ArrayList<URL>();
		}
		return Collections.unmodifiableList(methodUrlsMap.get(method.getName()));
	}
	
	private GenericKeyedObjectPool.Config makePoolConfig() {
		GenericKeyedObjectPool.Config poolConfig = new GenericKeyedObjectPool.Config();
		poolConfig.maxActive = 20;
		/*
		 * 从连接池取对象的时候，如果无可用对象，会新建连接。因此连接超时可与从连接池取对象超时时间一致，方便管理
		 */
		poolConfig.maxWait = 3000;
		poolConfig.maxTotal=50;
		poolConfig.maxIdle = 5;
		poolConfig.minIdle = 5;
		poolConfig.minEvictableIdleTimeMillis = 180000;
		poolConfig.timeBetweenEvictionRunsMillis = 180000 * 2L;
		poolConfig.testOnBorrow = true;
		poolConfig.testOnReturn = false;
		poolConfig.testWhileIdle = false;
		return poolConfig;
	}
}
