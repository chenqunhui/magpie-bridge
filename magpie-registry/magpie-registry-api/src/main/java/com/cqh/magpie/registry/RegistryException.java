package com.cqh.magpie.registry;

public class RegistryException extends RuntimeException {

	private static final long serialVersionUID = 3744940788451851351L;

	public RegistryException(String message){
		super(message);
	}
	
	public RegistryException(String message,Throwable e){
		super(message,e);
	}
}
