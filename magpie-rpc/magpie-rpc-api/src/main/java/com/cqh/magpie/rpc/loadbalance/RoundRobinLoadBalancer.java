package com.cqh.magpie.rpc.loadbalance;

import java.util.List;

import com.cqh.magpie.common.URL;
import com.cqh.magpie.common.utils.AtomicPositiveInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {

	public static final String NAME = "roundrobin";
	
	private AtomicPositiveInteger pos = new AtomicPositiveInteger();
	
	@Override
	public URL select(List<URL> urls, int hashcode) {
		// 总个数
        int length = urls.size();

        int currentSequence = pos.getAndIncrement();
        // 取模轮循
        return urls.get(currentSequence % length);
	}
	

}
