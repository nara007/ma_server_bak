package com.sy.nara007.service;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by nara007 on 16/12/10.
 */

public class EventHandler {

    private Socket client;
    private boolean isSocketClosed = false;

    public EventHandler(Socket client) {
        this.client = client;
    }

    public void start() {

        try {
            System.out.println("客户端数据已经连接");
            DataInputStream inputStream = null;
            DataOutputStream outputStream = null;
            String strInputstream = "";
            inputStream = new DataInputStream(this.client.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] by = new byte[3];
            Arrays.fill(by, (byte) 0);
            int n;

            while (isSocketClosed != true) {
                System.out.println("*****");
                while ((n = inputStream.read(by)) != -1) {
                    baos.write(by, 0, n);
                    if (by[2] == 33) {
//                        Arrays.fill(by, (byte) 0);
                        break;
                    }
                    System.out.println(new String(baos.toByteArray()));
                }
//                System.out.println("&&&&&");
                strInputstream = new String(baos.toByteArray());
                Arrays.fill(by, (byte) 0);

//                System.out.println(strInputstream);
                if (strInputstream.equals("stop")) {
                    isSocketClosed = true;
                }



            }
            baos.close();
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (this.client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    client = null;
                    System.out.println("服务端 finally 异常:" + e.getMessage());
                }
            }
        }
    }
}
