package com.sy.nara007.UI;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sy.nara007.service.SocketService;
import com.sy.nara007.service.SocketThread;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private SoundPool soundpool;
    private HashMap<Integer, Integer> soundmap = new HashMap<Integer, Integer>();   //创建一个HashMap对象
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;   //startActivityForResult操作要求的标识码
    private boolean flag = false;


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
    private static final byte BLUETOOTHMSG = 0x3;
    private static final byte FRONT = 0x4;
    private static final byte TELLME = 0x5;
    private SocketService socketService;

    // quaternion
    private float[] rotVecValues = null;
    private float[] rotvecR = new float[9], rotQ = new float[4];
    private float[] rotvecOrientValues = new float[3];
    private float frontDirection;


    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    boolean haveAccelerometer = false;
    boolean haveMagnetometer = false;

    private int mAzimuth = 0; // degree


    //    bluetooth  These constants are copied from the BluezService
    public static final String SESSION_ID = "com.hexad.bluezime.sessionid";

    public static final String EVENT_KEYPRESS = "com.hexad.bluezime.keypress";
    public static final String EVENT_KEYPRESS_KEY = "key";
    public static final String EVENT_KEYPRESS_ACTION = "action";

    public static final String EVENT_DIRECTIONALCHANGE = "com.hexad.bluezime.directionalchange";
    public static final String EVENT_DIRECTIONALCHANGE_DIRECTION = "direction";
    public static final String EVENT_DIRECTIONALCHANGE_VALUE = "value";

    public static final String EVENT_CONNECTED = "com.hexad.bluezime.connected";
    public static final String EVENT_CONNECTED_ADDRESS = "address";

    public static final String EVENT_DISCONNECTED = "com.hexad.bluezime.disconnected";
    public static final String EVENT_DISCONNECTED_ADDRESS = "address";

    public static final String EVENT_ERROR = "com.hexad.bluezime.error";
    public static final String EVENT_ERROR_SHORT = "message";
    public static final String EVENT_ERROR_FULL = "stacktrace";

    public static final String REQUEST_STATE = "com.hexad.bluezime.getstate";

    public static final String REQUEST_CONNECT = "com.hexad.bluezime.connect";
    public static final String REQUEST_CONNECT_ADDRESS = "address";
    public static final String REQUEST_CONNECT_DRIVER = "driver";

    public static final String REQUEST_DISCONNECT = "com.hexad.bluezime.disconnect";

    public static final String EVENT_REPORTSTATE = "com.hexad.bluezime.currentstate";
    public static final String EVENT_REPORTSTATE_CONNECTED = "connected";
    public static final String EVENT_REPORTSTATE_DEVICENAME = "devicename";
    public static final String EVENT_REPORTSTATE_DISPLAYNAME = "displayname";
    public static final String EVENT_REPORTSTATE_DRIVERNAME = "drivername";

    public static final String REQUEST_FEATURECHANGE = "com.hexad.bluezime.featurechange";
    public static final String REQUEST_FEATURECHANGE_RUMBLE = "rumble"; //Boolean, true=on, false=off
    public static final String REQUEST_FEATURECHANGE_LEDID = "ledid"; //Integer, LED to use 1-4 for Wiimote
    public static final String REQUEST_FEATURECHANGE_ACCELEROMETER = "accelerometer"; //Boolean, true=on, false=off

    public static final String REQUEST_CONFIG = "com.hexad.bluezime.getconfig";

    public static final String EVENT_REPORT_CONFIG = "com.hexad.bluezime.config";
    public static final String EVENT_REPORT_CONFIG_VERSION = "version";
    public static final String EVENT_REPORT_CONFIG_DRIVER_NAMES = "drivernames";
    public static final String EVENT_REPORT_CONFIG_DRIVER_DISPLAYNAMES = "driverdisplaynames";


    private static final String BLUEZ_IME_PACKAGE = "com.hexad.bluezime";
    private static final String BLUEZ_IME_SERVICE = "com.hexad.bluezime.BluezService";

    //A string used to ensure that apps do not interfere with each other
    public static final String SESSION_NAME = "TEST-BLUEZ-IME";


    private String m_selectedDriver;
    private Button m_button;


    private ListView m_logList;
    private ArrayAdapter<String> m_logAdapter;

    private HashMap<Integer, CheckBox> m_buttonMap = new HashMap<Integer, CheckBox>();
    private ArrayList<String> m_logText = new ArrayList<String>();

    private boolean m_connected = false;

