package com.cqh.magpie.rpc.hyxtrix;

import lombok.Data;


@Data
public class HystrixContext {
    /**
     * 服务降级的伪装者类对象
     */
    private Object fallbackObject;
   
	/**
     * 错误熔断的百分比，取之范围0～100
     */
    private int errorThresholdPercentage = 50;
    
    
    public HystrixContext(Object fallbackObject,int errorThresholdPercentage){
    	this.fallbackObject = fallbackObject;
    	this.errorThresholdPercentage = errorThresholdPercentage;
    }
    
	public HystrixContext(Object fallbackObject){
	    this.fallbackObject = fallbackObject;
	}
	
	public HystrixContext(){
		
	}
}
