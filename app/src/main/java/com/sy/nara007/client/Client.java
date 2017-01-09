package com.sy.nara007.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by nara007 on 16/12/9.
 */

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("141.76.22.180", 10000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String msg = reader.readLine();
                out.println(msg);
                out.flush();
                if (msg.equals("bye")) {
                    break;
                }
                System.out.println(in.readLine());
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
