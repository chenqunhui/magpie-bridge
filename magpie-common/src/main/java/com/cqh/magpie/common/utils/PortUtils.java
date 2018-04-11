package com.cqh.magpie.common.utils;

import java.net.ServerSocket;

public class PortUtils {
    synchronized public static int getRandomPort() throws Exception {
        ServerSocket s = new ServerSocket(0);
        int port = s.getLocalPort();
        s.close();
        return port;
    }

    public static void main(String[] args) throws Exception {
        int port = getRandomPort();
        System.out.println(port);
    }
}
