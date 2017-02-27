package com.sy.nara007.service;

import com.sy.nara007.bean.Objects;
import com.sy.nara007.bean.OutputObject;
import com.thoughtworks.xstream.XStream;

/**
 * Created by nara007 on 17/2/27.
 */

public class TTSThread extends Thread {

    private String xmlStr;
    public TTSThread(String xmlStr) {
        this.xmlStr = xmlStr;
    }

    Objects objs;
    public void run(){
        deserialize();
        System.out.println(((OutputObject)(objs.getObjects().get(0))).getCategory());
    }

    private void deserialize(){
        XStream xstream = new XStream();

        // 声明标记名称对应的 Java 类
        xstream.alias("ArrayOfOutputObject", Objects.class);
        xstream.alias("OutputObject", OutputObject.class);
        xstream.addImplicitCollection(Objects.class, "objects");

        objs = (Objects) xstream.fromXML(xmlStr);
    }
}
