package com.lomoment.serialportsample.annotations;

import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author libin
 * @date 2018/1/12
 * @Description
 */

public class AnnotationsParse {
    public static void parseBindRid(View view, Object o) {
        if (o == null || view == null) {
            return;
        }
        ArrayList<Field> fields = new ArrayList<>();
        Class c = o.getClass();
        while (c != null && !c.getName().toLowerCase().equals("java.lang.object")) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }
        for (Field field : fields) {
            if (field.isAnnotationPresent(BindRid.class)) {
                field.setAccessible(true);
                BindRid rid = field.getAnnotation(BindRid.class);
                View cv = view.findViewById(rid.value());
                try {
                    field.set(o, cv);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
