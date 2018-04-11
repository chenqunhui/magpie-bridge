/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cqh.magpie.registry.zookeeper;

import com.cqh.magpie.common.Constants;
import com.cqh.magpie.common.URL;
import com.cqh.magpie.common.utils.IpUtils;
import com.cqh.magpie.common.utils.NetUtils;
import com.cqh.magpie.common.utils.StringUtils;
import com.cqh.magpie.registry.NotifyListener;
import com.cqh.magpie.registry.zookeeper.curator.CuratorZookeeperTransporter;

import junit.framework.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ZookeeperRegistryTest
 *
 */
public class ZookeeperRegistryTest {

    String service = "com.cqh.magpie.test.injvmServie";
    URL registryUrl = URL.valueOf("zookeeper://127.0.0.1/");
    URL serviceUrl = URL.valueOf("zookeeper://"+IpUtils.getLocalHostIP()+"/" + service
            + "?notify=false&methods=test1,test2");
    URL consumerUrl = URL.valueOf("zookeeper://consumer/" + service + "?notify=false&methods=test1,test2");
    ZookeeperRegistry registry    = new ZookeeperRegistry(registryUrl,new CuratorZookeeperTransporter());

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        registry.register(serviceUrl);
    }

    /*@Test(expected = IllegalStateException.class)
    public void testUrlerror() {
        URL errorUrl = URL.valueOf("zookeeper://zookeeper/");
        new ZookeeperRegistry(errorUrl);
    }*/

    @Test
    public void testDefaultPort() {
        Assert.assertEquals("10.20.153.10:2181", ZookeeperRegistry.appendDefaultPort("10.20.153.10:0"));
        Assert.assertEquals("10.20.153.10:2181", ZookeeperRegistry.appendDefaultPort("10.20.153.10"));
    }

    /**
     * Test method for {@link com.cqh.magpie.registry.zookeeper.ZookeeperRegistry#getRegistered()}.
     */
    @Test
    public void testRegister() {
        /*List<URL> registered = null;
        // clear first
        registered = registry.getRegistered(service);

        for (int i = 0; i < 2; i++) {
            registry.register(service, serviceUrl);
            registered = registry.getRegistered(service);
            assertTrue(registered.contains(serviceUrl));
        }
        // confirm only 1 regist success;
        registered = registry.getRegistered(service);
        assertEquals(1, registered.size());*/
    }

    /**
     * Test method for
     * {@link com.cqh.magpie.registry.zookeeper.ZookeeperRegistry#subscribe(URL, com.cqh.magpie.registry.NotifyListener)}
     * .
     */
    @Test
    public void testSubscribe() {
        final String subscribearg = "category=consumers&arg1=1&arg2=2";
        // verify lisener.
        final AtomicReference<Map<String, String>> args = new AtomicReference<Map<String, String>>();
        URL url = new URL("dubbo", NetUtils.getLocalHost(), 0, StringUtils.parseQueryString(subscribearg));
        url = url.addParameter(Constants.INTERFACE_KEY, service);
        registry.subscribe(url, new NotifyListener() {

            public void notify(List<URL> urls) {
                // FIXME assertEquals(ZookeeperRegistry.this.service, service);
                args.set(urls.get(0).getParameters());
            }
        });
        try {
			Thread.sleep(3000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Assert.assertEquals(serviceUrl.toParameterString(), StringUtils.toQueryString(args.get()));
        //Map<String, String> arg = registry.getSubscribed(service);
       // Assert.assertEquals(subscribearg, StringUtils.toQueryString(arg));

    }

}