//    bluetooth

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


    private void startVoiceRecognitionActivity() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,50000);
        intent.putExtra("android.speech.extra.DICTATION_MODE", true);


//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
//                "Speech recognition demo");    //设置语音识别Intent调用的特定属性参数
//        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  //该函数非接口内也非抽象函数，为何会Override？
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
                && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it
            // could have heard
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);  //解析存储识别返回的结果

            System.out.println(matches);
//            mList.setAdapter(new ArrayAdapter<String>(this,
//                    android.R.layout.simple_list_item_1, matches));  //在listview中显示结果
            for(String str:matches){
                if(str.contains("tell me") && !str.contains("not") && !str.contains("don't") && !str.contains("do not"))
                {
//                    System.out.println("this string contains tell me");
                    sendTELLMEMsg();
                    break;
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();

        soundpool = new SoundPool(2, AudioManager.STREAM_SYSTEM, 0);
        //将要播放的音频流保存到HashMap对象中
        soundmap.put(1, soundpool.load(this, R.raw.offline, 1));

        registerListeners();
        initOrientationSensor();

        this.mAccelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.haveAccelerometer = this.sensorManager.registerListener(sensoreventlistener, this.mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.mMagnetometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.haveMagnetometer = this.sensorManager.registerListener(sensoreventlistener, this.mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);


        //        bluetooth
        m_button = (Button) findViewById(R.id.bluetooth);
        m_selectedDriver = "wiimote";
        registerReceiver(stateCallback, new IntentFilter(EVENT_REPORT_CONFIG));
        registerReceiver(stateCallback, new IntentFilter(EVENT_REPORTSTATE));
        registerReceiver(stateCallback, new IntentFilter(EVENT_CONNECTED));
        registerReceiver(stateCallback, new IntentFilter(EVENT_DISCONNECTED));
        registerReceiver(stateCallback, new IntentFilter(EVENT_ERROR));

        registerReceiver(statusMonitor, new IntentFilter(EVENT_DIRECTIONALCHANGE));
        registerReceiver(statusMonitor, new IntentFilter(EVENT_KEYPRESS));

        m_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_connected) {
                    Intent serviceIntent = new Intent(REQUEST_DISCONNECT);
                    serviceIntent.setClassName(BLUEZ_IME_PACKAGE, BLUEZ_IME_SERVICE);
                    serviceIntent.putExtra(SESSION_ID, SESSION_NAME);
                    startService(serviceIntent);
                } else {
                    Intent serviceIntent = new Intent(REQUEST_CONNECT);
                    serviceIntent.setClassName(BLUEZ_IME_PACKAGE, BLUEZ_IME_SERVICE);
                    serviceIntent.putExtra(SESSION_ID, SESSION_NAME);
                    serviceIntent.putExtra(REQUEST_CONNECT_ADDRESS, "00:1E:35:3B:DF:72");
                    serviceIntent.putExtra(REQUEST_CONNECT_DRIVER, m_selectedDriver);
                    startService(serviceIntent);
                }
            }
        });
        //Request config, not present in version < 9
        Intent serviceIntent = new Intent(REQUEST_CONFIG);
        serviceIntent.setClassName(BLUEZ_IME_PACKAGE, BLUEZ_IME_SERVICE);
        serviceIntent.putExtra(SESSION_ID, SESSION_NAME);
        startService(serviceIntent);

        //Request device connection state
        serviceIntent = new Intent(REQUEST_STATE);
        serviceIntent.setClassName(BLUEZ_IME_PACKAGE, BLUEZ_IME_SERVICE);
        serviceIntent.putExtra(SESSION_ID, SESSION_NAME);
        startService(serviceIntent);
        //        bluetooth
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("on destroy...");
        sensorManager.unregisterListener(sensoreventlistener);

//        bluetooth

        unregisterReceiver(stateCallback);
        unregisterReceiver(statusMonitor);
