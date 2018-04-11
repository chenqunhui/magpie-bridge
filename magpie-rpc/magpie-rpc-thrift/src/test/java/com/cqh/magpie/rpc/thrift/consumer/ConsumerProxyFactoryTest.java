package com.cqh.magpie.rpc.thrift.consumer;

import org.apache.thrift.TException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cqh.magpie.rpc.thrift.DemoService;
import com.cqh.magpie.rpc.thrift.EchoService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerProxyFactoryTest {

	public static void main(String[] args){
		ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		DemoService.Iface demo = (DemoService.Iface)context.getBean("demoService");
		log.debug("start...");
		for(int i=0;i<1;i++){
			new Thread(){
				public void run(){
					try {
						demo.pt(String.valueOf(1)) ;
					} catch (TException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
			new Thread(){
				public void run(){
					try {
						demo.pt(String.valueOf(1)) ;
					} catch (TException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
			
			//System.out.println(demo.pt(String.valueOf(i))) ;
		}
		
		
	}
}
