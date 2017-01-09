package com.sy.nara007.service;

import android.os.Handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nara007 on 16/12/12.
 */

public class MyWorkThread extends Thread {
    private ServerSocket serverSocket;
    private volatile boolean shutdownRequested;
    private Handler msgHandler = null;

    private Socket client = null;
    private List<Socket> clients = Collections.synchronizedList(new ArrayList<Socket>());
    private int port = 10000;

    public MyWorkThread() {
        this.shutdownRequested = false;
    }


    private void InitServerSocket() {
        try {
            this.serverSocket = new ServerSocket();
            this.serverSocket.bind(new InetSocketAddress(port));
            System.out.println("server socket init... at :" + ": " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startService() {

        InitServerSocket();
        try {
            while (true) {
                System.out.println("work thread waitting...");
                this.client = serverSocket.accept();
//                clients.add(this.client);
//                new EventHandler(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        finally {
//            try {
//                this.serverSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void run() {
//        InitMsgQueue();
        startService();
    }

    public Socket getClient() {

        return this.client;
    }

    public void shutdownServerSocket() {
        this.shutdownRequested = true;
    }
}
