package com.cqh.magpie.rpc.thrift.provider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.cqh.magpie.common.Constants;
import com.cqh.magpie.common.URL;
import com.cqh.magpie.registry.Registry;
import com.cqh.magpie.registry.zookeeper.ZookeeperRegistry;
import com.cqh.magpie.registry.zookeeper.curator.CuratorZookeeperTransporter;
import com.cqh.magpie.rpc.thrift.EchoService;
import com.cqh.magpie.rpc.thrift.EchoServiceImpl;



public class ProviderTest {

	public static void main(String[] args) {
		Registry registry = new ZookeeperRegistry(new URL("zookeeper","127.0.0.1",2181),new CuratorZookeeperTransporter()); 
		TServerTransport serverTransport;
		int port =9094;
		try {
			 
			serverTransport = new TServerSocket(port);
			TProcessor tprocessor = new EchoService.Processor<>(new EchoServiceImpl());
			TServer server = new TSimpleServer(new Args(serverTransport).processor(tprocessor));
			Map<String,String> map = new HashMap<String,String>();
			map.put("interface", EchoService.class.getName());
			map.put("version", "1.0");
			
			try {
				Class<?> clazz = ProviderTest.class.getClassLoader().loadClass(EchoService.class.getName()+"$Iface");
				Method[] methods = clazz.getMethods();
				StringBuilder methodString = new StringBuilder();
				for(Method m :methods){
					methodString.append(m.getName()).append(",");
				}
				String str = methodString.toString();
				str = str.substring(0, str.length()-1);
				map.put(Constants.METHODS_KEY, str);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			registry.register(new URL("zookeeper","127.0.0.1",port,map));
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
		
	}
}
