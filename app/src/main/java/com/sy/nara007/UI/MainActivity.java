package com.sy.nara007.UI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sy.nara007.service.SocketService;
import com.sy.nara007.service.SocketThread;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private IBinder mBinder;
    private Boolean mIsBound;

    private Button startServer;
    private Button stopServer;
    private TextView tv_X;
    private TextView tv_Y;
    private TextView tv_Z;
    private TextView tv_info;
    private Intent serviceIntent;
    SensorManager sensorManager;//管理器对象
    Sensor accSensor;//传感器对象
    private static final byte CHANGE = 0x1;
    private static final byte QUIT = 0x2;
    private SocketService socketService;

    // quaternion
    private float[] rotVecValues = null;
    private float[] rotvecR = new float[9], rotQ = new float[4];
    private float[] rotvecOrientValues = new float[3];


    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            MainActivity.this.socketService = ((SocketService.ServiceBinder) iBinder).getService();
            System.out.println("onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            MainActivity.this.socketService = null;
            System.out.println("onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();

        registerListeners();
        initOrientationSensor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("on destroy...");
        sensorManager.unregisterListener(sensoreventlistener);
//        sensorManager.unregisterListener(sensoreventlistenerQuaternion);
    }


    private void initOrientationSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (accSensor == null) {
            Toast.makeText(this, "您的设备不支持该功能！", Toast.LENGTH_SHORT).show();
        } else {
            String str = "\n名字：" + accSensor.getName() + "\n电池：" + accSensor.getPower() + "\n类型：" + accSensor.getType() + "\nVendor:" + accSensor.getVendor() + "\n版本：" + accSensor.getVersion() + "\n幅度：" + accSensor.getMaximumRange();

            tv_info.setText(str);

        }
        /**
         * 注册监听器
         */
        sensorManager.registerListener(sensoreventlistener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initComponents() {
        this.startServer = (Button) findViewById(R.id.startServer);
        this.stopServer = (Button) findViewById(R.id.stopServer);
        this.serviceIntent = new Intent(this, SocketService.class);

        this.tv_X = (TextView) findViewById(R.id.tv_X);
        this.tv_Y = (TextView) findViewById(R.id.tv_Y);
        this.tv_Z = (TextView) findViewById(R.id.tv_Z);
        this.tv_info = (TextView) findViewById(R.id.tv_info);
    }

    private void registerListeners() {
        this.startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(MainActivity.this.serviceIntent);
                bindService(MainActivity.this.serviceIntent, MainActivity.this, Context.BIND_AUTO_CREATE);
//                doBindService();
            }
        });

        this.stopServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sendShutdownMsg();
                unbindService(MainActivity.this);
//                doUnbindService();
                stopService(MainActivity.this.serviceIntent);
            }
        });
    }

    public void doBindService() {
        bindService(MainActivity.this.serviceIntent,
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }


    private void sendShutdownMsg() {

        Message msg = new Message();
        msg.what = QUIT;

        if (MainActivity.this.socketService != null) {
            if (MainActivity.this.socketService.getSocketThread() != null) {
                if (MainActivity.this.socketService.getSocketThread().getMsgHandler() != null) {
                    MainActivity.this.socketService.getSocketThread().getMsgHandler().sendMessage(msg);
                }
            }
        }
    }

    /**
     * 传感器的监听
     */
    private SensorEventListener sensoreventlistener = new SensorEventListener() {
//        @Override
//        public void onSensorChanged(SensorEvent sensorEvent) {
//            float[] values = sensorEvent.values;
//            Message message = new Message();
//            message.obj = values;
//            message.what = CHANGE;
//
//
//            if (MainActivity.this.socketService != null) {
//                if (MainActivity.this.socketService.getSocketThread() != null) {
//                    if (MainActivity.this.socketService.getSocketThread().getMsgHandler() != null) {
//                        MainActivity.this.socketService.getSocketThread().getMsgHandler().sendMessage(message);
//                    }
//                }
//            }
//
//
//        }


        //        quaternion
        @Override
        public void onSensorChanged(SensorEvent event) {

            Message msg = new Message();
            Message msgToMainThread = new Message();


            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                if (rotVecValues == null) {
                    rotVecValues = new float[event.values.length];
                }
                for (int i = 0; i < rotVecValues.length; i++) {
                    rotVecValues[i] = event.values[i];
                }

                if (rotVecValues != null) {
                    SensorManager.getQuaternionFromVector(rotQ, rotVecValues);
                    SensorManager.getRotationMatrixFromVector(rotvecR, rotVecValues);
                    SensorManager.getOrientation(rotvecR, rotvecOrientValues);

                    msg.obj = rotQ;
                    msg.what = CHANGE;
                    msgToMainThread.obj = rotQ;
                    msgToMainThread.what = CHANGE;
                }
            }


            if (MainActivity.this.socketService != null) {
                if (MainActivity.this.socketService.getSocketThread() != null) {
                    if (MainActivity.this.socketService.getSocketThread().getMsgHandler() != null) {
                        MainActivity.this.socketService.getSocketThread().getMsgHandler().sendMessage(msg);
                    }
                }
            }

            mainHandler.sendMessage(msgToMainThread);


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            switch (msg.what) {

                case CHANGE:

                    float[] values = (float[]) msg.obj;

//                    tv_X.setText("手机沿Z  Yaw轴转过的角度为：" + Float.toString(values[0]));
//                    tv_Y.setText("手机沿X  Pitch轴转过的角度为：" + Float.toString(values[1]));
//                    tv_Z.setText("手机沿Y  Roll轴转过的角度为：" + Float.toString(values[2]));

                    tv_X.setText("w：" + Float.toString(values[0]));
                    tv_Y.setText("x：" + Float.toString(values[1]));
                    tv_Z.setText("y：" + Float.toString(values[2]));
                    break;
            }
            super.handleMessage(msg);
        }


    };


//    private SensorEventListener sensoreventlistenerQuaternion = new SensorEventListener(){
//
//        @Override
//        public void onSensorChanged(SensorEvent event){
//            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
//                if(rotVecValues == null){
//                    rotVecValues = new float[event.values.length];
//                }
//                for(int i = 0; i < rotVecValues.length; i++){
//                    rotVecValues[i] = event.values[i];
//                }
//
//                if(rotVecValues != null){
//                    SensorManager.getQuaternionFromVector(rotQ, rotVecValues);
//                    SensorManager.getRotationMatrixFromVector(rotvecR, rotVecValues);
//                    SensorManager.getOrientation(rotvecR, rotvecOrientValues);
//                }
//            }
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int i) {
//
//        }
//    };


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        MainActivity.this.socketService = ((SocketService.ServiceBinder) iBinder).getService();
        System.out.println("onServiceConnected");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        MainActivity.this.socketService = null;
        System.out.println("onServiceDisconnected");
    }

}
