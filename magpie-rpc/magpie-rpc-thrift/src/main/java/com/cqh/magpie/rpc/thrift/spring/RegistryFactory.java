package com.cqh.magpie.rpc.thrift.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.cqh.magpie.common.URL;
import com.cqh.magpie.registry.Registry;
import com.cqh.magpie.registry.zookeeper.ZookeeperRegistry;
import com.cqh.magpie.registry.zookeeper.curator.CuratorZookeeperTransporter;
import com.cqh.magpie.rpc.exception.RpcException;

import lombok.Data;

@Data
public class RegistryFactory implements InitializingBean,FactoryBean<Registry>{

	private String url;
	private String domainName;
	
	private Registry registry;

	@Override
	public void afterPropertiesSet() throws Exception {
		if(null == url || url.length() ==0){
			throw new RpcException("Registry url is required and can not be null,please set it with property 'url'. For example :'zookeeper://127.0.0.1:2181'.");
		}
		if(null == domainName || domainName.length() ==0){
			throw new RpcException("domainName is required, it describe that who provide or subscribe the service to the registry,please set it with property 'domainName'.");
		}
		URL registryUrl = URL.valueOf(url);
		if("zookeeper".equals(registryUrl.getProtocol())){
			registry = new ZookeeperRegistry(registryUrl,new CuratorZookeeperTransporter());
		}
	}

	@Override
	public Registry getObject() throws Exception {
		return registry;
	}

	@Override
	public Class<?> getObjectType() {
		return Registry.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
