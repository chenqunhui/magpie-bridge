package com.cqh.magpie.rpc.thrift;

import org.apache.thrift.TException;

public class DemoServiceImpl implements DemoService.Iface{

	@Override
	public String echo(String str) throws TException {
		return "demo.echo."+str;
	}

	@Override
	public String pt(String str) throws TException {
		return "demo.pt."+str;
	}

}
