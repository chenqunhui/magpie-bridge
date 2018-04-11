package com.cqh.magpie.rpc.thrift.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cqh.magpie.common.utils.UrlUtils;
import com.cqh.magpie.rpc.exception.RpcException;

import java.net.InetSocketAddress;

/**
 *
 */
public class ThriftClientFactory implements KeyedPoolableObjectFactory<String, TServiceClient> {

    private final TServiceClientFactory<TServiceClient> clientFactory;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private PoolOperationCallBack callback;
    private int connectTimeout;
    private int socketTimeout;

    public ThriftClientFactory(TServiceClientFactory<TServiceClient> clientFactory,
                                  PoolOperationCallBack callback,
                                  int connectTimeout,
                                  int socketTimeout){
        this.clientFactory = clientFactory;
        this.callback = callback;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }

    @Override
    public TServiceClient makeObject(String key) throws Exception {
        logger.info("makeObject:" + key);
        InetSocketAddress address = UrlUtils.getAddressFromURlKey(key);
        if (address == null) {
            throw new RpcException("No provider available for remote service");
        }
        logger.debug("connecting to service instance:{} with address:{}", key, address);
        TSocket tsocket = new TSocket(address.getHostName(), address.getPort());
        tsocket.setConnectTimeout(connectTimeout);
        tsocket.setSocketTimeout(socketTimeout);
        TTransport transport = new TFramedTransport(tsocket);
        TProtocol protocol = new TBinaryProtocol(transport);
        TServiceClient client = this.clientFactory.getClient(protocol);
        transport.open();


        if (callback != null) {
            try {
                callback.make(client);
            } catch (Exception e) {
                logger.warn("makeObject:{}", e);
            }
        }
        return client;
    }

    @Override
    public void destroyObject(String key, TServiceClient client) throws Exception {
        if (callback != null) {
            try {
                callback.destroy(client);
            } catch (Exception e) {
                logger.warn("destroyObject:{}", e);
            }
        }
        logger.info("destroyObject:key = {},client = {}", key, client);
        TTransport pin = client.getInputProtocol().getTransport();
        pin.close();
        TTransport pout = client.getOutputProtocol().getTransport();
        pout.close();
    }

    @Override
    public boolean validateObject(String key, TServiceClient client) {
        TTransport pin = client.getInputProtocol().getTransport();
        //logger.info("validateObject input:{}", pin.isOpen());
        TTransport pout = client.getOutputProtocol().getTransport();
        //logger.info("validateObject output:{}", pout.isOpen());
        return pin.isOpen() && pout.isOpen();
    }

    @Override
    public void activateObject(String key, TServiceClient client) throws Exception {

    }

    @Override
    public void passivateObject(String key, TServiceClient client) throws Exception {

    }

    static interface PoolOperationCallBack {
        /**
         * 销毁client之前执行
         *
         * @param client thrift service client
         * @return void
         */
        void destroy(TServiceClient client);

        /**
         * 创建成功时执行
         *
         * @param client thrift service client
         * @return void
         */
        void make(TServiceClient client);
    }

}
