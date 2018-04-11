package com.cqh.magpie.rpc.thrift.config;

import lombok.Data;

@Data
public class ConsumerConfig {

	private String version ="";
	
	private int timeout =500;//ms
	
	private int retry = 0;
	
	private String loadbalance ="roundbin";
	
}
