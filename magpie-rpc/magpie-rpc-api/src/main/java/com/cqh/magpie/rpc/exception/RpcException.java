package com.cqh.magpie.rpc.exception;

public class RpcException extends RuntimeException {
	
	private static final long serialVersionUID = -3431645130597580850L;

	public RpcException(String message){
		super(message);
	}
	
	
	public RpcException(Throwable e){
		super(e);
	}
	
	public RpcException(String message,Throwable e){
		super(message,e);
	}
}
