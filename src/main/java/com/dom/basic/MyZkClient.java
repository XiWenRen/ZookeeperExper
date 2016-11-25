package com.dom.basic;

import com.dom.utils.Properties;

/**
 * Date: 16/11/23
 * Author: dom
 * Usage:
 */
public class MyZkClient {

    public static void main(String[] args) {
        ZkClient client = new ZkClient();
        client.connect(Properties.CON_PATH_1);
        boolean b = client.exist("/lock");
        System.out.println(b);
        client.createEphemeralNode("/lock", "nothing");
        client.createEphemeralNode("/lock", "nothing");
        b = client.exist("/lock");
        System.out.println(b);
        client.close();
    }
}
