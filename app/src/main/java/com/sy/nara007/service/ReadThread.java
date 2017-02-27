package com.sy.nara007.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by nara007 on 17/2/27.
 */

public class ReadThread extends Thread {

    private Socket client = null;


    public void run() {

        connectClient();
    }

    private void connectClient() {
        while (client == null) {
            client = SocketThread.myWorkThread.getClient();
        }

        InputStream is = null;
        BufferedReader br = null;
        try {
            is = this.client.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

        } catch (IOException e) {
            e.printStackTrace();

            try {
                this.client.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            return;

        }

        String str;

        try {
            while ((str = br.readLine()) != null) {
                new TTSThread(str).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
