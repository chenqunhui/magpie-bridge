package com.cqh.magpie.rpc.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.cqh.magpie.rpc.hyxtrix.HystrixContext;
import com.cqh.magpie.rpc.hyxtrix.RpcHystrixCommand;



/**
 * 消费者端代理对象
 * @author chenqunhui
 *
 */
public class ConsumerProxy  implements InvocationHandler{

	private HystrixContext context;
	
	private ConsumerInvoker consumerInvoker;
	
	public ConsumerProxy(ConsumerInvoker consumerInvoker,HystrixContext context){
		this.consumerInvoker = consumerInvoker;
		this.context = context;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		 if (context != null && context.getFallbackObject() != null) {
	            return new RpcHystrixCommand(proxy, method, args, consumerInvoker, context).execute();
	        } else {
	            return consumerInvoker.invoke(proxy, method, args);
	        }
	}

}
