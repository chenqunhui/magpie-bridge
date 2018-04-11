package com.cqh.magpie.rpc.loadbalance;

import java.util.List;
import java.util.Random;

import com.cqh.magpie.common.URL;
import com.cqh.magpie.rpc.exception.RpcException;

public class RandomLoadBalance implements LoadBalance {

	@Override
	public URL select(List<URL> urls, int hashcode) {
		if(null == urls || urls.isEmpty()){
			throw new RpcException("No provider can be used for loadbalance");
		}
		if(urls.size() == 1){
			return urls.get(0);
		}
		int index = new Random().nextInt(urls.size());
		return urls.get(index);
	}

}
