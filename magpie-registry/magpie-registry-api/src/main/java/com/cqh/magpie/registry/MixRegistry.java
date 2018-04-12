package com.cqh.magpie.registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.cqh.magpie.common.URL;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class MixRegistry implements Registry {

	private Map<NotifyListener,Mark> listenerMarkMap = new ConcurrentHashMap<NotifyListener,Mark>();
	private Registry[] registrys;
	
	@Override
	public URL getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void register(URL url) {
		for(Registry registry:registrys){
			try{
				registry.register(url);
			}catch(Exception e){
				//TODO 
			}
		}

	}

	@Override
	public void unregister(URL url) {
		for(Registry registry:registrys){
			try{
				registry.unregister(url);
			}catch(Exception e){
				//TODO 
			}
		}
	}

	@Override
	public void subscribe(URL url, NotifyListener listener) {
		Mark mark = listenerMarkMap.get(listener);
		if(null == mark){
			mark = new Mark(listener);
			listenerMarkMap.putIfAbsent(listener, mark);
			mark = listenerMarkMap.get(listener);
		}
		for(Registry registry:registrys){
			try{
				MixNotifyListener mixListener = new MixNotifyListener(registry,mark);
				registry.subscribe(url,mixListener);
			}catch(Exception e){
				log.error("subscribe url: {} to registry :{} error!",url.toFullString(),registry.getUrl());
			}
		}

	}

	@Override
	public void unsubscribe(URL url, NotifyListener listener) {
		Mark mark = listenerMarkMap.get(listener);
		if(null == mark){
			return;
		}
		for(Registry registry:registrys){
			try{
				MixNotifyListener mixListener = new MixNotifyListener(registry,mark);
				registry.unsubscribe(url,mixListener);
			}catch(Exception e){
				log.error("unsubscribe url: {} to registry :{} error!",url.toFullString(),registry.getUrl());
			}
		}
		listenerMarkMap.remove(listener);

	}

	@Override
	public List<URL> lookup(URL url) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	class MixNotifyListener implements NotifyListener{
		private Registry registry;
		private Mark mark;
	
		public MixNotifyListener(Registry registry,Mark mark){
			this.registry = registry;
			this.mark = mark;

		}
		@Override
		public void notify(List<URL> urls) {
			mark.notify(registry,urls);
		}
		
	}

	
	class Mark{
		Map<Registry,List<URL>> registryUrlsMap = new ConcurrentHashMap<Registry,List<URL>>();
		private NotifyListener listener;
		public Mark(NotifyListener listener){
			this.listener = listener;
		}
		public void notify(Registry registry,List<URL> urls){
			registryUrlsMap.put(registry, urls);
			Iterator<Entry<Registry,List<URL>>> iterator = registryUrlsMap.entrySet().iterator();
			List<URL> urlTotal = new ArrayList<URL>();
			while(iterator.hasNext()){
				urlTotal.addAll(iterator.next().getValue());
			}
			listener.notify(urlTotal);
		}
	}
}