//        bluetooth
//        sensorManager.unregisterListener(sensoreventlistenerQuaternion);
    }


    //    bluetooth
    private BroadcastReceiver stateCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null)
                return;

            //Filter everything that is not related to this session
            if (!SESSION_NAME.equals(intent.getStringExtra(SESSION_ID)))
                return;

            if (intent.getAction().equals(EVENT_REPORT_CONFIG)) {
                Toast.makeText(MainActivity.this, "Bluez-IME version " + intent.getIntExtra(EVENT_REPORT_CONFIG_VERSION, 0), Toast.LENGTH_SHORT).show();
//				populateDriverBox(intent.getStringArrayExtra(EVENT_REPORT_CONFIG_DRIVER_NAMES), intent.getStringArrayExtra(EVENT_REPORT_CONFIG_DRIVER_DISPLAYNAMES));
            } else if (intent.getAction().equals(EVENT_REPORTSTATE)) {
                m_connected = intent.getBooleanExtra(EVENT_REPORTSTATE_CONNECTED, false);
                m_button.setText(m_connected ? R.string.bluezime_connected : R.string.bluezime_disconnected);

                //After we connect, we rumble the device for a second if it is supported
                if (m_connected) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent req = new Intent(REQUEST_FEATURECHANGE);
                            req.putExtra(REQUEST_FEATURECHANGE_LEDID, 2);
                            req.putExtra(REQUEST_FEATURECHANGE_RUMBLE, true);
                            startService(req);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            req.putExtra(REQUEST_FEATURECHANGE_LEDID, 1);
                            req.putExtra(REQUEST_FEATURECHANGE_RUMBLE, false);
                            startService(req);
                        }
                    });
                }

            } else if (intent.getAction().equals(EVENT_CONNECTED)) {
                m_button.setText(R.string.bluezime_connected);
                m_connected = true;
            } else if (intent.getAction().equals(EVENT_DISCONNECTED)) {
                m_button.setText(R.string.bluezime_disconnected);
                m_connected = false;
            } else if (intent.getAction().equals(EVENT_ERROR)) {
                Toast.makeText(MainActivity.this, "Error: " + intent.getStringExtra(EVENT_ERROR_SHORT), Toast.LENGTH_SHORT).show();
//                reportUnmatched("Error: " + intent.getStringExtra(EVENT_ERROR_FULL));
                System.out.println("Error: EVENT_ERROR_FULL");
                m_connected = false;
            }

        }
    };

    private BroadcastReceiver statusMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null)
                return;
            if (!SESSION_NAME.equals(intent.getStringExtra(SESSION_ID)))
                return;

            if (intent.getAction().equals(EVENT_DIRECTIONALCHANGE)) {


            } else if (intent.getAction().equals(EVENT_KEYPRESS)) {
                int key = intent.getIntExtra(EVENT_KEYPRESS_KEY, 0);
                int action = intent.getIntExtra(EVENT_KEYPRESS_ACTION, 100);
//                action=1 key down event
                if (action == 1) {
                    System.out.println("***********key:" + key);
                    Message bluetoothMsg = new Message();
                    bluetoothMsg.what = BLUETOOTHMSG;
                    bluetoothMsg.obj = key;

                    if (MainActivity.this.socketService != null) {
                        if (MainActivity.this.socketService.getSocketThread() != null && MainActivity.this.socketService.getSocketThread().isAlive()) {
                            if (MainActivity.this.socketService.getSocketThread().getMsgHandler() != null) {
                                MainActivity.this.socketService.getSocketThread().getMsgHandler().sendMessage(bluetoothMsg);
                            }
                        }
                    }
                }

//                if (m_buttonMap.containsKey(key))
//                    m_buttonMap.get(key).setChecked(action == KeyEvent.ACTION_DOWN);
//                else {
//                    reportUnmatched(String.format(getString(action == KeyEvent.ACTION_DOWN ? R.string.unmatched_key_event_down : R.string.unmatched_key_event_up), key + ""));
//                }
//                System.out.println("***********key:" + key + " ^^^action:" + action);
            }
        }
    };
