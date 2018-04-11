package com.cqh.magpie.rpc.consumer;

import java.lang.reflect.Method;


/**
 * 消费者端实际执行
 * 
 * 
 * 
 * @author chenqunhui
 *
 */
public interface ConsumerInvoker {
	
	public Object invoke(Object proxy,Method method,Object[] args);
	
	public Class<?> getInterface();
}
