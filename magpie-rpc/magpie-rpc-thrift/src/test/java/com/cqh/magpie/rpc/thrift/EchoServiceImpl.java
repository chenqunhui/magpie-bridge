package com.cqh.magpie.rpc.thrift;

import org.apache.thrift.TException;

public class EchoServiceImpl implements EchoService.Iface{

	@Override
	public String echo(String str) throws TException {
		return "rpc get "+str;
	}

	@Override
	public String pt(String str) throws TException {
		// TODO Auto-generated method stub
		return "pt"+str;
	}

}