//    bluebooth


    private void initOrientationSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (accSensor == null) {
            Toast.makeText(this, "您的设备不支持该功能！", Toast.LENGTH_SHORT).show();
        } else {
            String str = "\n名字：" + accSensor.getName() + "\n电池：" + accSensor.getPower() + "\n类型：" + accSensor.getType() + "\nVendor:" + accSensor.getVendor() + "\n版本：" + accSensor.getVersion() + "\n幅度：" + accSensor.getMaximumRange();

//            tv_info.setText(str);

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
//        this.tv_info = (TextView) findViewById(R.id.tv_info);
    }

    private void registerListeners() {
        this.startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(MainActivity.this.serviceIntent);
//                bindService(MainActivity.this.serviceIntent, MainActivity.this, Context.BIND_AUTO_CREATE);
                doBindService();
            }
        });

        this.stopServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendShutdownMsg();
//                unbindService(MainActivity.this);
                doUnbindService();
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

    private void sendTELLMEMsg(){

        Message msg = new Message();
        msg.what = TELLME;

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


        float[] gData = new float[3]; // accelerometer
        float[] mData = new float[3]; // magnetometer
        float[] rMat = new float[9];
        float[] iMat = new float[9];
        float[] orientation = new float[3];
        float[] dataMainThread = new float[3];

        //        quaternion
        @Override
        public void onSensorChanged(SensorEvent event) {

            Message msg = new Message();
            Message msgToMainThread = new Message();
            Message msgToWorkingThread = new Message();

            float[] data;


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
//                    msgToMainThread.obj = rotvecOrientValues;
//                    msgToMainThread.what = CHANGE;


                    if (MainActivity.this.socketService != null) {
                        if (MainActivity.this.socketService.getSocketThread() != null && MainActivity.this.socketService.getSocketThread().isAlive()) {
                            if (MainActivity.this.socketService.getSocketThread().getMsgHandler() != null) {
                                MainActivity.this.socketService.getSocketThread().getMsgHandler().sendMessage(msg);
                            }
                        }
                    }


                }
            } else {

                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        gData = event.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        mData = event.values.clone();
                        break;
                    default:
                        return;
                }

                if (SensorManager.getRotationMatrix(rMat, iMat, gData, mData)) {

                    SensorManager.getOrientation(rMat, orientation);
//                    mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
//                    dataMainThread[0] = (float) Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]);
//                    dataMainThread[1] = (float) Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[1]);
//                    dataMainThread[2] = (float) Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[2]);
//                    System.out.println("azimuth:" + (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360);
                    // Pitch scaling


                    dataMainThread[0] = (float) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
                    dataMainThread[1] = (float) Math.toDegrees(orientation[1]);

//                    System.out.println("pitch:" + dataMainThread[1]);
                    msgToMainThread.obj = dataMainThread;
                    msgToMainThread.what = CHANGE;

                    msgToWorkingThread.obj = dataMainThread[0];
                    msgToWorkingThread.what = FRONT;

                    MainActivity.this.frontDirection = dataMainThread[0];
                    mainHandler.sendMessage(msgToMainThread);

//                    send front direction to working thread
                    if (MainActivity.this.socketService != null) {
                        if (MainActivity.this.socketService.getSocketThread() != null && MainActivity.this.socketService.getSocketThread().isAlive()) {
                            if (MainActivity.this.socketService.getSocketThread().getMsgHandler() != null) {
                                MainActivity.this.socketService.getSocketThread().getMsgHandler().sendMessage(msgToWorkingThread);
                            }
                        }
                    }
                }
            }


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

//                    tv_X.setText("w：" + Float.toString(values[0]));
//                    tv_Y.setText("x：" + Float.toString(values[1]));
//                    tv_Z.setText("y：" + Float.toString(values[2]));

//                    System.out.println("main线程收到消息:" + Float.toString(values[0]) + "  " + Float.toString(values[1]) + "  " + Float.toString(values[2]));


                    startOrEndVoiceRecognition(values);
                    break;
            }
            super.handleMessage(msg);
        }


    };


    private void startOrEndVoiceRecognition(float[] values) {
        float value = values[1];
//        System.out.println(values[1]);
        if (value > -50) {
            if (flag == false) {
                flag = true;
                startVoiceRecognitionActivity();
            }
        } else {
            if (flag == true) {
                flag = false;
                finishActivity(VOICE_RECOGNITION_REQUEST_CODE);
                soundpool.play(soundmap.get(1), 1, 1, 0, 0, 1);
            }

        }
    }


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
