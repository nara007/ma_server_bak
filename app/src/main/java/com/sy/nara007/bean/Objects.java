package com.sy.nara007.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nara007 on 17/2/27.
 */

public class Objects {

    private List objects = new ArrayList();

    public List getObjects() {
        return objects;
    }

    public void setObjects(List objects) {
        this.objects = objects;
    }

//    public static void main(String[] args) {
//        XStream xstream = new XStream();
//
//        // 声明标记名称对应的 Java 类
//        xstream.alias("ArrayOfOutputObject", Objects.class);
//        xstream.alias("OutputObject", OutputObject.class);
//        xstream.addImplicitCollection(Objects.class, "objects");
//
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream("student.xml");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        Objects objs = (Objects) xstream.fromXML(fis);
//        System.out.println(((OutputObject)(objs.getObjects().get(2))).getCategory());
//
//    }
}
