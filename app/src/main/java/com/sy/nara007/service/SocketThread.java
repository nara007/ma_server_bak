package com.sy.nara007.service;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.RunnableFuture;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by nara007 on 16/12/7.
 */

public class SocketThread extends Thread {


    private ServerSocket serverSocket;
    private Handler msgHandler = null;

    private MyWorkThread myWorkThread = new MyWorkThread();

    public void stopServerSocket() {
        this.myWorkThread.shutdownServerSocket();
    }

    private int port = 10000;

    public SocketThread() {

    }

//
//    private void InitServerSocket() {
//        try {
//            this.serverSocket = new ServerSocket();
//            this.serverSocket.bind(new InetSocketAddress(port));
//            System.out.println("server socket init... at :" + ": " + port);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void startService() {
//
//        InitServerSocket();
//        try {
//            while (!shutdownRequested) {
//                this.client = serverSocket.accept();
//                new EventHandler(client).start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        String line = null;
//        InputStream input;
//        OutputStream output;
//        String str = "hello world!";
//        Socket socket = null;
//        try {
//            socket = serverSocket.accept();
//            System.out.println("server accepted...");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            output = socket.getOutputStream();
//            input = socket.getInputStream();
//            BufferedReader bff = new BufferedReader(
//                    new InputStreamReader(input));
//            output.write(str.getBytes("gbk"));
//            output.flush();
//            //半关闭socket
//            socket.shutdownOutput();
//            //获取客户端的信息
//            while ((line = bff.readLine()) != null) {
//                System.out.print(line);
//            }
//            //关闭输入输出流
//            output.close();
//            bff.close();
//            input.close();
//            socket.close();
//            serverSocket.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//    }

    private void InitMsgQueue() {
        //初始化looper
        Looper.prepare();
        this.msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0x1) {
                    float[] values = (float[]) msg.obj;
//                System.out.println("子线程收到消息:" + Float.toString(values[0]) + "  " + Float.toString(values[1]) + "  " + Float.toString(values[2]));


//                byte[] yaw = SocketThread.getByteArray(values[0]);
//                byte[] pitch = SocketThread.getByteArray(values[1]);
//                byte[] roll = SocketThread.getByteArray(values[2]);
//                OutputToClient(byteMerger(byteMerger(yaw, pitch), roll));

                    byte[] type = SocketThread.getByteArray(0x1);
                    byte[] w = SocketThread.getByteArray(values[0]);
                    byte[] x = SocketThread.getByteArray(values[1]);
                    byte[] y = SocketThread.getByteArray(values[2]);
                    byte[] z = SocketThread.getByteArray(values[3]);
                    OutputToClient(byteMerger(type, byteMerger(w, byteMerger(byteMerger(x, y), z))));

//                    System.out.println("Rotation Vector Sensor w,x,y,z: " + values[0] + " " + values[1] + " " + values[2] + " " + values[3]);
                } else if (msg.what == 0x2) {
                    shutdownWorkThread();
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.getLooper().quit();
                } else if (msg.what == 0x3) {
                    int key = (int) msg.obj;
                    System.out.println(key);
                    byte[] type = SocketThread.getByteArray(0x3);
                    byte[] keyInByte = SocketThread.getByteArray(key);
                    OutputToClient(byteMerger(type, keyInByte));
                } else if(msg.what == 0x4){

                    float frontDirection = (float) msg.obj;
                    byte[] type = SocketThread.getByteArray(0x4);
                    byte[] front = SocketThread.getByteArray(frontDirection);
                    OutputToClient(byteMerger(type, front));
                    System.out.println("front "+frontDirection);

                }
                else if(msg.what == 0x5){
                    byte[] type = SocketThread.getByteArray(0x5);
                    OutputToClient(type);
                    System.out.println("tell me");
                }

                else {
                }
            }
        };

        Looper.loop();
    }


    public Handler getMsgHandler() {
        return this.msgHandler;
    }

    public void run() {
        myWorkThread.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        InitMsgQueue();
    }


    private void OutputToClient(byte[] bytes) {


        if (myWorkThread.isAlive()) {
            if (myWorkThread.getClient() != null) {

                try {
                    OutputStream outputStream = myWorkThread.getClient().getOutputStream();
                    outputStream.write(bytes);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // int转换为byte[4]数组
    public static byte[] getByteArray(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) ((i & 0xff000000) >> 24);
        b[1] = (byte) ((i & 0x00ff0000) >> 16);
        b[2] = (byte) ((i & 0x0000ff00) >> 8);
        b[3] = (byte) (i & 0x000000ff);
//        System.out.println(b[0] + " " + b[1] + " " + b[2] + " " + b[3] + " ");
        return b;
    }

    // float转换为byte[4]数组
    public static byte[] getByteArray(float f) {
//        int intbits = Float.floatToIntBits(f);//将float里面的二进制串解释为int整数
        int intbits = Math.round(f * 1000000);
//        System.out.println("changed to int: " + intbits);
        return getByteArray(intbits);
    }


    //java 合并两个byte数组
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }


    private void shutdownWorkThread() {
        this.stopServerSocket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("127.0.0.1", 10000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
