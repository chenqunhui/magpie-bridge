package com.cqh.magpie.rpc.loadbalance;

import java.util.List;

import com.cqh.magpie.common.URL;

public interface LoadBalancer {
	public URL select(List<URL> urls,int hashcode);
}
