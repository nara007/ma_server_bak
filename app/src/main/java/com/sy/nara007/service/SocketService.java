package com.sy.nara007.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by nara007 on 16/12/4.
 */

public class SocketService extends Service {

    private SocketThread socketThread = new SocketThread();
    private final ServiceBinder serviceBinder = new ServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("service on bind...");
        return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("service start...");
        socketThread.start();
    }


    @Override
    public boolean onUnbind(Intent intent) {


//        this.socketThread.stopServerSocket();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {

        System.out.println("service on destroyed...");
        super.onDestroy();
    }


    public class ServiceBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    public SocketThread getSocketThread() {
        return this.socketThread;
    }
}
