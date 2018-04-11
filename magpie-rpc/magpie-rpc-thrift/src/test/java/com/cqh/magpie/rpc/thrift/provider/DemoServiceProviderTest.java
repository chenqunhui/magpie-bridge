package com.cqh.magpie.rpc.thrift.provider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.cqh.magpie.common.Constants;
import com.cqh.magpie.common.URL;
import com.cqh.magpie.registry.Registry;
import com.cqh.magpie.registry.zookeeper.ZookeeperRegistry;
import com.cqh.magpie.registry.zookeeper.curator.CuratorZookeeperTransporter;
import com.cqh.magpie.rpc.thrift.DemoService;
import com.cqh.magpie.rpc.thrift.DemoServiceImpl;

public class DemoServiceProviderTest {

	public static void main(String[] args) {
		Registry registry = new ZookeeperRegistry(new URL("zookeeper","127.0.0.1",2181),new CuratorZookeeperTransporter()); 
		TNonblockingServerSocket serverTransport;
		int port =9095;
		try {
			 
			serverTransport = new TNonblockingServerSocket(port);
			TProcessor tprocessor = new DemoService.Processor<>(new DemoServiceImpl());
            TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);
            TProcessorFactory processorFactory = new TProcessorFactory(tprocessor);
            tArgs.processorFactory(processorFactory);
            tArgs.transportFactory(new TFramedTransport.Factory());
            tArgs.protocolFactory(new TBinaryProtocol.Factory(true, true));
            tArgs.selectorThreads(8);
            tArgs.workerThreads(20);
            TServer server = new TThreadedSelectorServer(tArgs);
			Map<String,String> map = new HashMap<String,String>();
			map.put("interface", DemoService.class.getName());
			map.put("version", "1.0");
			
			try {
				Class<?> clazz = DemoServiceProviderTest.class.getClassLoader().loadClass(DemoService.class.getName()+"$Iface");
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
			URL url = new URL("zookeeper","127.0.0.1",port,map);
			registry.register(url);
			Runtime.getRuntime().addShutdownHook(new Thread(()->registry.unregister(url)));
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
		
	}
}
