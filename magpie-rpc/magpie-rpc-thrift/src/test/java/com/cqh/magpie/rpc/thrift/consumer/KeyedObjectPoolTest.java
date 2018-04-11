package com.cqh.magpie.rpc.thrift.consumer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.junit.Before;
import org.junit.Test;

import com.cqh.magpie.rpc.exception.RpcException;
import com.cqh.magpie.rpc.thrift.DemoService;

import junit.framework.Assert;

public class KeyedObjectPoolTest {

	private GenericKeyedObjectPool<String,TServiceClient> pool;
	private ThriftClientFactory factory;
	
	@Before
	public void setup(){
		// 加载Client.Factory类
        Class<TServiceClientFactory<TServiceClient>> fi;
		try {
			fi = (Class<TServiceClientFactory<TServiceClient>>)
			this.getClass().getClassLoader().loadClass(DemoService.class.getName() + "$Client$Factory");
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
		factory = new ThriftClientFactory(clientFactory, null, 3000, 3000);
		pool = new  GenericKeyedObjectPool<String,TServiceClient>(factory,makePoolConfig());
	}
	
	@Test
	public void testBorrowObject(){
		String key =DemoService.class.getName()+"@127.0.0.1:9095";
		try {
			Method m = DemoService.Iface.class.getDeclaredMethod("pt", String.class);
			TServiceClient o  = factory.makeObject(key);
			TServiceClient o1  = factory.makeObject(key);
			System.out.println(o.hashCode()+":"+m.invoke(o, "123"));
			System.out.println(o1.hashCode()+":"+m.invoke(o, "456"));
			Assert.assertNotSame(o, o1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(1);
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
