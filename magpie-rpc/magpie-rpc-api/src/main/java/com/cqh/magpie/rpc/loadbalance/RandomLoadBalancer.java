package com.cqh.magpie.rpc.loadbalance;

import java.util.List;
import java.util.Random;

import com.cqh.magpie.common.Constants;
import com.cqh.magpie.common.URL;

public class RandomLoadBalancer implements LoadBalancer {
	public static final String NAME = "random";
	
	private final Random random = new Random();
	@Override
	public URL select(List<URL> urls, int hashcode) {
		if(null == urls || urls.isEmpty()){
			return null;
		}
		int length = urls.size();
		if(length == 1){
			return urls.get(0);
		}
		int totalWeight = 0; 
		boolean sameWeight = true;
		for (int i = 0; i < length; i++) {
            int weight = getWeight(urls.get(i));
            totalWeight += weight; // Sum
            if (sameWeight && i > 0
                    && weight != getWeight(urls.get(i - 1))) {
                sameWeight = false;
            }
        }
		if (totalWeight > 0 && !sameWeight) {
            // If (not every invoker has the same weight & at least one invoker's weight>0), select randomly based on totalWeight.
            int offset = random.nextInt(totalWeight);
            // Return a invoker based on the random value.
            for (int i = 0; i < length; i++) {
                offset -= getWeight(urls.get(i));
                if (offset < 0) {
                    return urls.get(i);
                }
            }
        }
		return urls.get(random.nextInt(length));
	}
	
	private int getWeight(URL url){
		return url.getParameter(Constants.WEIGHT_KEY, 0);
	}

}
