package com.sy.nara007.service;

import android.icu.math.BigDecimal;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.sy.nara007.UI.MainActivity;
import com.sy.nara007.bean.Objects;
import com.sy.nara007.bean.OutputObject;
import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.Iterator;

import static android.content.ContentValues.TAG;

/**
 * Created by nara007 on 17/2/27.
 */

public class TTSThread extends Thread {

    private String xmlStr;
    private TextToSpeech tts = MainActivity.tts;
    private boolean isSimpleString;


    public TTSThread(String xmlStr, boolean isSimpleString) {
        this.isSimpleString = isSimpleString;
        this.xmlStr = xmlStr;
        System.out.println("TTS:" +xmlStr);
    }

    Objects objs;

    public void run() {
        if(isSimpleString){
            speak(this.xmlStr);
        }
        else{
            deserialize();
            speak(extractStringFromObjs());
        }

    }

    private void deserialize() {
        XStream xstream = new XStream();

        // 声明标记名称对应的 Java 类
        xstream.alias("ArrayOfOutputObject", Objects.class);
        xstream.alias("OutputObject", OutputObject.class);
        xstream.addImplicitCollection(Objects.class, "objects");

        objs = (Objects) xstream.fromXML(xmlStr);
    }


    private String extractStringFromObjs() {

        String str = "";
        ArrayList list = (ArrayList) (objs.getObjects());
        if (list.size() == 0) {
            return "nothing has been found";
        }

        int i = 1;
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            OutputObject obj = (OutputObject) (it.next());
//            BigDecimal b = new BigDecimal(obj.getDistance());

//            str += "distance " + b.setScale(2).floatValue() + " ";



            double num = obj.getDistance();
            int num1 = (int)(Math.floor(num));
            int num2 = (int)(Math.floor(num*10-num1*10));
            int num3 = (int)(Math.floor(num*100-num1*100-num2*10));

            str += "Object " + i + " ";
            str += "Kategorie " + obj.getCategory() + " ";
            str += "Abstand " + num1 + " Punkt "+ num2+" "+num3+" Meter";
            i++;
        }

        System.out.println(str);

        return str;
    }

    public void speak(String text) {

        if (tts.isSpeaking()) {
            tts.stop();
            return;
        }

        if (null != tts) {
            tts.setSpeechRate(0.5f);
        }

        if (null != tts) {
            tts.setPitch(1.0f);
        }

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

        if (Build.VERSION.SDK_INT >= 15) {
            int listenerResult = tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    System.out.println("progress on Done " + utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                    System.out.println("progress on Error " + utteranceId);
                }

                @Override
                public void onStart(String utteranceId) {
                    System.out.println("progress on Start " + utteranceId);
                }

            });
            if (listenerResult != TextToSpeech.SUCCESS) {
                System.out.println("failed to add utterance progress listener");
            }
        } else {
            System.out.println("Build VERSION is less than API 15");
        }


    }
}
