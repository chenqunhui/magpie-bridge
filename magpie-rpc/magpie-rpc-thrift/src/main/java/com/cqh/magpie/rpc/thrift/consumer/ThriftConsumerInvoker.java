package com.cqh.magpie.rpc.thrift.consumer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.TServiceClient;

import com.cqh.magpie.common.Constants;
import com.cqh.magpie.common.URL;
import com.cqh.magpie.common.utils.Log;
import com.cqh.magpie.common.utils.UrlUtils;
import com.cqh.magpie.rpc.consumer.ConsumerInvoker;
import com.cqh.magpie.rpc.exception.RpcException;
import com.cqh.magpie.rpc.loadbalance.LoadBalancer;

import lombok.extern.slf4j.Slf4j;

/**
 * 1.负载均衡
 * 2.反射执行远程调用
 * @author chenqunhui
 *
 */
@Slf4j
public  class ThriftConsumerInvoker implements ConsumerInvoker{

	private LoadBalancer loadBalance;
	private Class<?> clazz;
	private RegistryInvokerFactory registryInvokerFactory;
	public ThriftConsumerInvoker(Class<?> clazz,
			RegistryInvokerFactory registryInvokerFactory,
			LoadBalancer loadBalance){
		this.clazz = clazz;
		this.loadBalance = loadBalance;
		this.registryInvokerFactory = registryInvokerFactory;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		List<URL> urls = registryInvokerFactory.getAvailableUrls(method);
		if(null == urls || urls.isEmpty()){
			throw new RpcException("No provider for service :"+clazz.getName()+"."+method.getName());
		}
		URL url;
		if(urls.size() == 1){
			url = urls.get(0);
		}else{
			url = loadBalance.select(urls, this.hashCode());
		}
		TServiceClient client;
		try {
			client = registryInvokerFactory.borrowObject(UrlUtils.getServiceCientKey(url));
			if(log.isDebugEnabled())
			log.debug("borrow thrift serivce:{} client: {} ", url.getParameter(Constants.INTERFACE_KEY)+"."+method.getName(),client.hashCode());
		} catch (Exception e) {
			throw new RpcException("get client for service"+clazz.getName()+"."+method.getName()+" error!",e);
		}
		Object result = null;
		try {
			result = method.invoke(client, args);
		} catch (Exception e) {
			throw new RpcException(e);
		}finally{
			registryInvokerFactory.returnObject(UrlUtils.getServiceCientKey(url), client);
		}
		return result;
	}

	@Override
	public Class<?> getInterface() {
		return clazz;
	}

}
